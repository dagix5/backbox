package it.backbox.transaction.task;

import it.backbox.ICompress;
import it.backbox.ISecurityManager;
import it.backbox.ISplitter;
import it.backbox.bean.Chunk;
import it.backbox.bean.File;
import it.backbox.compress.Zipper;
import it.backbox.exception.BackBoxException;
import it.backbox.utility.Utility;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.output.DeferredFileOutputStream;

public class DownloadTask extends BoxTask {

	private static final String PREFIX = "BB_DOWNLOAD-";
	private static final String SUFFIX = ".temp";
	
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
		
		int threshold = 1024*1024*100;
		OutputStream out;
		DeferredFileOutputStream dfout = null;
		OutputStream fout = Utility.getOutputStream(filename);
		
		if (file.isCompressed() || file.isEncrypted()) {
			dfout = new DeferredFileOutputStream(threshold, PREFIX, SUFFIX, tempDir);
			out = dfout;
		} else
			out = fout;
		
		if (stop) { out.close(); return; }
		
		for (Chunk c : file.getChunks()) {
			if (stop) return;
			chunk = c;
			callBox();
			s.mergeNextChunk(new ByteArrayInputStream(chunkContent), out);
		}
		
		if (stop) { out.close(); return; }
		
		if (file.isEncrypted()) {
			InputStream in = Utility.getInputStream(dfout);
			
			if (file.isCompressed() || file.isEncrypted()) {
				dfout = new DeferredFileOutputStream(threshold, PREFIX, SUFFIX, tempDir);
				out = dfout;
			} else
				out = fout;
			
			ISecurityManager sm = getSecurityManager();
			sm.decrypt(in, out);
		}
		
		if (stop) return;
		
		if (file.isCompressed()) {
			InputStream in = Utility.getInputStream(dfout);
			
			ICompress z = new Zipper();
			z.decompress(in, fout, filename.substring(filename.lastIndexOf("\\") + 1, filename.length()));
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
