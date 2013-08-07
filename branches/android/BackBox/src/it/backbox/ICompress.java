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
	 * @throws IOException 
	 */
	public byte[] compress(byte[] src, String name) throws IOException;

	/**
	 * Compress a file
	 * 
	 * @param filename
	 *            Name of the file to compress
	 * @param name
	 *            Zip entry name
	 * @return Compressed file content byte array
	 * @throws IOException 
	 */
	public byte[] compress(String filename, String name) throws IOException;

	/**
	 * Decompress a byte array content
	 * 
	 * @param src
	 *            Byte array to decompress
	 * @param name
	 *            Zip entry name
	 * @return Byte array decompressed
	 * @throws IOException 
	 */
	public byte[] decompress(byte[] src, String name) throws IOException;

	/**
	 * Decompress a byte array in a file
	 * 
	 * @param src
	 *            Byte array to decompress
	 * @param name
	 *            Zip entry name
	 * @param destfilename
	 *            File decompressed
	 * @throws IOException 
	 */
	public void decompress(byte[] src, String name, String destfilename) throws IOException;
	
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
	public void compress(InputStream in, OutputStream out, String name) throws IOException;
	
	/**
	 * UnZip an InputStream to an OutputStream
	 * 
	 * @param in
	 *            InputStream to unzip
	 * @param out
	 *            OutputStream unzipped
	 * @param name
	 *            Zip entry name
	 * @throws IOException
	 */
	public void decompress(InputStream in, OutputStream out, String name) throws IOException;

}
