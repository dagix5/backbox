package it.backbox;

import it.backbox.bean.Chunk;

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
	
	/**
	 * Split a byte array in Chunk(s)
	 * 
	 * @param src
	 *            Byte array to split
	 * @param chunkprefix
	 *            Prefix to append a progressive to create a chunk name
	 * @return List of Chunk
	 * @throws Exception 
	 */
	public ArrayList<Chunk> splitChunk(byte[] src, String chunkprefix) throws Exception;

	/**
	 * Split a file in Chunk(s)
	 * 
	 * @param filename
	 *            File to split
	 * @param chunkprefix
	 *            Prefix to append a progressive to create a chunk name
	 * @return List of Chunk
	 */
	public ArrayList<Chunk> splitChunk(String filename, String chunkprefix) throws Exception;

	/**
	 * Merge a list of Chunk in a byte array
	 * 
	 * @param chunks
	 *            Chunk to merge
	 * @return Merged byte array
	 */
	public byte[] mergeChunk(ArrayList<Chunk> chunks) throws Exception;

	/**
	 * Merge a list of Chunk in a file
	 * 
	 * @param chunks
	 *            List of Chunk to merge
	 * @param destfilename
	 *            Merged file
	 */
	public void mergeChunk(ArrayList<Chunk> chunks, String destfilename) throws Exception;

}
