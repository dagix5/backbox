package it.backbox.transaction;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.DeferredFileOutputStream;

import it.backbox.ICompress;
import it.backbox.ISecurityManager;
import it.backbox.ISplitter;
import it.backbox.bean.Chunk;
import it.backbox.compress.GZipper;
import it.backbox.compress.Zipper;
import it.backbox.exception.BackBoxException;
import it.backbox.utility.Utility;

public class DownloadTask extends BoxTask {

	private String path;
	private it.backbox.bean.File file;
	
	/** Local operation variables */
	private byte[] chunkContent;
	
	public void setInput(String path, it.backbox.bean.File file) {
		this.path = path;
		this.file = file;
	}
	
	public DownloadTask() {
		super();
	}
	
	public DownloadTask(String path, it.backbox.bean.File file) {
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
		String filename = FilenameUtils.separatorsToSystem(path + File.separatorChar + file.getFilename());
		
		if (stop) { out.close(); return; }
		
		try {
			ISplitter s = getSplitter();
			boolean secondChance = false;
			for (int i = 0; i < file.getChunks().size();) {
				final Chunk chunk = file.getChunks().get(i);
				if (stop) return;
				try {
					callBox(new Callable<Void>() {
						
						@Override
						public Void call() throws Exception {
							chunkContent = getBoxManager().downloadChunk(chunk);
							return null;
						}
					});
					s.mergeNextChunk(new BufferedInputStream(new ByteArrayInputStream(chunkContent)), out);
					i++;
					secondChance = false;
				} catch (Exception e) {
					if (secondChance)
						throw e;
					_log.warning("[" + getId() +"] Second chance for " + file.getFilename());
					secondChance = true;
				}
			}
		} finally {
			out.close();
			chunkContent = null;
		}
		
		if (stop) return;
		
		if (file.getEncrypted() == ISecurityManager.ENABLED_MODE) {
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

		InputStream in = Utility.getInputStream((DeferredFileOutputStream) out);
		if (file.getCompressed() == ICompress.UNKNOWN_MODE)
			file.setCompressed(Utility.getCompressMode(in));
		
		if (file.getCompressed() != ICompress.DISABLED_MODE) {
			out = new DeferredFileOutputStream(THRESHOLD, PREFIX, SUFFIX, getTempDir());
			ICompress z;
			if (file.getCompressed() == ICompress.ZIP_MODE)
				z = new Zipper();
			else
				z = new GZipper();
			try {
				z.decompress(in, out, FilenameUtils.getName(filename));
			} finally {
				in.close();
				out.close();
			}
			
			in = Utility.getInputStream((DeferredFileOutputStream) out);
		}
		
		if (stop) return;
		
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
		
		if (file.getTimestamp() != null) {
			File df = new File(filename);
			df.setLastModified(file.getTimestamp().getTime());
		}
	}

}
