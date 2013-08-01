package it.backbox.boxcom;

import it.backbox.IBoxManager;
import it.backbox.IRestClient;
import it.backbox.bean.Chunk;
import it.backbox.client.rest.bean.BoxError;
import it.backbox.client.rest.bean.BoxFile;
import it.backbox.client.rest.bean.BoxFolder;
import it.backbox.client.rest.bean.BoxItemCollection;
import it.backbox.client.rest.bean.BoxSearchResult;
import it.backbox.client.rest.bean.BoxUserInfo;
import it.backbox.exception.BackBoxException;
import it.backbox.exception.RestException;
import it.backbox.utility.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.api.client.http.HttpResponseException;

public class BoxManager implements IBoxManager {
	private static final Logger _log = Logger.getLogger(BoxManager.class.getCanonicalName());

	public static final String ROOT_FOLDER_NAME = "BackBox";
	
	private IRestClient client;

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
	 * @see it.backbox.IBoxManager#mkdir(java.lang.String, java.lang.String)
	 */
	public String mkdir(String folderName, String parentFolderID) throws IOException, RestException, BackBoxException {
		BoxFolder folder;
		try {
			folder = client.mkdir(folderName, parentFolderID);
			if ((folder != null) && (folder.id != null) && !folder.id.isEmpty()) {
				if (_log.isLoggable(Level.INFO)) _log.info("Folder created id: " + folder.id);
				return folder.id;
			}
		} catch (RestException e) {
			HttpResponseException httpe = e.getHttpException();
			if ((httpe != null) && (httpe.getStatusCode() == 409)) {
				if (_log.isLoggable(Level.WARNING)) _log.warning(folderName + " exists");
				BoxError error = e.getError();
				if ((error != null) && 
						(error.context_info != null) && 
						(error.context_info.conflicts != null) &&
						(error.context_info.conflicts.id != null) &&
						!error.context_info.conflicts.id.isEmpty())
					return error.context_info.conflicts.id;
			} else
				throw e;
		}
		
		throw new BackBoxException("Folder not created");
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManager#getBoxID(java.lang.String)
	 */
	public String getBoxID(String filename) throws IOException, RestException {
		BoxSearchResult results = client.search(filename);
		if ((results != null) && (results.entries != null) && !results.entries.isEmpty()) {
			if (_log.isLoggable(Level.INFO)) _log.info(filename + " found");
			return results.entries.get(0).id;
		}
		if (_log.isLoggable(Level.INFO)) _log.info(filename + " not found");
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManager#upload(java.lang.String, java.lang.String)
	 */
	@Override
	public String upload(String filename, String folderID) throws IOException, RestException, BackBoxException {
		String[] ns = filename.split("\\\\");
		String n = ns[ns.length - 1];
		
		byte[] content = Utility.read(filename);
		BoxFile file = upload(n, content, folderID, DigestUtils.sha1Hex(content));
		if (_log.isLoggable(Level.INFO)) _log.info(new StringBuilder(n).append(" uploaded with id ").append(file.id).toString());
		return file.id;
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManager#download(java.lang.String)
	 */
	@Override
	public byte[] download(String fileID) throws IOException, RestException {
		byte[] file = client.download(fileID);
		if (_log.isLoggable(Level.INFO)) _log.info(fileID + " downloaded");
		return file;
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManager#deleteFolder(java.lang.String)
	 */
	public void deleteFolder(String folderID) throws IOException, RestException {
		client.delete(folderID, true);
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManager#delete(java.lang.String)
	 */
	@Override
	public void delete(String fileID) throws IOException, RestException {
		client.delete(fileID, false);
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManagerChunk#deleteChunk(java.util.List)
	 */
	@Override
	public void deleteChunk(List<Chunk> chunks) throws IOException, RestException {
		for(Chunk c : chunks)
			client.delete(c.getBoxid(), false);
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManagerChunk#downloadChunk(java.util.List)
	 */
	@Override
	public List<byte[]> downloadChunk(List<Chunk> chunks) throws IOException, RestException {
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
	public byte[] downloadChunk(Chunk chunk) throws IOException, RestException {
		return download(chunk.getBoxid());
	}
	
	/**
	 * Upload a file to Box.com
	 * 
	 * @param name
	 *            Name of the file to upload
	 * @param content
	 *            Content to upload
	 * @param folderID
	 *            Folder ID where upload the file
	 * @param hash
	 *            File content digest
	 * @return Box file informations
	 * @throws IOException
	 * @throws RestException
	 * @throws BackBoxException 
	 */
	private BoxFile upload(String name, byte[] content, String folderID, String hash) throws IOException, RestException, BackBoxException {
		BoxFile file = null;
		try {
			file = client.upload(name, null, content, folderID, hash);
		} catch (RestException e) {
			HttpResponseException httpe = e.getHttpException();
			if ((httpe != null) && (httpe.getStatusCode() == 409)) {
				if (_log.isLoggable(Level.INFO)) _log.info("Uploading new version");
				BoxError error = e.getError();
				if ((error != null) &&
						(error.context_info != null) &&
						(error.context_info.conflicts != null) &&
						(error.context_info.conflicts.id != null) &&
						!error.context_info.conflicts.id.isEmpty()) {
					String id = error.context_info.conflicts.id;
					if (_log.isLoggable(Level.INFO)) _log.info("upload: 409 Conflict, fileID " + id);
					file = client.upload(name, id, content, folderID, hash);
				} else {
					_log.severe("Problem parsing an 409 HTTP response: missing information of confliction file");
					throw e;
				}
			} else
				throw e;
		}
		if  ((file.id == null) || file.id.isEmpty() || file.id.equals("null"))
			throw new BackBoxException("Uploaded file ID not retrieved");
		return file;
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManager#uploadChunk(it.backbox.bean.Chunk, java.lang.String)
	 */
	@Override
	public void uploadChunk(Chunk chunk, String folderID) throws IOException, RestException, BackBoxException {
		String name = chunk.getChunkname();
		String[] ns = name.split("\\\\");
		String n = ns[ns.length - 1];
		
		BoxFile file = upload(n, chunk.getContent(), folderID, chunk.getChunkhash());
		if (_log.isLoggable(Level.INFO)) _log.info(new StringBuilder(n).append(" uploaded with id ").append(file.id).toString());
		chunk.setBoxid(file.id);
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManager#getFolderChunks(java.lang.String)
	 */
	public Map<String, List<Chunk>> getFolderChunks(String folderID) throws IOException, RestException {
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
			if (_log.isLoggable(Level.INFO)) _log.info("Rebuilding chunks, added chunk " + hash);
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

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManager#getFreeSpace()
	 */
	@Override
	public long getFreeSpace() throws IOException, RestException {
		BoxUserInfo userInfo = client.getUserInfo();
		if (_log.isLoggable(Level.INFO)) _log.info("Retrieved information about user: " + userInfo.login);
		return userInfo.space_amount - userInfo.space_used;
	}
}
