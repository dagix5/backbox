package it.backbox.transaction.task;

import it.backbox.ICompress;
import it.backbox.ISecurityManager;
import it.backbox.ISplitter;
import it.backbox.bean.File;
import it.backbox.compress.Zipper;
import it.backbox.exception.BackBoxException;
import it.backbox.utility.Utility;

import java.util.List;

public class DownloadTask extends BoxTask {

	private String path;
	private File file;
	
	/** Local operation variables */
	private List<byte[]> chunks;
	
	public void setInput(String path, File file) {
		this.path = path;
		this.file = file;
	}
	
	public DownloadTask() {
		super();
	}
	
	public DownloadTask(String path, File file) {
		super();
		setInput(path, file);
	}

	public Long getSize() {
		if (file != null)
			return file.getSize();
		return null;
	}
	
	@Override
	public void run() throws Exception {
		byte[] data = null;
		String filename = new StringBuilder(path).append('\\').append(file.getFilename()).toString();
		
		callBox();
		
		if (stop) return;
		
		ISplitter s = getSplitter();
		if (!file.isEncrypted() && !file.isCompressed())
			s.merge(chunks, filename);
		else
			data = s.merge(chunks);
		
		if (stop) return;
		
		if (file.isEncrypted()) {
			ISecurityManager sm = getSecurityManager();
			if (file.isCompressed()) {
				byte[] decrypted = sm.decrypt(data);
				data = decrypted;
			} else
				sm.decrypt(data, filename);
		}
		
		if (stop) return;
		
		if (file.isCompressed()) {
			ICompress z = new Zipper();
			z.decompress(data, filename.substring(filename.lastIndexOf("\\") + 1, filename.length()), filename);
		}
		
		if (stop) return;
			
		if (!Utility.checkIntegrity(filename, file.getHash()))
			throw new BackBoxException(filename + ": File integrity check failed");
	}

	@Override
	protected void boxMethod() throws Exception {
		chunks = getBoxManager().downloadChunk(file.getChunks());
	}

}
