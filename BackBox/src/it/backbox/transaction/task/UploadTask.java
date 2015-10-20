package it.backbox.transaction.task;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.DeferredFileOutputStream;
import org.apache.commons.lang.StringEscapeUtils;

import it.backbox.ICompress;
import it.backbox.ISecurityManager;
import it.backbox.ISplitter;
import it.backbox.bean.Chunk;
import it.backbox.bean.Folder;
import it.backbox.compress.Zipper;
import it.backbox.exception.BackBoxException;
import it.backbox.utility.Utility;

public class UploadTask extends BoxTask {
	
	private boolean encryptEnabled = true;
	private boolean compressEnabled = true;
	
	private String hash;
	private File file;
	private String relativePath;
	private Folder folder;
	
	/** Local operation variables */
	private List<Chunk> chunks;
	
	public void setInput(String hash, File file, String relativePath, Folder folder) {
		this.hash = hash;
		this.file = file;
		this.relativePath = relativePath;
		this.folder = folder;
	}
	
	public UploadTask() {
		super();
	}
	
	public UploadTask(String hash, File file, String relativePath, Folder folder) {
		super();
		setInput(hash, file, relativePath, folder);
	}
	
	public Long getSize() {
		if (file != null)
			return file.length();
		return null;
	}

	@Override
	public void run() throws Exception {
		InputStream in = new BufferedInputStream(new FileInputStream(file));
		DeferredFileOutputStream out = new DeferredFileOutputStream(THRESHOLD, PREFIX, SUFFIX, getTempDir());
		
		try {
			long size = file.length();
			chunks = new ArrayList<>();
			
			ISplitter s = getSplitter();
			
			if (stop) { in.close(); out.close(); return; }
			
			if (isCompressEnabled()) {
				ICompress z = new Zipper();
				String filename = file.getCanonicalPath();
				String name = filename.substring(filename.lastIndexOf("\\") + 1, filename.length());
				z.compress(in, out, name);
				
				size = out.getByteCount();
				
				in = Utility.getInputStream(out);
				out = new DeferredFileOutputStream(THRESHOLD, PREFIX, SUFFIX, getTempDir());
			}
			
			if (stop) { in.close(); out.close(); return; }
			
			if (isEncryptEnabled()) {
				ISecurityManager sm = getSecurityManager();
				sm.encrypt(in, out);
				
				size = out.getByteCount();
				
				in = Utility.getInputStream(out);
				out = new DeferredFileOutputStream(THRESHOLD, PREFIX, SUFFIX, getTempDir());
			}
			
			int totalBytesRead = 0;
			int i = 0;
			while (totalBytesRead < size) {
				if (stop) { in.close(); out.close(); return; }
				
				byte[] c = s.splitNextChunk(in, size, totalBytesRead);
				totalBytesRead += c.length;
				
				final Chunk chunk = new Chunk();
				chunk.setChunkname(Utility.buildChunkName(hash, i++));
				chunk.setContent(c);
				chunk.setChunkhash(DigestUtils.sha1Hex(c));
				chunk.setSize(c.length);
				
				callBox(new Callable<Void>() {
					
					@Override
					public Void call() throws Exception {
						getBoxManager().uploadChunk(chunk, folder.getId());
						return null;
					}
				});
				
				if ((chunk.getBoxid() == null) || chunk.getBoxid().isEmpty() || (chunk.getBoxid().equals("null")))
					throw new BackBoxException("Uploaded file ID null");
				
				chunk.setContent(null);
				chunks.add(chunk);
				
				c = null;
			}
			
			getDbManager().insert(file, relativePath, folder.getAlias(), hash, chunks, isEncryptEnabled(), isCompressEnabled(), (chunks.size() > 1));
		} finally {
			in.close();
			out.close();
		}
	}

	@Override
	public boolean rollback() {
		if ((chunks == null) || (chunks.isEmpty())) {
			if (_log.isLoggable(Level.INFO))
				_log.info("[" + getId() + "] Nothing to rollback");
			return false;
		}
		
		try {
			callBox(new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					getBoxManager().deleteChunk(chunks);
					getDbManager().delete(StringEscapeUtils.escapeSql(FilenameUtils.separatorsToWindows(relativePath)), hash);
					return null;
				}
			});
		} catch (Exception e) {
			if (_log.isLoggable(Level.WARNING))
				_log.log(Level.WARNING, "[" + getId() + "] Error rollback, deleting chunks " + file.getName());
			return false;
		}
		
		return true;
	};

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
