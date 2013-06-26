package it.backbox.utility;

import it.backbox.bean.Chunk;
import it.backbox.exception.BackBoxException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.output.DeferredFileOutputStream;
import org.apache.commons.lang.math.RandomUtils;

public class Utility {
	
	public static final int BUFFER = 4096;
	
	/**
	 * Generate a random String that can be used as ID
	 * 
	 * @return The ID
	 */
	public static String genID() {
		return DigestUtils.sha1Hex(String.valueOf(RandomUtils.nextInt(Integer.MAX_VALUE)));
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
		File file = getFileWithParents(filename);
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
		
		InputStream in = new BufferedInputStream(new FileInputStream(file));
		in.read(content);
		in.close();
		
		return content;
	}
	
	/**
	 * Create a new file with all the parent directories
	 * 
	 * @param filePath
	 *            File path to create
	 * @return The File created
	 * @throws IOException
	 */
	public static File getFileWithParents(String filePath) throws IOException {
		File file = new File(filePath);
		File parent = file.getParentFile();
		if((parent != null) && !parent.exists() && !parent.mkdirs()){
		    throw new IllegalStateException("Couldn't create dir: " + parent);
		}
		file.createNewFile();
		
		return file;
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
	
	/**
	 * Fill the list of chunks with the chunkfile hash
	 * 
	 * @param chunks
	 *            ArrayList of Chunks
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static void hashChunks(List<Chunk> chunks) throws NoSuchAlgorithmException, IOException {
		for (Chunk c : chunks)
			c.setChunkhash(DigestUtils.sha1Hex(c.getContent()));
	}

	/**
	 * Check the file integrity, comparing its digest with that one passed as
	 * parameter
	 * 
	 * @param filename
	 *            Name of the file to check
	 * @param hash
	 *            Hash to check
	 * @return true if the file is intact, false otherwise
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws BackBoxException 
	 */
	public static boolean checkIntegrity(String filename, String hash) throws NoSuchAlgorithmException, IOException, BackBoxException {
		File file = new File(filename);
		if (!file.exists())
			throw new BackBoxException("File not found");
		String newHash = DigestUtils.sha1Hex(new BufferedInputStream(new FileInputStream(file)));
		return hash.equals(newHash);
	}
	
	/**
	 * Get the chunk names
	 * 
	 * @param chunkprefix
	 *            Prefix of chunk names
	 * @param index
	 *            Chunk index
	 * @return The chunk name
	 */
	public static String buildChunkName(String chunkprefix, int index) {
		StringBuilder name = new StringBuilder(chunkprefix);
		name.append(it.backbox.bean.File.EXT);
		if (index < 10)
			name.append(0);
		name.append(index);
		return name.toString();
	}
	
	/**
	 * Build a string with memory status informations
	 * 
	 * @return Memory status informations
	 */
	public static String getMemoryStats() {
		int mb = 1024*1024;
        
        //Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();
         
        StringBuilder s = new StringBuilder("##### Heap utilization statistics [MB] #####");
         
        //Print used memory
        s.append("\nUsed Memory: ").append((runtime.totalMemory() - runtime.freeMemory()) / mb);
 
        //Print free memory
        s.append("\nFree Memory: ").append(runtime.freeMemory() / mb);
         
        //Print total available memory
        s.append("\nTotal Memory: ").append(runtime.totalMemory() / mb);
 
        //Print Maximum available memory
        s.append("\nMax Memory: ").append(runtime.maxMemory() / mb);
        
        return s.toString();
	}
	
	/**
	 * Return the InputStream of data written in a DeferredFileOutputStream. You
	 * haven't to be aware if the data are in a file or in a byte arrray.
	 * 
	 * @param out
	 *            The DeferredFileOutputStream
	 * @return Data InputStream
	 * @throws FileNotFoundException
	 */
	public static InputStream getInputStream(DeferredFileOutputStream out)
			throws FileNotFoundException {
		if (out.isInMemory())
			return new BufferedInputStream(new ByteArrayInputStream(out.getData()));
		else
			return new BufferedInputStream(new FileInputStream(out.getFile()));
	}

}
