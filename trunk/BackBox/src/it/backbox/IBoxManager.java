package it.backbox;

import java.util.ArrayList;
import java.util.HashMap;

public interface IBoxManager {

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
	public HashMap<String, String> upload(ArrayList<byte[]> src, ArrayList<String> filename, String remotefolderID) throws Exception;

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
	public HashMap<String, String> upload(ArrayList<String> filename, String remotefolderID) throws Exception;

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
	public ArrayList<byte[]> download(ArrayList<String> fileID) throws Exception;

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
	public void download(ArrayList<String> fileID, ArrayList<String> destfilename) throws Exception;
	
	/**
	 * Delete a file from Box.com
	 * 
	 * @param fileID
	 *            ID of the file to delete
	 * @throws Exception 
	 */
	public void delete(String fileID) throws Exception;
	
}
