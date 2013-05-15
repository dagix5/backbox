package it.backbox.boxcom;

import it.backbox.IBoxManager;
import it.backbox.IRestClient;
import it.backbox.bean.Chunk;
import it.backbox.client.rest.bean.BoxFile;
import it.backbox.client.rest.bean.BoxFolder;
import it.backbox.client.rest.bean.BoxSearchResult;
import it.backbox.exception.BackBoxException;
import it.backbox.exception.RestException;
import it.backbox.security.DigestManager;
import it.backbox.utility.Utility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Hex;

public class BoxManager implements IBoxManager {
	private static Logger _log = Logger.getLogger(BoxManager.class.getCanonicalName());

	public static final String UPLOAD_FOLDER = "BackBox";

	private String backBoxFolderID;
	private IRestClient client;

	/**
	 * Costructor
	 * 
	 * @param backBoxFolderID
	 *            App folder ID
	 * @throws Exception 
	 */
	public BoxManager(String backBoxFolderID, IRestClient client) throws Exception {
		this.backBoxFolderID = backBoxFolderID;
		if (_log.isLoggable(Level.FINE)) _log.fine("BoxManager created with folder id: " + backBoxFolderID);
		this.client = client;
	}
	
	/**
	 * Costructor
	 * 
	 * @throws Exception 
	 */
	public BoxManager() throws Exception {
		this(null, null);
	}
	
	public void setRestClient(IRestClient client) {
		this.client = client;
	}
	
	/**
	 * Set the app folder ID
	 * 
	 * @param uploadFolderID
	 *            The app folder ID
	 */
	public void setBackBoxFolderID(String backBoxFolderID) {
		this.backBoxFolderID = backBoxFolderID;
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManager#getBackBoxFolderID()
	 */
	public String getBackBoxFolderID() {
		return backBoxFolderID;
	}
	
	/**
	 * Create a folder on box.com
	 * 
	 * @param folderName
	 *            Folder name
	 * @return ID of the folder created
	 * @throws IOException
	 * @throws RestException 
	 */
	public String mkdir(String folderName) throws Exception {
		BoxFolder folder = client.mkdir(folderName);
		if (folder != null) {
			if (_log.isLoggable(Level.FINE)) _log.fine("Folder created id: " + folder.id);
			return folder.id;
		}
		if (_log.isLoggable(Level.FINE)) _log.fine("Folder not created");
		return null;
	}
	
	/**
	 * Get the box.com ID of the file with name filename
	 * 
	 * @param filename
	 *            Name of the file to search
	 * @return Box.com ID
	 * @throws IOException
	 * @throws RestException 
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
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManager#upload(byte[], java.lang.String, java.lang.String)
	 */
	@Override
	public String upload(byte[] src, String filename, String remotefolderID) throws Exception {
		ArrayList<byte[]> srcs = new ArrayList<>();
		srcs.add(src);
		ArrayList<String> filenames = new ArrayList<>();
		filenames.add(filename);
		
		HashMap<String, String> result = upload(srcs, filenames, remotefolderID);
		
		return result.get(filename);
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManager#upload(java.util.ArrayList, java.util.ArrayList, java.lang.String)
	 */
	@Override
	public HashMap<String, String> upload(ArrayList<byte[]> src, ArrayList<String> filenames, String remotefolderID) throws Exception {
		if (src.size() != filenames.size())
			throw new BackBoxException("src and filename lists should have same size");
		
		try {
			HashMap<String, String> ids = new HashMap<String, String>();
			for (int i = 0; i < filenames.size(); i++) {
				String name = filenames.get(i);
				String[] ns = name.split("\\\\");
				String n = ns[ns.length - 1];
				
				BoxFile file = client.upload(n, getBoxID(n), src.get(i), remotefolderID, Hex.encodeHexString(DigestManager.hash(src.get(i))));
				String id = ((file != null) ? file.id : null);
				if (_log.isLoggable(Level.FINE)) _log.fine(n + " uploaded with id" + id);
				ids.put(name, id);
			}
			return ids;
		} catch (RestException e) {
			throw new BackBoxException(e.getLocalizedMessage());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManager#upload(java.lang.String, java.lang.String)
	 */
	@Override
	public String upload(String filename, String remotefolderID) throws Exception {
		ArrayList<String> filenames = new ArrayList<>();
		filenames.add(filename);
		HashMap<String, String> result = upload(filenames, remotefolderID);
		return result.get(filename);
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManager#upload(java.util.ArrayList, java.lang.String)
	 */
	@Override
	public HashMap<String, String> upload(ArrayList<String> filenames, String remotefolderID) throws Exception{
		HashMap<String, String> ids = new HashMap<String, String>();
		for (String name : filenames) {
			String[] ns = name.split("\\\\");
			String n = ns[ns.length - 1];
			
			byte[] content = Utility.read(name);
			BoxFile file = client.upload(n, getBoxID(n), content, remotefolderID,Hex.encodeHexString(DigestManager.hash(content)));
			String id = ((file != null) ? file.id : null);
			if (_log.isLoggable(Level.FINE)) _log.fine(n + " uploaded with id" + id);
			ids.put(name, id);
		}
		return ids;
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
	 * @see it.backbox.IBoxManager#download(java.util.ArrayList)
	 */
	@Override
	public ArrayList<byte[]> download(ArrayList<String> fileID) throws Exception {
		ArrayList<byte[]> result = new ArrayList<>();
		for (String fID : fileID)
			result.add(download(fID));
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManager#download(java.lang.String, java.lang.String)
	 */
	@Override
	public void download(String fileID, String destfilename) throws Exception {
		File file = new File(destfilename);
		File parent = file.getParentFile();
		if((parent != null) && !parent.exists() && !parent.mkdirs()){
		    throw new IllegalStateException("Couldn't create dir: " + parent);
		}
		file.createNewFile();
		
		Utility.write(download(fileID), file);
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManager#download(java.util.ArrayList, java.util.ArrayList)
	 */
	@Override
	public void download(ArrayList<String> fileID, ArrayList<String> destfilename) throws Exception {
		if (fileID.size() != destfilename.size())
			throw new BackBoxException("fileID and destfilename lists should have same size");
		for (int i = 0; i < fileID.size(); i++) {
			String fID = fileID.get(i);
			String dfn = destfilename.get(i);
			download(fID, dfn);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManager#delete(java.lang.String)
	 */
	@Override
	public void delete(String fileID) throws Exception {
		client.delete(fileID);
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManagerChunk#deleteChunk(it.backbox.bean.Chunk)
	 */
	@Override
	public void deleteChunk(Chunk chunk) throws Exception {
		delete(chunk.getBoxid());
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManagerChunk#deleteChunk(java.util.ArrayList)
	 */
	@Override
	public void deleteChunk(ArrayList<Chunk> chunks) throws Exception {
		for(Chunk c : chunks)
			deleteChunk(c);
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManagerChunk#downloadChunk(it.backbox.bean.Chunk)
	 */
	@Override
	public byte[] downloadChunk(Chunk chunk) throws Exception {
		return download(chunk.getBoxid());
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManagerChunk#downloadChunk(it.backbox.bean.Chunk, java.lang.String)
	 */
	@Override
	public void downloadChunk(Chunk chunk, String destfolder) throws Exception {
		download(chunk.getBoxid(), new StringBuilder(destfolder).append("\\").append(chunk.getChunkname()).toString());
		
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManagerChunk#downloadChunk(java.util.ArrayList)
	 */
	@Override
	public ArrayList<byte[]> downloadChunk(ArrayList<Chunk> chunks) throws Exception {
		ArrayList<byte[]> result = new ArrayList<>();
		for(Chunk c : chunks)
			result.add(downloadChunk(c));
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManagerChunk#downloadChunk(java.util.ArrayList, java.lang.String)
	 */
	@Override
	public void downloadChunk(ArrayList<Chunk> chunks, String destfolder) throws Exception {
		for(Chunk c : chunks)
			downloadChunk(c, new StringBuilder(destfolder).append("\\").append(c.getChunkname()).toString());
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManagerChunk#uploadChunk(it.backbox.bean.Chunk, java.lang.String)
	 */
	@Override
	public void uploadChunk(Chunk chunk, String remotefolderID) throws Exception {
		ArrayList<Chunk> chunks = new ArrayList<>();
		chunks.add(chunk);
		uploadChunk(chunks, remotefolderID);
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManagerChunk#uploadChunk(java.util.ArrayList, java.lang.String)
	 */
	@Override
	public void uploadChunk(ArrayList<Chunk> chunks, String remotefolderID) throws Exception {
		ArrayList<byte[]> srcs = new ArrayList<>();
		ArrayList<String> filenames = new ArrayList<>();
		for (Chunk c : chunks) {
			srcs.add(c.getContent());
			filenames.add(c.getChunkname());
		}
		HashMap<String, String> result = upload(srcs, filenames, remotefolderID);
		for (int i = 0; i < result.size(); i++)
        	chunks.get(i).setBoxid(result.get(chunks.get(i).getChunkname()));
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManagerChunk#uploadChunk(java.util.ArrayList, java.lang.String, java.lang.String)
	 */
	@Override
	public void uploadChunk(ArrayList<Chunk> chunks, String srcFolder, String remotefolderID) throws Exception {
		ArrayList<String> filenames = new ArrayList<>();
		for (Chunk c : chunks)
			filenames.add(srcFolder + "\\" + c.getChunkname());
		
		HashMap<String, String> result = upload(filenames, remotefolderID);
		for (int i = 0; i < result.size(); i++)
        	chunks.get(i).setBoxid(result.get(chunks.get(i).getChunkname()));
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IBoxManagerChunk#uploadChunk(it.backbox.bean.Chunk, java.lang.String, java.lang.String)
	 */
	@Override
	public void uploadChunk(Chunk chunk, String srcFolder, String remotefolderID) throws Exception {
		ArrayList<Chunk> chunks = new ArrayList<>();
		chunks.add(chunk);
		uploadChunk(chunks, srcFolder, remotefolderID);
	}
}
