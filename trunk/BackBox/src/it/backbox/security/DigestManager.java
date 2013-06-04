package it.backbox.security;

import it.backbox.bean.Chunk;
import it.backbox.exception.BackBoxException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Hex;

public class DigestManager {
	private static Logger _log = Logger.getLogger(DigestManager.class.getCanonicalName());
	
	private static final String DIGEST_ALGO = "SHA-1";
	private static final int BUFFER_LENGTH = 1024;
	
	/**
	 * Get the hash of a byte array
	 * 
	 * @param data
	 *            Byte array to hash
	 * @return Byte array of hash
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static byte[] hash(byte[] data) throws NoSuchAlgorithmException, IOException {
		MessageDigest md = MessageDigest.getInstance(DIGEST_ALGO);
		byte[] result = null;
		if (data != null) {
			md.update(data);
			result = md.digest();
		}
		return result;
	}
	
	/**
	 * Get the hash of a file
	 * 
	 * @param file
	 *            File to hash
	 * @return Hex-encoded hash
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static String hash(File file) throws NoSuchAlgorithmException, IOException {
		InputStream in = null;
		String toReturn = null;
		try {
			MessageDigest md = MessageDigest.getInstance(DIGEST_ALGO);
			byte[] result = null;
					
			in = new BufferedInputStream(new FileInputStream(file));

			byte[] buf = new byte[BUFFER_LENGTH];
			int count = in.read(buf);
			while (count >= 0) {
				md.update(buf);
				count = in.read(buf);
			}
			result = md.digest();
			
			if (result != null) {
				if (_log.isLoggable(Level.FINE))  _log.fine(file.getName() + "-> hash ok");
				toReturn =  Hex.encodeHexString(result);
			}
		} finally {
			if (in != null)
				in.close();
		}
		return toReturn;
	}
	
	/**
	 * Fill the list of chunks with the chunkfile hash
	 * 
	 * @param path
	 *            Folder where find the chunk files
	 * @param chunks
	 *            ArrayList of Chunks
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static void hashChunks(String path, ArrayList<Chunk> chunks) throws NoSuchAlgorithmException, IOException {
		for (Chunk c : chunks) {
			File cf = new File(path + "\\" + c.getChunkname());
			c.setChunkhash(hash(cf));
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
			c.setChunkhash(Hex.encodeHexString(hash(c.getContent())));
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
		String newHash = DigestManager.hash(file);
		return hash.equals(newHash);
	}
	
	/**
	 * Check the chunk file integrity
	 * 
	 * @param path
	 *            Folder where find the chunk files
	 * @param chunks
	 *            ArrayList of Chunks
	 * @throws BackBoxException 
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static void checkChunks(String path, ArrayList<Chunk> chunks) throws NoSuchAlgorithmException, IOException, BackBoxException {
		for (Chunk c : chunks) {
			if (!checkIntegrity(path + "\\" + c.getChunkname(), c.getChunkhash()))
				throw new BackBoxException("Chunk integrity check failed");
		}
	}
}
