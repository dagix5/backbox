package it.backbox.boxcom;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import it.backbox.client.rest.RestClient;
import it.backbox.progress.ProgressManager;
import it.backbox.util.TestUtil;
import it.backbox.utility.Utility;

import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Test;

public class BoxManagerTest {

	private static Logger _log = Logger.getLogger("it.backbox");
	
	private static BoxManager bm;
	
	private static byte[] plain;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConsoleHandler ch = new ConsoleHandler();
		_log.addHandler(ch);
		ch.setLevel(Level.ALL);
		_log.setLevel(Level.ALL);
		
		bm = new BoxManager(new RestClient());
		
		String folderID = bm.getBoxID("Test");
		if (folderID == null)
			folderID = bm.mkdir("Test");
		
		bm.setBackBoxFolderID(folderID);
		assertNotNull(bm.getBackBoxFolderID());
		
		plain = Utility.read(TestUtil.filename);
		
		ProgressManager.getInstance().setSpeed(ProgressManager.DOWNLOAD_ID, 200*1024);
	}

	@Test
	public void testUploadDownload() throws Exception {
		String id = bm.upload(TestUtil.filename);
		assertNotNull(id);
		
		byte[] d = bm.download(id);
		
		assertTrue(Arrays.equals(plain, d));
	}

	@Test
	public void testDelete() throws Exception {
		String id = bm.upload(TestUtil.filename);
		assertNotNull(id);
		
		bm.delete(id);
		id = bm.getBoxID(TestUtil.filename);
		assertNull(id);
	}

}
