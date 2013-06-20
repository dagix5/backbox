package it.backbox;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ICompress {
	
	/**
	 * Compress a byte array content
	 * 
	 * @param src
	 *            Byte array to compress
	 * @param name
	 *            Zip entry name
	 * @return Byte array compressed
	 * @throws Exception 
	 */
	public byte[] compress(byte[] src, String name) throws Exception;

	/**
	 * Compress a file
	 * 
	 * @param filename
	 *            Name of the file to compress
	 * @param name
	 *            Zip entry name
	 * @return Compressed file content byte array
	 * @throws Exception 
	 */
	public byte[] compress(String filename, String name) throws Exception;

	/**
	 * Decompress a byte array content
	 * 
	 * @param src
	 *            Byte array to decompress
	 * @param name
	 *            Zip entry name
	 * @return Byte array decompressed
	 * @throws Exception 
	 */
	public byte[] decompress(byte[] src, String name) throws Exception;

	/**
	 * Decompress a byte array in a file
	 * 
	 * @param src
	 *            Byte array to decompress
	 * @param name
	 *            Zip entry name
	 * @param destfilename
	 *            File decompressed
	 * @throws Exception 
	 */
	public void decompress(byte[] src, String name, String destfilename) throws Exception;
	
	/**
	 * Compress an InputStream to an OutputStream
	 * 
	 * @param in
	 *            InputStream to zip
	 * @param out
	 *            OutputStream zipped
	 * @param name
	 *            Entry name
	 * @throws IOException
	 */
	public void compress(InputStream in, OutputStream out, String name) throws Exception;

}
