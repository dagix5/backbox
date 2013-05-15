package it.backbox;

public interface IZipper {
	
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
	 * Compress a file
	 * 
	 * @param srcfilename
	 *            File to compress
	 * @param destfilename
	 *            File compressed
	 * @param name
	 *            Zip entry name
	 * @throws Exception 
	 */
	public void compress(String srcfilename, String destfilename, String name) throws Exception;

	/**
	 * Compress a byte array content in a file
	 * 
	 * @param src
	 *            Byte array to compress
	 * @param name
	 *            Zip entry name
	 * @param destfilename
	 *            File compressed
	 * @throws Exception 
	 */
	public void compress(byte[] src, String name, String destfilename) throws Exception;

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
	 * Decompress a file
	 * 
	 * @param filename
	 *            Name of the file to decompress
	 * @param name
	 *            Zip entry name            
	 * @return Decompressed file content byte array
	 * @throws Exception 
	 */
	public byte[] decompress(String filename, String name) throws Exception;

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
	 * Decompress a file
	 * 
	 * @param srcfilename
	 *            File to decompress
	 * @param destfilenam
	 *            File decompressed
	 * @param name
	 *            Zip entry name
	 * @throws Exception 
	 */
	public void decompress(String srcfilename, String destfilename, String name) throws Exception;

}
