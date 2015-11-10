package it.backbox.transaction;

import it.backbox.IDBManager;
import it.backbox.bean.Folder;
import it.backbox.exception.BackBoxException;

import java.io.File;

public class InsertTask extends Task {

	private String hash;
	private File file;
	private String relativePath;
	private String otherRelativePath;
	private Folder folder;
	
	public void setInput(String hash, File file, String relativePath, String otherRelativePath, Folder folder) {
		this.hash = hash;
		this.file = file;
		this.relativePath = relativePath;
		this.otherRelativePath = otherRelativePath;
		this.folder = folder;
	}
	
	public InsertTask() {
		super();
	}
	
	public InsertTask(String hash, File file, String relativePath, String otherRelativePath, Folder folder) {
		super();
		setInput(hash, file, relativePath, otherRelativePath, folder);
	}

	@Override
	public void run() throws Exception {
		IDBManager dbm = getDbManager();
		
		it.backbox.bean.File f = dbm.getFileRecord(null, hash, otherRelativePath);
		if (f == null)
			throw new BackBoxException("DB record not found");
		
		dbm.insert(file, relativePath, folder.getAlias(), hash, f.getChunks(), f.getEncrypted(), f.getCompressed(), f.getSplitted());
		
	}

}
