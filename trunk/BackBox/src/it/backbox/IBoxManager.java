package it.backbox;

import it.backbox.bean.Chunk;

import java.util.List;
import java.util.Map;

public interface IBoxManager {
	
	/**
	 * Get the app folder ID
	 * 
	 * @return App folder ID
	 */
	public String getBackBoxFolderID();
	
	/**
	 * Check if the token to access to Box.com is valid
	 * 
	 * @return True if the token is valid, false otherwise
	 */
	public boolean isAccessTokenValid();

	/**
	 * Upload a byte array content to Box.com
	 * 
	 * @param src
	 *            Byte array to upload
	 * @param filename
	 *            Name of the file to upload
	 * @param remotefolderID
	 *            ID of the folder where upload the byte array
	 * @return Uploaded file ID
	 * @throws Exception 
	 */
	public String upload(byte[] src, String filename, String remotefolderID) throws Exception;

	/**
	 * Upload a list of byte array contents to Box.com
	 * 
	 * @param src
	 *            List of byte array to upload
	 * @param filename
	 *            List of names of the files to upload
	 * @param remotefolderID
	 *            ID of the folder where upload the byte arrays
	 * @return List of uploaded file IDs
	 * @throws Exception 
	 */
	public Map<String, String> upload(List<byte[]> src, List<String> filename, String remotefolderID) throws Exception;

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
	public String upload(String filename, String remotefolderID) throws Exception;

	/**
	 * Upload a list of files to Box.com
	 * 
	 * @param filename
	 *            List of names of the files to upload
	 * @param remotefolderID
	 *            ID of the folder where upload the chunks
	 * @return Uploaded file ID
	 * @throws Exception 
	 */
	public Map<String, String> upload(List<String> filename, String remotefolderID) throws Exception;

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
	 * Download a list of files from Box.com
	 * 
	 * @param fileID
	 *            List of IDs of the files to download
	 * @return List of Byte arrays downloaded files contents
	 * @throws Exception 
	 */
	public List<byte[]> download(List<String> fileID) throws Exception;

	/**
	 * Download a file from Box.com to a file
	 * 
	 * @param fileID
	 *            ID of the file to download
	 * @param destfilename
	 *            Downloaded file
	 * @throws Exception 
	 */
	public void download(String fileID, String destfilename) throws Exception;

	/**
	 * Download a list of files from Box.com to files
	 * 
	 * @param fileID
	 *            List of IDs of the files to download
	 * @param destfilename
	 *            Downloaded files
	 * @throws Exception 
	 */
	public void download(List<String> fileID, List<String> destfilename) throws Exception;
	
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
	 * Delete a Chunk from Box.com
	 * 
	 * @param chunk
	 *            Chunk to download
	 * @throws Exception 
	 */
	public void deleteChunk(Chunk chunk) throws Exception;
	
	/**
	 * Delete a list of Chunk from Box.com
	 * 
	 * @param chunks
	 *            List of Chunk to delete
	 * @throws Exception 
	 */
	public void deleteChunk(List<Chunk> chunks) throws Exception;
	

	/**
	 * Download a Chunk from Box.com
	 * 
	 * @param chunk
	 *            Chunk to download
	 * @return Byte array chunk content
	 * @throws Exception 
	 */
	public byte[] downloadChunk(Chunk chunk) throws Exception;

	/**
	 * Download a Chunk to a file from Box.com
	 * 
	 * @param chunk
	 *            Chunk to download
	 * @param destfolder
	 *            Folder where put downloaded file
	 * @throws Exception 
	 */
	public void downloadChunk(Chunk chunk, String destfolder) throws Exception;

	/**
	 * Download a list of Chunk from Box.com
	 * 
	 * @param chunks
	 *            List of Chunk to download
	 * @return List of byte arrays chunks contents
	 */
	public List<byte[]> downloadChunk(List<Chunk> chunks) throws Exception;

	/**
	 * Download a list of Chunk to files from Box.com
	 * 
	 * @param chunks
	 *            List of Chunk to download
	 * @param destfolder
	 *            Folder where put downloaded files
	 * @throws Exception 
	 */
	public void downloadChunk(List<Chunk> chunks, String destfolder) throws Exception;
	
	/**
	 * Upload a Chunk (content) to Box.com
	 * 
	 * @param chunks
	 *            Chunk to upload
	 * @param remotefolderID
	 *            ID of the folder where upload the chunks
	 * @throws Exception 
	 */
	public void uploadChunk(Chunk chunk, String remotefolderID) throws Exception;

	/**
	 * Upload a list of Chunk (contents) to Box.com
	 * 
	 * @param chunks
	 *            List of Chunk to upload
	 * @param remotefolderID
	 *            ID of the folder where upload the chunks
	 * @throws Exception 
	 */
	public void uploadChunk(List<Chunk> chunks, String remotefolderID) throws Exception;

	/**
	 * Upload a list of Chunk (files) to Box.com
	 * 
	 * @param chunks
	 *            List of Chunk to upload
	 * @param srcFolder
	 *            Chunks files folder
	 * @param remotefolderID
	 *            ID of the folder where upload the chunks
	 * @throws Exception 
	 */
	public void uploadChunk(List<Chunk> chunks, String srcFolder, String remotefolderID) throws Exception;

	/**
	 * Upload a Chunk (files) to Box.com
	 * 
	 * @param chunks
	 *            List of Chunk to upload
	 * @param srcFolder
	 *            Chunk file folder
	 * @param remotefolderID
	 *            ID of the folder where upload the chunks
	 * @throws Exception 
	 */
	public void uploadChunk(Chunk chunk, String srcFolder, String remotefolderID) throws Exception;
	
}
