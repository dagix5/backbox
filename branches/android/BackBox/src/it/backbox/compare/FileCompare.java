package it.backbox.compare;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;

public class FileCompare {
	private static final Logger _log = Logger.getLogger(FileCompare.class.getCanonicalName());

	private Map<String, Map<String, File>> files = null;
	private Map<String, Map<String, it.backbox.bean.File>> records = null;
	private Map<String, Map<String, it.backbox.bean.File>> recordsNotInFiles = null;
	private Map<String, Map<String, File>> filesNotInRecords = null;
	
	private Path folder;
	private Set<String> exclusions;
	
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
	public FileCompare(Map<String, Map<String, it.backbox.bean.File>> records, Path root, Set<String> exclusions) {
		this.records = records;
        
		this.folder = root;
		this.exclusions = exclusions;
	}
	
	/**
	 * Walk through all files in a folder and collect their hashes
	 * 
	 * @throws IOException
	 */
	public void load() throws IOException {
		getFiles().clear();
		
		Files.walkFileTree(folder, new FileVisitor<Path>() {
			
			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}
			
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				String hash;
				try {
					hash = DigestUtils.sha1Hex(new BufferedInputStream(new FileInputStream(file.toFile())));
				} catch (IOException e) {
					if (_log.isLoggable(Level.WARNING)) _log.log(Level.WARNING, file.toString() + " not accessible");
					return FileVisitResult.CONTINUE;
				}
				String relativePath = folder.relativize(file).toString();
				if (files.containsKey(hash))
					files.get(hash).put(relativePath, file.toFile());
				else {
					Map<String, File> fs = new HashMap<String, File>();
					fs.put(relativePath, file.toFile());
					files.put(hash, fs);
				}
				
				Map<String, File> ff = getFilesNotInRecords().get(hash);
				if (ff == null) {
					ff = new HashMap<String, File>();
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
				if (exclusions.contains(folder.relativize(dir).toString()))
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
		recordsNotInFiles = new HashMap<String, Map<String, it.backbox.bean.File>>();
		for (String hash : getRecords().keySet()) {
			Map<String, it.backbox.bean.File> m = new HashMap<String, it.backbox.bean.File>();
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
			filesNotInRecords = new HashMap<String, Map<String, File>>();
		return filesNotInRecords;
	}

	/**
	 * Get Map with all files
	 * 
	 * @return Files Map
	 */
	public Map<String, Map<String, File>> getFiles() {
		if (files == null)
			files = new HashMap<String, Map<String, File>>();
		return files;
	}

	/**
	 * Get Map with all records
	 * 
	 * @return Map with informations loaded from database
	 */
	public Map<String, Map<String, it.backbox.bean.File>> getRecords() {
		if (records == null)
			records = new HashMap<String, Map<String, it.backbox.bean.File>>();
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
