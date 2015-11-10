package it.backbox;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import it.backbox.bean.Chunk;
import it.backbox.exception.BackBoxException;

public interface IDBManager {

	/**
	 * Open the jdbc connection
	 * 
	 * @throws BackBoxException
	 * @throws SQLException 
	 */
	public void openDB() throws BackBoxException, SQLException;

	/**
	 * Close the jdbc connection
	 * 
	 * @throws SQLException
	 */
	public void closeDB() throws SQLException;

	/**
	 * Delete file informations from database
	 * 
	 * @param folder
	 *            Folder alias where the file is
	 * @param filename
	 *            Name of the file to delete
	 * @param hash
	 *            File hash to delete
	 * @return Row count deleted
	 * @throws BackBoxException
	 * @throws SQLException 
	 */
	public int delete(String folder, String filename, String hash) throws BackBoxException, SQLException;

	/**
	 * Insert new file information in database
	 * 
	 * @param file
	 *            New file
	 * @param filename
	 *            File relative path
	 * @param folder
	 *            Folder where the file is
	 * @param hash
	 *            New file hash
	 * @param chunks
	 *            List of new file chunks
	 * @param encrypted
	 * @param compressed
	 * @param splitted
	 * 
	 * @return Row count added
	 * @throws SQLException 
	 */
	public int insert(File file, String filename, String folder, String hash, List<Chunk> chunks, short encrypted,
			short compressed, short splitted) throws SQLException;

	/**
	 * Edit file information in database
	 * 
	 * @param hash
	 *            Hash of the file to edit
	 * @param folder
	 *            Folder of the file to edit
	 * @param filename
	 *            Filename of the file to edit
	 * @param newFolder
	 *            New folder name
	 * @param newFilename
	 *            New filename
	 * @param fileLastModified
	 *            New file last modified time
	 * @param fileSize
	 *            New file size
	 * @param encrypted
	 * @param compressed
	 * @param splitted
	 * @returnRow count added
	 * @throws BackBoxException
	 * @throws SQLException 
	 */
	public int update(String hash, String folder, String filename, String newFolder, String newFilename,
			long fileLastModified, long fileSize, short encrypted, short compressed, short splitted)
					throws BackBoxException, SQLException;

	/**
	 * Get the record of a file
	 * 
	 * @param folder
	 *            Folder alias where the file is
	 * @param filename
	 *            Name of the file
	 * @param hash
	 *            Hash of the file
	 * 
	 * @return The file record
	 * @throws SQLException
	 */
	public it.backbox.bean.File getFileRecord(String folder, String filename, String hash) throws SQLException;

	/**
	 * Get the chunks of the file with the hash in input
	 * 
	 * @param filehash
	 *            File hash
	 * @return File chunks
	 * @throws SQLException
	 */
	public List<it.backbox.bean.Chunk> getFileChunks(String filehash) throws SQLException;

	/**
	 * Get all the files with the hash in input
	 * 
	 * @param filehash
	 *            File hash
	 * @return File list
	 * @throws SQLException
	 */
	public List<it.backbox.bean.File> getFiles(String filehash) throws SQLException;

	/**
	 * Get all the files in a folder
	 * 
	 * @param folder
	 *            Folder to list
	 * @return File list
	 * @throws SQLException
	 */
	public List<it.backbox.bean.File> getFilesInFolder(String folder) throws SQLException;

	/**
	 * Load all the files information from database
	 * 
	 * @param folder
	 *            Folder to load
	 * @param loadChunks
	 *            True if chunks are loaded, false otherwise
	 * 
	 * @return Map with < Hash, < Filename, it.backbox.bean.File >> with files
	 *         informations in database
	 * @throws SQLException
	 */
	public Map<String, Map<String, it.backbox.bean.File>> getFolderRecords(String folder, boolean loadChunks)
			throws SQLException;

	/**
	 * Reset or create the database and open the connection
	 * 
	 * @throws BackBoxException
	 * @throws SQLException
	 */
	public void createDB() throws BackBoxException, SQLException;

	/**
	 * Get all files in the database
	 * 
	 * @return List of all files
	 * @throws SQLException
	 */
	public List<it.backbox.bean.File> getAllFiles() throws SQLException;

	/**
	 * Get all chunks in the database
	 * 
	 * @return List of all chunks
	 * @throws SQLException
	 */
	public List<it.backbox.bean.Chunk> getAllChunks() throws SQLException;

	/**
	 * Check if the db has been modified
	 * 
	 * @return True if the db has been modified, false otherwise
	 */
	public boolean isModified();

}
