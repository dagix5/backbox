package it.backbox;

import it.backbox.bean.Chunk;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface ISplitter {
	
	/**
	 * Split a byte array
	 * 
	 * @param src
	 *            Byte array to split
	 * @return List of byte array chunks
	 * @throws IOException 
	 */
	public List<byte[]> split(byte[] src) throws IOException;

	/**
	 * Merge a list of byte arrays
	 * 
	 * @param chunks
	 *            List of byte arrays to merge
	 * @return Merged byte array
	 * @throws IOException 
	 */
	public byte[] merge(List<byte[]> chunks) throws IOException;

	/**
	 * Merge a list of byte array in a file
	 * 
	 * @param chunks
	 *            List of byte array to merge
	 * @param destfilename
	 *            Merged file
	 * @throws IOException 
	 */
	public void merge(List<byte[]> chunks, String destfilename) throws IOException;
	
	/**
	 * Split a byte array in Chunk(s)
	 * 
	 * @param src
	 *            Byte array to split
	 * @param chunkprefix
	 *            Prefix to append a progressive to create a chunk name
	 * @return List of Chunk
	 * @throws IOException 
	 */
	public List<Chunk> splitChunk(byte[] src, String chunkprefix) throws IOException;
	
	/**
	 * Split a file in Chunk(s)
	 * 
	 * @param filename
	 *            File to split
	 * @param chunkprefix
	 *            Prefix to append a progressive to create a chunk name
	 * @return List of Chunk
	 * @throws IOException
	 */
	public List<Chunk> splitChunk(String filename, String chunkprefix) throws IOException;


	/**
	 * Merge a list of Chunk in a file
	 * 
	 * @param chunks
	 *            List of Chunk to merge
	 * @param destfilename
	 *            Merged file
	 * @throws IOException
	 */
	public void mergeChunk(List<Chunk> chunks, String destfilename) throws IOException;
	
	/**
	 * Get the next chunk content
	 * 
	 * @param inStream
	 *            Input stream to read
	 * @param size
	 *            Total size
	 * @param bytesRead
	 *            Bytes read until now
	 * @return Chunk content
	 * @throws IOException
	 */
	public byte[] splitNextChunk(InputStream inStream, long size, int bytesRead) throws IOException;
	
	/**
	 * Merge the chunk stream in the output stream; call this method with for
	 * all the chunks.
	 * 
	 * @param in
	 *            InputStream chunk
	 * @param out
	 *            Merged file OutputStream
	 * @throws IOException
	 */
	public void mergeNextChunk(InputStream in, OutputStream out) throws IOException;

}
