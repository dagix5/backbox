package it.backbox;

import it.backbox.bean.Chunk;
import it.backbox.exception.BackBoxException;
import it.backbox.exception.RestException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IBoxManager {
	
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
	public String getBoxID(String filename) throws IOException, RestException;
	
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
	 * @param parentFolderID
	 * 			  ID of the parent folder
	 * @return ID of the folder created
	 * @throws IOException
	 * @throws RestException 
	 * @throws BackBoxException
	 */
	public String mkdir(String folderName, String parentFolderID) throws IOException, RestException, BackBoxException;

	/**
	 * Upload a file to Box.com
	 * 
	 * @param filename
	 *            Name of the file to upload
	 * @param folderID
	 *            Folder ID where upload the file
	 * @return Uploaded file ID
	 * @throws IOException
	 * @throws RestException 
	 * @throws BackBoxException 
	 */
	public String upload(String filename, String folderID) throws IOException, RestException, BackBoxException;
	
	/**
	 * Upload a new version of a file to Box.com
	 * 
	 * @param filename
	 *            Name of the file to upload
	 * @param fileID
	 * 			  File ID to overwrite
	 * @param folderID
	 *            Folder ID where upload the file
	 * @return Uploaded file ID
	 * @throws IOException
	 * @throws RestException 
	 * @throws BackBoxException 
	 */
	public String upload(String filename, String fileID, String folderID) throws IOException, RestException, BackBoxException;

	/**
	 * Download a file from Box.com
	 * 
	 * @param fileID
	 *            ID of the file to download
	 * @return Byte array downloaded file content
	 * @throws IOException
	 * @throws RestException 
	 */
	public byte[] download(String fileID) throws IOException, RestException;

	/**
	 * Delete a folder from Box.com
	 * 
	 * @param folderID
	 *            ID of the folder to delete
	 * @throws IOException
	 * @throws RestException 
	 */
	public void deleteFolder(String folderID) throws IOException, RestException;
	
	/**
	 * Delete a file from Box.com
	 * 
	 * @param fileID
	 *            ID of the file to delete
	 * @throws IOException
	 * @throws RestException 
	 */
	public void delete(String fileID) throws IOException, RestException;
	
	/**
	 * Delete a list of Chunk from Box.com
	 * 
	 * @param chunks
	 *            List of Chunk to delete
	 * @throws IOException
	 * @throws RestException 
	 */
	public void deleteChunk(List<Chunk> chunks) throws IOException, RestException;

	/**
	 * Download a list of Chunk from Box.com
	 * 
	 * @param chunks
	 *            List of Chunk to download
	 * @return List of byte arrays chunks contents
	 * @throws IOException
	 * @throws RestException 
	 */
	public List<byte[]> downloadChunk(List<Chunk> chunks) throws IOException, RestException;
	
	/**
	 * Download a chunk from Box.com
	 * 
	 * @param chunk
	 *            Chunk to download
	 * @return Byte array chunk content
	 * @throws IOException
	 * @throws RestException 
	 */
	public byte[] downloadChunk(Chunk chunk) throws IOException, RestException;
	
	/**
	 * Upload a Chunk (contents) to Box.com
	 * 
	 * @param chunks
	 *            Chunk to upload
	 * @param folderID
	 *            ID of the folder where upload the chunks
	 * @throws IOException
	 * @throws RestException 
	 * @throws BackBoxException
	 */
	public void uploadChunk(Chunk chunk, String folderID) throws IOException, RestException, BackBoxException;
	
	/**
	 * Get a map with all chunks for all the files in the remote folder with ID
	 * <i>folderID</i>
	 * 
	 * @param folderID
	 *            ID of the folder to scan
	 * @return Map with <File hash, List of Chunks>
	 * @throws IOException
	 * @throws RestException 
	 */
	public Map<String, List<Chunk>> getFolderChunks(String folderID) throws IOException, RestException;
	
	/**
	 * Get the space available on Box.com account
	 * 
	 * @return The free space
	 * @throws IOException
	 * @throws RestException
	 */
	public long getFreeSpace() throws IOException, RestException;
	
	/**
	 * Check if a file was deleted remotely
	 * 
	 * @param fileID
	 *            ID of the file to check
	 * @return false if the file was deleted, true otherwise
	 * @throws IOException
	 * @throws RestException
	 */
	public boolean checkRemoteFile(String fileID) throws IOException, RestException;

}
