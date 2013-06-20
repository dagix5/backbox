package it.backbox.compress;

import static org.junit.Assert.assertTrue;
import it.backbox.util.TestUtil;
import it.backbox.utility.Utility;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

public class ZipperTest {
	
	private static Zipper z;
	private static byte[] in;

	@BeforeClass
	public static void setUpBeforeClass() throws IOException {
		z = new Zipper();
		
		in = Utility.read(TestUtil.filename);
	}
	
	@Test
	public void testCompressByteArrayString2() throws Exception {
		byte[] c = z.compress(in, "entry");
		
		byte[] d = z.decompress(c, "entry");

		assertTrue(TestUtil.checkTest(in, d));
	}
	
	@Test
	public void testCompressByteArrayString4() throws Exception {
		byte[] c = z.compress(in, "entry");

		z.decompress(c, "entry", TestUtil.folder + "testCompressByteArrayString4");

		assertTrue(TestUtil.checkTest(in, TestUtil.folder + "testCompressByteArrayString4"));
	}
	
	@Test
	public void testCompressStringString2() throws Exception {
		byte[] c = z.compress(TestUtil.filename, "entry");
		
		byte[] d = z.decompress(c, "entry");

		assertTrue(TestUtil.checkTest(in, d));
	}
	
	@Test
	public void testCompressStringString4() throws Exception {
		byte[] c = z.compress(TestUtil.filename, "entry");
		
		z.decompress(c, "entry", TestUtil.folder + "testCompressStringString4d");

		assertTrue(TestUtil.checkTest(in, TestUtil.folder + "testCompressStringString4d"));
	}
	
}
