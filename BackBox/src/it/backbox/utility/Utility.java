package it.backbox.utility;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Utility {
	
	/**
	 * Clean temp folder
	 * 
	 * @param folder
	 *            Folder to clean
	 */
	public static void cleanFolder(String folder) {
		File temp = new File(folder);
		File[] files = temp.listFiles();
		if (files == null)
			return;
		for (File c : files)
			Utility.delete(c);
	}
	
	/**
	 * Delete all the file in a directory
	 * 
	 * @param f
	 *            Root folder (or file)
	 */
	private static void delete(File f) {
		if (f.isDirectory())
			for (File s : f.listFiles())
				delete(s);
		else
			f.delete();
	}
	
	/**
	 * Get the output stream of a file, even if the parent folders don't exist
	 * 
	 * @param filename
	 *            The name of the file
	 * @return The output stream of the file
	 * @throws IOException
	 */
	public static OutputStream getOutputStream(String filename) throws IOException {
		File file = new File(filename);
		File parent = file.getParentFile();
		if((parent != null) && !parent.exists() && !parent.mkdirs()){
		    throw new IllegalStateException("Couldn't create dir: " + parent);
		}
		file.createNewFile();
		OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
		return out;
	}
	
	/**
	 * Get an human readable string to represent bytes
	 * 
	 * @param bytes
	 *            The bytes
	 * @param si
	 *            true if 1 KB = 1000 B, false if 1 KB = 1024 B
	 * @return The human readable string to represent bytes
	 */
	public static String humanReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
	public static void write(byte[] content, String filename) throws IOException {
		File file = new File(filename);
		file.createNewFile();

		FileOutputStream fout = new FileOutputStream(file);
		fout.write(content);
		fout.close();
	}
	
	public static void write(byte[] content, File file) throws IOException {
		FileOutputStream fout = new FileOutputStream(file);
		fout.write(content);
		fout.close();
	}
	
	public static byte[] read(String filename) throws IOException {
		File file = new File(filename);

		byte[] content = new byte[(int) file.length()];
		
		FileInputStream fin = new FileInputStream(file);
		fin.read(content);
		fin.close();
		
		return content;
	}
}
