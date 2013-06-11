package it.backbox.transaction.task;

import it.backbox.IBoxManager;
import it.backbox.ISecurityManager;
import it.backbox.ISplitter;
import it.backbox.bean.Chunk;
import it.backbox.compress.Zipper;
import it.backbox.utility.Utility;

import java.io.File;
import java.util.List;

public class UploadTask extends BoxTask {

	private boolean encryptEnabled = true;
	private boolean compressEnabled = true;
	
	private String hash;
	private File file;
	private String relativePath;
	
	/** Local operation variables */
	private List<Chunk> splitted;
	
	public void setInput(String hash, File file, String relativePath) {
		this.hash = hash;
		this.file = file;
		this.relativePath = relativePath;
	}
	
	public UploadTask() {
		super();
	}
	
	public UploadTask(String hash, File file, String relativePath) {
		super();
		setInput(hash, file, relativePath);
	}
	
	public Long getSize() {
		if (file != null)
			return file.length();
		return null;
	}

	@Override
	public void run() throws Exception {
		ISplitter s = getSplitter();
		
		if (stop) return;
		
		byte[] data = null;
		
		if (isCompressEnabled()) {
			Zipper z = new Zipper();
			data = z.compress(file.getCanonicalPath(), null);
		}
		
		if (stop) return;
		
		if (isEncryptEnabled()) {
			ISecurityManager sm = getSecurityManager();
			byte[] encrypted = null;
			if (isCompressEnabled())			
				encrypted = sm.encrypt(data);
			else
				encrypted = sm.encrypt(file.getCanonicalPath());
			data = encrypted;
		}
		
		if (stop) return;
		
		if (data != null)
			splitted = s.splitChunk(data, hash);
		else
			splitted = s.splitChunk(file.getCanonicalPath(), hash);
		
		if (stop) return;
		
		Utility.hashChunks(splitted);
		
		if (stop) return;
		
		callBox();
		
		getDbManager().insert(file, relativePath, hash, splitted, isEncryptEnabled(), isCompressEnabled(), (splitted.size() > 1));
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
		IBoxManager bm = getBoxManager();
		bm.uploadChunk(splitted, bm.getBackBoxFolderID());
	}

}
