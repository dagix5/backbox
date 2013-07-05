package it.backbox.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.google.api.client.util.Key;

public class Configuration {

	@Key
	private String pwdDigest;
	@Key
	private String salt;

	@Key
	private int defaultUploadSpeed;
	@Key
	private int chunkSize;
	@Key
	private String rootFolderID;
	@Key
	private Level logLevel;
	
	@Key
	private List<Folder> backupFolders;
	@Key
	private ProxyConfiguration proxyConfiguration;

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
			backupFolders = new ArrayList<>();
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

	public Level getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(Level logLevel) {
		this.logLevel = logLevel;
	}

}
