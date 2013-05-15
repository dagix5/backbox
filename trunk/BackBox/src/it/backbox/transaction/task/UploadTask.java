package it.backbox.transaction.task;

import it.backbox.bean.Chunk;
import it.backbox.boxcom.BoxManager;
import it.backbox.compress.Zipper;
import it.backbox.db.DBManager;
import it.backbox.security.SecurityManager;
import it.backbox.split.Splitter;
import it.backbox.security.DigestManager;

import java.io.File;
import java.util.ArrayList;

public class UploadTask extends Task {

	private boolean encryptEnabled = true;
	private boolean compressEnabled = true;
	
	private String hash;
	private File file;
	private int chunkSize;
	private String relativePath;
	
	public void setInput(String hash, File file, int chunkSize, String relativePath) {
		this.hash = hash;
		this.file = file;
		this.chunkSize = chunkSize;
		this.relativePath = relativePath;
	}
	
	public UploadTask() {
		super();
	}
	
	public UploadTask(String hash, File file, int chunkSize, String relativePath) {
		super();
		setInput(hash, file, chunkSize, relativePath);
	}
	
	public Long getSize() {
		if (file != null)
			return file.length();
		return null;
	}

	@Override
	public void run() throws Exception {
		DBManager dbm = DBManager.getInstance();
		Splitter s = new Splitter(chunkSize);
		
		if (stop) return;
		
		byte[] data = null;
		ArrayList<Chunk> splitted = null;
		
		if (isCompressEnabled()) {
			Zipper z = new Zipper();
			data = z.compress(file.getCanonicalPath(), null);
		}
		
		if (stop) return;
		
		if (isEncryptEnabled()) {
			SecurityManager sm = SecurityManager.getInstance();
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
		
		DigestManager.hashChunks(splitted);
		
		if (stop) return;
		
		BoxManager bm = BoxManager.getInstance();
		bm.uploadChunk(splitted, bm.getBackBoxFolderID());
		
		dbm.insert(file, relativePath, hash, splitted, isEncryptEnabled(), isCompressEnabled(), (splitted.size() > 1));
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
