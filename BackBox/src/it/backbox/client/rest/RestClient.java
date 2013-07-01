package it.backbox.client.rest;

import it.backbox.IRestClient;
import it.backbox.client.http.MultipartContent.MultipartFormDataContent;
import it.backbox.client.oauth.OAuth2Client;
import it.backbox.client.rest.bean.BoxError;
import it.backbox.client.rest.bean.BoxFile;
import it.backbox.client.rest.bean.BoxFolder;
import it.backbox.client.rest.bean.BoxItemCollection;
import it.backbox.client.rest.bean.BoxSearchResult;
import it.backbox.client.rest.bean.BoxUploadedFile;
import it.backbox.client.rest.bean.ProxyConfiguration;
import it.backbox.exception.BackBoxException;
import it.backbox.exception.RestException;
import it.backbox.progress.ProgressManager;
import it.backbox.progress.stream.InputStreamCounter;
import it.backbox.progress.stream.OutputStreamCounter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.ExponentialBackOffPolicy;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.GenericData;

public class RestClient implements IRestClient {
	private static Logger _log = Logger.getLogger(RestClient.class.getCanonicalName());

	/** Global instance of the HTTP transport. */
	private static HttpTransport HTTP_TRANSPORT;

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();

	private static final String baseUri = "https://api.box.com/2.0/";
	private static final String baseUriUpload = "https://upload.box.com/api/2.0/files/";

	private HttpRequestFactory requestFactory;

	private static Credential credential;
	
	private class RestHttpRequestInitializer implements HttpRequestInitializer {

		@Override
		public void initialize(HttpRequest request) throws IOException {
			credential.initialize(request);
			request.setParser(new JsonObjectParser(JSON_FACTORY));
//			request.setReadTimeout(60*60*1000);
			request.setBackOffPolicy(new CustomBackOffPolicy());
		}
		
	}

	/**
	 * Constructor
	 * 
	 * @throws Exception
	 */
	public RestClient() throws Exception {
		this(null);
	}
	
	/**
	 * Constructor
	 * 
	 * @param pc
	 *            Proxy configuration
	 * @throws Exception
	 */
	public RestClient(ProxyConfiguration pc) throws Exception {
		if ((pc == null) || !pc.isEnabled())
			HTTP_TRANSPORT = new NetHttpTransport();
		else {
			String address = pc.getAddress();
			int port = pc.getPort();
			if ((address == null) || address.isEmpty() || (port <= 0))
				throw new BackBoxException("Proxy configuration not valid");
			HTTP_TRANSPORT = new NetHttpTransport.Builder().setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(address, port))).build();
		}
		
		credential = OAuth2Client.getCredential(HTTP_TRANSPORT);
		requestFactory = HTTP_TRANSPORT.createRequestFactory(new RestHttpRequestInitializer());
		_log.fine("Rest Client init ok");
	}
	
	/**
	 * Execute an HTTP request
	 * 
	 * @param request
	 *            Request to execute
	 * @return The HTTP response
	 * @throws IOException
	 *             Parsing error failed
	 * @throws RestException
	 *             HTTP Error
	 */
	private HttpResponse execute(HttpRequest request) throws IOException, RestException {
		HttpResponse response = null;
		try {
			response = request.execute();
		} catch (HttpResponseException e) {
			String message = new StringBuilder(request.getRequestMethod()).append(' ').append(request.getUrl()).append(" -> ").append(e.getStatusCode()).toString(); 
			JsonObjectParser parser = new JsonObjectParser(JSON_FACTORY);
			BoxError error = null;
			if (e.getContent() != null)
				error = parser.parseAndClose(new StringReader(e.getContent()), BoxError.class);
			throw new RestException(message, e, error);
		}
		return response;
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.IRestClient#download(java.lang.String)
	 */
	public byte[] download(String fileID) throws IOException, RestException {
		GenericUrl url = new GenericUrl(baseUri + "/files/" + fileID + "/content");
		HttpRequest request = requestFactory.buildGetRequest(url);
		_log.fine("Download: " + request.getUrl().toString());
		HttpResponse response = execute(request);
		_log.fine("Download: " + response.getStatusCode());
		if (response.getStatusCode() == 202) {
			String retry = response.getHeaders().getRetryAfter();
			try {
				Thread.sleep(Long.parseLong(retry));
			} catch (NumberFormatException | InterruptedException e) {
				_log.log(Level.SEVERE, "Error waiting to retry the download, retrying now!", e);
			}
			response = execute(request);
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStreamCounter outputStream = new OutputStreamCounter(baos, ProgressManager.DOWNLOAD_ID);
		
		response.download(outputStream);
		return baos.toByteArray();
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IRestClient#upload(java.lang.String, java.lang.String, byte[], java.lang.String, java.lang.String)
	 */
	public BoxFile upload(String name, String fileID, byte[] content, String folderID, String sha1) throws RestException, IOException {
		String uri;
		if (fileID == null)
			uri = baseUriUpload + "content";
		else
			uri = baseUriUpload + fileID + "/content";
		
		GenericUrl url = new GenericUrl(uri);
		
		MultipartFormDataContent mpc = new MultipartFormDataContent();
		mpc.addPart(new MultipartFormDataContent.Part(null, name, new InputStreamContent("application/octet-stream", new InputStreamCounter(new ByteArrayInputStream(content), ProgressManager.UPLOAD_ID))));
		mpc.addPart(new MultipartFormDataContent.Part("filename", null, ByteArrayContent.fromString("file", name)));
		mpc.addPart(new MultipartFormDataContent.Part("parent_id", null, ByteArrayContent.fromString("string", folderID)));
		
		HttpRequest request = requestFactory.buildPostRequest(url, mpc);
		if ((sha1 != null) && !sha1.isEmpty()) {
			HttpHeaders h = new HttpHeaders();
			List<String> sha = new ArrayList<String>();
			sha.add(sha1);
			h.set("Content-MD5", sha);
			request.setHeaders(h);
		}
		_log.fine("Upload: " + request.getUrl().toString());
		HttpResponse response = execute(request);
		_log.fine("Upload: " + response.getStatusCode());
		BoxUploadedFile file =  response.parseAs(BoxUploadedFile.class);
		if ((file != null) && (file.entries != null) && !file.entries.isEmpty())
			return file.entries.get(0);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IRestClient#delete(java.lang.String, boolean)
	 */
	public void delete(String fileID, boolean isFolder) throws RestException, IOException {
		GenericUrl url = new GenericUrl(baseUri + (isFolder ? "folders/" : "files/") + fileID);
		if (isFolder)
			url.put("recursive", "true");
		HttpRequest request = requestFactory.buildDeleteRequest(url);
		_log.fine("Delete: " + request.getUrl().toString());
		HttpResponse response = execute(request);
		_log.fine("Delete: " + response.getStatusCode());
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.IRestClient#search(java.lang.String)
	 */
	public BoxSearchResult search(String query) throws IOException, RestException {
		GenericUrl url = new GenericUrl(baseUri + "search");
		url.put("query", query);
		HttpRequest request = requestFactory.buildGetRequest(url);
		_log.fine("Search: " + request.getUrl().toString());
		HttpResponse response = execute(request);
		_log.fine("Search: " + response.getStatusCode());
		return response.parseAs(BoxSearchResult.class);
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.IRestClient#mkdir(java.lang.String)
	 */
	public BoxFolder mkdir(String name) throws IOException, RestException {
		GenericUrl url = new GenericUrl(baseUri + "folders");
		GenericData data = new GenericData();
        data.put("name", name);
        GenericData parentData = new GenericData();
        parentData.put("id", "0");
        data.put("parent", parentData);
		HttpRequest request = requestFactory.buildPostRequest(url, new JsonHttpContent(JSON_FACTORY, data));
		_log.fine("MkDir: " + request.getUrl().toString());
		HttpResponse response = execute(request);
		return response.parseAs(BoxFolder.class);
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.IRestClient#getFolderItems(java.lang.String)
	 */
	public BoxItemCollection getFolderItems(String folderID) throws IOException, RestException {
		GenericUrl url = new GenericUrl(baseUri + "/folders/" + folderID + "/items");
		url.put("limit", "1000");
		url.put("fields", "name,id,sha1");
		HttpRequest request = requestFactory.buildGetRequest(url);
		_log.fine("getFolderItems: " + request.getUrl().toString());
		HttpResponse response = execute(request);
		return response.parseAs(BoxItemCollection.class);
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.IRestClient#isAccessTokenValid()
	 */
	@Override
	public boolean isAccessTokenValid() {
		// check if token will expire in 5 minutes
		if ((credential == null)
				|| (credential.getAccessToken() == null)
				|| ((credential.getExpiresInSeconds() != null) && (credential.getExpiresInSeconds() <= 300)))
			return false;
		return true;
	}
	
	/**
	 * Custom BackOff Policy to include the status 429 - TOO MANY REQUEST in the policy
	 */
	public class CustomBackOffPolicy extends ExponentialBackOffPolicy {
		@Override
		public boolean isBackOffRequired(int statusCode) {
			return super.isBackOffRequired(statusCode) || (statusCode == 429);
		}
	}

}
