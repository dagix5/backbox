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
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Hex;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;

public class BackBoxHelper {
	private static Logger _log = Logger.getLogger(BackBoxHelper.class.getCanonicalName());
	
	public static final String DB_FILE = "backbox.db";
	public static final String DB_FILE_TEMP = "backbox.db.temp";
	
	public static final String CONFIG_FILE = "config.json";
	
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();
	private Configuration configuration;
	
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
				JsonObjectParser parser = new JsonObjectParser(JSON_FACTORY);
				configuration = parser.parseAndClose(new FileReader(CONFIG_FILE), Configuration.class);
			} catch (FileNotFoundException e) {
				_log.log(Level.WARNING, "Configuration file not found", e);
				configuration = new Configuration();
			}
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
			_log.fine("Decrypted DB found");
			return true;
		}
		return Files.exists(Paths.get(DB_FILE));
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
		
		bm = new BoxManager(new RestClient(getConfiguration().getProxyConfiguration()));
		String rootFolderID = getConfiguration().getRootFolderID();
		if ((rootFolderID == null) || rootFolderID.isEmpty())
			rootFolderID = bm.getBoxID(BoxManager.ROOT_FOLDER_NAME);
		
		bm.upload(DB_FILE, rootFolderID);
		bm.upload(CONFIG_FILE, rootFolderID);
	}
	
	/**
	 * Download the configuration from Box.com
	 * 
	 * @throws Exception
	 */
	public void downloadConf() throws Exception {
		logout();
		
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
	 * @throws Exception 
	 */
	private void removeBackupFolder(int index, String folderID) throws Exception {
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
	 * @throws Exception
	 */
	public void updateBackupFolders(List<Folder> folders) throws Exception {
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

		sm = new SecurityManager(password, getConfiguration().getPwdDigest(), getConfiguration().getSalt());
		_log.fine("SecurityManager init OK");

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
		ISplitter s = new Splitter(getConfiguration().getChunkSize());
		
		tm = new TransactionManager(dbm, bm, sm, s, z);
		_log.fine("TransactionManager init OK");
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
		_log.fine("SecurityManager init OK");
		
		getConfiguration().setPwdDigest(sm.getPwdDigest());
		getConfiguration().setSalt(Hex.encodeHexString(sm.getSalt()));
		
		dbm = new DBManager(DB_FILE_TEMP);
		_log.fine("DBManager init OK");
		dbm.createDB();
		_log.fine("DB created");
		
		bm = new BoxManager(new RestClient(getConfiguration().getProxyConfiguration()));
		String rootFolderID = bm.getBoxID(BoxManager.ROOT_FOLDER_NAME);
		if (rootFolderID != null) {
			_log.warning("Box Upload folder exists");
			bm.deleteFolder(rootFolderID);
		}
		rootFolderID = bm.mkdir(BoxManager.ROOT_FOLDER_NAME, null);
		
		getConfiguration().setRootFolderID(rootFolderID);
		
		getConfiguration().setBackupFolders(new ArrayList<Folder>());
		for (Folder folder : backupFolders)
			addBackupFolder(folder);

		_log.fine("BoxManager init OK");
		
		ICompress z = new Zipper();
		ISplitter s = new Splitter(chunksize);
		
		tm = new TransactionManager(dbm, bm, sm, s, z);
		_log.fine("TransactionManager init OK");
		
		getConfiguration().setChunkSize(chunksize);
		
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
		
		for (Folder backupFolder : getConfiguration().getBackupFolders()) {
			Path base = Paths.get(restoreFolder, backupFolder.getAlias());
			FileCompare c = new FileCompare(dbm.getFolderRecords(backupFolder.getAlias()), base, ex);
			c.load();
			
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
		}
		
		if (startNow)
			tm.shutdown();
		
		return tt;
	}
	
	/**
	 * Backup all files
	 * 
	 * @param startNow
	 *            true if start the transactions, false if just create them
	 * @return The created transactions
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public ArrayList<Transaction> backup(boolean startNow) throws SQLException, IOException {
		ArrayList<Transaction> tt = new ArrayList<>();
		
		List<String> ex = new ArrayList<>();
		ex.add(".deleted");
		
		for (Folder backupFolder : getConfiguration().getBackupFolders()) {
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
		_log.fine("SecurityManager init OK");

		dbm = new DBManager(DB_FILE);
		dbm.createDB();
		_log.fine("DBManager init OK");
		
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
			List<String> ex = new ArrayList<>();
			ex.add(".deleted");
			
			FileCompare c = new FileCompare(dbm.getFolderRecords(f.getAlias()), Paths.get(f.getPath()), ex);
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
					dbm.insert(file, path, f.getPath(), hash, chunks, true, true, chunks.size() > 1);
				}
			}
		}
		
		logout();
	}
	
}