package it.backbox.transaction.task;

import it.backbox.IDBManager;
import it.backbox.bean.Folder;
import it.backbox.exception.BackBoxException;

import java.io.File;

public class InsertTask extends Task {

	private String hash;
	private File file;
	private String relativePath;
	private Folder folder;
	
	public void setInput(String hash, File file, String relativePath, Folder folder) {
		this.hash = hash;
		this.file = file;
		this.relativePath = relativePath;
		this.folder = folder;
	}
	
	public InsertTask() {
		super();
	}
	
	public InsertTask(String hash, File file, String relativePath, Folder folder) {
		super();
		setInput(hash, file, relativePath, folder);
	}

	@Override
	public void run() throws Exception {
		IDBManager dbm = getDbManager();
		
		it.backbox.bean.File f = dbm.getFileRecord(hash);
		if (f == null)
			throw new BackBoxException("DB record not found");
		
		dbm.insert(file, relativePath, folder.getAlias(), hash, f.getChunks(), f.isEncrypted(), f.isCompressed(), f.isSplitted());
		
	}

}
