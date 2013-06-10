package it.backbox;

import it.backbox.client.rest.bean.BoxFile;
import it.backbox.client.rest.bean.BoxFolder;
import it.backbox.client.rest.bean.BoxItemCollection;
import it.backbox.client.rest.bean.BoxSearchResult;
import it.backbox.exception.RestException;

import java.io.IOException;

public interface IRestClient {
	
	/**
	 * Check if the token to access to Box.com is valid
	 * 
	 * @return True if the token is valid, false otherwise
	 */
	public boolean isAccessTokenValid();
	
	/**
	 * Download a file with ID <i>fileID</i>
	 * 
	 * @param fileID
	 *            ID file to download
	 * @return File content
	 * @throws IOException, RestException
	 */
	public byte[] download(String fileID) throws IOException, RestException;
	
	/**
	 * Create a new remote folder
	 * 
	 * @param name
	 *            Folder name
	 * @return New folder info
	 * @throws IOException, RestException
	 */
	public BoxFolder mkdir(String name) throws IOException, RestException;
	
	/**
	 * Search remote items
	 * 
	 * @param query
	 *            Search query to execute
	 * @return Search result
	 * @throws IOException, RestException
	 */
	public BoxSearchResult search(String query) throws IOException, RestException;
	
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
	 * @throws IOException, RestException
	 */
	public BoxFile upload(String name, String fileID, byte[] content, String folderID, String sha1) throws IOException, RestException;
	
	/**
	 * Delete a remote file
	 * 
	 * @param fileID
	 *            ID of the file to delete
	 * @param isFolder
	 *            True if the item to delete is a folder, false otherwise
	 * @throws IOException, RestException
	 */
	public void delete(String fileID, boolean isFolder) throws IOException, RestException;
	
	/**
	 * Get all the items in a folder
	 * 
	 * @param folderID
	 *            ID of the folder
	 * @return Items in the folder
	 * @throws IOException
	 */
	public BoxItemCollection getFolderItems(String folderID) throws IOException, RestException;

}
