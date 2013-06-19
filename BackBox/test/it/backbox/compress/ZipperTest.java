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
	public void testCompressByteArrayString1() throws Exception {
		byte[] c = z.compress(in, "entry");
		TestUtil.write(c, TestUtil.folder + "testCompressByteArrayString1");
		
		byte[] d = z.decompress(TestUtil.folder + "testCompressByteArrayString1", "entry");
		
		assertTrue(TestUtil.checkTest(in, d));
	}
	
	@Test
	public void testCompressByteArrayString2() throws Exception {
		byte[] c = z.compress(in, "entry");
		
		byte[] d = z.decompress(c, "entry");

		assertTrue(TestUtil.checkTest(in, d));
	}
	
	@Test
	public void testCompressByteArrayString3() throws Exception {
		byte[] c = z.compress(in, "entry");
		TestUtil.write(c, TestUtil.folder + "testCompressByteArrayString3");

		z.decompress(TestUtil.folder + "testCompressByteArrayString3", TestUtil.folder + "testCompressByteArrayString3d", "entry");

		assertTrue(TestUtil.checkTest(in, TestUtil.folder + "testCompressByteArrayString3d"));
	}

	@Test
	public void testCompressByteArrayString4() throws Exception {
		byte[] c = z.compress(in, "entry");

		z.decompress(c, "entry", TestUtil.folder + "testCompressByteArrayString4");

		assertTrue(TestUtil.checkTest(in, TestUtil.folder + "testCompressByteArrayString4"));
	}
	
	@Test
	public void testCompressStringString1() throws Exception {
		byte[] c = z.compress(TestUtil.filename, "entry");
		TestUtil.write(c, TestUtil.folder + "testCompressStringString1");
		
		byte[] d = z.decompress(TestUtil.folder + "testCompressStringString1", "entry");
		
		assertTrue(TestUtil.checkTest(in, d));	
	}
	
	@Test
	public void testCompressStringString2() throws Exception {
		byte[] c = z.compress(TestUtil.filename, "entry");
		
		byte[] d = z.decompress(c, "entry");

		assertTrue(TestUtil.checkTest(in, d));
	}
	
	@Test
	public void testCompressStringString3() throws Exception {
		byte[] c = z.compress(TestUtil.filename, "entry");
		TestUtil.write(c, TestUtil.folder + "testCompressStringString3");

		z.decompress(TestUtil.folder + "testCompressStringString3", TestUtil.folder + "testCompressStringString3d", "entry");

		assertTrue(TestUtil.checkTest(in, TestUtil.folder + "testCompressStringString3d"));
	}
	
	@Test
	public void testCompressStringString4() throws Exception {
		byte[] c = z.compress(TestUtil.filename, "entry");
		
		z.decompress(c, "entry", TestUtil.folder + "testCompressStringString4d");

		assertTrue(TestUtil.checkTest(in, TestUtil.folder + "testCompressStringString4d"));
	}
	
	@Test
	public void testCompressByteArrayStringString1() throws Exception {
		z.compress(in, "entry", TestUtil.folder + "testCompressByteArrayStringString1");
		
		byte[] d = z.decompress(TestUtil.folder + "testCompressByteArrayStringString1", "entry");
		
		assertTrue(TestUtil.checkTest(in, d));	
	}
	
	@Test
	public void testCompressByteArrayStringString2() throws Exception {
		z.compress(in, "entry", TestUtil.folder + "testCompressByteArrayStringString2");
		
		byte[] d = z.decompress(Utility.read(TestUtil.folder + "testCompressByteArrayStringString2"), "entry");

		assertTrue(TestUtil.checkTest(in, d));
	}
	
	@Test
	public void testCompressByteArrayStringString3() throws Exception {
		z.compress(in, "entry", TestUtil.folder + "testCompressByteArrayStringString3");

		z.decompress(TestUtil.folder + "testCompressByteArrayStringString3", TestUtil.folder + "testCompressByteArrayStringString3d", "entry");

		assertTrue(TestUtil.checkTest(in, TestUtil.folder + "testCompressByteArrayStringString3d"));
	}
	
	@Test
	public void testCompressByteArrayStringString4() throws Exception {
		z.compress(in, "entry", TestUtil.folder + "testCompressByteArrayStringString4");
		
		z.decompress(Utility.read(TestUtil.folder + "testCompressByteArrayStringString4"), "entry", TestUtil.folder + "testCompressByteArrayStringString4d");

		assertTrue(TestUtil.checkTest(in, TestUtil.folder + "testCompressByteArrayStringString4d"));
	}
	
	@Test
	public void testCompressStringStringString1() throws Exception {
		z.compress(TestUtil.filename, TestUtil.folder + "testCompressStringStringString1", "entry");
		
		byte[] d = z.decompress(TestUtil.folder + "testCompressStringStringString1", "entry");
		
		assertTrue(TestUtil.checkTest(in, d));	
	}
	
	@Test
	public void testCompressStringStringString2() throws Exception {
		z.compress(TestUtil.filename, TestUtil.folder + "testCompressStringStringString2", "entry");
		
		byte[] d = z.decompress(Utility.read(TestUtil.folder + "testCompressStringStringString2"), "entry");

		assertTrue(TestUtil.checkTest(in, d));
	}
	
	@Test
	public void testCompressStringStringString3() throws Exception {
		z.compress(TestUtil.filename, TestUtil.folder + "testCompressStringStringString3", "entry");

		z.decompress(TestUtil.folder + "testCompressStringStringString3", TestUtil.folder + "testCompressStringStringString3d", "entry");

		assertTrue(TestUtil.checkTest(in, TestUtil.folder + "testCompressStringStringString3d"));
	}
	
	@Test
	public void testCompressStringStringString4() throws Exception {
		z.compress(TestUtil.filename, TestUtil.folder + "testCompressStringStringString4", "entry");
		
		z.decompress(Utility.read(TestUtil.folder + "testCompressStringStringString4"), "entry", TestUtil.folder + "testCompressStringStringString4d");

		assertTrue(TestUtil.checkTest(in, TestUtil.folder + "testCompressStringStringString4d"));
	}

}
