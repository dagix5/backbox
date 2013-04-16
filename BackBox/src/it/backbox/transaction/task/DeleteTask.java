package it.backbox.transaction.task;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class DeleteTask extends Task {

	private String srcFolder;
	private String deletePath;
	private String src;
	
	public void setInput(String srcFolder, String src, String deletePath) {
		this.srcFolder = srcFolder;
		this.deletePath = deletePath;
		this.src = src;
	}
	
	public DeleteTask() {
		super();
	}
	
	public DeleteTask(String srcFolder, String src, String deletePath) {
		super();
		setInput(srcFolder, src, deletePath);
	}

	@Override
	public void run() throws Exception {
		Path source = Paths.get(srcFolder);
		Path src = Paths.get(this.src);
		Path dest = Paths.get(deletePath);
		Files.move(source.resolve(this.src), dest.resolve(src), StandardCopyOption.REPLACE_EXISTING);
	}

}
