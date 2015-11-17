package it.backbox.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import it.backbox.IDBManager;
import it.backbox.bean.Chunk;
import it.backbox.exception.BackBoxException;

public class DBManager implements IDBManager {
	private static final Logger _log = Logger.getLogger(DBManager.class.getCanonicalName());

	private Connection connection;

	private boolean open;
	private String filename;
	private boolean modified;

	/**
	 * Constructor
	 * 
	 * @param filename
	 *            Database file name
	 * @throws BackBoxException
	 *             If filename is null
	 */
	public DBManager(String filename) throws BackBoxException {
		if (filename == null)
			throw new BackBoxException("DB filename null");
		this.filename = filename;
		open = false;
		modified = false;
	}

	@Override
	public void openDB() throws BackBoxException, SQLException {
		if (open)
			return;

		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:" + filename);
		} catch (ClassNotFoundException e) {
			throw new BackBoxException("Error opening DB", e);
		}
		open = true;
	}

	@Override
	public void closeDB() throws SQLException {
		if (!open)
			return;

		if (connection != null)
			connection.close();

		open = false;
		modified = false;
	}

	@Override
	public void createDB() throws BackBoxException, SQLException {
		closeDB();
		if (!open)
			openDB();

		Statement statement = null;
		try {
			statement = connection.createStatement();
	
			statement.executeUpdate("drop table if exists files");
			statement.executeUpdate("drop table if exists chunks");
			statement.executeUpdate("create table files (hash string, filename string, folder string, timestamp date, size INTEGER, encrypted INTEGER, compressed INTEGER, splitted INTEGER, primary key(hash, filename, folder))");
			statement.executeUpdate("create table chunks (filehash string, chunkname string, chunkhash string, boxid string not null, size INTEGER, foreign key(filehash) references files(hash))");
			statement.executeUpdate("create index chunks_filehash on chunks (filehash)");
			statement.executeUpdate("PRAGMA journal_mode = OFF");
			_log.info("DB created");
		} finally {
			if (statement != null)
				statement.close();
		}
		modified = true;
	}

	@Override
	public int update(String hash, String folder, String filename, String newFolder, String newFilename, long fileLastModified, long fileSize, short encrypted,
			short compressed, short splitted) throws SQLException {
		PreparedStatement statement = null;
		StringBuilder query = null;
		int r = 0;
		
		try {
			int c = 0;
			query = new StringBuilder("update files set ");
			if (StringUtils.isNotBlank(newFolder)) {
				query.append("folder = ? ");
				c++;
			}
			if (StringUtils.isNotBlank(newFilename)) {
				if (c > 0)
					query.append(',');
				c++;
				query.append("filename = ? ");
			}
			if (fileLastModified >= 0) {
				if (c > 0)
					query.append(',');
				c++;
				query.append("timestamp = ? ");
			}
			if (fileSize >= 0) {
				if (c > 0)
					query.append(',');
				c++;
				query.append("size = ? ");
			}
			if (encrypted >= 0) {
				if (c > 0)
					query.append(',');
				c++;
				query.append("encrypted = ? ");
			}
			if (compressed >= 0) {
				if (c > 0)
					query.append(',');
				c++;
				query.append("compressed = ? ");
			}
			if (splitted >= 0) {
				if (c > 0)
					query.append(',');
				c++;
				query.append("splitted = ? ");
			}
			
			if (c == 0) {
				if (_log.isLoggable(Level.WARNING))
					_log.warning("No conditions satisfied, nothing to update");
				return 0;
			}
			
			query.append(" where hash = ? and folder = ?  and filename = ?");
			
			statement = connection.prepareStatement(query.toString());
			int i = 0;
			if (StringUtils.isNotBlank(newFolder))
				statement.setString(++i, newFolder);
			if (StringUtils.isNotBlank(newFilename))
				statement.setString(++i, StringEscapeUtils.escapeSql(FilenameUtils.separatorsToWindows(newFilename)));
			if (fileLastModified >= 0)
				statement.setLong(++i, fileLastModified);
			if (fileSize >= 0)
				statement.setLong(++i, fileSize);
			if (encrypted >= 0)
				statement.setShort(++i, encrypted);
			if (compressed >= 0)
				statement.setShort(++i, compressed);
			if (splitted >= 0)
				statement.setShort(++i, splitted);
			statement.setString(++i, hash);
			statement.setString(++i, folder);
			statement.setString(++i, StringEscapeUtils.escapeSql(FilenameUtils.separatorsToWindows(filename)));
			
			r = statement.executeUpdate();

			if (_log.isLoggable(Level.FINE))
				_log.fine("Query executed: " + query.toString());
			
			modified = true;
		} finally {
			if (statement != null)
				statement.close();
		}
		return r;
	}
	
	@Override
	public int insert(File file, String filename, String folder, String digest, List<Chunk> chunks, short encrypted,
			short compressed, short splitted) throws SQLException {
		return insert(folder, filename, digest, file.lastModified(), file.length(), chunks, encrypted, compressed,
				splitted);
	}

	/**
	 * Insert new file informations in database
	 * 
	 * @param folder
	 *            Folder where the file is
	 * @param filename
	 *            File relative path
	 * @param hash
	 *            New file hash
	 * @param fileLastModified
	 *            New file last modified time
	 * @param fileSize
	 *            New file size
	 * @param chunks
	 *            List of new file chunks
	 * @param encrypted
	 * @param compressed
	 * @param splitted
	 * @return Row count added
	 * @throws SQLException 
	 */
	public int insert(String folder, String filename, String hash, long fileLastModified, long fileSize,
			List<Chunk> chunks, short encrypted, short compressed, short splitted) throws SQLException {
		PreparedStatement statement = null;
		String sql = "";
		int r = 0;
		
		try {
//			connection.setAutoCommit(false);
			
			sql = "insert into files(hash, filename, folder, timestamp, size, encrypted, compressed, splitted) values(?, ?, ?, ?, ?, ?, ?, ?)";
			statement = connection.prepareStatement(sql);
			statement.setString(1, hash);
			statement.setString(2, StringEscapeUtils.escapeSql(FilenameUtils.separatorsToWindows(filename)));
			statement.setString(3, folder);
			statement.setLong(4, fileLastModified);
			statement.setLong(5, fileSize);
			statement.setShort(6, encrypted);
			statement.setShort(7, compressed);
			statement.setShort(8, splitted);

			r = statement.executeUpdate();
			statement.close();

			sql = "select filehash from chunks where filehash = ?";
			statement = connection.prepareStatement(sql);
			statement.setString(1, hash);

			ResultSet rs = statement.executeQuery();

			if (!rs.next()) {
				statement.close();
				
				sql = "insert into chunks(filehash, chunkname, chunkhash, boxid, size) values(?, ?, ?, ?, ?)";
				statement = connection.prepareStatement(sql);
				
				for (int i = 0; i < chunks.size(); i++) {
					Chunk chunk = chunks.get(i);
					
					statement.setString(1, hash);
					statement.setString(2, chunk.getChunkname());
					statement.setString(3, chunk.getChunkhash());
					statement.setString(4, chunk.getBoxid());
					statement.setLong(5, chunk.getSize());
					
					statement.addBatch();
				}
				
				statement.executeBatch();
			}

//			connection.commit();
			modified = true;
		} catch (SQLException e ) {
			_log.severe("Insert transaction rollback");
//			connection.rollback();
			throw e;
		} finally {
			if (statement != null)
				statement.close();
//			connection.setAutoCommit(true);
		}
		return r;
	}

	@Override
	public int delete(String folder, String filename, String hash) throws BackBoxException, SQLException {
		PreparedStatement statement = null;
		int r = 0;
		int i = 0;
		String sql = "";
		
		filename = StringEscapeUtils.escapeSql(FilenameUtils.separatorsToWindows(filename));
		
		try {
			sql = "select count(hash) from files where hash = ? and filename = ? and folder = ?";
			statement = connection.prepareStatement(sql);
			statement.setString(1, hash);
			statement.setString(2, filename);
			statement.setString(3, folder);
			
			ResultSet rs = statement.executeQuery();
			if (rs.next())
		        i = rs.getInt(1);

			if (i < 1) {
				if (_log.isLoggable(Level.SEVERE))
					_log.severe("File " + filename + " with hash " + hash + " in folder " + folder + " not found");
				throw new BackBoxException(
						"File " + filename + " with hash " + hash + "in folder " + folder + " not found");
			}
			statement.close();
			
			// find the chunks of the file we want to delete (we don't check the
			// folder and the filename because we need to know how many files
			// with the same hash we have)
			sql = "select count(hash) from files where hash = ?";
			statement = connection.prepareStatement(sql);
			statement.setString(1, hash);
			
			rs = statement.executeQuery();
			if (rs.next())
		        i = rs.getInt(1);

			statement.close();
			
			// if this is the last file with that hash, we delete the chunks
			if (i <= 1) {
				sql = "delete from chunks where filehash = ?";
				statement = connection.prepareStatement(sql);
				statement.setString(1, hash);
				
				statement.executeUpdate();
				statement.close();
			}

			sql = "delete from files where hash = ? and filename = ? and folder = ?";
			statement = connection.prepareStatement(sql);
			statement.setString(1, hash);
			statement.setString(2, filename);
			statement.setString(3, folder);

			r = statement.executeUpdate();

			modified = true;

			if (_log.isLoggable(Level.INFO))
				_log.info(hash + "-> delete ok");
		} finally {
			if (statement != null)
				statement.close();
		}
		return r;
	}

	@Override
	public List<it.backbox.bean.File> getAllFiles() throws SQLException {
		// TODO Paginate
		List<it.backbox.bean.File> files = new ArrayList<>();

		Statement statement = null;
		try {
			statement = connection.createStatement();

			ResultSet rs = statement.executeQuery("select * from files");
			while (rs.next()) {
				it.backbox.bean.File file = buildFile(rs);
				file.setChunks(getFileChunks(file.getHash()));
				files.add(file);
			}
		} finally {
			if (statement != null)
				statement.close();
		}
		return files;
	}

	@Override
	public List<Chunk> getAllChunks() throws SQLException {
		List<Chunk> chunks = new ArrayList<>();
		
		Statement statement = null;
		try {
			statement = connection.createStatement();
	
			ResultSet rs = statement.executeQuery("select * from chunks");
			while (rs.next()) {
				Chunk chunk = buildChunk(rs);
				chunks.add(chunk);
			}
		} finally {
			if (statement != null)
				statement.close();
		}
		return chunks;
	}

	@Override
	public it.backbox.bean.File getFileRecord(String folder, String filename, String hash) throws SQLException {
		it.backbox.bean.File file;
		PreparedStatement statement = null;
		
		try {
			statement = connection.prepareStatement("select * from files where hash = ? and filename = ? and folder = ?");
			statement.setString(1, hash);
			statement.setString(2, StringEscapeUtils.escapeSql(FilenameUtils.separatorsToWindows(filename)));
			statement.setString(3, folder);
	
			ResultSet rs = statement.executeQuery();
			if (!rs.next())
				return null;
	
			file = buildFile(rs);
			file.setChunks(getFileChunks(file.getHash()));
		} finally {
			if (statement != null)
				statement.close();
		}

		return file;
	}
	
	@Override
	public List<it.backbox.bean.File> getFiles(String filehash) throws SQLException {
		List<it.backbox.bean.File> files = new ArrayList<>();
		PreparedStatement statement = null;
		
		try {
			statement = connection.prepareStatement("select * from files where hash = ?");
			statement.setString(1, filehash);
	
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				it.backbox.bean.File file = buildFile(rs);
				file.setChunks(getFileChunks(file.getHash()));
				files.add(file);
			}
		} finally {
			if (statement != null)
				statement.close();
		}
		return files;
	}

	@Override
	public List<it.backbox.bean.File> getFilesInFolder(String folder) throws SQLException {
		List<it.backbox.bean.File> files = new ArrayList<>();
		PreparedStatement statement = null;
		
		try {
			statement = connection.prepareStatement("select * from files where folder = ?");
			statement.setString(1, folder);
	
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				it.backbox.bean.File file = buildFile(rs);
				file.setChunks(getFileChunks(file.getHash()));
				files.add(file);
			}
		} finally {
			if (statement != null)
				statement.close();
		}

		return files;
	}

	@Override
	public List<it.backbox.bean.Chunk> getFileChunks(String filehash) throws SQLException {
		List<it.backbox.bean.Chunk> chunks = new ArrayList<>();
		PreparedStatement statement = null;
		
		try {
			statement = connection.prepareStatement("select * from chunks where filehash = ?");
			statement.setString(1, filehash);
	
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				Chunk c = buildChunk(rs);
				chunks.add(c);
			}
		} finally {
			if (statement != null)
				statement.close();
		}

		return chunks;
	}

	@Override
	public boolean isModified() {
		return modified;
	}

	/**
	 * Build a {@link Chunk} from a {@link ResultSet}
	 * 
	 * @throws SQLException
	 */
	private Chunk buildChunk(ResultSet rs) throws SQLException {
		Chunk c = new Chunk();
		c.setChunkname(rs.getString("chunkname"));
		c.setBoxid(rs.getString("boxid"));
		c.setChunkhash(rs.getString("chunkhash"));
		c.setSize(rs.getLong("size"));
		return c;
	}

	/**
	 * Build a {@link it.backbox.bean.File} from a {@link ResultSet}
	 * 
	 * @throws SQLException
	 */
	private it.backbox.bean.File buildFile(ResultSet rs) throws SQLException {
		it.backbox.bean.File file = new it.backbox.bean.File();
		file.setHash(rs.getString("hash"));
		file.setFilename(rs.getString("filename").replaceAll("''", "'"));
		file.setTimestamp(rs.getDate("timestamp"));
		file.setFolderAlias(rs.getString("folder"));
		file.setSize(rs.getLong("size"));
		file.setEncrypted(rs.getShort("encrypted"));
		file.setCompressed(rs.getShort("compressed"));
		file.setSplitted(rs.getShort("splitted"));
		return file;
	}
}