package it.backbox.transaction.task;

import it.backbox.bean.File;

public class DeleteBoxTask extends BoxTask {

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
		callBox();
		
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

	@Override
	protected void boxMethod() throws Exception {
		getBoxManager().deleteChunk(file.getChunks());
	}

}
