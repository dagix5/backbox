package it.backbox;

import it.backbox.bean.Chunk;
import it.backbox.exception.RestException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IBoxManager {
	
	/**
	 * Set the app folder ID
	 * 
	 * @param backBoxFolderID
	 *            The app folder ID
	 */
	public void setBackBoxFolderID(String backBoxFolderID);
	
	/**
	 * Set the client to use for rest operations
	 * 
	 * @param client
	 *            Rest Client
	 */
	public void setRestClient(IRestClient client);
	
	/**
	 * Get the box.com ID of the file with name filename
	 * 
	 * @param filename
	 *            Name of the file to search
	 * @return Box.com ID
	 * @throws IOException
	 * @throws RestException 
	 */
	public String getBoxID(String filename) throws Exception;
	
	/**
	 * Check if the token to access to Box.com is valid
	 * 
	 * @return True if the token is valid, false otherwise
	 */
	public boolean isAccessTokenValid();

	/**
	 * Create a folder on box.com
	 * 
	 * @param folderName
	 *            Folder name
	 * @return ID of the folder created
	 * @throws IOException
	 * @throws RestException 
	 */
	public String mkdir(String folderName) throws IOException, RestException;

	/**
	 * Upload a file to Box.com
	 * 
	 * @param filename
	 *            Name of the file to upload
	 * @return Uploaded file ID
	 * @throws Exception 
	 */
	public String upload(String filename) throws Exception;

	/**
	 * Download a file from Box.com
	 * 
	 * @param fileID
	 *            ID of the file to download
	 * @return Byte array downloaded file content
	 * @throws Exception 
	 */
	public byte[] download(String fileID) throws Exception;

	/**
	 * Delete a folder from Box.com
	 * 
	 * @param folderID
	 *            ID of the folder to delete
	 * @throws Exception 
	 */
	public void deleteFolder(String folderID) throws Exception;
	
	/**
	 * Delete a file from Box.com
	 * 
	 * @param fileID
	 *            ID of the file to delete
	 * @throws Exception 
	 */
	public void delete(String fileID) throws Exception;
	
	/**
	 * Delete a list of Chunk from Box.com
	 * 
	 * @param chunks
	 *            List of Chunk to delete
	 * @throws Exception 
	 */
	public void deleteChunk(List<Chunk> chunks) throws Exception;

	/**
	 * Download a list of Chunk from Box.com
	 * 
	 * @param chunks
	 *            List of Chunk to download
	 * @return List of byte arrays chunks contents
	 */
	public List<byte[]> downloadChunk(List<Chunk> chunks) throws Exception;

	/**
	 * Upload a list of Chunk (contents) to Box.com
	 * 
	 * @param chunks
	 *            List of Chunk to upload
	 * @throws Exception 
	 */
	public void uploadChunk(List<Chunk> chunks) throws Exception;
	
	/**
	 * Upload a Chunk (contents) to Box.com
	 * 
	 * @param chunk
	 *            Chunk to upload
	 * @throws Exception 
	 */
	public void uploadChunk(Chunk chunk) throws Exception;
	
	/**
	 * Get a map with all chunks for all the files in the remote folder with ID
	 * <i>folderID</i>
	 * 
	 * @param folderID
	 *            ID of the folder to scan
	 * @return Map with <File hash, List of Chunks>
	 * @throws Exception
	 */
	public Map<String, List<Chunk>> getFolderChunks(String folderID) throws Exception;

}
