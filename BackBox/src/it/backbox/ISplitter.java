package it.backbox;

import java.util.ArrayList;

public interface ISplitter {

	/**
	 * Split a byte array
	 * 
	 * @param src
	 *            Byte array to split
	 * @return List of byte array chunks
	 * @throws Exception 
	 */
	public ArrayList<byte[]> split(byte[] src) throws Exception;

	/**
	 * Split a file
	 * 
	 * @param filename
	 *            File to split
	 * @return List of byte array chunks
	 * @throws Exception 
	 */
	public ArrayList<byte[]> split(String filename) throws Exception;

	/**
	 * Split a file
	 * 
	 * @param filename
	 *            File to split
	 * @param chunkprefix
	 *            Prefix to append a progressive to create a chunk name
	 * @param destfolder
	 *            Folder where put the chunk files
	 * @return List of chunks names
	 * @throws Exception 
	 */
	public ArrayList<String> split(String filename, String chunkprefix,	String destfolder) throws Exception;

	/**
	 * Split a byte array in files
	 * 
	 * @param src
	 *            Byte array to split
	 * @param chunkprefix
	 *            Prefix to append a progressive to create a chunk name
	 * @param destfolder
	 *            Folder where put the chunk files
	 * @return List of chunks names
	 * @throws Exception 
	 */
	public ArrayList<String> split(byte[] src, String chunkprefix, String destfolder) throws Exception;

	/**
	 * Merge a list of byte arrays
	 * 
	 * @param chunks
	 *            List of byte arrays to merge
	 * @return Merged byte array
	 * @throws Exception 
	 */
	public byte[] merge(ArrayList<byte[]> chunks) throws Exception;

	/**
	 * Merge a list of byte array in a file
	 * 
	 * @param chunks
	 *            List of byte array to merge
	 * @param destfilename
	 *            Merged file
	 * @throws Exception 
	 */
	public void merge(ArrayList<byte[]> chunks, String destfilename) throws Exception;

}
