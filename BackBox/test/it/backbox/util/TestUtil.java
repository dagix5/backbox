package it.backbox.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class TestUtil {
	
	public static String folder = "put_folder_name";
	public static String filename = "put_file_name";
	
	public static void write(byte[] content, String filename) throws IOException {
		File file = new File(filename);
		file.createNewFile();
		file.deleteOnExit();

		FileOutputStream fout = new FileOutputStream(file);
		fout.write(content);
		fout.close();
	}
	
	public static byte[] read(String filename) throws IOException {
		File file = new File(filename);

		byte[] content = new byte[(int) file.length()];
		
		FileInputStream fin = new FileInputStream(file);
		fin.read(content);
		fin.close();
		
		return content;
	}
	
	public static boolean checkTest(byte[] b1, byte[] b2) {
		return Arrays.equals(b1, b2);
	}
	
	public static boolean checkTest(String f1, String f2) throws IOException {
		byte[] b1 = read(f1);
		byte[] b2 = read(f2);
		return checkTest(b1, b2);
	}
	
	public static boolean checkTest(String f1, byte[] b2) throws IOException {
		byte[] b1 = read(f1);
		return checkTest(b1, b2);
	}
	
	public static boolean checkTest(byte[] b1, String f2) throws IOException {
		byte[] b2 = read(f2);
		return checkTest(b1, b2);
	}

}
