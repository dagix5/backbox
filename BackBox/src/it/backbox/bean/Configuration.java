package it.backbox.bean;

import java.util.ArrayList;
import java.util.List;

import com.google.api.client.util.Key;

public class Configuration {
	
	public static final String DEFAULT_DB_FILENAME = "backbox.db";

	@Key
	private String pwdDigest;
	@Key
	private String salt;

	@Key
	private int defaultUploadSpeed;
	@Key
	private int defaultDownloadSpeed;
	@Key
	private int chunkSize;
	@Key
	private String rootFolderID;

	@Key
	private List<Folder> backupFolders;
	@Key
	private ProxyConfiguration proxyConfiguration;
	
	@Key
	private boolean autoUploadConf;
	@Key
	private String dbFileID;
	@Key
	private String dbFilename;
	@Key
	private String confFileID;
	
	private boolean modified = false;

	public String getPwdDigest() {
		return pwdDigest;
	}

	public void setPwdDigest(String pwdDigest) {
		this.pwdDigest = pwdDigest;
		modified = true;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
		modified = true;
	}

	public int getDefaultUploadSpeed() {
		return defaultUploadSpeed;
	}

	public void setDefaultUploadSpeed(int defaultUploadSpeed) {
		this.defaultUploadSpeed = defaultUploadSpeed;
		modified = true;
	}

	public int getDefaultDownloadSpeed() {
		return defaultDownloadSpeed;
	}

	public void setDefaultDownloadSpeed(int defaultDownloadSpeed) {
		this.defaultDownloadSpeed = defaultDownloadSpeed;
		modified = true;
	}

	public int getChunkSize() {
		return chunkSize;
	}

	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
		modified = true;
	}

	public String getRootFolderID() {
		return rootFolderID;
	}

	public void setRootFolderID(String rootFolderID) {
		this.rootFolderID = rootFolderID;
		modified = true;
	}

	public List<Folder> getBackupFolders() {
		if (backupFolders == null)
			backupFolders = new ArrayList<>();
		return backupFolders;
	}

	public void setBackupFolders(List<Folder> backupFolders) {
		this.backupFolders = backupFolders;
		modified = true;
	}

	public ProxyConfiguration getProxyConfiguration() {
		if (proxyConfiguration == null)
			proxyConfiguration = new ProxyConfiguration(false, null, 0);
		return proxyConfiguration;
	}

	public void setProxyConfiguration(ProxyConfiguration proxyConfiguration) {
		this.proxyConfiguration = proxyConfiguration;
		modified = true;
	}

	public boolean isEmpty() {
		return (pwdDigest == null) || pwdDigest.isEmpty();
	}

	public boolean isAutoUploadConf() {
		return autoUploadConf;
	}

	public void setAutoUploadConf(boolean autoUploadConf) {
		this.autoUploadConf = autoUploadConf;
		modified = true;
	}

	public String getDbFileID() {
		return dbFileID;
	}

	public void setDbFileID(String dbFileID) {
		this.dbFileID = dbFileID;
		modified = true;
	}

	public String getConfFileID() {
		return confFileID;
	}

	public void setConfFileID(String confFileID) {
		this.confFileID = confFileID;
		modified = true;
	}

	public boolean isModified() {
		return modified;
	}

	public String getDbFilename() {
		if ((dbFilename == null) || dbFilename.isEmpty())
			dbFilename = DEFAULT_DB_FILENAME;
		return dbFilename;
	}

	public void setDbFilename(String dbFilename) {
		this.dbFilename = dbFilename;
	}

}
