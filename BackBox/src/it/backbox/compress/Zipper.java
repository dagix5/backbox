package it.backbox.compress;

import it.backbox.ICompress;
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

public class Zipper implements ICompress{
	private static Logger _log = Logger.getLogger(Zipper.class.getCanonicalName());
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.ICompress#zip(java.io.InputStream, java.io.OutputStream, java.lang.String)
	 */
	@Override
	public void compress(InputStream in, OutputStream out, String name) throws IOException {
		ZipOutputStream zout = null;
		try{
			zout = new ZipOutputStream(new BufferedOutputStream(out));
		
			ZipEntry entry = new ZipEntry(name);
			zout.putNextEntry(entry);
			
			byte[] buffer = new byte[Utility.BUFFER];
		    int len;
	
		    while((len = in.read(buffer)) >= 0)
		    	zout.write(buffer, 0, len);
	
		    if (_log.isLoggable(Level.INFO)) _log.info(name + " entry zipped");
		} finally {
		    if (in != null) in.close();
		    if (zout != null) zout.close();
		    if (out != null) out.close();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.ICompress#decompress(java.io.InputStream, java.io.OutputStream, java.lang.String)
	 */
	@Override
	public void decompress(InputStream in, OutputStream out, String name) throws IOException {
		ZipInputStream zin = null;
		try {
			zin = new ZipInputStream(in);
			ZipEntry zipEntry = null;
			do {
				zipEntry = zin.getNextEntry();
			} while ((zipEntry != null) && !zipEntry.getName().equals(name));
			
			byte[] buffer = new byte[Utility.BUFFER];
		    int len;
	
		    while((len = zin.read(buffer)) >= 0)
		    	out.write(buffer, 0, len);
	
		    if (_log.isLoggable(Level.INFO)) _log.info(name + " entry unzipped");
		} finally {
		    if (in != null) in.close();
		    if (zin != null) zin.close();
		    if (out != null) out.close();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.IZipper#compress(byte[], java.lang.String)
	 */
	@Override
	public byte[] compress(byte[] src, String name) throws IOException {
		InputStream in = new BufferedInputStream(new ByteArrayInputStream(src));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		compress(in, out, name);
		
		return out.toByteArray();
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IZipper#compress(java.lang.String, java.lang.String)
	 */
	@Override
	public byte[] compress(String filename, String name) throws IOException {
		InputStream in = new BufferedInputStream(new FileInputStream(filename));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		if (name == null)
			name = filename.substring(filename.lastIndexOf("\\") + 1, filename.length());
		
		compress(in, out, name);
		
		return out.toByteArray();
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IZipper#decompress(byte[], java.lang.String)
	 */
	@Override
	public byte[] decompress(byte[] src, String name) throws IOException {
		InputStream in = new BufferedInputStream(new ByteArrayInputStream(src));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		decompress(in, out, name);
		
		return out.toByteArray();
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.IZipper#decompress(byte[], java.lang.String)
	 */
	@Override
	public void decompress(byte[] src, String name, String destfilename) throws IOException {
		InputStream in = new BufferedInputStream(new ByteArrayInputStream(src));
		OutputStream out = Utility.getOutputStream(destfilename);
		
		decompress(in, out, name);
		
	}

}
