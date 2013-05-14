package it.backbox.utility;

import it.backbox.boxcom.BoxManager;
import it.backbox.compare.FileCompare;
import it.backbox.db.DBManager;
import it.backbox.exception.BackBoxException;
import it.backbox.security.SecurityManager;
import it.backbox.transaction.TransactionManager;
import it.backbox.transaction.task.CopyTask;
import it.backbox.transaction.task.DeleteBoxTask;
import it.backbox.transaction.task.DeleteDBTask;
import it.backbox.transaction.task.DeleteTask;
import it.backbox.transaction.task.DownloadTask;
import it.backbox.transaction.task.InsertTask;
import it.backbox.transaction.task.Transaction;
import it.backbox.transaction.task.UploadTask;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

public class BackBoxHelper {
	private static Logger _log = Logger.getLogger(BackBoxHelper.class.getCanonicalName());
	
	private static final String CHARSET = "UTF-8";
	public static final String BACKUP_FOLDER = "backupFolder";
	public static final String RESTORE_FOLDER = "restoreFolder";
	public static final String SALT = "salt";
	public static final String PWD_DIGEST = "pwdDigest";
	public static final String DEFAULT_UPLOAD_SPEED = "defaultUploadSpeed";
	public static final String CHUNK_SIZE = "chunkSize";
	public static final String FOLDER_ID = "folderID";
	
	private XMLConfiguration configuration;
	private String folderID;
	
	protected FileCompare c;
	protected SecurityManager sm;
	protected DBManager dbm;
	protected TransactionManager tm;
	protected BoxManager bm;

	/**
	 * Constructor
	 */
	public BackBoxHelper() {
		c = new FileCompare();
	}
	
	/**
	 * Chech if the configuration file exists
	 * 
	 * @param configFilename
	 *            The configuration file name
	 * @return true if it exists, false otherwise
	 */
	public boolean confExists(String configFilename) {
		return Files.exists(Paths.get(configFilename)) && DBManager.exists();
	}
	
	/**
	 * Load the configuration, open and load the database
	 * 
	 * @param configFilename
	 *            The configuration file name
	 * @param password
	 *            User password
	 * @return true if everything is ok, false otherwise
	 */
	public boolean login(String configFilename, String password) {
		try {
			loadConfiguration(configFilename);
			if (getConfiguration().isEmpty()) {
				_log.fine("Configuration not found.");
				return false;
			}
			_log.fine("Configuration load OK");

			sm = SecurityManager.createInstance(password, configuration.getString(PWD_DIGEST), configuration.getString(SALT));
			_log.fine("SecurityManager init OK");

			dbm = DBManager.createInstance(sm);
			_log.fine("DBManager init OK");
			
			if (DBManager.exists()) {
				_log.fine("DB found");
				dbm.openDB();
			} else {
				_log.fine("DB not found. Creating..");
				dbm.createDB();
			}
			
			tm = TransactionManager.getInstance();
			_log.fine("TransactionManager init OK");
			
			bm = BoxManager.createInstance();
			String folderID = configuration.getString(FOLDER_ID);
			if ((folderID == null) || folderID.isEmpty())
				folderID = bm.getBoxID(BoxManager.UPLOAD_FOLDER);
			if ((folderID != null) && !folderID.isEmpty()) {
				bm.setBackBoxFolderID(folderID);
				_log.fine("BoxManager init OK");
				getConfiguration().setProperty(FOLDER_ID, folderID);
				saveConfiguration(configFilename);
			} else
				_log.fine("BoxManager init OK, but folder ID null");
			
			c.setRecords(dbm.loadDB());
			_log.fine("DB load OK");
			
			return true;
		} catch (Exception e) {
			_log.log(Level.SEVERE, "Errore login", e);
		}
		return false;
	}
	
	/**
	 * Create a new configuration, login to Box
	 * 
	 * @param configFilename
	 *            Configuration file name
	 * @param password
	 *            User passoword
	 * @param callback
	 *            Callback method to call after login to Box.xom
	 * @throws Exception
	 */
	public void register(String configFilename, String password) throws Exception {
		DBManager.delete();
		
		sm = SecurityManager.createInstance(password);
		_log.fine("SecurityManager init OK");
		
		getConfiguration().setProperty(PWD_DIGEST, sm.getPwdDigest());
		getConfiguration().setProperty(SALT, Hex.encodeHexString(sm.getSalt()));
		
		dbm = DBManager.createInstance(sm);
		_log.fine("DBManager init OK");
		dbm.createDB();
		_log.fine("DB created");
		
		tm = TransactionManager.getInstance();
		_log.fine("TransactionManager init OK");
		
		c.setRecords(dbm.loadDB());
		_log.fine("DB load OK");
		
		bm = BoxManager.createInstance();
		_log.fine("BoxManager init OK");

		folderID = bm.getBoxID(BoxManager.UPLOAD_FOLDER);
		if (folderID == null)
			folderID = bm.mkdir(BoxManager.UPLOAD_FOLDER);
		else
			_log.warning("Box Upload folder exists");
		bm.setBackBoxFolderID(folderID);
		
		getConfiguration().setProperty(FOLDER_ID, folderID);
		
		saveConfiguration(configFilename);
		_log.fine("Configuration saved");
		
	}
	
	/**
	 * Get the configuration
	 * 
	 * @return The configration
	 */
	public XMLConfiguration getConfiguration() {
		if (configuration == null)
			configuration = new XMLConfiguration();
		return configuration;
	}
	
	/**
	 * Save the configuration
	 * 
	 * @param confFile
	 *            Configuration file name
	 * @throws ConfigurationException
	 */
	public void saveConfiguration(String confFile) throws ConfigurationException {
		getConfiguration().setEncoding(CHARSET);
		getConfiguration().save(confFile);
	}
	
	/**
	 * Load the configuration
	 * 
	 * @param confFile
	 *            Configuration file name
	 * @throws ConfigurationException
	 */
	public void loadConfiguration(String confFile) throws ConfigurationException {
		getConfiguration().load(confFile);
	}
	
	/**
	 * Upload the database on Box.com
	 * 
	 * @throws Exception
	 */
	public void uploadDB() throws Exception {
		dbm.closeDB();
		bm.upload(DBManager.DB_NAME, bm.getBackBoxFolderID());
	}
	
	/**
	 * Download the database from Box.com
	 * 
	 * @throws Exception
	 */
	public void downloadDB() throws Exception {
		dbm.closeDB();
		String name = DBManager.DB_NAME + ".new";
		bm.download(bm.getBoxID(DBManager.DB_NAME), name);
		File f = new File(name);
		if (f.exists() && (f.length() > 0))
			Files.move(Paths.get(name), Paths.get(DBManager.DB_NAME), StandardCopyOption.REPLACE_EXISTING);
		else
			throw new BackBoxException("DB file empty");
	}
	
	/**
	 * Close the connection
	 * 
	 * @throws Exception
	 */
	public void logout() throws Exception {
		dbm.closeDB();
	}
	
	/**
	 * Get a list of all records
	 * 
	 * @return The list of all records
	 * @throws SQLException
	 */
	public List<SimpleEntry<String, it.backbox.bean.File>> getRecords() throws SQLException {
		c.setRecords(dbm.loadDB());
		Map<String, Map<String, it.backbox.bean.File>> map = c.getRecords();
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
	 * @throws Exception
	 */
	public Transaction downloadFile(String key, String downloadPath, boolean startNow) throws Exception {
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
	 * @throws Exception
	 */
	public ArrayList<Transaction> restore(String restoreFolder, boolean startNow) throws Exception {
		if (restoreFolder == null)
			throw new BackBoxException("Restore path not specified");
		
		ArrayList<Transaction> tt = new ArrayList<>();
		
		c.setRecords(dbm.loadDB());
		
		List<String> ex = new ArrayList<>();
		ex.add(".deleted");
		
		File root = new File(restoreFolder);
		c.listFiles(root, ex);
		
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
		if (!c.getFiles().isEmpty()) {
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
	 * @param chunkSize
	 *            Size of the chunks to upload
	 * @param startNow
	 *            true if start the transactions, false if just create them
	 * @return The created transactions
	 * @throws Exception
	 */
	public ArrayList<Transaction> backup(String backupFolder, int chunkSize, boolean startNow) throws Exception {
		if (backupFolder == null)
			throw new BackBoxException("Backup path not specified");
		
		ArrayList<Transaction> tt = new ArrayList<>();
		
		c.setRecords(dbm.loadDB());
		
		List<String> ex = new ArrayList<>();
		ex.add(".deleted");
		
		File root = new File(backupFolder);
		c.listFiles(root, ex);
		
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
					UploadTask ut = new UploadTask(hash, c.getFiles().get(hash).get(path), chunkSize, path);
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

}
