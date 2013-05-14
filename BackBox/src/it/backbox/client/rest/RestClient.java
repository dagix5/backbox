package it.backbox.client.rest;

import it.backbox.client.http.MultipartContent.MultipartFormDataContent;
import it.backbox.client.oauth.OAuth2Client;
import it.backbox.client.rest.bean.BoxError;
import it.backbox.client.rest.bean.BoxFile;
import it.backbox.client.rest.bean.BoxFolder;
import it.backbox.client.rest.bean.BoxSearchResult;
import it.backbox.client.rest.bean.BoxUploadedFile;
import it.backbox.exception.RestException;
import it.backbox.progress.ProgressManager;
import it.backbox.progress.stream.InputStreamCounter;
import it.backbox.progress.stream.OutputStreamCounter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.ByteArrayContent;
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

public class RestClient {
	private static Logger _log = Logger.getLogger(RestClient.class.getCanonicalName());

	/** Global instance of the HTTP transport. */
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();

	private static final String baseUri = "https://api.box.com/2.0/";
	private static final String baseUriUpload = "https://upload.box.com/api/2.0/files/";

	private HttpRequestFactory requestFactory;

	private Credential credential;

	public RestClient() throws Exception {
		credential = OAuth2Client.getCredential();
		requestFactory = HTTP_TRANSPORT.createRequestFactory(
				new HttpRequestInitializer() {
					@Override
					public void initialize(HttpRequest request) throws IOException {
						credential.initialize(request);
						request.setParser(new JsonObjectParser(JSON_FACTORY));
//						request.setReadTimeout(60*60*1000);
					}
				});
		_log.fine("Rest Client init ok");
	}

	public byte[] download(String fileID) throws IOException, NumberFormatException, InterruptedException {
		GenericUrl url = new GenericUrl(baseUri + "/files/" + fileID + "/content");
		HttpRequest request = requestFactory.buildGetRequest(url);
		_log.fine("Download: " + request.getUrl().toString());
		HttpResponse response = null;
		try {
			response = request.execute();
		} catch (HttpResponseException e) {
			if ((e.getStatusCode() == 401) && credential.refreshToken()) {
				_log.fine("Download: Token refreshed");
				request = requestFactory.buildGetRequest(url);
				response = request.execute();
			} else
				throw e;
			
		}
		_log.fine("Download: " + response.getStatusCode());
		if (response.getStatusCode() == 202) {
			String retry = response.getHeaders().getRetryAfter();
			Thread.sleep(Long.parseLong(retry));
			response = request.execute();
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStreamCounter outputStream = new OutputStreamCounter(baos, ProgressManager.DOWNLOAD_ID);
		
		response.download(outputStream);
		return baos.toByteArray();
	}

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
		
		HttpResponse response = null;
		try {
			response = request.execute();
		} catch (HttpResponseException e) {
			if ((e.getStatusCode() == 401) && credential.refreshToken()) {
				request = requestFactory.buildPostRequest(url, mpc);
				response = request.execute();
				_log.fine("Upload: Token refreshed");
			} if (e.getStatusCode() == 409) {
				_log.fine("Upload: 409 Conflict, uploading new version");
				JsonObjectParser parser = new JsonObjectParser(JSON_FACTORY);
				BoxError error = parser.parseAndClose(new StringReader(e.getContent()), BoxError.class);
				if ((error == null) || (error.context_info == null) || (error.context_info.conflicts == null) || (error.context_info.conflicts.isEmpty()))
					throw e;
				String id = error.context_info.conflicts.get(0).id;
				String sha = error.context_info.conflicts.get(0).sha1;
				_log.fine("Upload: 409 Conflict, fileID " + id);
				return upload(name, id, content, folderID, sha);
			} else
				throw e;
			
		}
		_log.fine("Upload: " + response.getStatusCode());
		BoxUploadedFile file =  response.parseAs(BoxUploadedFile.class);
		if ((file != null) && (file.entries != null) && !file.entries.isEmpty())
			return file.entries.get(0);
		return null;
	}

	public void delete(String fileID) throws RestException, IOException {
		GenericUrl url = new GenericUrl(baseUri + "/files/" + fileID);
		HttpRequest request = requestFactory.buildDeleteRequest(url);
		_log.fine("Delete: " + request.getUrl().toString());
		HttpResponse response = null;
		try {
			response = request.execute();
		} catch (HttpResponseException e) {
			if ((e.getStatusCode() == 401) && credential.refreshToken()) {
				request = requestFactory.buildDeleteRequest(url);
				response = request.execute();
				_log.fine("Delete: Token refreshed");
			} else
				throw e;
		}
		_log.fine("Delete: " + response.getStatusCode());
	}
	
	public BoxSearchResult search(String query) throws IOException, RestException {
		GenericUrl url = new GenericUrl(baseUri + "/search");
		url.put("query", query);
		HttpRequest request = requestFactory.buildGetRequest(url);
		_log.fine("Search: " + request.getUrl().toString());
		HttpResponse response = null;
		try {
			response = request.execute();
		} catch (HttpResponseException e) {
			if ((e.getStatusCode() == 401) && credential.refreshToken()) {
				request = requestFactory.buildGetRequest(url);
				response = request.execute();
				_log.fine("Search: Token refreshed");
			} else
				throw e;
		}
		_log.fine("Search: " + response.getStatusCode());
		return response.parseAs(BoxSearchResult.class);
	}
	
	public BoxFolder mkdir(String name) throws IOException, RestException {
		GenericUrl url = new GenericUrl(baseUri + "/folders");
		GenericData data = new GenericData();
        data.put("name", name);
        GenericData parentData = new GenericData();
        parentData.put("id", "0");
        data.put("parent", parentData);
		HttpRequest request = requestFactory.buildPostRequest(url, new JsonHttpContent(JSON_FACTORY, data));
		_log.fine("MkDir: " + request.getUrl().toString());
		HttpResponse response = null;
		try {
			request.execute();
		} catch (HttpResponseException e) {
			if ((e.getStatusCode() == 401) && credential.refreshToken()) {
				request = requestFactory.buildPostRequest(url, new JsonHttpContent(JSON_FACTORY, data));
				response = request.execute();
				_log.fine("MkDir: Token refreshed");
			} else
				throw e;
		}
		_log.fine("MkDir: " + response.getStatusCode());
		return response.parseAs(BoxFolder.class);
	}
}
