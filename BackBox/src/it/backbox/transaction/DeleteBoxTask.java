package it.backbox.transaction;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import it.backbox.IDBManager;
import it.backbox.bean.File;
import it.backbox.exception.BackBoxException;

public class DeleteBoxTask extends BoxTask {

	private it.backbox.bean.File file;
	
	public void setInput(it.backbox.bean.File file) {
		this.file = file;
	}
	
	public DeleteBoxTask() {
		super();
	}
	
	public DeleteBoxTask(it.backbox.bean.File file) {
		super();
		setInput(file);
	}
	
	@Override
	public void run() throws Exception {
		IDBManager dbm = getDbManager();
		
		List<File> files = dbm.getFiles(file.getHash());
		if (files.size() <= 1) {
			if (_log.isLoggable(Level.INFO))
				_log.info("[" + getId() + "] Last file with hash " + file.getHash() + ". Deleting chunks...");
			
			callBox(new Callable<Void>() {
				
				@Override
				public Void call() throws Exception {
					if (file != null)
						getBoxManager().deleteChunk(file.getChunks());
					return null;
				}
			});
		}
		
		int r = dbm.delete(file.getFolderAlias(), file.getFilename(), file.getHash());
		if (r != 1)
			throw new BackBoxException("Problems deleting files from DB, delete result: " + r);
	}

}
