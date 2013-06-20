package it.backbox.utility;

import it.backbox.IBoxManager;
import it.backbox.ICompress;
import it.backbox.IDBManager;
import it.backbox.ISecurityManager;
import it.backbox.ISplitter;
import it.backbox.bean.Chunk;
import it.backbox.boxcom.BoxManager;
import it.backbox.client.rest.RestClient;
import it.backbox.client.rest.bean.ProxyConfiguration;
import it.backbox.compare.FileCompare;
import it.backbox.compress.Zipper;
import it.backbox.db.DBManager;
import it.backbox.exception.BackBoxException;
import it.backbox.security.SecurityManager;
import it.backbox.split.Splitter;
import it.backbox.transaction.TransactionManager;
import it.backbox.transaction.task.CopyTask;
import it.backbox.transaction.task.DeleteBoxTask;
import it.backbox.transaction.task.DeleteDBTask;
import it.backbox.transaction.task.DeleteTask;
import it.backbox.transaction.task.DownloadTask;
import it.backbox.transaction.task.InsertTask;
import it.backbox.transaction.task.Transaction;
import it.backbox.transaction.task.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

public class BackBoxHelper {
	private static Logger _log = Logger.getLogger(BackBoxHelper.class.getCanonicalName());
	
	private static final String CHARSET = "UTF-8";
	public static final String CONFIG_FILE = "config.xml";
	public static final String DB_FILE = "backbox.db";
	public static final String DB_FILE_TEMP = "backbox.db.temp";
	
	public static final String BACKUP_FOLDER = "backupFolder";
	public static final String RESTORE_FOLDER = "restoreFolder";
	public static final String SALT = "salt";
	public static final String PWD_DIGEST = "pwdDigest";
	public static final String DEFAULT_UPLOAD_SPEED = "defaultUploadSpeed";
	public static final String CHUNK_SIZE = "chunkSize";
	public static final String FOLDER_ID = "folderID";
	public static final String PROXY_ADDRESS = "proxy.address";
	public static final String PROXY_PORT = "proxy.port";
	public static final String PROXY_ENABLED = "proxy.enabled";
	
	private XMLConfiguration configuration;
	private ProxyConfiguration pc;
	
	protected ISecurityManager sm;
	protected IDBManager dbm;
	protected TransactionManager tm;
	protected IBoxManager bm;
	
	public TransactionManager getTransactionManager() {
		return tm;
	}
	
	/**
	 * Chech if the configuration file exists
	 * 
	 * @return true if it exists, false otherwise
	 */
	public boolean confExists() {
		return Files.exists(Paths.get(CONFIG_FILE)) && dbExists();
	}
	
	/**
	 * Check if the database file exists
	 * 
	 * @return true if the file exists, false otherwise
	 */
	public boolean dbExists() {
		if (Files.exists(Paths.get(DB_FILE_TEMP))) {
			_log.fine("Decrypted DB found");
			return true;
		}
		return Files.exists(Paths.get(DB_FILE));
	}
	
	/**
	 * Load the configuration, open and load the database
	 * 
	 * @param password
	 *            User password
	 * @throws Exception 
	 */
	public void login(String password) throws Exception {
		if (getConfiguration().isEmpty()) {
			_log.fine("Configuration not found.");
			return;
		}
		_log.fine("Configuration load OK");

		sm = new SecurityManager(password, getConfiguration().getString(PWD_DIGEST), getConfiguration().getString(SALT));
		_log.fine("SecurityManager init OK");

		bm = new BoxManager();
		bm.setRestClient(new RestClient(getProxyConfiguration()));
		String folderID = getConfiguration().getString(FOLDER_ID);
		if ((folderID == null) || folderID.isEmpty()) {
			folderID = bm.getBoxID(BoxManager.UPLOAD_FOLDER);
			getConfiguration().setProperty(FOLDER_ID, folderID);
			saveConfiguration();
		}
		if ((folderID != null) && !folderID.isEmpty()) {
			bm.setBackBoxFolderID(folderID);
			_log.fine("BoxManager init OK");
		} else
			_log.fine("BoxManager init OK, but folder ID null");
		
		//if something goes wrong you could have (only) the decrypted db file
		if (Files.exists(Paths.get(DB_FILE_TEMP))) {
			_log.warning("Something went wrong, decrypted DB found. Trying to open it...");
		} else {
			if (!Files.exists(Paths.get(DB_FILE)))
				throw new BackBoxException("DB not found");
			
			_log.fine("DB found");
			sm.decrypt(DB_FILE, DB_FILE_TEMP);
		}
		
		dbm = new DBManager(DB_FILE_TEMP);
		dbm.openDB();
		_log.fine("DBManager init OK");
		
		ICompress z = new Zipper();
		ISplitter s = new Splitter(getConfiguration().getInt(BackBoxHelper.CHUNK_SIZE));
		
		tm = new TransactionManager(dbm, bm, sm, s, z);
		_log.fine("TransactionManager init OK");
	}
	
	/**
	 * Create a new configuration, login to Box
	 * 
	 * @param password
	 *            User password
	 * @param chunksize
	 *            Chunk size limit of cloud provider
	 * @throws Exception
	 */
	public void register(String password, int chunksize) throws Exception {
		logout();
		
		if (Files.exists(Paths.get(DB_FILE)))
			Files.delete(Paths.get(DB_FILE));
		if (Files.exists(Paths.get(DB_FILE_TEMP)))
			Files.delete(Paths.get(DB_FILE_TEMP));
		
		sm = new SecurityManager(password);
		_log.fine("SecurityManager init OK");
		
		getConfiguration().setProperty(PWD_DIGEST, sm.getPwdDigest());
		getConfiguration().setProperty(SALT, Hex.encodeHexString(sm.getSalt()));
		
		dbm = new DBManager(DB_FILE_TEMP);
		_log.fine("DBManager init OK");
		dbm.createDB();
		_log.fine("DB created");
		
		bm = new BoxManager(new RestClient(getProxyConfiguration()));
		String folderID = bm.getBoxID(BoxManager.UPLOAD_FOLDER);
		if (folderID != null) {
			_log.warning("Box Upload folder exists");
			bm.deleteFolder(folderID);
		}
		folderID = bm.mkdir(BoxManager.UPLOAD_FOLDER);
		bm.setBackBoxFolderID(folderID);
		_log.fine("BoxManager init OK");
		
		ICompress z = new Zipper();
		ISplitter s = new Splitter(chunksize);
		
		tm = new TransactionManager(dbm, bm, sm, s, z);
		_log.fine("TransactionManager init OK");
		
		getConfiguration().setProperty(FOLDER_ID, folderID);
		getConfiguration().setProperty(CHUNK_SIZE, chunksize);
		
		saveConfiguration();
		_log.fine("Configuration saved");
		
	}
	
	/**
	 * Get the configuration
	 * 
	 * @return The configration
	 * @throws ConfigurationException 
	 */
	public XMLConfiguration getConfiguration() throws ConfigurationException {
		if (configuration == null) {
			configuration = new XMLConfiguration();
			if (confExists())
				configuration.load(CONFIG_FILE);
		}
		return configuration;
	}
	
	/**
	 * Save the configuration
	 * 
	 * @throws ConfigurationException
	 */
	public void saveConfiguration() throws ConfigurationException {
		getConfiguration().setEncoding(CHARSET);
		getConfiguration().save(CONFIG_FILE);
	}
	
	/**
	 * Upload the configuration on Box.com
	 * 
	 * @throws Exception
	 */
	public void uploadConf() throws Exception {
		if (!confExists())
			throw new BackBoxException("Configuration not found");
		
		logout();
		
		bm = new BoxManager(new RestClient(getProxyConfiguration()));
		bm.setBackBoxFolderID(bm.getBoxID(BoxManager.UPLOAD_FOLDER));
		
		bm.upload(DB_FILE);
		bm.upload(CONFIG_FILE);
	}
	
	/**
	 * Download the configuration from Box.com
	 * 
	 * @throws Exception
	 */
	public void downloadConf() throws Exception {
		logout();
		
		bm = new BoxManager(new RestClient(getProxyConfiguration()));
		bm.setBackBoxFolderID(bm.getBoxID(BoxManager.UPLOAD_FOLDER));
		
		String name = DB_FILE + ".new";
		String id = bm.getBoxID(DB_FILE);
		if (id == null)
			throw new BackBoxException("DB file not found");
		Files.write(Paths.get(name), bm.download(id));
		File f = new File(name);
		if (f.exists() && (f.length() > 0))
			Files.move(Paths.get(name), Paths.get(DB_FILE), StandardCopyOption.REPLACE_EXISTING);
		else
			throw new BackBoxException("DB file empty");
		
		byte[] conf = bm.download(bm.getBoxID("config.xml"));
		if (conf.length == 0)
			throw new BackBoxException("Configuration file empty");
		getConfiguration().load(new ByteArrayInputStream(conf));
		saveConfiguration();
	}
	
	/**
	 * Close the connection
	 * 
	 * @throws Exception
	 */
	public void logout() throws Exception {
		if (dbm != null)
			dbm.closeDB();
		if ((sm != null) && Files.exists(Paths.get(DB_FILE_TEMP))) {
			sm.encrypt(DB_FILE_TEMP, DB_FILE);
			Files.delete(Paths.get(DB_FILE_TEMP));
		}
	}
	
	/**
	 * Get a list of all records
	 * 
	 * @return The list of all records
	 * @throws SQLException
	 */
	public List<SimpleEntry<String, it.backbox.bean.File>> getRecords() throws SQLException {
		Map<String, Map<String, it.backbox.bean.File>> map = dbm.loadDB();
		List<SimpleEntry<String, it.backbox.bean.File>> ret = new ArrayList<>();
		for (Map<String, it.backbox.bean.File> m : map.values())
			for (it.backbox.bean.File f : m.values())
				ret.add(new SimpleEntry<String, it.backbox.bean.File>(f.getHash(),f));
		
		return ret;
	}
	
	/**
	 * Download a single file
	 * 
	 * @param key
	 *            File key
	 * @param downloadPath
	 *            Path where download
	 * @param startNow
	 *            true if start the transaction, false if just create it
	 * @return The created transaction
	 * @throws SQLException 
	 */
	public Transaction downloadFile(String key, String downloadPath, boolean startNow) throws SQLException {
		it.backbox.bean.File file = dbm.getFileRecord(key);
		
		Transaction t = new Transaction();
		t.setId(file.getHash());
		
		DownloadTask dt = new DownloadTask(downloadPath, file);
		dt.setWeight(file.getSize());
		dt.setCountWeight(false);
		dt.setDescription(file.getFilename());
		
		t.addTask(dt);
		
		if (startNow) {
			tm.runTransaction(t);
			tm.shutdown();
		} else
			tm.addTransaction(t);
		return t;
	}
	
	/**
	 * Restore all files
	 * 
	 * @param restoreFolder
	 *            Folder where restore
	 * @param startNow
	 *            true if start the transactions, false if just create them
	 * @return The created transactions
	 * @throws BackBoxException 
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public ArrayList<Transaction> restore(String restoreFolder, boolean startNow) throws BackBoxException, SQLException, IOException {
		if (restoreFolder == null)
			throw new BackBoxException("Restore path not specified");
		
		ArrayList<Transaction> tt = new ArrayList<>();
		
		List<String> ex = new ArrayList<>();
		ex.add(".deleted");
		
		FileCompare c = new FileCompare(dbm.loadDB(), restoreFolder, ex);
		c.load();
		
		Map<String, Map<String, it.backbox.bean.File>> toDownload = c.getRecordsNotInFiles();
		for (String hash : toDownload.keySet()) {
			Transaction t = new Transaction();
			t.setId(hash);
			boolean first = true;
			String fileToCopy = null;
			for (String path : toDownload.get(hash).keySet()) {
				if (c.getFiles().containsKey(hash) && !c.getFiles().get(hash).containsKey(path)) {
					CopyTask ct = new CopyTask(c.getFiles().get(hash).values().iterator().next().getCanonicalPath(), restoreFolder + "\\" + c.getRecords().get(hash).get(path).getFilename());
					ct.setDescription(path);
					t.addTask(ct);
				} else if (!c.getFiles().containsKey(hash)) {
					it.backbox.bean.File file = c.getRecords().get(hash).get(path);
					if (first) {
						DownloadTask dt = new DownloadTask(restoreFolder, file);
						dt.setWeight(file.getSize());
						dt.setCountWeight(false);
						dt.setDescription(file.getFilename());
						t.addTask(dt);
						fileToCopy = file.getFilename();
						first = false;
					} else {
						CopyTask ct = new CopyTask(restoreFolder + "\\" + fileToCopy, restoreFolder + "\\" + file.getFilename());
						ct.setDescription(path);
						t.addTask(ct);
					}
				}
			}
			if (!t.getTasks().isEmpty()) {
				tt.add(t);
				if (startNow)
					tm.runTransaction(t);
				else
					tm.addTransaction(t);
			}
		}
		
		File del = null;
		if (!c.getFilesNotInRecords().isEmpty()) {
			del = new File(restoreFolder + "\\.deleted");
			del.mkdirs();
		}
		
		Map<String, Map<String, File>> toDelete = c.getFilesNotInRecords();
		for (String hash : toDelete.keySet()) {
			Transaction t = new Transaction();
			t.setId(hash);
			for (String path : toDelete.get(hash).keySet()) {
				DeleteTask dt = new DeleteTask(restoreFolder, path, del.getCanonicalPath());
				dt.setDescription(path);
				t.addTask(dt);
			}
			if (!t.getTasks().isEmpty()) {
				tt.add(t);
				if (startNow)
					tm.runTransaction(t);
				else
					tm.addTransaction(t);
			}
		}
				
		if (startNow)
			tm.shutdown();
		
		return tt;
	}
	
	/**
	 * Backup all files
	 * 
	 * @param backupFolder
	 *            Foldet to backup
	 * @param startNow
	 *            true if start the transactions, false if just create them
	 * @return The created transactions
	 * @throws BackBoxException 
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public ArrayList<Transaction> backup(String backupFolder, boolean startNow) throws BackBoxException, SQLException, IOException {
		if (backupFolder == null)
			throw new BackBoxException("Backup path not specified");
		
		ArrayList<Transaction> tt = new ArrayList<>();
		
		List<String> ex = new ArrayList<>();
		ex.add(".deleted");
		
		FileCompare c = new FileCompare(dbm.loadDB(), backupFolder, ex);
		c.load();
		
		Map<String, Map<String, File>> toUpload = c.getFilesNotInRecords();
		for (String hash : toUpload.keySet()) {
			Transaction t = new Transaction();
			t.setId(hash);
			boolean first = true;
			for (String path : toUpload.get(hash).keySet()) {
				if ((c.getRecords().containsKey(hash) && !c.getRecords().get(hash).containsKey(path)) || 
						(!c.getRecords().containsKey(hash) && !first)) {
					InsertTask it =  new InsertTask(hash, c.getFiles().get(hash).get(path), path);
					it.setDescription(path);
					t.addTask(it);
				} else if (!c.getRecords().containsKey(hash) && first) {
					UploadTask ut = new UploadTask(hash, c.getFiles().get(hash).get(path), path);
					ut.setWeight(c.getFiles().get(hash).get(path).length());
					ut.setCountWeight(false);
					ut.setDescription(path);
					t.addTask(ut);
					first = false;
				}
			}
			if (!t.getTasks().isEmpty()) {
				tt.add(t);
				if (startNow)
					tm.runTransaction(t);
				else
					tm.addTransaction(t);
			}
		}
		
		Map<String, Map<String, it.backbox.bean.File>> toDelete = c.getRecordsNotInFiles();
		for (String hash : toDelete.keySet()) {
			Transaction t = new Transaction();
			t.setId(hash);
			boolean first = true;
			for (String path : toDelete.get(hash).keySet()) {
				if ((c.getFiles().containsKey(hash) && !c.getFiles().get(hash).containsKey(path)) ||
						(!c.getFiles().containsKey(hash) && !first)) {
					DeleteDBTask rt = new DeleteDBTask(c.getRecords().get(hash).get(path));
					rt.setDescription(path);
					t.addTask(rt);
				} else if (!c.getFiles().containsKey(hash) && first) {
					DeleteBoxTask dt = new DeleteBoxTask(c.getRecords().get(hash).get(path));
					dt.setDescription(path);
					t.addTask(dt);
					first = false;
				}
			}
			if (!t.getTasks().isEmpty()) {
				tt.add(t);
				if (startNow)
					tm.runTransaction(t);
				else
					tm.addTransaction(t);
			}
		}
		
		if (startNow)
			tm.shutdown();
		
		return tt;
	}
	
	/**
	 * Delete a file from backup
	 * 
	 * @param key
	 *            Key of the file to delete
	 * @param startNow
	 *            true if start the transaction, false if just create it
	 * @return The created transaction
	 * @throws SQLException 
	 */
	public Transaction delete(String key, boolean startNow) throws SQLException {
		it.backbox.bean.File file = dbm.getFileRecord(key);
		
		Transaction t = new Transaction();
		t.setId(file.getHash());
		
		DeleteBoxTask dt = new DeleteBoxTask(file);
		dt.setDescription(file.getFilename());
		
		t.addTask(dt);
		
		if (startNow) {
			tm.runTransaction(t);
			tm.shutdown();
		} else
			tm.addTransaction(t);
		return t;
	}
	
	/**
	 * Build the database from remote files
	 * 
	 * @param password
	 *            User password
	 * 
	 * @throws Exception
	 */
	public void buildDB(String password) throws Exception {
		if (getConfiguration().isEmpty())
			throw new BackBoxException("Configuration not found.");
		_log.fine("Configuration load OK");

		sm = new SecurityManager(password, getConfiguration().getString(PWD_DIGEST), getConfiguration().getString(SALT));
		_log.fine("SecurityManager init OK");

		dbm = new DBManager(DB_FILE);
		dbm.createDB();
		_log.fine("DBManager init OK");
		
		bm = new BoxManager(new RestClient(getProxyConfiguration()));
		String folderID = getConfiguration().getString(FOLDER_ID);
		if ((folderID == null) || folderID.isEmpty()) {
			folderID = bm.getBoxID(BoxManager.UPLOAD_FOLDER);
			if ((folderID == null) || folderID.isEmpty())
				throw new BackBoxException("Remote folder not found");
		}
		bm.setBackBoxFolderID(folderID);
		
		Map<String, List<Chunk>> remoteInfo = bm.getFolderChunks(folderID);
		List<String> ex = new ArrayList<>();
		ex.add(".deleted");
		
		if (!getConfiguration().containsKey(BACKUP_FOLDER))
			throw new BackBoxException("Backup folder not found in configuration");
		
		FileCompare c = new FileCompare(dbm.loadDB(), getConfiguration().getString(BACKUP_FOLDER), ex);
		c.load();
		
		Map<String, Map<String, File>> localInfo = c.getFiles();
		for (String hash : remoteInfo.keySet()) {
			_log.fine("Restoring " + hash);
			List<Chunk> chunks = remoteInfo.get(hash);
			if (!localInfo.containsKey(hash)) {
				bm.deleteChunk(chunks);
				_log.fine("Not found locally, deleted " + hash);
				break;
			}
			Map<String, File> fileInfo = localInfo.get(hash);
			for (String path : fileInfo.keySet()) {
				File file = fileInfo.get(path);
				_log.fine("Insert " + hash + " " + path + " " + chunks.size());
				dbm.insert(file, path, hash, chunks, true, true, chunks.size() > 1);
			}
		}
		
		logout();
	}
	
	/**
	 * Set and save the proxy configuration
	 * 
	 * @param pc
	 *            Proxy configuration
	 * @throws Exception
	 */
	public void setProxyConfiguration(ProxyConfiguration pc) throws Exception {
		this.pc = pc;
		if (pc != null) {
			getConfiguration().setProperty(PROXY_ENABLED, pc.isEnabled());
			getConfiguration().setProperty(PROXY_ADDRESS, pc.getAddress());
			getConfiguration().setProperty(PROXY_PORT, pc.getPort());
		} else
			getConfiguration().setProperty(PROXY_ENABLED, false);
		saveConfiguration();
		
		if (bm != null)
			bm.setRestClient(new RestClient(pc));
			
	}
	
	/**
	 * Get the proxy configuration
	 * 
	 * @return the proxy configuration
	 */
	public ProxyConfiguration getProxyConfiguration() {
		if (pc == null)
			try {
				pc = new ProxyConfiguration(getConfiguration().getBoolean(PROXY_ENABLED), getConfiguration().getString(PROXY_ADDRESS), getConfiguration().getInt(PROXY_PORT));
			} catch (ConfigurationException | NoSuchElementException e) {
				_log.log(Level.FINE, "No proxy configuration found", e);
				pc = new ProxyConfiguration(false, null, 0);
			}
		return pc;
	}

}
