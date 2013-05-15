package it.backbox;

import it.backbox.bean.Chunk;

import java.io.File;
import java.util.List;

public interface IDBManager {
	
	/**
	 * Delete file informations from database
	 * 
	 * @param filename
	 *            Name of the file to delete
	 * @param digest
	 *            File hash to delete
	 * @throws SQLException
	 */
	public void delete(String filename, String digest) throws Exception;
	
	/**
	 * Insert new file informations in database
	 * 
	 * @param file
	 *            New file
	 * @param relativePath
	 *            File relative path
	 * @param digest
	 *            New file hash
	 * @param chunks
	 *            List of new file chunks
	 * @param encrypted
	 * @param compressed
	 * @param splitted
	 * @throws SQLException
	 * @throws IOException
	 */
	public void insert(File file, String relativePath, String digest, List<Chunk> chunks, boolean encrypted, boolean compressed, boolean splitted) throws Exception;
	
	/**
	 * Get the record of a file
	 * 
	 * @param key
	 *            Key of the record
	 * @return The file record
	 * @throws SQLException
	 */
	public it.backbox.bean.File getFileRecord(String key) throws Exception;

}
