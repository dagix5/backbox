package it.backbox.db;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import it.backbox.bean.Chunk;
import it.backbox.bean.File;
import it.backbox.exception.BackBoxException;

public class DBManagerTest {

	public static final String DBFILE = "test.db";
	private DBManager dbm;

	@Before
	public void setUpBeforeClass() throws BackBoxException, SQLException {
		dbm = new DBManager(DBFILE);
		dbm.createDB();
	}

	@After
	public void tearDown() throws SQLException {
		dbm.closeDB();
	}
	
	private int insert(int i, int f, int h) throws BackBoxException {
		List<Chunk> chunks = new ArrayList<Chunk>();
		Chunk c = new Chunk();
		c.setBoxid("BOXID1");
		c.setChunkhash("CHUNKHASH1");
		c.setChunkname("HASH" + h);
		c.setSize(0);
		chunks.add(c);

		c = new Chunk();
		c.setBoxid("BOXID2");
		c.setChunkhash("CHUNKHASH2");
		c.setChunkname("HASH" + h);
		c.setSize(0);
		chunks.add(c);

		return dbm.insert("FOLDER" + i, "FILENAME" + f, "HASH" + h, 0, 0, chunks, (short) 0, (short) 0, (short) 0);
	}
	
	private int delete(int i, int f, int h) throws BackBoxException {
		return dbm.delete("FOLDER" + i, "FILENAME" + f, "HASH" + h);
	}

	@Test
	public void testInsertDelete() throws BackBoxException {
		int r = insert(1, 1, 1);
		assertTrue(r == 1);
		assertTrue(dbm.isModified());
		
		r = delete(1, 1, 1);
		assertTrue(r == 1);
		assertTrue(dbm.isModified());
	}

	@Test(expected = BackBoxException.class)
	public void testInsertFail() throws BackBoxException {
		int r = insert(2, 2, 2);
		assertTrue(r > 0);
		assertTrue(dbm.isModified());
		insert(2, 2, 2);
	}
	
	@Test(expected = BackBoxException.class)
	public void testDeleteFail() throws BackBoxException {
		delete(4, 4, 4);
	}
	
	@Test
	public void testUpdate() throws BackBoxException, SQLException {
		int r = insert(1, 1, 1);
		assertTrue(r == 1);
		assertTrue(dbm.isModified());
		
		r = dbm.update("HASH1", "FOLDER1", "FILENAME1", "FOLDER2", "FILENAME2", 1, 1, (short) 1, (short) 1, (short) 1);
		assertTrue(r == 1);
		assertTrue(dbm.isModified());
		
		File file = dbm.getFileRecord("FOLDER2", "FILENAME2", "HASH1");
		assertTrue(file.getSize() == 1);
		assertTrue(file.getEncrypted() == 1);
		assertTrue(file.getCompressed() == 1);
		assertTrue(file.getSplitted() == 1);
	}
	
	@Test
	public void testUpdateFail() throws BackBoxException, SQLException {
		int r = insert(1, 1, 1);
		assertTrue(r == 1);
		assertTrue(dbm.isModified());
		
		r = dbm.update("HASH1", "FOLDER1", "FILENAME1", "", "", -1, -1, (short) -1, (short) -1, (short) -1);
		assertTrue(r == 0);
	}
	
	@Test
	public void testGetAllFiles() throws SQLException, BackBoxException {
		List<File> files = dbm.getAllFiles();
		assertFalse(files == null);
		assertTrue(files.isEmpty());
		
		insert(1, 1, 1);
		
		files = dbm.getAllFiles();
		assertFalse(files == null);
		assertTrue(files.size() == 1);
		
		File file = files.get(0);
		assertTrue(file.getFolder().equals("FOLDER1"));
		assertTrue(file.getFilename().equals("FILENAME1"));
		assertTrue(file.getHash().equals("HASH1"));
		
		List<Chunk> chunks = file.getChunks();
		assertTrue(chunks.size() == 2);
		
		Chunk c = chunks.get(0);
		assertTrue(c.getBoxid().equals("BOXID1"));
		assertTrue(c.getChunkhash().equals("CHUNKHASH1"));
		assertTrue(c.getChunkname().equals(file.getHash()));
		
		insert(1, 2, 2);
		
		files = dbm.getAllFiles();
		assertFalse(files == null);
		assertTrue(files.size() == 2);
		
		insert(3, 3, 3);
		
		files = dbm.getAllFiles();
		assertFalse(files == null);
		assertTrue(files.size() == 3);
		
		delete(1, 1, 1);
		delete(1, 2, 2);
		delete(3, 3, 3);
		
		files = dbm.getAllFiles();
		assertFalse(files == null);
		assertTrue(files.isEmpty());
	}
	
	@Test
	public void testGetFiles() throws SQLException, BackBoxException {
		List<File> files = dbm.getFiles("HASH1");
		assertFalse(files == null);
		assertTrue(files.isEmpty());
		
		insert(1, 1, 1);
		
		files = dbm.getFiles("HASH1");
		assertFalse(files == null);
		assertTrue(files.size() == 1);
		
		File file = files.get(0);
		assertTrue(file.getFolder().equals("FOLDER1"));
		assertTrue(file.getFilename().equals("FILENAME1"));
		assertTrue(file.getHash().equals("HASH1"));
		
		List<Chunk> chunks = file.getChunks();
		assertTrue(chunks.size() == 2);
		
		Chunk c = chunks.get(0);
		assertTrue(c.getBoxid().equals("BOXID1"));
		assertTrue(c.getChunkhash().equals("CHUNKHASH1"));
		assertTrue(c.getChunkname().equals(file.getHash()));
		
		insert(1, 2, 2);
		
		files = dbm.getFiles("HASH1");
		assertFalse(files == null);
		assertTrue(files.size() == 1);
		
		files = dbm.getFiles("HASH2");
		assertFalse(files == null);
		assertTrue(files.size() == 1);
		
		insert(2, 2, 1);
		
		files = dbm.getFiles("HASH1");
		assertFalse(files == null);
		assertTrue(files.size() == 2);
		
		delete(1, 1, 1);
		delete(2, 2, 1);
		
		files = dbm.getFiles("HASH1");
		assertFalse(files == null);
		assertTrue(files.isEmpty());
	}
	
	@Test
	public void testGetFilesInFolder() throws SQLException, BackBoxException {
		List<File> files = dbm.getFilesInFolder("FOLDER1");
		assertFalse(files == null);
		assertTrue(files.isEmpty());
		
		insert(1, 1, 1);
		
		files = dbm.getFilesInFolder("FOLDER1");
		assertFalse(files == null);
		assertTrue(files.size() == 1);
		
		File file = files.get(0);
		assertTrue(file.getFolder().equals("FOLDER1"));
		assertTrue(file.getFilename().equals("FILENAME1"));
		assertTrue(file.getHash().equals("HASH1"));
		
		List<Chunk> chunks = file.getChunks();
		assertTrue(chunks.size() == 2);
		
		Chunk c = chunks.get(0);
		assertTrue(c.getBoxid().equals("BOXID1"));
		assertTrue(c.getChunkhash().equals("CHUNKHASH1"));
		assertTrue(c.getChunkname().equals(file.getHash()));
		
		insert(2, 2, 2);
		
		files = dbm.getFilesInFolder("FOLDER1");
		assertFalse(files == null);
		assertTrue(files.size() == 1);
		
		insert(1, 2, 2);
		
		files = dbm.getFilesInFolder("FOLDER1");
		assertFalse(files == null);
		assertTrue(files.size() == 2);
		
		delete(1, 1, 1);
		delete(1, 2, 2);
		
		files = dbm.getFilesInFolder("FOLDER1");
		assertFalse(files == null);
		assertTrue(files.isEmpty());
	}
	
	@Test
	public void testGetFileRecord() throws SQLException, BackBoxException {
		File file = dbm.getFileRecord("FOLDER1", "FILENAME1", "HASH1");
		assertTrue(file == null);
		
		insert(1, 1, 1);
		
		file = dbm.getFileRecord("FOLDER1", "FILENAME1", "HASH1");
		assertFalse(file == null);
		assertTrue(file.getFolder().equals("FOLDER1"));
		assertTrue(file.getFilename().equals("FILENAME1"));
		assertTrue(file.getHash().equals("HASH1"));
		
		List<Chunk> chunks = file.getChunks();
		assertTrue(chunks.size() == 2);
		
		Chunk c = chunks.get(0);
		assertTrue(c.getBoxid().equals("BOXID1"));
		assertTrue(c.getChunkhash().equals("CHUNKHASH1"));
		assertTrue(c.getChunkname().equals(file.getHash()));
		
		delete(1, 1, 1);
		
		file = dbm.getFileRecord("FOLDER1", "FILENAME1", "HASH1");
		assertTrue(file == null);
	}
	
	@Test
	public void testGetAllChunks() throws SQLException, BackBoxException {
		List<Chunk> chunks = dbm.getAllChunks();
		assertFalse(chunks == null);
		assertTrue(chunks.isEmpty());
		
		insert(1, 1, 1);
		
		chunks = dbm.getAllChunks();
		assertFalse(chunks == null);
		assertTrue(chunks.size() == 2);
		
		insert(3, 3, 3);
		
		chunks = dbm.getAllChunks();
		assertFalse(chunks == null);
		assertTrue(chunks.size() == 4);
		
		delete(1, 1, 1);
		delete(3, 3, 3);
		
		chunks = dbm.getAllChunks();
		assertFalse(chunks == null);
		assertTrue(chunks.isEmpty());
	}
}
