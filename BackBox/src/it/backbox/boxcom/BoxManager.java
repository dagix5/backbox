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
import org.apache.commons.io.FilenameUtils;

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
				BoxFile file = getBoxFileFromConflict(error);
				if (file == null)
					return null;
				else
					return file.id;
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
		return upload(filename, null, folderID);
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManager#upload(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String upload(String filename, String fileID, String folderID) throws IOException, RestException, BackBoxException {
		String[] ns = filename.split("\\\\");
		String n = ns[ns.length - 1];
		
		byte[] content = Utility.read(filename);
		BoxFile file = upload(n, fileID, content, folderID, DigestUtils.sha1Hex(content));
		if (_log.isLoggable(Level.INFO)) _log.info(n + " uploaded with id " + file.id);
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
	 * @param fileID
	 * 			  File ID (null for new files)
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
	private BoxFile upload(String name, String fileID, byte[] content, String folderID, String hash) throws IOException, RestException, BackBoxException {
		BoxFile file = null;
		try {
			file = client.upload(name, fileID, content, folderID, hash);
		} catch (RestException e) {
			HttpResponseException httpe = e.getHttpException();
			if ((httpe != null) && (httpe.getStatusCode() == 409)) {
				_log.info("Uploading new version");
				BoxError error = e.getError();
				BoxFile conflict = getBoxFileFromConflict(error);
				if (conflict != null) {
					if (_log.isLoggable(Level.WARNING)) _log.warning("upload: 409 Conflict, fileID " + conflict.id);
					if ((conflict.sha1 != null) &&
							!conflict.sha1.isEmpty() &&
							(hash != null) &&
							conflict.sha1.equalsIgnoreCase(hash)) {
						file = conflict;
						_log.info("upload: 409 Conflict, new file is the same");
					} else {
						_log.info("upload: 409 Conflict, uploading new version...");
						file = client.upload(name, conflict.id, content, folderID, hash);
					}
				} else {
					_log.severe("Problem parsing an 409 HTTP response: missing information of confliction file");
					throw e;
				}
			} else
				throw e;
		}
		if ((file.id == null) || file.id.isEmpty() || file.id.equals("null"))
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
		
		BoxFile file = upload(n, null, chunk.getContent(), folderID, chunk.getChunkhash());
		if (_log.isLoggable(Level.INFO)) _log.info(n + " uploaded with id " + file.id);
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
			String hash = FilenameUtils.getBaseName(file.name);
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
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManager#checkRemoteFile(java.lang.String)
	 */
	@Override
	public boolean checkRemoteFile(String fileID) throws IOException, RestException {
		try {
			BoxFile file = client.getFileInfo(fileID);
			if  ((file.id == null) || file.id.isEmpty() || file.id.equals("null")) {
				if (_log.isLoggable(Level.WARNING))
					_log.warning("File ID " + fileID + " not found but status 200");
				return false;
			}
		} catch (RestException e) {
			if (e.getHttpException().getStatusCode() == 404)
				return false;
			throw e;
		}
		return true;
	}
	
	private BoxFile getBoxFileFromConflict(BoxError error) {
		if ((error != null) && (error.context_info != null)) {
			if ((error.context_info.conflicts != null) && (error.context_info.conflicts.length > 0)
					&& (error.context_info.conflicts[0].id != null) && !error.context_info.conflicts[0].id.isEmpty())
				return error.context_info.conflicts[0];
			else if ((error.context_info.conflict != null) && (error.context_info.conflict.id != null)
					&& !error.context_info.conflict.id.isEmpty())
				return error.context_info.conflict;
		}
		return null;
	}
}
