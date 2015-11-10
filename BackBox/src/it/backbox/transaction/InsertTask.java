package it.backbox.transaction;

import it.backbox.IDBManager;
import it.backbox.bean.Folder;
import it.backbox.exception.BackBoxException;

import java.io.File;

public class InsertTask extends Task {

	private String hash;
	private File file;
	private String filename;
	private String otherFilename;
	private Folder folder;
	
	public void setInput(String hash, File file, String relativePath, String otherFilename, Folder folder) {
		this.hash = hash;
		this.file = file;
		this.filename = relativePath;
		this.otherFilename = otherFilename;
		this.folder = folder;
	}
	
	public InsertTask() {
		super();
	}
	
	public InsertTask(String hash, File file, String relativePath, String otherFilename, Folder folder) {
		super();
		setInput(hash, file, relativePath, otherFilename, folder);
	}

	@Override
	public void run() throws Exception {
		IDBManager dbm = getDbManager();
		
		it.backbox.bean.File f = dbm.getFileRecord(null, otherFilename, hash);
		if (f == null)
			throw new BackBoxException("DB record not found");
		
		dbm.insert(file, filename, folder.getAlias(), hash, f.getChunks(), f.getEncrypted(), f.getCompressed(), f.getSplitted());
		
	}

}
