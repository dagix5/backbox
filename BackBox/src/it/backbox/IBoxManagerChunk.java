package it.backbox;

import it.backbox.bean.Chunk;

import java.util.ArrayList;

public interface IBoxManagerChunk {
	
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
	public void deleteChunk(ArrayList<Chunk> chunks) throws Exception;
	

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
	public ArrayList<byte[]> downloadChunk(ArrayList<Chunk> chunks) throws Exception;

	/**
	 * Download a list of Chunk to files from Box.com
	 * 
	 * @param chunks
	 *            List of Chunk to download
	 * @param destfolder
	 *            Folder where put downloaded files
	 * @throws Exception 
	 */
	public void downloadChunk(ArrayList<Chunk> chunks, String destfolder) throws Exception;
	
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
	public void uploadChunk(ArrayList<Chunk> chunks, String remotefolderID) throws Exception;

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
	public void uploadChunk(ArrayList<Chunk> chunks, String srcFolder, String remotefolderID) throws Exception;

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
