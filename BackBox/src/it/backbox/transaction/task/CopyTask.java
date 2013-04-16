package it.backbox.transaction.task;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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
		Path source = Paths.get(srcPath);
		Path dest = Paths.get(destPath);
		Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
	}

}
