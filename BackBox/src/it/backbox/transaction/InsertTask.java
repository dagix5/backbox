package it.backbox.transaction;

import it.backbox.IDBManager;
import it.backbox.bean.Folder;
import it.backbox.exception.BackBoxException;

import java.io.File;
import java.util.List;

public class InsertTask extends Task {

	private String hash;
	private File file;
	private String filename;
	private Folder folder;
	
	public void setInput(String hash, File file, String relativePath, Folder folder) {
		this.hash = hash;
		this.file = file;
		this.filename = relativePath;
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
		
		List<it.backbox.bean.File> files = dbm.getFiles(hash);
		if ((files == null) || files.isEmpty())
			throw new BackBoxException("DB record not found");

		it.backbox.bean.File f = files.get(0);
		dbm.insert(file, filename, folder.getAlias(), hash, f.getChunks(), f.getEncrypted(), f.getCompressed(), f.getSplitted());
		
	}

}
