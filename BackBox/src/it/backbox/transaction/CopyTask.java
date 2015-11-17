package it.backbox.transaction;

import it.backbox.utility.Utility;

import java.io.File;

public class CopyTask extends Task {

	private String srcPath;
	private String destPath;
	
	public void setInput(String srcPath, String destPath) {
		this.srcPath = srcPath;
		this.destPath = destPath;
	}
	
	public CopyTask() {
		super();
	}
	
	public CopyTask(String srcPath, String destPath) {
		super();
		setInput(srcPath, destPath);
	}

	@Override
	public void run() throws Exception {
		File dest = new File(destPath);
		File parent = dest.getParentFile();
		if (parent == null)
		    throw new IllegalStateException("Couldn't create dir: " + dest);
		if (!parent.exists())
			parent.mkdirs();
		dest.createNewFile();
		Utility.copy(new File(srcPath), dest);
	}

}
