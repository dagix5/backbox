package it.backbox.boxcom;

import it.backbox.IBoxManager;
import it.backbox.IRestClient;
import it.backbox.bean.Chunk;
import it.backbox.client.rest.bean.BoxError;
import it.backbox.client.rest.bean.BoxFile;
import it.backbox.client.rest.bean.BoxFolder;
import it.backbox.client.rest.bean.BoxItemCollection;
import it.backbox.client.rest.bean.BoxSearchResult;
import it.backbox.exception.RestException;
import it.backbox.utility.Utility;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.api.client.http.HttpResponseException;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;

public class BoxManager implements IBoxManager {
	private static Logger _log = Logger.getLogger(BoxManager.class.getCanonicalName());

	public static final String UPLOAD_FOLDER = "BackBox";
	
	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();

	private String backBoxFolderID;
	private IRestClient client;

	/**
	 * Costructor
	 * 
	 * @param backBoxFolderID
	 *            App folder ID
	 * @param client
	 *            Rest Client
	 */
	public BoxManager(String backBoxFolderID, IRestClient client) {
		this.backBoxFolderID = backBoxFolderID;
		if (_log.isLoggable(Level.FINE)) _log.fine("BoxManager created with folder id: " + backBoxFolderID);
		this.client = client;
	}
	
	/**
	 * Costructor
	 * 
	 * @param client
	 *            Rest Client
	 */
	public BoxManager(IRestClient client) {
		this.client = client;
	}
	
	/**
	 * Constructor
	 */
	public BoxManager() {
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManager#setRestClient(it.backbox.IRestClient)
	 */
	@Override
	public void setRestClient(IRestClient client) {
		this.client = client;
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManager#setBackBoxFolderID(java.lang.String)
	 */
	@Override
	public void setBackBoxFolderID(String backBoxFolderID) {
		this.backBoxFolderID = backBoxFolderID;
	}

	/**
	 * Get the app folder ID
	 * 
	 * @return App folder ID
	 */
	public String getBackBoxFolderID() {
		return backBoxFolderID;
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManager#mkdir(java.lang.String)
	 */
	public String mkdir(String folderName) throws IOException, RestException {
		BoxFolder folder;
		try {
			folder = client.mkdir(folderName);
			if (folder != null) {
				if (_log.isLoggable(Level.FINE)) _log.fine("Folder created id: " + folder.id);
				return folder.id;
			}
		} catch (RestException e) {
			HttpResponseException httpe = e.getHttpException();
			if ((httpe != null) && (httpe.getStatusCode() == 409)) {
				_log.warning(folderName + " exists");
				JsonObjectParser parser = new JsonObjectParser(JSON_FACTORY);
				BoxError error = parser.parseAndClose(new StringReader(httpe.getContent()), BoxError.class);
				if ((error != null) && 
						(error.context_info != null) && 
						(error.context_info.conflicts != null) && 
						!error.context_info.conflicts.isEmpty())
					return error.context_info.conflicts.get(0).id;
			} else
				throw e;
		}
		
		if (_log.isLoggable(Level.FINE)) _log.fine("Folder not created");
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManager#getBoxID(java.lang.String)
	 */
	public String getBoxID(String filename) throws Exception {
		BoxSearchResult results = client.search(filename);
		if ((results != null) && (results.entries != null) && !results.entries.isEmpty()) {
			if (_log.isLoggable(Level.FINE)) _log.fine(filename + " found");
			return results.entries.get(0).id;
		}
		if (_log.isLoggable(Level.FINE)) _log.fine(filename + " not found");
		return null;
	}
	
	/**
	 * Upload a file to Box.com
	 * 
	 * @param filename
	 *            Name of the file to upload
	 * @param remotefolderID
	 *            ID of the folder where upload the chunks
	 * @return Uploaded file ID
	 * @throws Exception 
	 */
	private String upload(String filename, String remotefolderID) throws Exception {
		String[] ns = filename.split("\\\\");
		String n = ns[ns.length - 1];
		
		byte[] content = Utility.read(filename);
		BoxFile file = upload(n, content, remotefolderID, DigestUtils.sha1Hex(content));
		String id = ((file != null) ? file.id : null);
		if (_log.isLoggable(Level.FINE)) _log.fine(n + " uploaded with id " + id);
		return id;
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManager#upload(java.lang.String)
	 */
	@Override
	public String upload(String filename) throws Exception {
		return upload(filename, getBackBoxFolderID());
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManager#download(java.lang.String)
	 */
	@Override
	public byte[] download(String fileID) throws Exception {
		byte[] file = client.download(fileID);
		if (_log.isLoggable(Level.FINE)) _log.fine(fileID + " downloaded");
		return file;
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManager#deleteFolder(java.lang.String)
	 */
	public void deleteFolder(String folderID) throws Exception {
		client.delete(folderID, true);
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManager#delete(java.lang.String)
	 */
	@Override
	public void delete(String fileID) throws Exception {
		client.delete(fileID, false);
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManagerChunk#deleteChunk(java.util.List)
	 */
	@Override
	public void deleteChunk(List<Chunk> chunks) throws Exception {
		for(Chunk c : chunks)
			client.delete(c.getBoxid(), false);
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManagerChunk#downloadChunk(java.util.List)
	 */
	@Override
	public List<byte[]> downloadChunk(List<Chunk> chunks) throws Exception {
		List<byte[]> result = new ArrayList<>();
		for(Chunk c : chunks)
			result.add(downloadChunk(c));
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManager#downloadChunk(it.backbox.bean.Chunk)
	 */
	@Override
	public byte[] downloadChunk(Chunk chunk) throws Exception {
		return download(chunk.getBoxid());
	}
	
	/**
	 * Upload a file to Box.com
	 * 
	 * @param name
	 *            Name of the file to upload
	 * @param content
	 *            Content to upload
	 * @param remotefolderID
	 *            Folder ID where upload the file
	 * @param hash
	 *            File content digest
	 * @return Box file informations
	 * @throws IOException
	 * @throws RestException
	 */
	private BoxFile upload(String name, byte[] content, String remotefolderID, String hash) throws IOException, RestException {
		BoxFile file = null;
		try {
			file = client.upload(name, null, content, remotefolderID, hash);
		} catch (RestException e) {
			HttpResponseException httpe = e.getHttpException();
			if ((httpe != null) && (httpe.getStatusCode() == 409)) {
				_log.fine("Uploading new version");
				JsonObjectParser parser = new JsonObjectParser(JSON_FACTORY);
				BoxError error = parser.parseAndClose(new StringReader(httpe.getContent()), BoxError.class);
				if ((error != null) &&
						(error.context_info != null) &&
						(error.context_info.conflicts != null) &&
						!error.context_info.conflicts.isEmpty()) {
					String id = error.context_info.conflicts.get(0).id;
					_log.fine("upload: 409 Conflict, fileID " + id);
					file = client.upload(name, id, content, remotefolderID, hash);
				} else
					throw e;
			}
		}
		return file;
	}

	/**
	 * Upload a Chunk (contents) to Box.com
	 * 
	 * @param chunks
	 *            Chunk to upload
	 * @param remotefolderID
	 *            ID of the folder where upload the chunks
	 * @throws Exception 
	 */
	private void uploadChunk(Chunk chunk, String remotefolderID) throws Exception {
		String name = chunk.getChunkname();
		String[] ns = name.split("\\\\");
		String n = ns[ns.length - 1];
		
		BoxFile file = upload(n, chunk.getContent(), remotefolderID, chunk.getChunkhash());
		String id = ((file != null) ? file.id : null);
		if (_log.isLoggable(Level.FINE)) _log.fine(n + " uploaded with id " + id);
		chunk.setBoxid(id);
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManager#uploadChunk(it.backbox.bean.Chunk)
	 */
	@Override
	public void uploadChunk(Chunk chunk) throws Exception {
		uploadChunk(chunk, getBackBoxFolderID());
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManager#uploadChunk(java.util.List)
	 */
	@Override
	public void uploadChunk(List<Chunk> chunks) throws Exception {
		for (Chunk c : chunks)
			uploadChunk(c, getBackBoxFolderID());
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManager#getFolderChunks(java.lang.String)
	 */
	public Map<String, List<Chunk>> getFolderChunks(String folderID) throws Exception {
		Map<String, List<Chunk>> info = new HashMap<>();
		BoxItemCollection items = client.getFolderItems(folderID);
		List<BoxFile> files = items.entries;
		for (BoxFile file : files) {
			Chunk c = new Chunk();
			c.setBoxid(file.id);
			c.setChunkhash(file.sha1);
			c.setChunkname(file.name);
			String hash = file.name.split("\\.")[0];
			if (!info.containsKey(hash)) {
				List<Chunk> chunks = new ArrayList<>();
				info.put(hash, chunks);
			}
			info.get(hash).add(c);
			
		}
		return info;
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManager#isAccessTokenValid()
	 */
	@Override
	public boolean isAccessTokenValid() {
		if (client == null)
			return false;
		return client.isAccessTokenValid();
	}

}
