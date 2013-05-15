package it.backbox.transaction.task;

import it.backbox.bean.File;

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
		getBoxManager().deleteChunk(file.getChunks());
		
		getDbManager().delete(file.getFilename(), file.getHash());
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
