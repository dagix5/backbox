package it.backbox.bean;

import java.util.ArrayList;
import java.util.List;

import com.google.api.client.util.Key;

public class Configuration {

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
	private String logLevel;
	@Key
	private int logSize;

	@Key
	private List<Folder> backupFolders;
	@Key
	private ProxyConfiguration proxyConfiguration;
	
	@Key
	private boolean autoUploadConf;
	@Key
	private String dbFileID;
	@Key
	private String confFileID;

	public String getPwdDigest() {
		return pwdDigest;
	}

	public void setPwdDigest(String pwdDigest) {
		this.pwdDigest = pwdDigest;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public int getDefaultUploadSpeed() {
		return defaultUploadSpeed;
	}

	public void setDefaultUploadSpeed(int defaultUploadSpeed) {
		this.defaultUploadSpeed = defaultUploadSpeed;
	}

	public int getDefaultDownloadSpeed() {
		return defaultDownloadSpeed;
	}

	public void setDefaultDownloadSpeed(int defaultDownloadSpeed) {
		this.defaultDownloadSpeed = defaultDownloadSpeed;
	}

	public int getChunkSize() {
		return chunkSize;
	}

	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}

	public String getRootFolderID() {
		return rootFolderID;
	}

	public void setRootFolderID(String rootFolderID) {
		this.rootFolderID = rootFolderID;
	}

	public List<Folder> getBackupFolders() {
		if (backupFolders == null)
			backupFolders = new ArrayList<Folder>();
		return backupFolders;
	}

	public void setBackupFolders(List<Folder> backupFolders) {
		this.backupFolders = backupFolders;
	}

	public ProxyConfiguration getProxyConfiguration() {
		if (proxyConfiguration == null)
			proxyConfiguration = new ProxyConfiguration(false, null, 0);
		return proxyConfiguration;
	}

	public void setProxyConfiguration(ProxyConfiguration proxyConfiguration) {
		this.proxyConfiguration = proxyConfiguration;
	}

	public boolean isEmpty() {
		return (pwdDigest == null) || pwdDigest.isEmpty();
	}

	public String getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}

	public int getLogSize() {
		return logSize;
	}

	public void setLogSize(int logSize) {
		this.logSize = logSize;
	}

	public boolean isAutoUploadConf() {
		return autoUploadConf;
	}

	public void setAutoUploadConf(boolean autoUploadConf) {
		this.autoUploadConf = autoUploadConf;
	}

	public String getDbFileID() {
		return dbFileID;
	}

	public void setDbFileID(String dbFileID) {
		this.dbFileID = dbFileID;
	}

	public String getConfFileID() {
		return confFileID;
	}

	public void setConfFileID(String confFileID) {
		this.confFileID = confFileID;
	}

}
