package it.backbox.compress;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.FilenameUtils;

import it.backbox.ICompress;
import it.backbox.utility.Utility;

public class GZipper implements ICompress {
	
	private static final Logger _log = Logger.getLogger(GZipper.class.getCanonicalName());

	@Override
	public byte[] compress(byte[] src, String name) throws IOException {
		InputStream in = new BufferedInputStream(new ByteArrayInputStream(src));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		compress(in, out, name);
		
		return out.toByteArray();
	}

	@Override
	public byte[] compress(String filename, String name) throws IOException {
		InputStream in = new BufferedInputStream(new FileInputStream(filename));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		if (name == null)
			name = FilenameUtils.getName(filename);
		
		compress(in, out, name);
		
		return out.toByteArray();
	}

	@Override
	public byte[] decompress(byte[] src, String name) throws IOException {
		InputStream in = new BufferedInputStream(new ByteArrayInputStream(src));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		decompress(in, out, name);
		
		return out.toByteArray();
	}

	@Override
	public void decompress(byte[] src, String name, String destfilename) throws IOException {
		InputStream in = new BufferedInputStream(new ByteArrayInputStream(src));
		OutputStream out = Utility.getOutputStream(destfilename);
		
		decompress(in, out, name);
		
	}

	@Override
	public void compress(InputStream in, OutputStream out, String name) throws IOException {
		GZIPOutputStream gzout = null;
		try {
			gzout = new GZIPOutputStream(out);
			byte[] buffer = new byte[Utility.BUFFER];
		    int len;
	
		    while((len = in.read(buffer)) >= 0)
		    	gzout.write(buffer, 0, len);
		    
		    if (_log.isLoggable(Level.INFO)) _log.info(name + " entry gzipped");
			
		} finally {
		    if (in != null) in.close();
		    if (gzout != null) gzout.close();
		    if (out != null) out.close();
		}
	}

	@Override
	public void decompress(InputStream in, OutputStream out, String name) throws IOException {
		GZIPInputStream gzin = null;
		try {
			gzin = new GZIPInputStream(in);
			
			byte[] buffer = new byte[Utility.BUFFER];
		    int len;
	
		    while((len = gzin.read(buffer)) >= 0)
		    	out.write(buffer, 0, len);
	
		    if (_log.isLoggable(Level.INFO)) _log.info(name + " entry gunzipped");
		} finally {
		    if (in != null) in.close();
		    if (gzin != null) gzin.close();
		    if (out != null) out.close();
		}
	}

}
