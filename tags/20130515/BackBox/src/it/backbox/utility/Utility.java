package it.backbox.utility;

import it.backbox.security.DigestManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.math.RandomUtils;

public class Utility {
	
	/**
	 * Generate a random String that can be used as ID
	 * 
	 * @return The ID
	 */
	public static String genID() {
		try {
			return Hex.encodeHexString(DigestManager.hash(String.valueOf(RandomUtils.nextInt(Integer.MAX_VALUE)).getBytes()));
		} catch (NoSuchAlgorithmException | IOException e) {
			return null;
		}
	}
	
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
			delete(c);
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

	/**
	 * Write content to file
	 * 
	 * @param content
	 *            Bytes to write in file
	 * @param file
	 *            File to be written
	 * @throws IOException
	 */
	public static void write(byte[] content, File file) throws IOException {
		FileOutputStream fout = new FileOutputStream(file);
		fout.write(content);
		fout.close();
	}
	
	/**
	 * Read bytes from file
	 * 
	 * @param filename
	 *            Name of the file to read
	 * @return File content
	 * @throws IOException
	 */
	public static byte[] read(String filename) throws IOException {
		File file = new File(filename);

		byte[] content = new byte[(int) file.length()];
		
		FileInputStream fin = new FileInputStream(file);
		fin.read(content);
		fin.close();
		
		return content;
	}
	
	/**
	 * Copy file src content to file dest
	 * 
	 * @param src
	 *            Source file
	 * @param dest
	 *            Destination file
	 * @throws IOException
	 */
	public static void copy(File src, File dest) throws IOException {
		try (FileInputStream fis = new FileInputStream(src);
				FileOutputStream fos = new FileOutputStream(dest);
				FileChannel fin = fis.getChannel();
				FileChannel fout = fos.getChannel();) {
			fin.transferTo(0, fin.size(), fout);
		}
	}

}
