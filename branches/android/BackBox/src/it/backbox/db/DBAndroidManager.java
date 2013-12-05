/**
 * 
 */
package it.backbox.db;

import it.backbox.IDBManager;
import it.backbox.bean.Chunk;
import it.backbox.exception.BackBoxException;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBAndroidManager implements IDBManager {
	private static final Logger _log = Logger.getLogger(DBAndroidManager.class.getCanonicalName());
	
	private boolean open;
	private String filename;
	private SQLiteDatabase db;
	
	/**
	 * Constructor
	 * 
	 * @param filename
	 *            Database file name
	 * @throws BackBoxException
	 *             If filename is null
	 */
	public DBAndroidManager(String filename) throws BackBoxException {
		if (filename == null)
			throw new BackBoxException("DB filename null");
		this.filename = filename;
		open = false;
	}

	/* (non-Javadoc)
	 * @see it.backbox.IDBManager#openDB()
	 */
	@Override
	public void openDB() {
		if (open)
			return;
		
		db = SQLiteDatabase.openDatabase(filename, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS|SQLiteDatabase.OPEN_READONLY);
		open = true;
	}

	/* (non-Javadoc)
	 * @see it.backbox.IDBManager#closeDB()
	 */
	@Override
	public void closeDB() throws SQLException {
		db = null;
		open = false;
	}

	/* (non-Javadoc)
	 * @see it.backbox.IDBManager#delete(java.lang.String, java.lang.String)
	 */
	@Override
	public void delete(String filename, String digest) throws BackBoxException {
		throw new BackBoxException("Not implemented");
	}

	/* (non-Javadoc)
	 * @see it.backbox.IDBManager#insert(java.io.File, java.lang.String, java.lang.String, java.lang.String, java.util.List, boolean, boolean, boolean)
	 */
	@Override
	public void insert(File file, String relativePath, String folder,
			String digest, List<Chunk> chunks, boolean encrypted,
			boolean compressed, boolean splitted) throws BackBoxException {
		throw new BackBoxException("Not implemented");
	}

	/* (non-Javadoc)
	 * @see it.backbox.IDBManager#getFileRecord(java.lang.String)
	 */
	@Override
	public it.backbox.bean.File getFileRecord(String key) throws SQLException {
		Cursor c = db.query("files", null, "hash like '" + key + "'", null, null, null, null);
		if (c.moveToFirst()) {
			it.backbox.bean.File file = new it.backbox.bean.File();
			file.setHash(c.getString(0));
			file.setFilename(c.getString(1).replaceAll("''", "'"));
			file.setFolder(c.getString(2));
			file.setTimestamp(new Date(c.getLong(3)));
			file.setSize(c.getLong(4));
			file.setEncrypted(c.getInt(5) != 0);
			file.setCompressed(c.getInt(6) != 0);
			file.setSplitted(c.getInt(7) != 0);
			
			return file;
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see it.backbox.IDBManager#getFolderRecords(java.lang.String, boolean)
	 */
	@Override
	public Map<String, Map<String, it.backbox.bean.File>> getFolderRecords(String folder, boolean loadChunks) throws SQLException {
		Map<String, Map<String, it.backbox.bean.File>> records = new HashMap<String, Map<String, it.backbox.bean.File>>();
		
		Cursor c = db.query("files", null, "folder like '" + folder + "'", null, null, null, null);
		if (c.moveToFirst()) {
			do {
				it.backbox.bean.File file = new it.backbox.bean.File();
				file.setHash(c.getString(0));
				file.setFilename(c.getString(1).replaceAll("''", "'"));
				file.setFolder(c.getString(2));
				file.setTimestamp(new Date(c.getLong(3)));
				file.setSize(c.getLong(4));
				file.setEncrypted(c.getInt(5) != 0);
				file.setCompressed(c.getInt(6) != 0);
				file.setSplitted(c.getInt(7) != 0);
				
				if (records.containsKey(file.getHash()))
					records.get(file.getHash()).put(file.getFilename(), file);
				else {
					Map<String, it.backbox.bean.File> files = new HashMap<String, it.backbox.bean.File>();
					files.put(file.getFilename(), file);
					records.put(file.getHash(), files);
				}

			} while (c.moveToNext());
		}
		return records;
	}

	/* (non-Javadoc)
	 * @see it.backbox.IDBManager#createDB()
	 */
	@Override
	public void createDB() throws SQLException, BackBoxException {
		throw new BackBoxException("Not implemented");
	}

	/* (non-Javadoc)
	 * @see it.backbox.IDBManager#getAllFiles()
	 */
	@Override
	public List<it.backbox.bean.File> getAllFiles() throws SQLException {
		List<it.backbox.bean.File> records = new ArrayList<it.backbox.bean.File>();
		
		Cursor c = db.query("files", null, null, null, null, null, null);
		if (c.moveToFirst()) {
			do {
				it.backbox.bean.File file = new it.backbox.bean.File();
				file.setHash(c.getString(0));
				file.setFilename(c.getString(1).replaceAll("''", "'"));
				file.setFolder(c.getString(2));
				file.setTimestamp(new Date(c.getLong(3)));
				file.setSize(c.getLong(4));
				file.setEncrypted(c.getInt(5) != 0);
				file.setCompressed(c.getInt(6) != 0);
				file.setSplitted(c.getInt(7) != 0);
				
				records.add(file);

			} while (c.moveToNext());
		}
		
		return records;
	}

	/* (non-Javadoc)
	 * @see it.backbox.IDBManager#isModified()
	 */
	@Override
	public boolean isModified() {
		return false;
	}

}
