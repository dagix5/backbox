package it.backbox.boxcom;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

import it.backbox.client.rest.RestClient;
import it.backbox.progress.ProgressManager;
import it.backbox.util.TestUtil;
import it.backbox.utility.Utility;

public class BoxManagerTest {

	private static BoxManager bm;
	
	private static byte[] plain;
	private static String folderID;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		bm = new BoxManager(new RestClient());
		
		folderID = bm.getBoxID("Test");
		if (folderID == null)
			folderID = bm.mkdir("Test", null);
		
		assertNotNull(folderID);
		
		plain = Utility.read(TestUtil.filename);
		
		ProgressManager.getInstance().setSpeed(ProgressManager.DOWNLOAD_ID, 200*1024);
	}

	@Test
	public void testUploadDownload() throws Exception {
		String id = bm.upload(TestUtil.filename, folderID);
		assertNotNull(id);
		
		byte[] d = bm.download(id);
		
		assertTrue(Arrays.equals(plain, d));
	}

	@Test
	public void testDelete() throws Exception {
		String id = bm.upload(TestUtil.filename, folderID);
		assertNotNull(id);
		
		bm.delete(id);
		id = bm.getBoxID(TestUtil.filename);
		assertNull(id);
	}

}
