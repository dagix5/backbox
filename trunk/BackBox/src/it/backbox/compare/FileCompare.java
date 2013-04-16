package it.backbox.compare;

import it.backbox.security.SecurityManager;

import java.io.File;
import java.io.IOException;
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
	private void listFiles(File file, String path, Map<String, Map<String, File>> list, List<String> exclusions) throws NoSuchAlgorithmException, IOException {
		if (!path.endsWith("\\"))
			path+="\\";
		String relativePath = "";
		
		String absolutePath = file.getCanonicalPath();
		if (path.length() < absolutePath.length())
			relativePath = absolutePath.substring(path.length());
		
		if (exclusions.contains(relativePath))
			return;
		if (!file.isDirectory()) {
			String hash = SecurityManager.hash(file);
			if (list.containsKey(hash))
				list.get(hash).put(relativePath, file);
			else {
				Map<String, File> files = new HashMap<>();
				files.put(relativePath, file);
				list.put(hash, files);
			}
			return;
		}

		File[] files = file.listFiles();
		for (File f : files)
			listFiles(f, path, list, exclusions);
	}
	
	/**
	 * Put all the files in a folder with their hash in "file" hashmap
	 * 
	 * @param file
	 *            Root folder (or file)
	 * @param exclusions
	 *            Folders/files to exclude
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public void listFiles(File file, List<String> exclusions) throws NoSuchAlgorithmException, IOException {
		getFiles().clear();
		listFiles(file, file.getCanonicalPath(), getFiles(), exclusions);
	}

	/**
	 * Get all records not in files
	 * 
	 * @return Map with records
	 */
	public Map<String, Map<String, it.backbox.bean.File>> getRecordsNotInFiles() {
		Map<String, Map<String, it.backbox.bean.File>> ret = new HashMap<>();
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
			ret.put(hash, m);
		}
		return ret;
	}
	
	/**
	 * Get all files not in records
	 * 
	 * @return Map with files
	 */
	public Map<String, Map<String, File>> getFilesNotInRecords() {
		Map<String, Map<String, File>> ret = new HashMap<>();
		for (String hash : getFiles().keySet()) {
			Map<String, File> m = new HashMap<>();
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
		return ret;
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
