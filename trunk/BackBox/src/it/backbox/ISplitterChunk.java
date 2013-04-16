package it.backbox;

import it.backbox.bean.Chunk;

import java.util.ArrayList;

public interface ISplitterChunk {

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
