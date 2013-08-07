package it.backbox.compare;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
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
	
	private File folder;
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
	public FileCompare(Map<String, Map<String, it.backbox.bean.File>> records, File root, Set<String> exclusions) {
		this.records = records;
        
		this.folder = root;
		this.exclusions = exclusions;
	}
	
	/**
	 * Get all the files in a folder with their hash
	 * 
	 * @param file
	 *            Root folder (or file)
	 * @param path
	 *            Root path
	 * @param list
	 *            Output map <hash, Map<canonicalPath, java.io.File>>
	 * @param exclusions
	 *            Folders/files to exclude
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	private void listFiles(File file, String path, Map<String, Map<String, File>> list, Set<String> exclusions) throws IOException {
		if (!path.endsWith("\\"))
			path+="\\";
		String relativePath = "";
		
		String absolutePath = file.getCanonicalPath();
		if (path.length() < absolutePath.length())
			relativePath = absolutePath.substring(path.length());
		
		if (exclusions.contains(relativePath))
			return;
		if (!file.isDirectory()) {
			String hash;
			try {
				hash = DigestUtils.sha1Hex(new BufferedInputStream(new FileInputStream(file)));
			} catch (IOException e) {
				_log.log(Level.WARNING, file.toString() + " not accessible");
				return;
			}
			if (list.containsKey(hash))
				list.get(hash).put(relativePath, file);
			else {
				Map<String, File> files = new HashMap<String, File>();
				files.put(relativePath, file);
				list.put(hash, files);
			}
			return;
		}

		File[] files = file.listFiles();
		if (files != null)
			for (File f : files)
				listFiles(f, path, list, exclusions);
	}
	
	/**
	 * Walk through all files in a folder and collect their hashes
	 * 
	 * @throws IOException
	 */
	public void load() throws IOException {
		getFiles().clear();
		listFiles(folder, folder.getCanonicalPath(), getFiles(), exclusions);
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
		if (filesNotInRecords != null)
			return filesNotInRecords;
		filesNotInRecords = new HashMap<String, Map<String, File>>();
		Map<String, Map<String, File>> ret = new HashMap<String, Map<String, File>>();
		for (String hash : getFiles().keySet()) {
			Map<String, File> m = new HashMap<String, File>();
			if (getRecords().containsKey(hash))
				for (String path : getFiles().get(hash).keySet())
					if (getRecords().get(hash).containsKey(path)) {
						if (_log.isLoggable(Level.FINE)) _log.fine("Found " + path);
					} else
						m.put(path, getFiles().get(hash).get(path));
			else
				for (String path : getFiles().get(hash).keySet())
					m.put(path, getFiles().get(hash).get(path));
			ret.put(hash, m);
		}
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
