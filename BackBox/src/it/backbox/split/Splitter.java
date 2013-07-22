package it.backbox.split;

import it.backbox.ISplitter;
import it.backbox.bean.Chunk;
import it.backbox.utility.Utility;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Splitter implements ISplitter {
	
	private static Logger _log = Logger.getLogger(Splitter.class.getCanonicalName());
	
	private int chunkSize;
	
	/**
	 * Constructor
	 * 
	 * @param chunkSize
	 *            Chunk size
	 */
	public Splitter(int chunkSize) {
		this.chunkSize = chunkSize;
	}
	
	/**
	 * Get the chunk size
	 * 
	 * @return The chunk size
	 */
	public int getChunkSize() {
		return chunkSize;
	}

	/**
	 * Set chunk size
	 * 
	 * @param chunkSize
	 *            Chunk size
	 */
	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}
	
	/**
	 * Split an InputStream in chunks
	 * 
	 * @param inStream
	 *            InputStream to split
	 * @param size
	 *            InputStream size
	 * @return List of byte array chunks
	 * @throws IOException
	 */
	private List<byte[]> split(InputStream inStream, long size) throws IOException {
		List<byte[]> chunkList = new ArrayList<byte[]>();
		int totalBytesRead = 0;
		try {
			while (totalBytesRead < size) {
				byte[] buffer = splitNextChunk(inStream, size, totalBytesRead);
				totalBytesRead += buffer.length;
				chunkList.add(buffer);
			}
		} finally {
			inStream.close();
		}
		return chunkList;
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.ISplitter#splitNextChunk(java.io.InputStream, long, int)
	 */
	@Override
	public byte[] splitNextChunk(InputStream inStream, long size, int bytesRead) throws IOException {
		int csize = chunkSize;
		int bytesRemaining = (int) (size - bytesRead);
		if (bytesRemaining < csize) {
			csize = bytesRemaining;
			if (_log.isLoggable(Level.FINE)) _log.fine("Chunk size: " + csize);
		}
		byte[] buffer = new byte[csize];
		inStream.read(buffer, 0, csize);

		if (_log.isLoggable(Level.FINE)) _log.fine("Total Bytes Read: " + bytesRead);
		return buffer;
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.ISplitter#split(byte[])
	 */
	@Override
	public List<byte[]> split(byte[] src) throws IOException {
		InputStream in = new BufferedInputStream(new ByteArrayInputStream(src));
		return split(in, src.length);
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.ISplitter#merge(java.util.List)
	 */
	@Override
	public byte[] merge(List<byte[]> chunks) throws IOException {
		ByteArrayOutputStream out = null;
		try {
			out = new ByteArrayOutputStream();
			for (byte[] c : chunks) {
				InputStream in = new BufferedInputStream(new  ByteArrayInputStream(c));
				
				mergeNextChunk(in, out);
			}
		} finally {
			if (out != null)
				out.close();
		}
		return out.toByteArray();

	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.ISplitter#mergeNextChunk(java.io.InputStream, java.io.OutputStream)
	 */
	public void mergeNextChunk(InputStream in, OutputStream out) throws IOException {
		byte[] buf = new byte[Utility.BUFFER];
		int count = in.read(buf);
		while (count >= 0) {
			out.write(buf, 0, count); 
			count = in.read(buf);
		}
		in.close();
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.ISplitter#merge(java.util.List, java.lang.String)
	 */
	@Override
	public void merge(List<byte[]> chunks, String destfilename) throws IOException {
		OutputStream out = null;
		try {
			out = Utility.getOutputStream(destfilename);
			for (byte[] c : chunks) {
				InputStream in = new BufferedInputStream(new ByteArrayInputStream(c));
				
				mergeNextChunk(in, out);
			}
		} finally {
			if (out != null)
				out.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.ISplitterChunk#splitChunk(byte[], java.lang.String)
	 */
	@Override
	public List<Chunk> splitChunk(byte[] src, String chunkprefix) throws IOException {
		List<byte[]> cs = split(src);
		return saveChunks(cs, chunkprefix);
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.ISplitterChunk#splitChunk(java.lang.String, java.lang.String)
	 */
	@Override
	public List<Chunk> splitChunk(String filename, String chunkprefix) throws IOException {
		File file = new File(filename);
		InputStream in = new BufferedInputStream(new FileInputStream(file));
		List<byte[]> cs = split(in, file.length());
		return saveChunks(cs, chunkprefix);
	}

	/**
	 * Create a List of Chunk
	 * 
	 * @param cs
	 *            List of byte arrays chunks contents
	 * @param chunkprefix
	 *            Prefix to append a progressive to create a chunk name
	 * @return List of Chunk
	 */
	private List<Chunk> saveChunks(List<byte[]> cs, String chunkprefix) {
		List<Chunk> chunks = new ArrayList<>();
		for (int i = 0; i < cs.size(); i++) {
			byte[] f = cs.get(i);
			Chunk c = new Chunk();
			c.setChunkname(Utility.buildChunkName(chunkprefix, i));
			c.setContent(f);
			c.setSize(f.length);
			chunks.add(c);
		}
		return chunks;
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.ISplitterChunk#mergeChunk(java.util.List, java.lang.String)
	 */
	@Override
	public void mergeChunk(List<Chunk> chunks, String destfilename) throws IOException {
		List<byte[]> cs = new ArrayList<>();
		for (Chunk c : chunks)
			cs.add(c.getContent());
		merge(cs, destfilename);
	}

}
