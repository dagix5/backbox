package it.backbox;

import it.backbox.client.rest.bean.BoxFile;
import it.backbox.client.rest.bean.BoxFolder;
import it.backbox.client.rest.bean.BoxSearchResult;

public interface IRestClient {
	
	/**
	 * Download a file with ID <i>fileID</i>
	 * 
	 * @param fileID
	 *            ID file to download
	 * @return File content
	 * @throws Exception
	 */
	public byte[] download(String fileID) throws Exception;
	
	/**
	 * Create a new remote folder
	 * 
	 * @param name
	 *            Folder name
	 * @return New folder info
	 * @throws Exception
	 */
	public BoxFolder mkdir(String name) throws Exception;
	
	/**
	 * Search remote items
	 * 
	 * @param query
	 *            Search query to execute
	 * @return Search result
	 * @throws Exception
	 */
	public BoxSearchResult search(String query) throws Exception;
	
	/**
	 * Upload a new file (or a new version of a file) to the cloud
	 * 
	 * @param name
	 *            Name of the file to upload
	 * @param fileID
	 *            ID of the file to update, null if it is a new file
	 * @param content
	 *            File content
	 * @param folderID
	 *            ID of the remote folder where put the uploaded file
	 * @param sha1
	 *            Hash of the file content
	 * @return New remote file info
	 * @throws Exception
	 */
	public BoxFile upload(String name, String fileID, byte[] content, String folderID, String sha1) throws Exception;
	
	/**
	 * Delete a remote file
	 * 
	 * @param fileID
	 *            ID of the file to delete
	 * @throws Exception
	 */
	public void delete(String fileID) throws Exception;

}
