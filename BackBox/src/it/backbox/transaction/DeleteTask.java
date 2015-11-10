package it.backbox.transaction;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

import it.backbox.exception.BackBoxException;

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
		Path dest = Paths.get(deletePath).resolve(Paths.get(this.src));
		if (dest == null)
			throw new BackBoxException("Delete path not found");
		
		File filedest = dest.toFile();
		if (!filedest.exists())
			filedest.mkdirs();

		Path source = Paths.get(srcFolder);
		Path srcPath = source.resolve(this.src); 
		
		Files.move(srcPath, dest, StandardCopyOption.REPLACE_EXISTING);
		
		Path path = srcPath.getParent();
		while ((path != null) && (path.getParent() != null) && !path.equals(source)) {
			File file = path.toFile();
			if ((file != null) && file.exists()) {
				String[] list = file.list();
				if ((list != null) && (list.length == 0)) {
					try {
						Files.delete(path);
					} catch (NoSuchFileException e) {
						if (_log.isLoggable(Level.WARNING)) 
								_log.log(Level.WARNING, "[" + getId() + "] Error deleting folder, file already deleted by another thread?", e);
						// file already deleted by another thread?
					}
				}
			}
			path = path.getParent();
		}
	}

}
