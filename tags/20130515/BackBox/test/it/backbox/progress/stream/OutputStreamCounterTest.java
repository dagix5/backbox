package it.backbox.progress.stream;

import static org.junit.Assert.assertTrue;
import it.backbox.progress.ProgressManager;
import it.backbox.util.TestUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

public class OutputStreamCounterTest {

	private static byte[] plain;
	private static final String ID = "Output";
	
	@BeforeClass
	public static void setUpBeforeClass() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		plain = TestUtil.read(TestUtil.filename);
		
		ProgressManager.getInstance().setSpeed(ID, 1024);
	}
	
	@Test
	public void testWriteInt() throws IOException {
		File f = new File(TestUtil.folder + "testWriteInt");
		
		OutputStreamCounter out = new OutputStreamCounter(new FileOutputStream(f), ID);
		
		for (byte b : plain)
			out.write(b);
		out.close();
		
		byte[] b = TestUtil.read(TestUtil.folder + "testWriteInt");
		
		assertTrue(Arrays.equals(plain, b));
	}

	@Test
	public void testWriteByteArrayIntInt() throws IOException {
		File f = new File(TestUtil.folder + "testWriteByteArrayIntInt");
		
		OutputStreamCounter out = new OutputStreamCounter(new FileOutputStream(f), ID);
		try {
			int r = 0;
            while (r < (plain.length - 1024)) {
                out.write(plain, r, 1024);
                r += 1024;
            }
            out.write(plain, r, (plain.length - r));
        } finally {
        	out.close();
        }
		
		byte[] b = TestUtil.read(TestUtil.folder + "testWriteByteArrayIntInt");
		
		assertTrue(Arrays.equals(plain, b));
	}

}
