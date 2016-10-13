package it.backbox.gui.utility;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.apache.commons.codec.digest.DigestUtils;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.common.collect.Table;

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
import it.backbox.compare.CompareResult;
import it.backbox.compare.CompareResult.Status;
import it.backbox.compare.FileCompare;
import it.backbox.compress.Zipper;
import it.backbox.db.DBManager;
import it.backbox.exception.BackBoxException;
import it.backbox.exception.RestException;
import it.backbox.progress.ProgressManager;
import it.backbox.security.SecurityManager;
import it.backbox.split.Splitter;
import it.backbox.transaction.CopyTask;
import it.backbox.transaction.DeleteBoxTask;
import it.backbox.transaction.DeleteTask;
import it.backbox.transaction.DownloadTask;
import it.backbox.transaction.InsertTask;
import it.backbox.transaction.Transaction;
import it.backbox.transaction.TransactionManager;
import it.backbox.transaction.UploadTask;

public class BackBoxHelper {
	private static final Logger _log = Logger.getLogger(BackBoxHelper.class.getCanonicalName());

	public static final String DB_FILE_TEMP = "backbox.db.temp";
	public static final String DEFAULT_CONFIG_FILE = "config.json";
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();
	private static final Set<String> ex = new HashSet<>();

	private static BackBoxHelper instance;

	private Configuration configuration;
	private String configFile = DEFAULT_CONFIG_FILE;

	public ISecurityManager sm;
	public IDBManager dbm;
	public TransactionManager tm;
	public IBoxManager bm;

	public static BackBoxHelper getInstance() {
		if (instance == null)
			instance = new BackBoxHelper();
		return instance;
	}

	private BackBoxHelper() {

	}
	
	public void setConfFile(String confFile) {
		this.configFile = confFile;
	}

	// -----------START CONFIGURATION HELPER---------------

	/**
	 * Load (if needed) the configuration
	 * 
	 * @return Configuration bean
	 * @throws IOException
	 */
	public void loadConfiguration() throws IOException {
		GuiUtility.checkEDT(false);

		if (configuration == null) {
			try {
				if (Files.exists(Paths.get(configFile))) {
					JsonObjectParser parser = new JsonObjectParser(JSON_FACTORY);
					configuration = parser.parseAndClose(new FileReader(configFile), Configuration.class);
					_log.log(Level.INFO, "Configuration file loaded: " + configFile);
					return;
				}
			} catch (FileNotFoundException e) {
				_log.log(Level.SEVERE, "Configuration file not found", e);
			}
			configuration = new Configuration();
		}
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * Save the configuration to file
	 * 
	 * @throws IOException
	 */
	public void saveConfiguration() throws IOException {
		GuiUtility.checkEDT(false);

		Files.write(Paths.get(configFile), JSON_FACTORY.toByteArray(configuration));
	}

	/**
	 * Chech if the configuration file exists
	 * 
	 * @return true if it exists, false otherwise
	 */
	public boolean confExists() {
		return Files.exists(Paths.get(configFile));// && dbExists();
	}

	/**
	 * Check if the database file exists
	 * 
	 * @return true if the file exists, false otherwise
	 */
	public boolean dbExists() {
		if (Files.exists(Paths.get(DB_FILE_TEMP))) {
			if (_log.isLoggable(Level.INFO))
				_log.info("Decrypted DB found");
			return true;
		}
		return Files.exists(Paths.get(getConfiguration().getDbFilename()));
	}

	/**
	 * Upload the configuration on Box.com
	 * 
	 * @param force
	 *            Force upload configuration, even if it is not modified
	 * @throws Exception
	 */
	public void uploadConf(boolean force) throws Exception {
		GuiUtility.checkEDT(false);

		if (!confExists())
			throw new BackBoxException("Configuration not found");

		boolean uploadDB = ((dbm != null) && dbm.isModified()) || force;
		boolean uploadConf = ((configuration != null) && configuration.isModified()) || force;

		logout();

		if (bm == null)
			bm = new BoxManager(new RestClient(getConfiguration().getProxyConfiguration()));

		String rootFolderID = getConfiguration().getRootFolderID();
		if ((rootFolderID == null) || rootFolderID.isEmpty())
			rootFolderID = bm.getBoxID(BoxManager.ROOT_FOLDER_NAME);

		if (uploadDB) {
			String id = bm.upload(getConfiguration().getDbFilename(), getConfiguration().getDbFileID(), rootFolderID);
			getConfiguration().setDbFileID(id);
		}
		if (uploadConf) {
			// TODO save conf file id in a different file
			String id = bm.upload(configFile, getConfiguration().getConfFileID(), rootFolderID);
			getConfiguration().setConfFileID(id);
			saveConfiguration();
		}
	}

	/**
	 * Download the configuration from Box.com
	 * 
	 * @throws Exception
	 */
	public void downloadConf() throws Exception {
		GuiUtility.checkEDT(false);

		logout();

		if (bm == null)
			bm = new BoxManager(new RestClient(getConfiguration().getProxyConfiguration()));

		byte[] conf = bm.download(bm.getBoxID(configFile));
		if (conf.length == 0)
			throw new BackBoxException("Configuration file empty");

		JsonObjectParser parser = new JsonObjectParser(JSON_FACTORY);
		configuration = parser.parseAndClose(new ByteArrayInputStream(conf), null, Configuration.class);
		saveConfiguration();
		
		String dbFileName = configuration.getDbFilename();
		String name = dbFileName + ".new";
		String id = bm.getBoxID(dbFileName);
		if (id == null)
			throw new BackBoxException("DB file not found");
		Files.write(Paths.get(name), bm.download(id));
		File f = new File(name);
		if (f.exists() && (f.length() > 0))
			Files.move(Paths.get(name), Paths.get(dbFileName), StandardCopyOption.REPLACE_EXISTING);
		else
			throw new BackBoxException("DB file empty");
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
		GuiUtility.checkEDT(false);

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
	 *            Configuration index of the folder to remove
	 * @param folder
	 *            Folder to remove
	 * @throws RestException
	 * @throws IOException
	 * @throws BackBoxException
	 * @throws SQLException 
	 */
	private void removeBackupFolder(int index, Folder folder) throws IOException, RestException, BackBoxException, SQLException {
		getConfiguration().getBackupFolders().remove(index);
		saveConfiguration();
		
		List<it.backbox.bean.File> files = dbm.getFilesInFolder(folder.getAlias());
		for (it.backbox.bean.File f : files) {
			dbm.delete(folder.getAlias(), f.getFilename(), f.getHash());
			List<it.backbox.bean.File> ofs = dbm.getFiles(f.getHash());
			if ((ofs == null) || ofs.isEmpty())
				bm.deleteChunk(f.getChunks());
		}
	}

	/**
	 * Edit a folder to backup
	 * 
	 * @param index
	 *            Configuration index of the folder to edit
	 * @param folder
	 *            Updated folder configuration
	 * @throws RestException
	 * @throws IOException
	 * @throws BackBoxException
	 */
	private void editBackupFolder(int index, Folder folder) throws IOException {
		getConfiguration().getBackupFolders().set(index, folder);
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
	 * @throws SQLException 
	 */
	public void updateBackupFolders(List<Folder> folders) throws IOException, BackBoxException, RestException, SQLException {
		GuiUtility.checkEDT(false);

		for (Folder f1 : folders) {
			boolean found = false;
			for (int i = 0; i < getConfiguration().getBackupFolders().size(); i++) {
				Folder f2 = getConfiguration().getBackupFolders().get(i);
				if (f1.getAlias().equals(f2.getAlias())) {
					editBackupFolder(i, f1);
					found = true;
					break;
				}
			}

			if (!found)
				addBackupFolder(f1);
		}

		loadConfiguration();
		
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
				removeBackupFolder(i, f1);
		}
	}

	/**
	 * Set the proxy configuration
	 * 
	 * @param pc
	 *            Proxy configuration
	 */
	public void setProxyConfiguration(ProxyConfiguration pc) throws Exception {
		GuiUtility.checkEDT(false);

		if (bm != null)
			bm.setRestClient(new RestClient(pc));

	}

	// -----------END CONFIGURATION HELPER---------------

	/**
	 * Load the configuration, open and load the database
	 * 
	 * @param password
	 *            User password
	 * @throws Exception
	 */
	public void login(String password) throws Exception {
		GuiUtility.checkEDT(false);

		if (getConfiguration().isEmpty())
			throw new BackBoxException("Configuration not found.");

		ex.add(".deleted");

		sm = new SecurityManager(password, getConfiguration().getPwdDigest(), getConfiguration().getSalt());
		if (_log.isLoggable(Level.INFO))
			_log.info("SecurityManager init OK");

		// try to instantiate the rest client before the boxmanager, because it
		// can fail
		RestClient client = new RestClient(getConfiguration().getProxyConfiguration());

		bm = new BoxManager();
		bm.setRestClient(client);
		String folderID = getConfiguration().getRootFolderID();
		if ((folderID == null) || folderID.isEmpty()) {
			folderID = bm.getBoxID(BoxManager.ROOT_FOLDER_NAME);
			getConfiguration().setRootFolderID(folderID);
			saveConfiguration();
		}

		ICompress z = new Zipper();

		// if something went wrong, we could have (only) the decrypted db file
		if (Files.exists(Paths.get(DB_FILE_TEMP))) {
			if (_log.isLoggable(Level.WARNING))
				_log.warning("Something went wrong, decrypted DB found. Trying to open it...");
		} else {
			String dbFileName = getConfiguration().getDbFilename();
			if (!Files.exists(Paths.get(dbFileName)))
				throw new BackBoxException("DB not found");

			if (_log.isLoggable(Level.INFO))
				_log.info("DB found");
			byte[] dbContent = sm.decrypt(dbFileName);
			z.decompress(dbContent, null, DB_FILE_TEMP);
			dbContent = null;
		}

		dbm = new DBManager(DB_FILE_TEMP);
		dbm.openDB();
		if (_log.isLoggable(Level.INFO))
			_log.info("DBManager init OK");

		ISplitter s = new Splitter(getConfiguration().getChunkSize());

		tm = new TransactionManager(dbm, bm, sm, s, z);
		if (_log.isLoggable(Level.INFO))
			_log.info("TransactionManager init OK");
	}

	/**
	 * Create a new configuration, login to Box
	 * 
	 * @param password
	 *            User password
	 * @param backupFolders
	 *            Folders to backup
	 * @param chunksize
	 *            Chunk size limit of cloud provider
	 * @throws Exception
	 */
	public void register(String password, List<Folder> backupFolders, int chunksize) throws Exception {
		GuiUtility.checkEDT(false);

		logout();

//		if (Files.exists(Paths.get(DEFAULT_DB_FILE)))
//			Files.delete(Paths.get(DEFAULT_DB_FILE));
		if (Files.exists(Paths.get(DB_FILE_TEMP)))
			Files.delete(Paths.get(DB_FILE_TEMP));

		sm = new SecurityManager(password);
		if (_log.isLoggable(Level.INFO))
			_log.info("SecurityManager init OK");

		getConfiguration().setPwdDigest(sm.getPwdDigest());
		getConfiguration().setSalt(Hex.encodeHexString(sm.getSalt()));

		dbm = new DBManager(DB_FILE_TEMP);
		if (_log.isLoggable(Level.INFO))
			_log.info("DBManager init OK");
		dbm.createDB();
		if (_log.isLoggable(Level.INFO))
			_log.info("DB created");

		bm = new BoxManager(new RestClient(getConfiguration().getProxyConfiguration()));
		String rootFolderID = bm.getBoxID(BoxManager.ROOT_FOLDER_NAME);
		if (rootFolderID != null) {
			if (_log.isLoggable(Level.WARNING))
				_log.warning("Box Upload folder exists");
			bm.deleteFolder(rootFolderID);
			getConfiguration().setConfFileID(null);
			getConfiguration().setDbFileID(null);
		}
		rootFolderID = bm.mkdir(BoxManager.ROOT_FOLDER_NAME, null);

		getConfiguration().setRootFolderID(rootFolderID);

		getConfiguration().setBackupFolders(new ArrayList<Folder>());
		for (Folder folder : backupFolders)
			addBackupFolder(folder);

		if (_log.isLoggable(Level.INFO))
			_log.info("BoxManager init OK");

		ICompress z = new Zipper();
		ISplitter s = new Splitter(chunksize);

		tm = new TransactionManager(dbm, bm, sm, s, z);
		if (_log.isLoggable(Level.INFO))
			_log.info("TransactionManager init OK");

		getConfiguration().setChunkSize(chunksize);

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
	public void logout() throws SQLException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException, IOException {
		GuiUtility.checkEDT(false);

		if (dbm != null)
			dbm.closeDB();
		if ((sm != null) && Files.exists(Paths.get(DB_FILE_TEMP))) {
			ICompress z = new Zipper();
			byte[] dbZip = z.compress(DB_FILE_TEMP, null);
			byte[] dbContent = sm.encrypt(dbZip);
			dbZip = null;
			Files.write(Paths.get(getConfiguration().getDbFilename()), dbContent);
			dbContent = null;
			Files.delete(Paths.get(DB_FILE_TEMP));
		}
		if (tm != null)
			tm.close();
		ProgressManager pm = ProgressManager.getInstance();
		pm.setListener(ProgressManager.UPLOAD_ID, null);
		pm.setListener(ProgressManager.UPLOAD_ID, null);

		dbm = null;
		sm = null;
		tm = null;
		bm = null;
	}

	/**
	 * Check if a file exists remotely
	 * 
	 * @param f
	 *            File to check
	 * @return true if the file exists remotely, false otherwise
	 * @throws IOException
	 * @throws RestException
	 */
	public boolean existsRemotely(it.backbox.bean.File f) throws IOException, RestException {
		for (Chunk c : f.getChunks()) {
			if ((c.getBoxid() == null) || c.getBoxid().isEmpty() || c.getBoxid().equals("null")) {
				if (_log.isLoggable(Level.INFO))
					_log.info("[" + f.getFilename() + "] [" + f.getHash() + "] [] box id null");

				return false;
			} else if (!bm.checkRemoteFile(c.getBoxid())) {
				if (_log.isLoggable(Level.INFO))
					_log.info("[" + f.getFilename() + "] [" + f.getHash() + "] [" + c.getBoxid() + "] not found on box");

				return false;
			} else if (_log.isLoggable(Level.FINE))
				_log.fine(f.getFilename() + " " + f.getHash() + " found on box");
		}
		return true;
	}

	/**
	 * Download a single file
	 * 
	 * @param folder
	 *            Folder where the file is
	 * @param filename
	 *            Name of the file
	 * @param hash
	 *            Hash of the file
	 * @param downloadPath
	 *            Path where download
	 * @param startNow
	 *            true if start the transaction, false if just create it
	 * 
	 * @return The created transaction
	 * @throws SQLException
	 */
	public Transaction downloadFile(String folder, String filename, String hash, String downloadPath, boolean startNow)
			throws SQLException {
		GuiUtility.checkEDT(false);

		it.backbox.bean.File file = dbm.getFileRecord(folder, filename, hash);

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
	
	private static Path getDelPath(String restoreFolder, Folder backupFolder) {
		Path del = Paths.get(restoreFolder, backupFolder.getAlias(), ".deleted");
		del.toFile().mkdirs();
		
		return del;
	}
	
	private static Path getBasePath(String restoreFolder, Folder backupFolder) {
		return Paths.get(restoreFolder, backupFolder.getAlias());
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
		GuiUtility.checkEDT(false);
		return restore(restoreFolder, getConfiguration().getBackupFolders());
	}

	/**
	 * Restore all files
	 * 
	 * @param restoreFolder
	 *            Folder where restore
	 * @param backupFolder
	 *            Folder to restore
	 * @param startNow
	 *            true if start the transactions, false if just create them
	 * @return The created transactions
	 * @throws BackBoxException
	 * @throws SQLException
	 * @throws IOException
	 */
	public List<Transaction> restore(String restoreFolder, List<Folder> backupFolder)
			throws BackBoxException, SQLException, IOException {
		GuiUtility.checkEDT(false);

		if (restoreFolder == null)
			throw new BackBoxException("Restore path not specified");
		
		FileCompare fc = new FileCompare(dbm, restoreFolder, backupFolder, ex);
		Table<String, String, CompareResult> result = fc.run();
		
		List<Transaction> tt = new ArrayList<>();
		
		for (String hash : result.rowKeySet()) {
			Transaction t = new Transaction();
			t.setId(hash);
			
			boolean first = true;
			String firstPath = null;
			
			for (String fn : result.columnKeySet()) {
				CompareResult cr = result.get(hash, fn);
				if (cr != null) {
					int s = cr.getStatus();
					switch (s) {
					case Status.NEW:
						if (orByFn(result, fn) == Status.NEW) {
							DeleteTask dt = new DeleteTask(getBasePath(restoreFolder, cr.getFolder()).toString(), cr.getFile().getFilename(), getDelPath(restoreFolder, cr.getFolder()).toString());
							dt.setDescription(cr.getFolder().getAlias() + '\\' + cr.getFile().getFilename());
							dt.setPriority((short) 0);
							t.addTask(dt);
							
							if (_log.isLoggable(Level.FINE))
								_log.fine("[" + hash + "] [" + fn + "] [" + s + "] [" + orByHash(result, hash) + "] [" + orByFn(result, fn) + "] [DELETE] [0]");
						}
							
						break;
					case Status.DELETED:
						if (first && orByHash(result, hash) ==  Status.DELETED) {
							DownloadTask dt = new DownloadTask(getBasePath(restoreFolder, cr.getFolder()).toString(), cr.getFile());
							dt.setWeight(cr.getFile().getSize());
							dt.setCountWeight(false);
							dt.setDescription(cr.getFolder().getAlias() + '\\' + cr.getFile().getFilename());
							dt.setPriority((short) 100);
							t.addTask(dt);
							
							first = false;
							firstPath = getBasePath(restoreFolder, cr.getFolder()).resolve(cr.getFile().getFilename()).toString();
							
							if (_log.isLoggable(Level.FINE))
								_log.fine("[" + hash + "] [" + fn + "] [" + s + "] [" + orByHash(result, hash) + "] [" + orByFn(result, fn) + "] [DOWNLOAD] [100]");
						} else {
							CopyTask ct = new CopyTask(firstPath, getBasePath(restoreFolder, cr.getFolder()).resolve(cr.getFile().getFilename()).toString());
							ct.setDescription(cr.getFolder().getAlias() + '\\' + cr.getFile().getFilename());
							ct.setPriority((short) 50);
							t.addTask(ct);
							
							if (_log.isLoggable(Level.FINE))
								_log.fine("[" + hash + "] [" + fn + "] [" + s + "] [" + orByHash(result, hash) + "] [" + orByFn(result, fn) + "] [COPY] [50]");
						}
						break;
					case Status.COPIED:
						DeleteTask dt = new DeleteTask(getBasePath(restoreFolder, cr.getFolder()).toString(), cr.getFile().getFilename(), getDelPath(restoreFolder, cr.getFolder()).toString());
						dt.setDescription(cr.getFolder().getAlias() + '\\' + cr.getFile().getFilename());
						dt.setPriority((short) 0);
						t.addTask(dt);
						
						
						if (_log.isLoggable(Level.FINE))
							_log.fine("[" + hash + "] [" + fn + "] [" + s + "] [" + orByHash(result, hash) + "] [" + orByFn(result, fn) + "] [DELETE] [0]");
						break;
					}
				}
			}
			
			if (!t.getTasks().isEmpty()) {
				tt.add(t);
				tm.addTransaction(t);
			}
		}

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
		GuiUtility.checkEDT(false);
		return backup(getConfiguration().getBackupFolders());
	}

	/**
	 * Backup all files
	 * 
	 * @param backupFolder
	 *            Folder to backup
	 * @return The created transactions
	 * @throws SQLException
	 * @throws IOException
	 */
	public List<Transaction> backup(List<Folder> backupFolder) throws SQLException, IOException {
		GuiUtility.checkEDT(false);

		List<Transaction> tt = new ArrayList<>();

		FileCompare fc = new FileCompare(dbm, null, backupFolder, ex);
		Table<String, String, CompareResult> result = fc.run();

		for (String hash : result.rowKeySet()) {
			Transaction t = new Transaction();
			t.setId(hash);
			
			boolean first = true;
			
			for (String fn : result.columnKeySet()) {
				CompareResult cr = result.get(hash, fn);
				if (cr != null) {
					int s = cr.getStatus();
					
					switch (s) {
					case Status.NEW:
						File file = cr.getPath().resolve(cr.getFile().getFilename()).toFile();
						if (first) {
							UploadTask ut = new UploadTask(hash, file, cr.getFile().getFilename(), cr.getFolder());
							ut.setWeight(file.length());
							ut.setCountWeight(false);
							ut.setDescription(cr.getFolder().getAlias() + '\\' + cr.getFile().getFilename());
							ut.setPriority((short) 100);
							t.addTask(ut);
							
							first = false;
							
							if (_log.isLoggable(Level.FINE))
								_log.fine("[" + hash + "] [" + fn + "] [" + s + "] [" + orByHash(result, hash) + "] [" + orByFn(result, fn) + "] [UPLOAD] [100]");
						} else {
							InsertTask it = new InsertTask(hash, file, cr.getFile().getFilename(), cr.getFolder());
							it.setDescription(cr.getFolder().getAlias() + '\\' + cr.getFile().getFilename());
							it.setPriority((short) 50);
							t.addTask(it);
							
							if (_log.isLoggable(Level.FINE))
								_log.fine("[" + hash + "] [" + fn + "] [" + s + "] [" + orByHash(result, hash) + "] [" + orByFn(result, fn) + "] [INSERT] [50]");
						}
						
						break;
					case Status.DELETED:
						DeleteBoxTask dt = new DeleteBoxTask(cr.getFile());
						dt.setDescription(cr.getFolder().getAlias() + '\\' + cr.getFile().getFilename());
						dt.setPriority((short) 0);
						t.addTask(dt);
						
						if (_log.isLoggable(Level.FINE))
							_log.fine("[" + hash + "] [" + fn + "] [" + s + "] [" + orByHash(result, hash) + "] [" + orByFn(result, fn) + "] [DELETE] [0]");
						
						break;
					case Status.COPIED:
						File file2 = cr.getPath().resolve(cr.getFile().getFilename()).toFile();
						InsertTask it = new InsertTask(hash, file2, cr.getFile().getFilename(), cr.getFolder());
						it.setDescription(cr.getFolder().getAlias() + '\\' + cr.getFile().getFilename());
						it.setPriority((short) 50);
						t.addTask(it);
						
						if (_log.isLoggable(Level.FINE))
							_log.fine("[" + hash + "] [" + fn + "] [" + s + "] [" + orByHash(result, hash) + "] [" + orByFn(result, fn) + "] [INSERT] [50]");
					}
				}
			}
			
			if (!t.getTasks().isEmpty()) {
				tt.add(t);
				tm.addTransaction(t);
			}
		}
		
		return tt;
	}
	
	private static int orByFn(Table<String, String, CompareResult> result, String colkey) {
		int or = 0;
		Map<String, CompareResult> col = result.column(colkey);
		for (CompareResult cr : col.values())
			if (cr != null)
				or |= cr.getStatus();
		return or;
	}
	
	private static int orByHash(Table<String, String, CompareResult> result, String rowkey) {
		int or = 0;
		Map<String, CompareResult> row = result.row(rowkey);
		for (CompareResult cr : row.values())
			if (cr != null)
				or |= cr.getStatus();
		return or;
	}

	/**
	 * Delete a file from backup
	 * 
	 * @param folder
	 *            Folder where the file is
	 * @param filename
	 *            Name of the file to delete
	 * @param hash
	 *            Hash of the file to delete
	 * @param startNow
	 *            true if start the transaction, false if just create it
	 * @return The created transaction
	 * @throws SQLException
	 */
	public Transaction delete(String folder, String filename, String hash, boolean startNow) throws SQLException {
		GuiUtility.checkEDT(false);

		it.backbox.bean.File file = dbm.getFileRecord(folder, filename, hash);

		Transaction t = new Transaction();
		t.setId(file.getHash());

		DeleteBoxTask dt = new DeleteBoxTask(file);
		dt.setDescription(new StringBuilder(file.getFolderAlias()).append('\\').append(file.getFilename()).toString());

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
		GuiUtility.checkEDT(false);

		if (getConfiguration().isEmpty())
			throw new BackBoxException("Configuration not found.");

		sm = new SecurityManager(password, getConfiguration().getPwdDigest(), getConfiguration().getSalt());
		if (_log.isLoggable(Level.INFO))
			_log.info("SecurityManager init OK");

		dbm = new DBManager(DB_FILE_TEMP);
		dbm.createDB();
		if (_log.isLoggable(Level.INFO))
			_log.info("DBManager init OK");

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

			Path folder = Paths.get(f.getPath());
			Map<String, Map<String, File>> files = new HashMap<>();
			
			Files.walkFileTree(folder, new FileVisitor<Path>() {
				
				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
					return FileVisitResult.CONTINUE;
				}
				
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					String hash;
					try {
						hash = DigestUtils.sha1Hex(new BufferedInputStream(new FileInputStream(file.toFile())));
					} catch (IOException e) {
						if (_log.isLoggable(Level.WARNING)) _log.log(Level.WARNING, file.toString() + " not accessible");
						return FileVisitResult.CONTINUE;
					}
					String relativePath = folder.relativize(file).toString();
					if (files.containsKey(hash))
						files.get(hash).put(relativePath, file.toFile());
					else {
						Map<String, File> fs = new HashMap<>();
						fs.put(relativePath, file.toFile());
						files.put(hash, fs);
					}
					
					return FileVisitResult.CONTINUE;
				}
				
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					if (ex.contains(folder.relativize(dir).toString()))
						return FileVisitResult.SKIP_SUBTREE;
					return FileVisitResult.CONTINUE;
				}
				
				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					return FileVisitResult.CONTINUE;
				}
			});
			
			
			for (String hash : remoteInfo.keySet()) {
				if (_log.isLoggable(Level.INFO))
					_log.info("Restoring " + hash);
				List<Chunk> chunks = remoteInfo.get(hash);
				if (!files.containsKey(hash)) {
					bm.deleteChunk(chunks);
					if (_log.isLoggable(Level.INFO))
						_log.info("Not found locally, deleted " + hash);
					break;
				}
				Map<String, File> fileInfo = files.get(hash);
				for (String path : fileInfo.keySet()) {
					File file = fileInfo.get(path);
					if (_log.isLoggable(Level.INFO))
						_log.info("Insert " + hash + " " + path + " " + chunks.size());
					dbm.insert(file, path, f.getAlias(), hash, chunks, ISecurityManager.ENABLED_MODE,
							ICompress.UNKNOWN_MODE, (short) ((chunks.size() > 1) ? 1 : 0));
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
		GuiUtility.checkEDT(false);

		if (bm != null)
			return bm.getFreeSpace();
		throw new BackBoxException("BoxManager null");
	}
}
