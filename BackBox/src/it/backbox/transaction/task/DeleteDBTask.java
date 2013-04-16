package it.backbox.transaction.task;

import it.backbox.bean.File;
import it.backbox.db.DBManager;

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
		DBManager dbm = DBManager.getInstance();
		dbm.delete(file.getFilename(), file.getHash());
	}

}
