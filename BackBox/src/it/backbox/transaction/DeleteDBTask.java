package it.backbox.transaction;

public class DeleteDBTask extends Task {
	
	private it.backbox.bean.File file;
	
	public void setInput(it.backbox.bean.File file) {
		this.file = file;
	}
	
	public DeleteDBTask() {
		super();
	}
	
	public DeleteDBTask(it.backbox.bean.File file) {
		super();
		setInput(file);
	}

	@Override
	public void run() throws Exception {	
		getDbManager().delete(file.getFolderAlias(), file.getFilename(), file.getHash());
	}

}
