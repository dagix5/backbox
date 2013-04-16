package it.backbox.transaction.task;

import it.backbox.bean.File;
import it.backbox.boxcom.BoxManager;
import it.backbox.db.DBManager;

public class DeleteBoxTask extends Task {

	private boolean encryptEnabled;
	private boolean compressEnabled;
	
	private File file;
	
	public void setInput(File file) {
		this.file = file;
	}
	
	public DeleteBoxTask() {
		super();
	}
	
	public DeleteBoxTask(File file) {
		super();
		setInput(file);
	}

	@Override
	public void run() throws Exception {
		BoxManager bm = BoxManager.getInstance();
		bm.deleteChunk(file.getChunks());
		
		DBManager dbm = DBManager.getInstance();
		dbm.delete(file.getFilename(), file.getHash());
	}

	public boolean isEncryptEnabled() {
		return encryptEnabled;
	}

	public void setEncryptEnabled(boolean encryptEnabled) {
		this.encryptEnabled = encryptEnabled;
	}

	public boolean isCompressEnabled() {
		return compressEnabled;
	}

	public void setCompressEnabled(boolean compressEnabled) {
		this.compressEnabled = compressEnabled;
	}

}
