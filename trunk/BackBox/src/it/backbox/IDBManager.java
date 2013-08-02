package it.backbox;

import it.backbox.bean.Chunk;
import it.backbox.exception.BackBoxException;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface IDBManager {
	
	/**
	 * Open the jdbc connection
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * 
	 */
	public void openDB() throws ClassNotFoundException, SQLException;
	
	/**
	 * Close the jdbc connection
	 * 
	 * @throws SQLException
	 */
	public void closeDB() throws SQLException;
	
	/**
	 * Delete file informations from database
	 * 
	 * @param filename
	 *            Name of the file to delete
	 * @param digest
	 *            File hash to delete
	 * @throws BackBoxException
	 */
	public void delete(String filename, String digest) throws BackBoxException;
	
	/**
	 * Insert new file informations in database
	 * 
	 * @param file
	 *            New file
	 * @param relativePath
	 *            File relative path
	 * @param folder
	 *            Folder where the file is
	 * @param digest
	 *            New file hash
	 * @param chunks
	 *            List of new file chunks
	 * @param encrypted
	 * @param compressed
	 * @param splitted
	 * @throws BackBoxException
	 */
	public void insert(File file, String relativePath, String folder, String digest, List<Chunk> chunks, boolean encrypted, boolean compressed, boolean splitted) throws BackBoxException;
	
	/**
	 * Get the record of a file
	 * 
	 * @param key
	 *            Key of the record
	 * @return The file record
	 * @throws SQLException
	 */
	public it.backbox.bean.File getFileRecord(String key) throws SQLException;
	
	/**
	 * Load all the files information from database
	 * 
	 * @param folder
	 *            Files folder
	 * 
	 * @return Hashmap with <Hash, it.backbox.bean.File> with files informations
	 *         in database
	 * @throws SQLException
	 */
	public Map<String, Map<String, it.backbox.bean.File>> getFolderRecords(String folder) throws SQLException;
	
	/**
	 * Reset or create the database and open the connection
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public void createDB() throws SQLException, ClassNotFoundException;
	
	/**
	 * Get all files in the database
	 * 
	 * @return List of all files
	 * @throws SQLException
	 */
	public List<it.backbox.bean.File> getAllFiles() throws SQLException;

}
