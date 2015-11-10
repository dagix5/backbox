package it.backbox.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

import it.backbox.utility.Utility;

public class TestUtil {
	
	public static String folder;
	public static String filename;
	
	static {
		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream("test.properties");
			prop.load(input);

			folder = prop.getProperty("folder");
			filename = prop.getProperty("filename");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (input != null)
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	
	public static void write(byte[] content, String filename) throws IOException {
		File file = new File(filename);
		file.createNewFile();
		file.deleteOnExit();

		FileOutputStream fout = new FileOutputStream(file);
		fout.write(content);
		fout.close();
	}
	
	public static boolean checkTest(byte[] b1, byte[] b2) {
		return Arrays.equals(b1, b2);
	}
	
	public static boolean checkTest(String f1, String f2) throws IOException {
		byte[] b1 = Utility.read(f1);
		byte[] b2 = Utility.read(f2);
		return checkTest(b1, b2);
	}
	
	public static boolean checkTest(String f1, byte[] b2) throws IOException {
		byte[] b1 = Utility.read(f1);
		return checkTest(b1, b2);
	}
	
	public static boolean checkTest(byte[] b1, String f2) throws IOException {
		byte[] b2 = Utility.read(f2);
		return checkTest(b1, b2);
	}

}
