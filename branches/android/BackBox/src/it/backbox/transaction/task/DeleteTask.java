package it.backbox.transaction.task;

import java.io.File;
import java.util.logging.Level;

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
//		Path source = Paths.get(srcFolder);
//		Path src = Paths.get(this.src);
//		Path dest = Paths.get(deletePath).resolve(src);
//		if (dest != null) {
//			File filedest = dest.toFile();
//			if (!filedest.exists())
//				filedest.mkdirs();
//		}
//		Path srcPath = source.resolve(this.src); 
//		
//		Files.move(srcPath, dest, StandardCopyOption.REPLACE_EXISTING);
//		
//		Path path = srcPath.getParent();
//		while ((path.getParent() != null) && !path.equals(source)) {
//			File file = path.toFile();
//			if (file.exists() && (file.list().length == 0)) {
//				try {
//					Files.delete(path);
//				} catch (NoSuchFileException e) {
//					_log.log(Level.FINE, "Error deleting folder, file already deleted by another thread?",  e);
//					//file already deleted by another thread?
//				}
//			}
//			path = path.getParent();
//		}
	}

}
