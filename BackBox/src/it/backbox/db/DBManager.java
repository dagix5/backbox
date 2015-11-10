package it.backbox.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	private static final int QUERY_TIMEOUT = 30;

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
	public void openDB() throws BackBoxException {
		if (open)
			return;

		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:" + filename);
		} catch (ClassNotFoundException | SQLException e) {
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

		Statement statement = connection.createStatement();
		statement.setQueryTimeout(QUERY_TIMEOUT);

		statement.executeUpdate("drop table if exists files");
		statement.executeUpdate("drop table if exists chunks");
		statement.executeUpdate("create table files (hash string, filename string, folder string, timestamp date, size INTEGER, encrypted INTEGER, compressed INTEGER, splitted INTEGER, primary key(hash, filename, folder))");
		statement.executeUpdate("create table chunks (filehash string, chunkname string, chunkhash string, boxid string not null, size INTEGER, foreign key(filehash) references files(hash))");
		statement.executeUpdate("create index chunks_filehash on chunks (filehash)");
		statement.executeUpdate("PRAGMA journal_mode = OFF");
		_log.info("DB created");

		modified = true;
	}

	@Override
	public int update(String hash, String folder, String filename, String newFolder, String newFilename, long fileLastModified, long fileSize, short encrypted,
			short compressed, short splitted) throws BackBoxException {
		StringBuilder query = null;
		int r = 0;
		try {
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(QUERY_TIMEOUT);
		
			int c = 0;
			query = new StringBuilder("update files set ");
			if (StringUtils.isNotBlank(newFolder)) {
				query.append("folder='").append(newFolder).append('\'');
				c++;
			}
			if (StringUtils.isNotBlank(newFilename)) {
				if (c > 0)
					query.append(',');
				c++;
				query.append("filename='").append(StringEscapeUtils.escapeSql(FilenameUtils.separatorsToWindows(newFilename))).append('\'');
			}
			if (fileLastModified >= 0) {
				if (c > 0)
					query.append(',');
				c++;
				query.append("timestamp=").append(fileLastModified);
			}
			if (fileSize >= 0) {
				if (c > 0)
					query.append(',');
				c++;
				query.append("size=").append(fileSize);
			}
			if (encrypted >= 0) {
				if (c > 0)
					query.append(',');
				c++;
				query.append("encrypted=").append(encrypted);
			}
			if (compressed >= 0) {
				if (c > 0)
					query.append(',');
				c++;
				query.append("compressed=").append(compressed);
			}
			if (splitted >= 0) {
				if (c > 0)
					query.append(',');
				c++;
				query.append("splitted=").append(splitted);
			}
			
			if (c == 0) {
				if (_log.isLoggable(Level.WARNING))
					_log.warning("No conditions satisfied, nothing to update");
				return 0;
			}
			
			query.append(" where hash='").append(hash).append("' ");
			query.append("and folder='").append(folder).append("' ");
			query.append("and filename='").append(StringEscapeUtils.escapeSql(FilenameUtils.separatorsToWindows(filename))).append("' ");
			
			r = statement.executeUpdate(query.toString());

			if (_log.isLoggable(Level.FINE))
				_log.fine("Query executed: " + query.toString());
			
			modified = true;
		} catch (SQLException e) {
			throw new BackBoxException(e, (query != null) ? query.toString() : "");
		}
		return r;
	}
	
	@Override
	public int insert(File file, String filename, String folder, String digest, List<Chunk> chunks, short encrypted,
			short compressed, short splitted) throws BackBoxException {
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
	 * @throws BackBoxException
	 */
	public int insert(String folder, String filename, String hash, long fileLastModified, long fileSize,
			List<Chunk> chunks, short encrypted, short compressed, short splitted) throws BackBoxException {
		StringBuilder query = null;
		int r = 0;
		try {
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(QUERY_TIMEOUT);

			query = new StringBuilder("insert into files values('");
			query.append(hash).append("','");
			query.append(StringEscapeUtils.escapeSql(FilenameUtils.separatorsToWindows(filename))).append("','");
			query.append(folder).append("','");
			query.append(fileLastModified).append("',");
			query.append(fileSize).append(',');
			query.append(encrypted).append(',');
			query.append(compressed).append(',');
			query.append(splitted).append(')');

			r = statement.executeUpdate(query.toString());

			if (_log.isLoggable(Level.FINE))
				_log.fine("Query executed: " + query.toString());

			query = new StringBuilder("select filehash from chunks where filehash='");
			query.append(hash);
			query.append('\'');

			ResultSet rs = statement.executeQuery(query.toString());

			if (_log.isLoggable(Level.FINE))
				_log.fine("Query executed: " + query.toString());

			if (!rs.next()) {
				query = new StringBuilder("insert into chunks ");
				for (int i = 0; i < chunks.size(); i++) {
					Chunk chunk = chunks.get(i);
					query.append("select '");
					query.append(hash).append("','");
					query.append(chunk.getChunkname()).append("','");
					query.append(chunk.getChunkhash()).append("','");
					query.append(chunk.getBoxid()).append("',");
					query.append(chunk.getSize());
					if (i < (chunks.size() - 1))
						query.append(" union ");
				}

				statement.executeUpdate(query.toString());

				if (_log.isLoggable(Level.FINE))
					_log.fine("Query executed: " + query.toString());
			}

			modified = true;
		} catch (SQLException e) {
			throw new BackBoxException(e, (query != null) ? query.toString() : "");
		}
		return r;
	}

	@Override
	public int delete(String folder, String filename, String hash) throws BackBoxException {
		StringBuilder query = null;
		int r = 0;
		try {
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(QUERY_TIMEOUT);

			filename = StringEscapeUtils.escapeSql(FilenameUtils.separatorsToWindows(filename));

			query = new StringBuilder("select * from files where hash='");
			query.append(hash).append("' and filename='");
			query.append(filename);
			query.append("' and folder='").append(folder).append('\'');
			ResultSet rs = statement.executeQuery(query.toString());

			if (!rs.next()) {
				if (_log.isLoggable(Level.SEVERE))
					_log.severe("File " + filename + " with hash " + hash + " in folder " + folder + " not found");
				throw new BackBoxException(
						"File " + filename + " with hash " + hash + "in folder " + folder + " not found");
			}

			// find the chunks of the file we want to delete (we don't check the
			// folder and the filename because we need to know how many files
			// with the same hash we have)
			query = new StringBuilder("select hash from files where hash='");
			query.append(hash).append('\'');
			rs = statement.executeQuery(query.toString());

			int i = 0;
			while (rs.next())
				i++;

			// if this is the last file with that hash, we delete the chunks
			if (i <= 1) {
				query = new StringBuilder("delete from chunks where filehash='");
				query.append(hash).append('\'');

				statement.executeUpdate(query.toString());
			}

			query = new StringBuilder("delete from files where hash='");
			query.append(hash).append("' and filename='").append(filename);
			query.append("' and folder='").append(folder).append('\'');

			r = statement.executeUpdate(query.toString());

			modified = true;

			if (_log.isLoggable(Level.INFO))
				_log.info(hash + "-> delete ok");
		} catch (SQLException e) {
			throw new BackBoxException(e, (query != null) ? query.toString() : "");
		}
		return r;
	}

	@Override
	public Map<String, Map<String, it.backbox.bean.File>> getFolderRecords(String folder, boolean loadChunks)
			throws SQLException {
		Statement statement = connection.createStatement();
		statement.setQueryTimeout(QUERY_TIMEOUT);

		ResultSet rs = statement.executeQuery(
				new StringBuilder("select * from files where folder='").append(folder).append('\'').toString());

		Map<String, Map<String, it.backbox.bean.File>> records = new HashMap<>();

		while (rs.next()) {
			it.backbox.bean.File file = buildFile(rs);

			if (loadChunks)
				file.setChunks(getFileChunks(file.getHash()));

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

	@Override
	public List<it.backbox.bean.File> getAllFiles() throws SQLException {
		Statement statement = connection.createStatement();
		statement.setQueryTimeout(QUERY_TIMEOUT);

		ResultSet rs = statement.executeQuery("select * from files");

		List<it.backbox.bean.File> files = new ArrayList<>();

		while (rs.next()) {
			it.backbox.bean.File file = buildFile(rs);
			file.setChunks(getFileChunks(file.getHash()));
			files.add(file);
		}

		return files;
	}

	@Override
	public List<Chunk> getAllChunks() throws SQLException {
		Statement statement = connection.createStatement();
		statement.setQueryTimeout(QUERY_TIMEOUT);

		ResultSet rs = statement.executeQuery("select * from chunks");

		List<Chunk> chunks = new ArrayList<>();

		while (rs.next()) {
			Chunk chunk = buildChunk(rs);
			chunks.add(chunk);
		}
		return chunks;
	}

	@Override
	public it.backbox.bean.File getFileRecord(String folder, String filename, String hash) throws SQLException {
		Statement statement = connection.createStatement();
		statement.setQueryTimeout(QUERY_TIMEOUT);

		StringBuilder query = new StringBuilder("select * from files where hash='");
		query.append(hash).append("' and filename='");
		query.append(StringEscapeUtils.escapeSql(FilenameUtils.separatorsToWindows(filename)));
		query.append("' and folder='").append(folder).append('\'');
		ResultSet rs = statement.executeQuery(query.toString());

		if (!rs.next())
			return null;

		it.backbox.bean.File file = buildFile(rs);
		file.setChunks(getFileChunks(file.getHash()));

		return file;
	}

	@Override
	public List<it.backbox.bean.File> getFiles(String filehash) throws SQLException {
		Statement statement = connection.createStatement();
		statement.setQueryTimeout(QUERY_TIMEOUT);

		ResultSet rs = statement.executeQuery("select * from files where hash='" + filehash + "'");

		List<it.backbox.bean.File> files = new ArrayList<>();

		while (rs.next()) {
			it.backbox.bean.File file = buildFile(rs);
			file.setChunks(getFileChunks(file.getHash()));
			files.add(file);
		}

		return files;
	}

	@Override
	public List<it.backbox.bean.File> getFilesInFolder(String folder) throws SQLException {
		Statement statement = connection.createStatement();
		statement.setQueryTimeout(QUERY_TIMEOUT);

		ResultSet rs = statement.executeQuery("select * from files where folder='" + folder + "'");

		List<it.backbox.bean.File> files = new ArrayList<>();

		while (rs.next()) {
			it.backbox.bean.File file = buildFile(rs);
			file.setChunks(getFileChunks(file.getHash()));
			files.add(file);
		}

		return files;
	}

	@Override
	public List<it.backbox.bean.Chunk> getFileChunks(String filehash) throws SQLException {
		Statement statement = connection.createStatement();
		statement.setQueryTimeout(QUERY_TIMEOUT);

		StringBuilder query = new StringBuilder("select * from chunks where filehash='");
		query.append(filehash).append('\'');

		ResultSet rs = statement.executeQuery(query.toString());

		List<it.backbox.bean.Chunk> chunks = new ArrayList<>();
		while (rs.next()) {
			Chunk c = buildChunk(rs);
			chunks.add(c);
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