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
import java.util.concurrent.Callable;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.DeferredFileOutputStream;

public class DownloadTask extends BoxTask {

	private String path;
	private File file;
	
	/** Local operation variables */
	private byte[] chunkContent;
	
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
		OutputStream out = new DeferredFileOutputStream(THRESHOLD, PREFIX, SUFFIX, getTempDir());
		String filename = new StringBuilder(path).append('\\').append(file.getFilename()).toString();
		filename = FilenameUtils.separatorsToSystem(filename);
		
		if (stop) { out.close(); return; }
		
		try {
			ISplitter s = getSplitter();
			for (final Chunk chunk : file.getChunks()) {
				if (stop) return;
				callBox(new Callable<Void>() {
					
					@Override
					public Void call() throws Exception {
						chunkContent = getBoxManager().downloadChunk(chunk);
						return null;
					}
				});
				s.mergeNextChunk(new BufferedInputStream(new ByteArrayInputStream(chunkContent)), out);
			}
		} finally {
			out.close();
			chunkContent = null;
		}
		
		if (stop) return;
		
		if (file.isEncrypted()) {
			InputStream in = Utility.getInputStream((DeferredFileOutputStream) out);
			out = new DeferredFileOutputStream(THRESHOLD, PREFIX, SUFFIX, getTempDir());
			
			try {
				ISecurityManager sm = getSecurityManager();
				sm.decrypt(in, out);
			} finally {
				in.close();
				out.close();
			}
		}
		
		if (stop) return;
		
		if (file.isCompressed()) {
			InputStream in = Utility.getInputStream((DeferredFileOutputStream) out);
			out = new DeferredFileOutputStream(THRESHOLD, PREFIX, SUFFIX, getTempDir());
			
			try {
				ICompress z = new Zipper();
				z.decompress(in, out, filename.substring(filename.lastIndexOf("\\") + 1, filename.length()));
			} finally {
				in.close();
				out.close();
			}
		}
		
		if (stop) return;
		
		InputStream in = Utility.getInputStream((DeferredFileOutputStream) out);
		
		if (!file.getHash().equals(DigestUtils.sha1Hex(in)))
			throw new BackBoxException(filename + ": File integrity check failed");
		
		in = Utility.getInputStream((DeferredFileOutputStream) out);
		out = Utility.getOutputStream(filename);
		
		try {
			IOUtils.copy(in, out);
		} finally {
			in.close();
			out.close();
		}
	}

}
