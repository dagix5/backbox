package it.backbox.transaction;

import java.util.concurrent.Callable;

import it.backbox.bean.Chunk;

public class DeleteRemoteTask extends BoxTask {

	private it.backbox.bean.File file;
	private Chunk chunk;
	
	public void setInput(it.backbox.bean.File file) {
		this.file = file;
	}
	
	public void setInput(Chunk chunk) {
		this.chunk = chunk;
	}
	
	public DeleteRemoteTask() {
		super();
	}
	
	public DeleteRemoteTask(it.backbox.bean.File file) {
		super();
		setInput(file);
	}
	
	public DeleteRemoteTask(Chunk chunk) {
		super();
		setInput(chunk);
	}

	@Override
	public void run() throws Exception {
		callBox(new Callable<Void>() {
			
			@Override
			public Void call() throws Exception {
				if (file != null)
					getBoxManager().deleteChunk(file.getChunks());
				if (chunk != null)
					getBoxManager().delete(chunk.getBoxid());
				return null;
			}
		});
		
		if (file != null)
			getDbManager().delete(file.getFolderAlias(), file.getFilename(), file.getHash());
	}

}
