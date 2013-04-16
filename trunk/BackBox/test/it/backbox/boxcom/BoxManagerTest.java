package it.backbox.boxcom;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import it.backbox.progress.ProgressManager;
import it.backbox.util.TestUtil;

import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Test;

public class BoxManagerTest {

	private static Logger _log = Logger.getLogger("it.backbox");
	
	private static BoxManager bm;
	private static String folderID;
	
	private static byte[] plain;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConsoleHandler ch = new ConsoleHandler();
		_log.addHandler(ch);
		ch.setLevel(Level.ALL);
		_log.setLevel(Level.ALL);
		
		bm = BoxManager.createInstance();
		
		folderID = bm.getBoxID("Test");
		if (folderID == null)
			folderID = bm.mkdir("Test");
		
		bm.setBackBoxFolderID(folderID);
		assertNotNull(bm.getBackBoxFolderID());
		
		plain = TestUtil.read(TestUtil.filename);
		
		ProgressManager.getInstance().setSpeed(ProgressManager.DOWNLOAD_ID, 200*1024);
	}

	@Test
	public void testUploadByteArrayStringString1() throws Exception {
		String id = bm.upload(plain, "testUploadByteArrayStringString1", folderID);
		assertNotNull(id);
		
		byte[] d = bm.download(id);
		
		assertTrue(Arrays.equals(plain, d));
	}
	
	@Test
	public void testUploadByteArrayStringString2() throws Exception {
		String id = bm.upload(plain, "testUploadByteArrayStringString2", folderID);
		assertNotNull(id);
		
		bm.download(id, TestUtil.folder + "testUploadByteArrayStringString2");
		byte[] d = TestUtil.read(TestUtil.folder + "testUploadByteArrayStringString2");
		
		assertTrue(Arrays.equals(plain, d));
	}

	@Test
	public void testUploadStringString1() throws Exception {
		String id = bm.upload(TestUtil.filename, folderID);
		assertNotNull(id);
		
		byte[] d = bm.download(id);
		
		assertTrue(Arrays.equals(plain, d));
	}
	
	@Test
	public void testUploadStringString2() throws Exception {
		String id = bm.upload(TestUtil.filename, folderID);
		assertNotNull(id);
		
		bm.download(id, TestUtil.folder + "testUploadStringString2");
		byte[] d = TestUtil.read(TestUtil.folder + "testUploadStringString2");
		
		assertTrue(Arrays.equals(plain, d));
	}

	@Test
	public void testDelete() throws Exception {
		String id = bm.upload(plain, "testDelete", folderID);
		assertNotNull(id);
		
		bm.delete(id);
		id = bm.getBoxID(TestUtil.filename);
		assertNull(id);
	}

}
