package it.backbox.gui.utility;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;

import it.backbox.gui.BackBoxGui;

public class FileUtility {
	
	private static final String FOLDER_KEY = "folder";

	private static final Logger _log = Logger.getLogger(BackBoxGui.class.getCanonicalName());
	
	private static Map<String, Icon> cacheIcon = new HashMap<>();
	private static Map<String, String> cacheType = new HashMap<>();
	private static FileSystemView fsv = FileSystemView.getFileSystemView();
	private static File temp = new File(System.getProperty("java.io.tmpdir"));
	
	public static Icon getIcon(String fileExt) {
		if (cacheIcon.containsKey(fileExt))
			return cacheIcon.get(fileExt);
		
		File f = new File(temp, "icon." + fileExt);
		try {
			f.createNewFile();
			f.deleteOnExit();
			if (f.exists()) {
				Icon i = fsv.getSystemIcon(f);
				cacheIcon.put(fileExt, i);
				return i;
			}
		} catch (IOException e) {
			_log.log(Level.WARNING, "Error creating temp file for icon", e);
		}
		
		return UIManager.getIcon("FileView.fileIcon");
	}

	public static Icon getFolderIcon() {
		if (cacheIcon.containsKey(FOLDER_KEY))
			return cacheIcon.get(FOLDER_KEY);
		
		File f = new File(temp, "directory");
		f.mkdir();
		if (f.exists()) {
			f.deleteOnExit();
			Icon i = fsv.getSystemIcon(f);
			cacheIcon.put(FOLDER_KEY, i);
			return i;
		}
		
		return UIManager.getIcon("FileView.directoryIcon");
	}
	
	public static String getType(String fileExt) {
		if (cacheType.containsKey(fileExt))
			return cacheType.get(fileExt);
		
		File f = new File(temp, "type." + fileExt);
		try {
			f.createNewFile();
			f.deleteOnExit();
			if (f.exists()) {
				String i = fsv.getSystemTypeDescription(f);
				cacheType.put(fileExt, i);
				return i;
			}
		} catch (IOException e) {
			_log.log(Level.WARNING, "Error creating temp file for type description", e);
		}
		
		return "";
	}
}
