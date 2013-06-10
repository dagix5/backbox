package it.backbox.compare;

import it.backbox.security.DigestManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileCompare {
	private static Logger _log = Logger.getLogger(FileCompare.class.getCanonicalName());

	private Map<String, Map<String, File>> files = null;
	private Map<String, Map<String, it.backbox.bean.File>> records = null;
	private Map<String, Map<String, it.backbox.bean.File>> recordsNotInFiles = null;
	private Map<String, Map<String, File>> filesNotInRecords = null;
	
	private String root;
	private List<String> exclusions;
	
	/**
	 * Constructor
	 * 
	 * @param records
	 *            Map of files in database
	 * @param root
	 *            Root folder
	 * @param exclusions
	 *            Folders/files to exclude
	 * 
	 */
	public FileCompare(Map<String, Map<String, it.backbox.bean.File>> records, String root, List<String> exclusions) {
		this.records = records;
        
		this.root = root;
		this.exclusions = exclusions;
	}
	
	/**
	 * Walk through all files in a folder and collect their hashes
	 * 
	 * @throws IOException
	 */
	public void load() throws IOException {
		getFiles().clear();
		
		final Path rootPath = Paths.get(root);
		
		Files.walkFileTree(rootPath, new FileVisitor<Path>() {
			
			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}
			
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				String hash;
				try {
					hash = DigestManager.hash(file.toFile());
				} catch (IOException e) {
					_log.log(Level.WARNING, file.toString() + " not accessible");
					return FileVisitResult.CONTINUE;
				} catch (NoSuchAlgorithmException e) {
					_log.log(Level.SEVERE, "Error walking directory tree", e);
					return FileVisitResult.TERMINATE;
				}
				String relativePath = rootPath.relativize(file).toString();
				if (files.containsKey(hash))
					files.get(hash).put(relativePath, file.toFile());
				else {
					Map<String, File> fs = new HashMap<>();
					fs.put(relativePath, file.toFile());
					files.put(hash, fs);
				}
				
				Map<String, File> ff = getFilesNotInRecords().get(hash);
				if (ff == null) {
					ff = new HashMap<>();
					filesNotInRecords.put(hash, ff);
				}
				if (getRecords().containsKey(hash) && getRecords().get(hash).containsKey(relativePath)) {
					if (_log.isLoggable(Level.FINE)) _log.fine("Found " + relativePath);
				} else
					ff.put(relativePath, getFiles().get(hash).get(relativePath));
				
				return FileVisitResult.CONTINUE;
			}
			
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				if (exclusions.contains(rootPath.relativize(dir).toString()))
					return FileVisitResult.SKIP_SUBTREE;
				return FileVisitResult.CONTINUE;
			}
			
			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Get all records not in files
	 * 
	 * @return Map with records
	 */
	public Map<String, Map<String, it.backbox.bean.File>> getRecordsNotInFiles() {
		if (recordsNotInFiles != null)
			return recordsNotInFiles;
		recordsNotInFiles = new HashMap<>();
		for (String hash : getRecords().keySet()) {
			Map<String, it.backbox.bean.File> m = new HashMap<>();
			if (getFiles().containsKey(hash))
				for (String path : getRecords().get(hash).keySet())
					if (getFiles().get(hash).containsKey(path)) {
						if (_log.isLoggable(Level.FINE)) _log.fine("Found " + path);
					} else
						m.put(path, getRecords().get(hash).get(path));
			else
				for (String path : getRecords().get(hash).keySet())
					m.put(path, getRecords().get(hash).get(path));
			recordsNotInFiles.put(hash, m);
		}
		return recordsNotInFiles;
	}
	
	/**
	 * Get all files not in records
	 * 
	 * @return Map with files
	 */
	public Map<String, Map<String, File>> getFilesNotInRecords() {
		if (filesNotInRecords == null)
			filesNotInRecords = new HashMap<>();
		return filesNotInRecords;
	}

	/**
	 * Get Map with all files
	 * 
	 * @return Files Map
	 */
	public Map<String, Map<String, File>> getFiles() {
		if (files == null)
			files = new HashMap<>();
		return files;
	}

	/**
	 * Get Map with all records
	 * 
	 * @return Map with informations loaded from database
	 */
	public Map<String, Map<String, it.backbox.bean.File>> getRecords() {
		if (records == null)
			records = new HashMap<>();
		return records;
	}

	/**
	 * Set Map with informations loaded from database
	 * 
	 * @param records
	 *            The Map
	 */
	public void setRecords(Map<String, Map<String, it.backbox.bean.File>> records) {
		this.records = records;
	}
}
