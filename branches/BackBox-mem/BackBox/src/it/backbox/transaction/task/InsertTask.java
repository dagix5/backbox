package it.backbox.transaction.task;

import it.backbox.IDBManager;
import it.backbox.exception.BackBoxException;

import java.io.File;

public class InsertTask extends Task {

	private String hash;
	private File file;
	private String relativePath;
	
	public void setInput(String hash, File file, String relativePath) {
		this.hash = hash;
		this.file = file;
		this.relativePath = relativePath;
	}
	
	public InsertTask() {
		super();
	}
	
	public InsertTask(String hash, File file, String relativePath) {
		super();
		setInput(hash, file, relativePath);
	}

	@Override
	public void run() throws Exception {
		IDBManager dbm = getDbManager();
		
		it.backbox.bean.File f = dbm.getFileRecord(hash);
		if (f == null)
			throw new BackBoxException("DB record not found");
		
		dbm.insert(file, relativePath, hash, f.getChunks(), f.isEncrypted(), f.isCompressed(), f.isSplitted());
		
	}

}
