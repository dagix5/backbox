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

	private static final int BUFFER = 1024;
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.ISplitter#getChunkSize()
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
	 * Constructor
	 * 
	 * @param chunkSize
	 *            Chunk size
	 */
	public Splitter(int chunkSize) {
		this.chunkSize = chunkSize;
	}
	
	/**
	 * Write data in a file
	 * 
	 * @param DataByteArray
	 *            Data to write
	 * @param DestinationFileName
	 *            Destination file of the data
	 * @throws IOException 
	 */
	private static void write(byte[] DataByteArray, String destfilename) throws IOException {
		OutputStream output = null;
		try {
			output = Utility.getOutputStream(destfilename);
			output.write(DataByteArray);
			_log.fine("Writing Process Was Performed");
		} finally {
			if (output != null)
				output.close();
		}
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
		byte[] temporary = null;
		int totalBytesRead = 0;
		try {
			int csize = chunkSize;
			while (totalBytesRead < size) {
				int bytesRemaining = (int) (size - totalBytesRead);
				if (bytesRemaining < csize) {
					csize = bytesRemaining;
					if (_log.isLoggable(Level.FINE)) _log.fine("CHUNK_SIZE: " + csize);
				}
				temporary = new byte[csize];
				int bytesRead = inStream.read(temporary, 0, csize);

				if (bytesRead > 0)
					totalBytesRead += bytesRead;

				chunkList.add(temporary);
				if (_log.isLoggable(Level.FINE)) _log.fine("Total Bytes Read: " + totalBytesRead);
			}
		} finally {
			inStream.close();
		}
		return chunkList;
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.ISplitter#split(byte[])
	 */
	@Override
	public List<byte[]> split(byte[] src) throws Exception {
		InputStream in = new BufferedInputStream(new ByteArrayInputStream(src));
		return split(in, src.length);
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.ISplitter#split(java.lang.String)
	 */
	@Override
	public List<byte[]> split(String filename) throws Exception {
		File file = new File(filename);
		InputStream in = new BufferedInputStream(new FileInputStream(file));
		return split(in, file.length());
	}

	/**
	 * Write in files chunks
	 * 
	 * @param chunkList
	 *            List of byte arrays chunks content to write in files
	 * @param chunkprefix
	 *            Prefix to append a progressive to create a chunk name
	 * @param destfolder
	 *            Destination folder
	 * @return List of chunks names
	 * @throws IOException
	 */
	private List<String> write(List<byte[]> chunkList, String chunkprefix, String destfolder) throws IOException {
		List<String> nameList = new ArrayList<String>();
		for (int i = 0; i < chunkList.size(); i++) {
			byte[] c = chunkList.get(i);
			String chunkname = buildChunkName(chunkprefix, i);
			
			write(c, destfolder + "\\" + chunkname);
			nameList.add(destfolder + "\\" + chunkname);
		}
		return nameList;
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
	private String buildChunkName(String chunkprefix, int index) {
		StringBuilder name = new StringBuilder(chunkprefix);
		name.append(it.backbox.bean.File.EXT);
		if (index < 10)
			name.append(0);
		name.append(index);
		return name.toString();
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.ISplitter#split(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public List<String> split(String filename, String chunkprefix, String destfolder) throws Exception {
		List<byte[]> chunkList = split(filename);
		return write(chunkList, chunkprefix, destfolder);
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.ISplitter#split(byte[], java.lang.String, java.lang.String)
	 */
	@Override
	public List<String> split(byte[] src, String chunkprefix, String destfolder) throws Exception {
		List<byte[]> chunkList = split(src);
		return write(chunkList, chunkprefix, destfolder);
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.ISplitter#merge(java.util.List)
	 */
	@Override
	public byte[] merge(List<byte[]> chunks) throws Exception {
		ByteArrayOutputStream out = null;
		try {
			out = new ByteArrayOutputStream();
			for (byte[] c : chunks) {
				InputStream in = new BufferedInputStream(new  ByteArrayInputStream(c));
				
				byte[] buf = new byte[BUFFER];
				int count = in.read(buf);
				while (count >= 0) {
					out.write(buf, 0, count); 
					count = in.read(buf);
				}
				in.close();
			}
		} finally {
			if (out != null)
				out.close();
		}
		return out.toByteArray();

	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.ISplitter#merge(java.util.List, java.lang.String)
	 */
	@Override
	public void merge(List<byte[]> chunks, String destfilename) throws Exception {
		OutputStream out = null;
		try {
			out = Utility.getOutputStream(destfilename);
			for (byte[] c : chunks) {
				InputStream in = new BufferedInputStream(new ByteArrayInputStream(c));
				
				byte[] buf = new byte[BUFFER];
				int count = in.read(buf);
				while (count >= 0) {
					out.write(buf, 0, count); 
					count = in.read(buf);
				}
				in.close();
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
	public List<Chunk> splitChunk(byte[] src, String chunkprefix) throws Exception {
		List<byte[]> cs = split(src);
		return saveChunks(cs, chunkprefix);
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.ISplitterChunk#splitChunk(java.lang.String, java.lang.String)
	 */
	@Override
	public List<Chunk> splitChunk(String filename, String chunkprefix) throws Exception {
		List<byte[]> cs = split(filename);
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
			c.setChunkname(buildChunkName(chunkprefix, i));
			c.setContent(f);
			c.setSize(f.length);
			chunks.add(c);
		}
		return chunks;
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.ISplitterChunk#mergeChunk(java.util.List)
	 */
	@Override
	public byte[] mergeChunk(List<Chunk> chunks) throws Exception {
		List<byte[]> cs = new ArrayList<>();
		for (Chunk c : chunks)
			cs.add(c.getContent());
		return merge(cs);
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.ISplitterChunk#mergeChunk(java.util.List, java.lang.String)
	 */
	@Override
	public void mergeChunk(List<Chunk> chunks, String destfilename) throws Exception {
		List<byte[]> cs = new ArrayList<>();
		for (Chunk c : chunks)
			cs.add(c.getContent());
		merge(cs, destfilename);
	}

}
