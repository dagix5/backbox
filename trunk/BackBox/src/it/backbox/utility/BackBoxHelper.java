package it.backbox.utility;

import it.backbox.IBoxManager;
import it.backbox.ICompress;
import it.backbox.IDBManager;
import it.backbox.ISecurityManager;
import it.backbox.ISplitter;
import it.backbox.bean.Chunk;
import it.backbox.bean.Configuration;
import it.backbox.bean.Folder;
import it.backbox.bean.ProxyConfiguration;
import it.backbox.boxcom.BoxManager;
import it.backbox.client.rest.RestClient;
import it.backbox.compare.FileCompare;
import it.backbox.compress.Zipper;
import it.backbox.db.DBManager;
import it.backbox.exception.BackBoxException;
import it.backbox.exception.RestException;
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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Hex;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;

public class BackBoxHelper {
	private static final Logger _log = Logger.getLogger(BackBoxHelper.class.getCanonicalName());
	
	public static final String DB_FILE = "backbox.db";
	public static final String DB_FILE_TEMP = "backbox.db.temp";
	
	public static final String CONFIG_FILE = "config.json";
	
	private static final int DEFAULT_LOG_SIZE = 2097152;
	
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();
	private Configuration configuration;
	
	private static final Set<String> ex = new HashSet<>();
	
	protected ISecurityManager sm;
	protected IDBManager dbm;
	protected TransactionManager tm;
	protected IBoxManager bm;
	
	/**
	 * Get the transaction manager
	 * 
	 * @return The transaction manager
	 */
	public TransactionManager getTransactionManager() {
		return tm;
	}
	
	//-----------START CONFIGURATION HELPER---------------
	
	/**
	 * Load (if needed) the configuration
	 * 
	 * @return Configuration bean
	 * @throws IOException
	 */
	public Configuration getConfiguration() throws IOException {
		if (configuration == null) {
			try {
				if (Files.exists(Paths.get(CONFIG_FILE))) {
					JsonObjectParser parser = new JsonObjectParser(JSON_FACTORY);
					configuration = parser.parseAndClose(new FileReader(CONFIG_FILE), Configuration.class);
					return configuration;
				}
			} catch (FileNotFoundException e) {
				_log.log(Level.SEVERE, "Configuration file not found", e);
			}
			configuration = new Configuration();
			
			// setting default values
			configuration.setLogLevel(Level.OFF.getName());
			configuration.setLogSize(2097152);
		}
		return configuration;
	}
	
	/**
	 * Save the configuration to file
	 * 
	 * @throws IOException
	 */
	public void saveConfiguration() throws IOException {
		Files.write(Paths.get(CONFIG_FILE), JSON_FACTORY.toByteArray(configuration));
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
			if (_log.isLoggable(Level.INFO)) _log.info("Decrypted DB found");
			return true;
		}
		return Files.exists(Paths.get(DB_FILE));
	}
	
	/**
	 * Upload the configuration on Box.com
	 * 
	 * @param force
	 *            Force upload configuration, even if it is not modified
	 * @throws Exception
	 */
	public void uploadConf(boolean force) throws Exception {
		if (!confExists())
			throw new BackBoxException("Configuration not found");
		
		if ((dbm != null) && dbm.isModified())
			force = true;
		
		logout();
		
		if (bm == null)
			bm = new BoxManager(new RestClient(getConfiguration().getProxyConfiguration()));

		String rootFolderID = getConfiguration().getRootFolderID();
		if ((rootFolderID == null) || rootFolderID.isEmpty())
			rootFolderID = bm.getBoxID(BoxManager.ROOT_FOLDER_NAME);
		
		if (force) {
			String id = bm.upload(DB_FILE, getConfiguration().getDbFileID(), rootFolderID);
			getConfiguration().setDbFileID(id);
		}
		String id = bm.upload(CONFIG_FILE, getConfiguration().getConfFileID(), rootFolderID);
		getConfiguration().setConfFileID(id);

		saveConfiguration();
	}
	
	/**
	 * Download the configuration from Box.com
	 * 
	 * @throws Exception
	 */
	public void downloadConf() throws Exception {
		logout();
		
		if (bm == null)
			bm = new BoxManager(new RestClient(getConfiguration().getProxyConfiguration()));
		
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
		
		byte[] conf = bm.download(bm.getBoxID(CONFIG_FILE));
		if (conf.length == 0)
			throw new BackBoxException("Configuration file empty");
		
		JsonObjectParser parser = new JsonObjectParser(JSON_FACTORY);
		configuration = parser.parseAndClose(new ByteArrayInputStream(conf), null, Configuration.class);
		saveConfiguration();
	}
	
	/**
	 * Add a folder to backup
	 * 
	 * @param folder
	 *            Folder to add
	 * @throws RestException
	 * @throws IOException
	 * @throws BackBoxException
	 */
	public void addBackupFolder(Folder folder) throws IOException, RestException, BackBoxException {
		if (bm == null)
			throw new BackBoxException("BoxManager null");

		String fID = bm.mkdir(folder.getAlias(), getConfiguration().getRootFolderID());
		folder.setId(fID);
		
		getConfiguration().getBackupFolders().add(folder);
		saveConfiguration();
	}
	
	/**
	 * Remove a folder to backup
	 * 
	 * @param index
	 * 			  Configuration index of the folder to remove
	 * @param folderID
	 *            ID of the folder to remove
	 * @throws RestException 
	 * @throws IOException 
	 * @throws BackBoxException 
	 */
	private void removeBackupFolder(int index, String folderID) throws IOException, RestException, BackBoxException {
		if (bm == null)
			throw new BackBoxException("BoxManager null");

		bm.deleteFolder(folderID);
		
		getConfiguration().getBackupFolders().remove(index);
		saveConfiguration();
	}
	
	/**
	 * Update backup folders configuration
	 * 
	 * @param folders
	 *            New list of folders to backup
	 * @throws IOException 
	 * @throws BackBoxException 
	 * @throws RestException 
	 */
	public void updateBackupFolders(List<Folder> folders) throws IOException, RestException, BackBoxException {
		for (Folder f1 : folders) {
			boolean found = false;
			for (Folder f2 : getConfiguration().getBackupFolders()) {
				if (f1.getAlias().equals(f2.getAlias())) {
					found = true;
					break;
				}
			}
			
			if (!found)
				addBackupFolder(f1);
		}
		
		for (int i = 0; i < getConfiguration().getBackupFolders().size(); i++) {
			Folder f1 = getConfiguration().getBackupFolders().get(i);
			boolean found = false;
			for (Folder f2 : folders) {
				if (f1.getAlias().equals(f2.getAlias())) {
					found = true;
					break;
				}
			}
			
			if (!found)
				removeBackupFolder(i, f1.getId());
		}
	}
	
	/**
	 * Set the proxy configuration
	 * 
	 * @param pc
	 *            Proxy configuration
	 */
	public void setProxyConfiguration(ProxyConfiguration pc) throws Exception {
		if (bm != null)
			bm.setRestClient(new RestClient(pc));
			
	}
	
	//-----------END CONFIGURATION HELPER---------------
	
	/**
	 * Load the configuration, open and load the database
	 * 
	 * @param password
	 *            User password
	 * @throws Exception 
	 */
	public void login(String password) throws Exception {
		if (getConfiguration().isEmpty())
			throw new BackBoxException("Configuration not found.");

		ex.add(".deleted");
		
		sm = new SecurityManager(password, getConfiguration().getPwdDigest(), getConfiguration().getSalt());
		if (_log.isLoggable(Level.INFO)) _log.info("SecurityManager init OK");

		//try to instantiate the rest client before the boxmanager, because it can fail
		RestClient client = new RestClient(getConfiguration().getProxyConfiguration());
		
		bm = new BoxManager();
		bm.setRestClient(client);
		String folderID = getConfiguration().getRootFolderID();
		if ((folderID == null) || folderID.isEmpty()) {
			folderID = bm.getBoxID(BoxManager.ROOT_FOLDER_NAME);
			getConfiguration().setRootFolderID(folderID);
			saveConfiguration();
		}
		
		//if something goes wrong you could have (only) the decrypted db file
		if (Files.exists(Paths.get(DB_FILE_TEMP))) {
			if (_log.isLoggable(Level.WARNING)) _log.warning("Something went wrong, decrypted DB found. Trying to open it...");
		} else {
			if (!Files.exists(Paths.get(DB_FILE)))
				throw new BackBoxException("DB not found");
			
			if (_log.isLoggable(Level.INFO)) _log.info("DB found");
			sm.decrypt(DB_FILE, DB_FILE_TEMP);
		}
		
		dbm = new DBManager(DB_FILE_TEMP);
		dbm.openDB();
		if (_log.isLoggable(Level.INFO)) _log.info("DBManager init OK");
		
		ICompress z = new Zipper();
		ISplitter s = new Splitter(getConfiguration().getChunkSize());
		
		tm = new TransactionManager(dbm, bm, sm, s, z);
		if (_log.isLoggable(Level.INFO)) _log.info("TransactionManager init OK");
	}
	
	/**
	 * Create a new configuration, login to Box
	 * 
	 * @param password
	 *            User password
	 * @param backupFolders
	 * 			  Folders to backup
	 * @param chunksize
	 *            Chunk size limit of cloud provider
	 * @throws Exception
	 */
	public void register(String password, List<Folder> backupFolders, int chunksize) throws Exception {
		logout();
		
		if (Files.exists(Paths.get(DB_FILE)))
			Files.delete(Paths.get(DB_FILE));
		if (Files.exists(Paths.get(DB_FILE_TEMP)))
			Files.delete(Paths.get(DB_FILE_TEMP));
		
		sm = new SecurityManager(password);
		if (_log.isLoggable(Level.INFO)) _log.info("SecurityManager init OK");
		
		getConfiguration().setPwdDigest(sm.getPwdDigest());
		getConfiguration().setSalt(Hex.encodeHexString(sm.getSalt()));
		
		dbm = new DBManager(DB_FILE_TEMP);
		if (_log.isLoggable(Level.INFO)) _log.info("DBManager init OK");
		dbm.createDB();
		if (_log.isLoggable(Level.INFO)) _log.info("DB created");
		
		bm = new BoxManager(new RestClient(getConfiguration().getProxyConfiguration()));
		String rootFolderID = bm.getBoxID(BoxManager.ROOT_FOLDER_NAME);
		if (rootFolderID != null) {
			if (_log.isLoggable(Level.WARNING)) _log.warning("Box Upload folder exists");
			bm.deleteFolder(rootFolderID);
			getConfiguration().setConfFileID(null);
			getConfiguration().setDbFileID(null);
		}
		rootFolderID = bm.mkdir(BoxManager.ROOT_FOLDER_NAME, null);
		
		getConfiguration().setRootFolderID(rootFolderID);
		
		getConfiguration().setBackupFolders(new ArrayList<Folder>());
		for (Folder folder : backupFolders)
			addBackupFolder(folder);

		if (_log.isLoggable(Level.INFO)) _log.info("BoxManager init OK");
		
		ICompress z = new Zipper();
		ISplitter s = new Splitter(chunksize);
		
		tm = new TransactionManager(dbm, bm, sm, s, z);
		if (_log.isLoggable(Level.INFO)) _log.info("TransactionManager init OK");
		
		getConfiguration().setChunkSize(chunksize);
		
		getConfiguration().setLogSize(DEFAULT_LOG_SIZE);
		
		saveConfiguration();
	}
	
	/**
	 * Close the connection
	 * 
	 * @throws SQLException
	 * @throws IOException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws InvalidParameterSpecException
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 */
	public void logout() throws SQLException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException, IOException {
		if (dbm != null)
			dbm.closeDB();
		if ((sm != null) && Files.exists(Paths.get(DB_FILE_TEMP))) {
			sm.encrypt(DB_FILE_TEMP, DB_FILE);
			Files.delete(Paths.get(DB_FILE_TEMP));
		}
		if (tm != null)
			tm.close();

		dbm = null;
		sm = null;
		tm = null;
		bm = null;
	}
	
	/**
	 * Get a list of all records
	 * 
	 * @return The list of all records
	 * @throws SQLException
	 * @throws IOException 
	 */
	public List<SimpleEntry<String, it.backbox.bean.File>> getRecords() throws SQLException, IOException {
		List<SimpleEntry<String, it.backbox.bean.File>> ret = new ArrayList<>();
		
		for (Folder folder : getConfiguration().getBackupFolders()) {
			Map<String, Map<String, it.backbox.bean.File>> map = dbm.getFolderRecords(folder.getAlias());
			for (Map<String, it.backbox.bean.File> m : map.values())
				for (it.backbox.bean.File f : m.values())
					ret.add(new SimpleEntry<String, it.backbox.bean.File>(f.getHash(),f));
		}
		
		return ret;
	}
	
	/**
	 * Get all files in the database with chunks remotely deleted
	 * 
	 * @param deleteFromDB
	 *            true if delete that files from database too, false otherwise
	 * @return List of files remotely deleted
	 * @throws IOException
	 * @throws SQLException
	 * @throws BackBoxException
	 * @throws RestException
	 */
	public List<it.backbox.bean.File> getRemotelyDeletedFiles(boolean deleteFromDB) throws IOException, SQLException, BackBoxException, RestException {
		List<it.backbox.bean.File> deleted = new ArrayList<>();
		
		List<it.backbox.bean.File> records = dbm.getAllFiles();
		for (it.backbox.bean.File f : records)
			for (Chunk c : f.getChunks())
				if ((c.getBoxid() == null) || 
						c.getBoxid().isEmpty() || 
						c.getBoxid().equals("null") || 
						!bm.checkRemoteFile(c.getBoxid())) {
					if (deleteFromDB) {
						try {
							if (f.getChunks().size() > 1)
								bm.deleteChunk(f.getChunks());
						} catch (RestException e) {	}
						dbm.delete(f.getFilename(), f.getHash());
					}
					deleted.add(f);
					break;
				}
		
		return deleted;
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
	 * @return The created transactions
	 * @throws BackBoxException 
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public List<Transaction> restoreAll(String restoreFolder) throws BackBoxException, SQLException, IOException {
		List<Transaction> tt = new ArrayList<>();
		
		for (Folder backupFolder : getConfiguration().getBackupFolders())
			tt.addAll(restore(restoreFolder, backupFolder, false));
		
		return tt;
	}
	
	/**
	 * Restore all files
	 * 
	 * @param restoreFolder
	 *            Folder where restore
	 * @param backupFolder
	 * 			  Folder to restore
	 * @param startNow
	 *            true if start the transactions, false if just create them
	 * @return The created transactions
	 * @throws BackBoxException 
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public List<Transaction> restore(String restoreFolder, Folder backupFolder, boolean startNow) throws BackBoxException, SQLException, IOException {
		if (restoreFolder == null)
			throw new BackBoxException("Restore path not specified");
		
		List<Transaction> tt = new ArrayList<>();
		
		Path base = Paths.get(restoreFolder, backupFolder.getAlias());
		FileCompare c = new FileCompare(dbm.getFolderRecords(backupFolder.getAlias()), base, ex);
		c.load();
		
		Path del = null;
		if (!c.getFilesNotInRecords().isEmpty()) {
			del = Paths.get(restoreFolder, backupFolder.getAlias(), ".deleted");
			del.toFile().mkdirs();
		}
		
		Map<String, Map<String, File>> toDelete = c.getFilesNotInRecords();
		for (String hash : toDelete.keySet()) {
			Transaction t = new Transaction();
			t.setId(hash);
			for (String path : toDelete.get(hash).keySet()) {
				DeleteTask dt = new DeleteTask(base.toString(), path, del.toString());
				dt.setDescription(new StringBuilder(backupFolder.getAlias()).append('\\').append(path).toString());
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
		
		Map<String, Map<String, it.backbox.bean.File>> toDownload = c.getRecordsNotInFiles();
		for (String hash : toDownload.keySet()) {
			Transaction t = new Transaction();
			t.setId(hash);
			boolean first = true;
			String fileToCopy = null;
			for (String path : toDownload.get(hash).keySet()) {
				if (c.getFiles().containsKey(hash) && !c.getFiles().get(hash).containsKey(path)) {
					CopyTask ct = new CopyTask(c.getFiles().get(hash).values().iterator().next().getCanonicalPath(), base.resolve(c.getRecords().get(hash).get(path).getFilename()).toString());
					ct.setDescription(new StringBuilder(backupFolder.getAlias()).append('\\').append(path).toString());
					t.addTask(ct);
				} else if (!c.getFiles().containsKey(hash)) {
					it.backbox.bean.File file = c.getRecords().get(hash).get(path);
					if (first) {
						DownloadTask dt = new DownloadTask(base.toString(), file);
						dt.setWeight(file.getSize());
						dt.setCountWeight(false);
						dt.setDescription(new StringBuilder(backupFolder.getAlias()).append('\\').append(file.getFilename()).toString());
						t.addTask(dt);
						fileToCopy = file.getFilename();
						first = false;
					} else {
						CopyTask ct = new CopyTask(base.resolve(fileToCopy).toString(), base.resolve(file.getFilename()).toString());
						ct.setDescription(new StringBuilder(backupFolder.getAlias()).append('\\').append(path).toString());
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
		
		if (startNow)
			tm.shutdown();
		
		return tt;
	}
	
	/**
	 * Backup all files
	 * 
	 * @return The created transactions
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public List<Transaction> backupAll() throws SQLException, IOException {
		List<Transaction> tt = new ArrayList<>();
		
		for (Folder backupFolder : getConfiguration().getBackupFolders())
			tt.addAll(backup(backupFolder, false));
		
		return tt;
	}
	
	/**
	 * Backup all files
	 * 
	 * @param backupFolder
	 * 			  Folder to backup
	 * @param startNow
	 *            true if start the transactions, false if just create them
	 * @return The created transactions
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public List<Transaction> backup(Folder backupFolder, boolean startNow) throws SQLException, IOException {
		List<Transaction> tt = new ArrayList<>();
		
		FileCompare c = new FileCompare(dbm.getFolderRecords(backupFolder.getAlias()), Paths.get(backupFolder.getPath()), ex);
		c.load();
		
		Map<String, Map<String, File>> toUpload = c.getFilesNotInRecords();
		for (String hash : toUpload.keySet()) {
			Transaction t = new Transaction();
			t.setId(hash);
			boolean first = true;
			for (String path : toUpload.get(hash).keySet()) {
				if ((c.getRecords().containsKey(hash) && !c.getRecords().get(hash).containsKey(path)) || 
						(!c.getRecords().containsKey(hash) && !first)) {
					InsertTask it =  new InsertTask(hash, c.getFiles().get(hash).get(path), path, backupFolder);
					it.setDescription(new StringBuilder(backupFolder.getAlias()).append('\\').append(path).toString());
					t.addTask(it);
				} else if (!c.getRecords().containsKey(hash) && first) {
					UploadTask ut = new UploadTask(hash, c.getFiles().get(hash).get(path), path, backupFolder);
					ut.setWeight(c.getFiles().get(hash).get(path).length());
					ut.setCountWeight(false);
					ut.setDescription(new StringBuilder(backupFolder.getAlias()).append('\\').append(path).toString());
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
					rt.setDescription(new StringBuilder(backupFolder.getAlias()).append('\\').append(path).toString());
					t.addTask(rt);
				} else if (!c.getFiles().containsKey(hash) && first) {
					DeleteBoxTask dt = new DeleteBoxTask(c.getRecords().get(hash).get(path));
					dt.setDescription(new StringBuilder(backupFolder.getAlias()).append('\\').append(path).toString());
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
		dt.setDescription(new StringBuilder(file.getFolder()).append('\\').append(file.getFilename()).toString());
		
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

		sm = new SecurityManager(password, getConfiguration().getPwdDigest(), getConfiguration().getSalt());
		if (_log.isLoggable(Level.INFO)) _log.info("SecurityManager init OK");

		dbm = new DBManager(DB_FILE_TEMP);
		dbm.createDB();
		if (_log.isLoggable(Level.INFO)) _log.info("DBManager init OK");
		
		bm = new BoxManager(new RestClient(getConfiguration().getProxyConfiguration()));
		String folderID = getConfiguration().getRootFolderID();
		if ((folderID == null) || folderID.isEmpty()) {
			folderID = bm.getBoxID(BoxManager.ROOT_FOLDER_NAME);
			if ((folderID == null) || folderID.isEmpty())
				throw new BackBoxException("Remote root folder not found");
		}
		
		List<Folder> folders = getConfiguration().getBackupFolders();
		
		for (Folder f : folders) {
			Map<String, List<Chunk>> remoteInfo = bm.getFolderChunks(f.getId());
			
			FileCompare c = new FileCompare(dbm.getFolderRecords(f.getAlias()), Paths.get(f.getPath()), ex);
			c.load();
			
			Map<String, Map<String, File>> localInfo = c.getFiles();
			for (String hash : remoteInfo.keySet()) {
				if (_log.isLoggable(Level.INFO)) _log.info("Restoring " + hash);
				List<Chunk> chunks = remoteInfo.get(hash);
				if (!localInfo.containsKey(hash)) {
					bm.deleteChunk(chunks);
					if (_log.isLoggable(Level.INFO)) _log.info("Not found locally, deleted " + hash);
					break;
				}
				Map<String, File> fileInfo = localInfo.get(hash);
				for (String path : fileInfo.keySet()) {
					File file = fileInfo.get(path);
					if (_log.isLoggable(Level.INFO)) _log.info("Insert " + hash + " " + path + " " + chunks.size());
					dbm.insert(file, path, f.getPath(), hash, chunks, true, true, chunks.size() > 1);
				}
			}
		}
		
		logout();
	}
	
	/**
	 * Get the free space
	 * 
	 * @return Free space in bytes
	 * @throws IOException
	 * @throws RestException
	 * @throws BackBoxException
	 */
	public long getFreeSpace() throws IOException, RestException, BackBoxException {
		if (bm != null)
			return bm.getFreeSpace();
		throw new BackBoxException("BoxManager null");
	}
}
