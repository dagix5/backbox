package it.backbox.transaction.task;

import it.backbox.IBoxManager;
import it.backbox.ICompress;
import it.backbox.IDBManager;
import it.backbox.ISecurityManager;
import it.backbox.ISplitter;
import it.backbox.transaction.BBPhaser;
import it.backbox.utility.Utility;

import java.io.File;
import java.util.logging.Logger;

import com.google.common.io.Files;

public abstract class Task {
	protected static Logger _log = Logger.getLogger(Task.class.getCanonicalName());
	
	private String id;
	private String description;
	private long weight = 1;
	private boolean countWeight = true;
	protected boolean stop = false;
	private long totalTime = 0;
	
	private IBoxManager boxManager;
	private IDBManager dbManager;
	private ISplitter splitter;
	private ISecurityManager securityManager;
	private ICompress zipper;
	
	private BBPhaser phaser;
	
	protected File tempDir;
	protected int threshold = 1024*1024*100;
	protected static final String SUFFIX = ".temp";

	public Task() {
		setId(Utility.genID());
		tempDir = Files.createTempDir();
	}
	
	public IBoxManager getBoxManager() {
		return boxManager;
	}

	public void setBoxManager(IBoxManager boxManager) {
		this.boxManager = boxManager;
	}

	public IDBManager getDbManager() {
		return dbManager;
	}

	public void setDbManager(IDBManager dbManager) {
		this.dbManager = dbManager;
	}

	public ISplitter getSplitter() {
		return splitter;
	}

	public void setSplitter(ISplitter splitter) {
		this.splitter = splitter;
	}

	public ISecurityManager getSecurityManager() {
		return securityManager;
	}

	public void setSecurityManager(ISecurityManager securityManager) {
		this.securityManager = securityManager;
	}

	public ICompress getZipper() {
		return zipper;
	}

	public void setZipper(ICompress zipper) {
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
	
	public void stop() {
		stop = true;
	}

	public long getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}

	public BBPhaser getPhaser() {
		return phaser;
	}

	public void setPhaser(BBPhaser phaser) {
		this.phaser = phaser;
	}

	public abstract void run() throws Exception;
		
}
