package it.backbox.transaction;

import it.backbox.IBoxManager;
import it.backbox.ICompress;
import it.backbox.IDBManager;
import it.backbox.ISecurityManager;
import it.backbox.ISplitter;
import it.backbox.utility.Utility;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Task {
	protected static final Logger _log = Logger.getLogger(Task.class.getCanonicalName());

	private String id;
	private String description;
	private long weight = 1;
	private boolean countWeight = true;
	protected boolean stop = false;
	private long totalTime = 0;
	private short priority;
	
	private IBoxManager boxManager;
	private IDBManager dbManager;
	private ISplitter splitter;
	private ISecurityManager securityManager;
	private ICompress zipper;

	private File tempDir;

	private BBPhaser phaser;

	protected static final int THRESHOLD = 1024 * 1024 * 50; // Runtime.getRuntime().freeMemory() / 10
	protected static final String SUFFIX = ".temp";
	protected String PREFIX;

	public Task() {
		setId(Utility.genID());
		PREFIX = getId() + "-";
	}

	protected IBoxManager getBoxManager() {
		return boxManager;
	}

	void setBoxManager(IBoxManager boxManager) {
		this.boxManager = boxManager;
	}

	protected IDBManager getDbManager() {
		return dbManager;
	}

	void setDbManager(IDBManager dbManager) {
		this.dbManager = dbManager;
	}

	protected ISplitter getSplitter() {
		return splitter;
	}

	void setSplitter(ISplitter splitter) {
		this.splitter = splitter;
	}

	protected ISecurityManager getSecurityManager() {
		return securityManager;
	}

	void setSecurityManager(ISecurityManager securityManager) {
		this.securityManager = securityManager;
	}

	protected ICompress getZipper() {
		return zipper;
	}

	void setZipper(ICompress zipper) {
		this.zipper = zipper;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getWeight() {
		return weight;
	}

	public void setWeight(long weight) {
		this.weight = weight;
	}

	public boolean isCountWeight() {
		return countWeight;
	}

	public void setCountWeight(boolean countWeight) {
		this.countWeight = countWeight;
	}

	void stop() {
		stop = true;
	}

	public long getTotalTime() {
		return totalTime;
	}

	void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}

	protected BBPhaser getPhaser() {
		return phaser;
	}

	void setPhaser(BBPhaser phaser) {
		this.phaser = phaser;
	}

	protected File getTempDir() {
		return tempDir;
	}

	void setTempDir(File tempDir) {
		this.tempDir = tempDir;
	}
	
	public short getPriority() {
		return priority;
	}

	public void setPriority(short priority) {
		this.priority = priority;
	}

	public abstract void run() throws Exception;

	public boolean rollback() {
		if (_log.isLoggable(Level.WARNING))
			_log.warning("[" + getId() + "] Default rollback method, returning false");
		return false;
	}

}
