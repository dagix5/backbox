package it.backbox.transaction.task;

import it.backbox.bean.File;
import it.backbox.boxcom.BoxManager;
import it.backbox.compress.Zipper;
import it.backbox.exception.BackBoxException;
import it.backbox.security.SecurityManager;
import it.backbox.split.Splitter;

import java.util.ArrayList;

public class DownloadTask extends Task {

	private String path;
	private File file;
	
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

	@Override
	public void run() throws Exception {
		byte[] data = null;
		String filename = path + "\\" + file.getFilename();
		
		BoxManager bm = BoxManager.getInstance();
		ArrayList<byte[]> chunks = bm.downloadChunk(file.getChunks());
		
		if (stop) return;
		
		Splitter s = new Splitter();
		if (!file.isEncrypted() && !file.isCompressed())
			s.merge(chunks, filename);
		else
			data = s.merge(chunks);
		
		if (stop) return;
		
		if (file.isEncrypted()) {
			SecurityManager sm = SecurityManager.getInstance();
			if (file.isCompressed()) {
				byte[] decrypted = sm.decrypt(data);
				data = decrypted;
			} else
				sm.decrypt(data, filename);
		}
		
		if (stop) return;
		
		if (file.isCompressed()) {
			Zipper z = new Zipper();
			z.decompress(data, filename.substring(filename.lastIndexOf("\\") + 1, filename.length()), filename);
		}
		
		if (stop) return;
			
		if (!SecurityManager.checkIntegrity(filename, file.getHash()))
			throw new BackBoxException(filename + ": File integrity check failed");
	}

}
