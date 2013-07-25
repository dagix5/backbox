package it.backbox.transaction.task;

import it.backbox.ICompress;
import it.backbox.ISecurityManager;
import it.backbox.ISplitter;
import it.backbox.bean.Chunk;
import it.backbox.bean.File;
import it.backbox.compress.Zipper;
import it.backbox.exception.BackBoxException;
import it.backbox.utility.Utility;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.output.DeferredFileOutputStream;

public class DownloadTask extends BoxTask {

	private String path;
	private File file;
	
	/** Local operation variables */
	private byte[] chunkContent;
	private Chunk chunk;
	
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
		ISplitter s = getSplitter();
		
		String filename = new StringBuilder(path).append('\\').append(file.getFilename()).toString();
		
		OutputStream out;
		
		if (file.isCompressed() || file.isEncrypted())
			out = new DeferredFileOutputStream(THRESHOLD, PREFIX, SUFFIX, getTempDir());
		else
			out = Utility.getOutputStream(filename);
		
		if (stop) { out.close(); return; }
		
		try {
			for (Chunk c : file.getChunks()) {
				if (stop) return;
				chunk = c;
				callBox();
				s.mergeNextChunk(new BufferedInputStream(new ByteArrayInputStream(chunkContent)), out);
			}
		} finally {
			out.close();
		}
		
		if (stop) return;
		
		if (file.isEncrypted()) {
			InputStream in = Utility.getInputStream((DeferredFileOutputStream) out);
			
			if (file.isCompressed())
				out = new DeferredFileOutputStream(THRESHOLD, PREFIX, SUFFIX, getTempDir());
			else
				out = Utility.getOutputStream(filename);
			
			ISecurityManager sm = getSecurityManager();
			sm.decrypt(in, out);
		}
		
		if (stop) return;
		
		if (file.isCompressed()) {
			InputStream in = Utility.getInputStream((DeferredFileOutputStream) out);
			
			ICompress z = new Zipper();
			z.decompress(in, Utility.getOutputStream(filename), filename.substring(filename.lastIndexOf("\\") + 1, filename.length()));
		}
		
		if (stop) return;
			
		if (!Utility.checkIntegrity(filename, file.getHash()))
			throw new BackBoxException(filename + ": File integrity check failed");
	}

	@Override
	protected void boxMethod() throws Exception {
		chunkContent = getBoxManager().downloadChunk(chunk);
	}

}
