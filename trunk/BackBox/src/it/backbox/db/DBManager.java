package it.backbox.db;

import it.backbox.IDBManager;
import it.backbox.bean.Chunk;
import it.backbox.exception.BackBoxException;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringEscapeUtils;

public class DBManager implements IDBManager {
	private static Logger _log = Logger.getLogger(DBManager.class.getCanonicalName());

	private static final int QUERY_TIMEOUT = 30;

	private Connection connection;

	private boolean open;
	private static String filename;
	
	/**
	 * Constructor
	 * 
	 * @param sm
	 *            SecurityManager to database encrypt/decrypt operations
	 * @param filename
	 *            Database file name
	 * @throws BackBoxException
	 *             If filename is null
	 */
	public DBManager(String filename) throws BackBoxException {
		if (filename == null)
			throw new BackBoxException("DB filename null");
		DBManager.filename = filename;
		open = false;
	}
	
	/**
	 * Decrypt the database and open the jdbc connection
	 * 
	 * @throws Exception
	 */
	public void openDB() throws Exception {
		if (open)
			return;

		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection("jdbc:sqlite:" + filename);
		open = true;
	}

	/**
	 * Close the jdbc connection and encrypt the database
	 */
	public void closeDB() {
		if (!open)
			return;
		
		try {
			if (connection != null)
				connection.close();
		} catch (SQLException e) {
			_log.log(Level.WARNING, "Error closing db connection", e);
		}

		open = false;
	}

	/**
	 * Reset or create the database and open the connection
	 * 
	 * @throws SQLException
	 */
	public void createDB() throws Exception {
		closeDB();
		if(!open) {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:" + filename);
			open = true;
		}
		
		Statement statement = connection.createStatement();
		statement.setQueryTimeout(QUERY_TIMEOUT);

		statement.executeUpdate("drop table if exists files");
		statement.executeUpdate("drop table if exists chunks");
		statement.executeUpdate("create table files (hash string, filename string, timestamp date, size INTEGER, encrypted INTEGER, compressed INTEGER, splitted INTEGER, primary key(hash, filename))");
		statement.executeUpdate("create table chunks (filehash string, chunkname string, chunkhash string, boxid string, size INTEGER, foreign key(filehash) references files(hash))");
		
		statement.executeUpdate("PRAGMA journal_mode = OFF");
		_log.fine("DB created");
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.IDBManager#insert(java.io.File, java.lang.String, java.lang.String, java.util.List, boolean, boolean, boolean)
	 */
	public void insert(File file, String relativePath, String digest, List<Chunk> chunks, boolean encrypted, boolean compressed, boolean splitted) throws BackBoxException {
		StringBuilder query = null;
		try {
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(QUERY_TIMEOUT);

			query = new StringBuilder("insert into files values('");
			query.append(digest).append("','");
			query.append(StringEscapeUtils.escapeSql(relativePath)).append("','");
			query.append(file.lastModified()).append("',");
			query.append(file.length()).append(",");
			query.append(encrypted ? 1 : 0).append(",");
			query.append(compressed ? 1 : 0).append(",");
			query.append(splitted ? 1 : 0).append(")");

			statement.executeUpdate(query.toString());

			query = new StringBuilder("select filehash from chunks where filehash = '");
			query.append(digest);
			query.append("'");

			ResultSet rs = statement.executeQuery(query.toString());

			if (!rs.next()) {
				query = new StringBuilder("insert into chunks ");
				for (int i = 0; i < chunks.size(); i++) {
					query.append("select '");
					query.append(digest).append("','");
					query.append(chunks.get(i).getChunkname()).append("','");
					query.append(chunks.get(i).getChunkhash()).append("','");
					query.append(chunks.get(i).getBoxid()).append("','");
					query.append(chunks.get(i).getSize()).append("'");
					if (i < (chunks.size() - 1))
						query.append(" union ");
				}

				statement.executeUpdate(query.toString());
			}
			
			if (_log.isLoggable(Level.FINE)) _log.fine(digest + "-> insert ok");

		} catch (SQLException e) {
			throw new BackBoxException(e, (query != null) ? query.toString() : "");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IDBManager#delete(java.lang.String, java.lang.String)
	 */
	public void delete(String filename, String digest) throws BackBoxException {
		StringBuilder query = null;
		try {
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(QUERY_TIMEOUT);
	
			query = new StringBuilder("select hash from files where hash = '");
			query.append(digest).append("'");
			ResultSet rs = statement.executeQuery(query.toString());
	
			int i = 0;
			while (rs.next())
				i++;
	
			if (i <= 1) {
				query = new StringBuilder("delete from chunks where filehash = '");
				query.append(digest).append("'");
	
				statement.executeUpdate(query.toString());
			}
	
			query = new StringBuilder("delete from files where hash = '");
			query.append(digest).append("' and filename = '").append(filename).append("'");
	
			statement.executeUpdate(query.toString());
			
			if (_log.isLoggable(Level.FINE)) _log.fine(digest + "-> delete ok");
		} catch (SQLException e) {
			throw new BackBoxException(e, (query != null) ? query.toString() : "");
		}
	}
	
	/**
	 * Load all the files information from database
	 * 
	 * @return Hashmap with <Hash, it.backbox.bean.File> with files informations
	 *         in database
	 * @throws SQLException
	 */
	public Map<String, Map<String, it.backbox.bean.File>> loadDB() throws SQLException {
		Statement statement = connection.createStatement();
		statement.setQueryTimeout(QUERY_TIMEOUT);

		ResultSet rs = statement.executeQuery("select * from files");

		Map<String, Map<String, it.backbox.bean.File>> records = new HashMap<>();

		while (rs.next()) {
			it.backbox.bean.File file = new it.backbox.bean.File();
			file.setHash(rs.getString("hash"));
			file.setFilename(rs.getString("filename").replaceAll("''", "'"));
			file.setTimestamp(rs.getDate("timestamp"));
			file.setSize(rs.getLong("size"));
			file.setEncrypted(rs.getBoolean("encrypted"));
			file.setCompressed(rs.getBoolean("compressed"));
			file.setSplitted(rs.getBoolean("splitted"));

			StringBuilder query = new StringBuilder("select * from chunks where filehash like '");
			query.append(rs.getString("hash"));
			query.append("'");
			//query.append("order by chunkname");

			Statement statement2 = connection.createStatement();
			statement2.setQueryTimeout(QUERY_TIMEOUT);
			ResultSet rs2 = statement2.executeQuery(query.toString());

			while (rs2.next()) {
				Chunk c = new Chunk();
				c.setChunkname(rs2.getString("chunkname"));
				c.setBoxid(rs2.getString("boxid"));
				c.setChunkhash(rs2.getString("chunkhash"));
				c.setSize(rs2.getLong("size"));
				file.getChunks().add(c);
			}

			if (records.containsKey(file.getHash()))
				records.get(file.getHash()).put(file.getFilename(), file);
			else {
				Map<String, it.backbox.bean.File> files = new HashMap<>();
				files.put(file.getFilename(), file);
				records.put(file.getHash(), files);
			}
		}

		return records;
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IDBManager#getFileRecord(java.lang.String)
	 */
	public it.backbox.bean.File getFileRecord(String key) throws SQLException {
		Statement statement = connection.createStatement();
		statement.setQueryTimeout(QUERY_TIMEOUT);

		StringBuilder query = new StringBuilder("select * from files where hash like '");
		query.append(key).append("'");
		ResultSet rs = statement.executeQuery(query.toString());

		if (!rs.next()) 
			return null;

		it.backbox.bean.File file = new it.backbox.bean.File();
		file.setHash(rs.getString("hash"));
		file.setFilename(rs.getString("filename").replaceAll("''", "'"));
		file.setTimestamp(rs.getDate("timestamp"));
		file.setSize(rs.getLong("size"));
		file.setEncrypted(rs.getBoolean("encrypted"));
		file.setCompressed(rs.getBoolean("compressed"));
		file.setSplitted(rs.getBoolean("splitted"));

		query = new StringBuilder("select * from chunks where filehash like '");
		query.append(key);
		query.append("'");

		rs = statement.executeQuery(query.toString());

		while (rs.next()) { 
			Chunk c = new Chunk();
			c.setChunkname(rs.getString("chunkname"));
			c.setBoxid(rs.getString("boxid"));
			c.setChunkhash(rs.getString("chunkhash"));
			c.setSize(rs.getLong("size"));
			file.getChunks().add(c);
		}

		return file;
	}
}