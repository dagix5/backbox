package it.backbox.progress.stream;

import static org.junit.Assert.assertTrue;
import it.backbox.progress.ProgressManager;
import it.backbox.util.TestUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

public class InputStreamCounterTest {

	private static byte[] plain;
	private static final String ID = "Input";
	
	@BeforeClass
	public static void setUpBeforeClass() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		plain = TestUtil.read(TestUtil.filename);
		
		ProgressManager.getInstance().setSpeed(ID, 1024*1024);
	}
	
	@Test
	public void testReadByteArray() throws IOException {
		File f = new File(TestUtil.filename);
		byte[] b = new byte[(int) f.length()];
		InputStreamCounter in = new InputStreamCounter(new FileInputStream(f), ID);
		in.read(b);
		in.close();
		
		assertTrue(Arrays.equals(plain, b));
	}

	@Test
	public void testReadByteArrayIntInt() throws IOException {
		File f = new File(TestUtil.filename);
		byte[] b = new byte[(int) f.length()];
		InputStreamCounter in = new InputStreamCounter(new FileInputStream(f), ID);
		try {
			int l = 0;
			int r = 0;
            while (r < b.length
                   && (l=in.read(b, r, b.length-r)) >= 0) {
                r += l;
            }
        } finally {
            in.close();
        }
		
		assertTrue(Arrays.equals(plain, b));
	}

}
