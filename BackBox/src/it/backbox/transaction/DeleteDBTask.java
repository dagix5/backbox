package it.backbox.transaction;

import it.backbox.bean.File;

public class DeleteDBTask extends Task {
	
	private File file;
	
	public void setInput(File file) {
		this.file = file;
	}
	
	public DeleteDBTask() {
		super();
	}
	
	public DeleteDBTask(File file) {
		super();
		setInput(file);
	}

	@Override
	public void run() throws Exception {	
		getDbManager().delete(file.getFolder(), file.getFilename(), file.getHash());
	}

}
