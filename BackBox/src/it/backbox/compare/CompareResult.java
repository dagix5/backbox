package it.backbox.compare;

import java.nio.file.Path;

import it.backbox.bean.File;
import it.backbox.bean.Folder;

public class CompareResult {

	private it.backbox.bean.File file;
	private int status;
	private Path path;
	private Folder folder;

	public CompareResult() {
	
	}
	
	public CompareResult(File file, int status) {
		this.file = file;
		this.status = status;
	}

	public it.backbox.bean.File getFile() {
		return file;
	}

	public void setFile(it.backbox.bean.File file) {
		this.file = file;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	public Folder getFolder() {
		return folder;
	}

	public void setFolder(Folder folder) {
		this.folder = folder;
	}

	public static final class Status {
		public static final int FOUND = 0b0000;
		public static final int NEW = 0b001;
		public static final int DELETED = 0b010;
		public static final int COPIED = 0b100;
	}
}
