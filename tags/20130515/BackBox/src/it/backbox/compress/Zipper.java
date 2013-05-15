package it.backbox.compress;

import it.backbox.IZipper;
import it.backbox.utility.Utility;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Zipper implements IZipper{
	private static Logger _log = Logger.getLogger(Zipper.class.getCanonicalName());
	
	private static final int BUFFER = 1024;

	/**
	 * Zip an InputStream to an OutputStream
	 * 
	 * @param in
	 *            InputStream to zip
	 * @param out
	 *            OutputStream zipped
	 * @param name
	 *            Zip entry name
	 * @throws IOException
	 */
	private static void zip(InputStream in, OutputStream out, String name) throws Exception {
		ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(out));
		
		ZipEntry entry = new ZipEntry(name);
		zout.putNextEntry(entry);
		
		byte[] buffer = new byte[BUFFER];
	    int len;

	    while((len = in.read(buffer)) >= 0)
	    	zout.write(buffer, 0, len);

	    if (_log.isLoggable(Level.FINE)) _log.fine(name + "entry zipped");
	    
	    in.close();
	    zout.close();
	    out.close();
	}
	
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
	private static void unzip(InputStream in, OutputStream out, String name) throws Exception {
		ZipInputStream zin = new ZipInputStream(in);
		ZipEntry zipEntry = null;
		do {
			zipEntry = zin.getNextEntry();
		} while ((zipEntry != null) && !zipEntry.getName().equals(name));
		
		byte[] buffer = new byte[BUFFER];
	    int len;

	    while((len = zin.read(buffer)) >= 0)
	    	out.write(buffer, 0, len);

	    if (_log.isLoggable(Level.FINE)) _log.fine(name + "entry unzipped");
	    
	    in.close();
	    zin.close();
	    out.close();
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.IZipper#compress(byte[], java.lang.String)
	 */
	@Override
	public byte[] compress(byte[] src, String name) throws Exception {
		InputStream in = new BufferedInputStream(new ByteArrayInputStream(src));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		zip(in, out, name);
		
		return out.toByteArray();
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IZipper#compress(java.lang.String, java.lang.String)
	 */
	@Override
	public byte[] compress(String filename, String name) throws Exception {
		InputStream in = new BufferedInputStream(new FileInputStream(filename));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		if (name == null)
			name = filename.substring(filename.lastIndexOf("\\") + 1, filename.length());
		
		zip(in, out, name);
		
		return out.toByteArray();
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IZipper#compress(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void compress(String srcfilename, String destfilename, String name) throws Exception {
		InputStream in = new BufferedInputStream(new FileInputStream(srcfilename));
		OutputStream out = Utility.getOutputStream(destfilename);
		
		if (name == null)
			name = destfilename.substring(destfilename.lastIndexOf("\\") + 1, destfilename.length());
		
		zip(in, out, name);
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IZipper#compress(byte[], java.lang.String, java.lang.String)
	 */
	@Override
	public void compress(byte[] src, String name, String destfilename) throws Exception {
		InputStream in = new BufferedInputStream(new ByteArrayInputStream(src));
		OutputStream out = Utility.getOutputStream(destfilename);
		
		zip(in, out, name);
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IZipper#decompress(byte[], java.lang.String)
	 */
	@Override
	public byte[] decompress(byte[] src, String name) throws Exception {
		InputStream in = new BufferedInputStream(new ByteArrayInputStream(src));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		unzip(in, out, name);
		
		return out.toByteArray();
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IZipper#decompress(java.lang.String, java.lang.String)
	 */
	@Override
	public byte[] decompress(String filename, String name) throws Exception {
		InputStream in = new BufferedInputStream(new FileInputStream(filename));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		if (name == null)
			name = filename.substring(filename.lastIndexOf("\\") + 1, filename.length());
		
		unzip(in, out, name);
		
		return out.toByteArray();
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IZipper#decompress(byte[], java.lang.String)
	 */
	@Override
	public void decompress(byte[] src, String name, String destfilename) throws Exception {
		InputStream in = new BufferedInputStream(new ByteArrayInputStream(src));
		OutputStream out = Utility.getOutputStream(destfilename);
		
		unzip(in, out, name);
		
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IZipper#decompress(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void decompress(String srcfilename, String destfilename, String name) throws Exception {
		InputStream in = new BufferedInputStream(new FileInputStream(srcfilename));
		OutputStream out = Utility.getOutputStream(destfilename);
		
		if (name == null)
			name = destfilename.substring(destfilename.lastIndexOf("\\") + 1, destfilename.length());
		
		unzip(in, out, name);
		
	}
	
}
