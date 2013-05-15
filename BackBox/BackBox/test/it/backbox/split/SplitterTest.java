package it.backbox.split;

import static org.junit.Assert.assertTrue;
import it.backbox.bean.Chunk;
import it.backbox.util.TestUtil;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;

public class SplitterTest {
	
	private static Splitter s;
	private static byte[] in;

	@BeforeClass
	public static void setUpBeforeClass() throws IOException {
		s = new Splitter(1024*1024);
		
		in = TestUtil.read(TestUtil.filename);
	}
	
	@Test
	public void splitByteArrayByteArray() throws Exception {
		ArrayList<byte[]> splitted = s.split(in);
		byte[] merged = s.merge(splitted);
		
		assertTrue(TestUtil.checkTest(in, merged));
	}
	
	@Test
	public void splitByteArrayFile() throws Exception {
		ArrayList<byte[]> splitted = s.split(in);
		
		for(int i = 0; i < splitted.size(); i++)
			TestUtil.write(splitted.get(i), TestUtil.folder + "\\splitByteArrayFile.c" + i);
		
		s.merge(splitted, TestUtil.folder + "\\mergedByteArrayFile");
		
		assertTrue(TestUtil.checkTest(in, TestUtil.folder + "\\mergedByteArrayFile"));
	}

	@Test
	public void splitByteArrayChunk() throws Exception {
		ArrayList<byte[]> splitted = s.split(in);
		
		ArrayList<Chunk> chunks = new ArrayList<>();
		for(int i = 0; i < splitted.size(); i++) {
			Chunk chunk = new Chunk();
			chunk.setChunkname("splitByteArrayChunk.c" + i);
			chunk.setContent(splitted.get(i));
			chunks.add(chunk);
		}
		
		byte[] merged = s.mergeChunk(chunks);
		
		assertTrue(TestUtil.checkTest(in, merged));
	}
	
	@Test
	public void splitByteArrayChunkFile() throws Exception {
		ArrayList<byte[]> splitted = s.split(in);
		
		ArrayList<Chunk> chunks = new ArrayList<>();
		for(int i = 0; i < splitted.size(); i++) {
			Chunk chunk = new Chunk();
			chunk.setChunkname("splitByteArrayChunkFile.c" + i);
			chunk.setContent(splitted.get(i));
			chunks.add(chunk);
		}
		
		s.mergeChunk(chunks, TestUtil.folder + "\\mergedByteArrayChunkFile");
		
		assertTrue(TestUtil.checkTest(in, TestUtil.folder + "\\mergedByteArrayChunkFile"));
	}
	
	@Test
	public void splitFileByteArray() throws Exception {
		ArrayList<byte[]> splitted = s.split(TestUtil.filename);
		byte[] merged = s.merge(splitted);
		
		assertTrue(TestUtil.checkTest(in, merged));
	}
	
	@Test
	public void splitFileFile() throws Exception {
		ArrayList<byte[]> splitted = s.split(TestUtil.filename);
		
		for(int i = 0; i < splitted.size(); i++)
			TestUtil.write(splitted.get(i), TestUtil.folder + "\\splitFileFile.c" + i);
		
		s.merge(splitted, TestUtil.folder + "\\mergedFileFile");
		
		assertTrue(TestUtil.checkTest(in, TestUtil.folder + "\\mergedFileFile"));
	}

	@Test
	public void splitFileChunk() throws Exception {
		ArrayList<byte[]> splitted = s.split(TestUtil.filename);
		
		ArrayList<Chunk> chunks = new ArrayList<>();
		for(int i = 0; i < splitted.size(); i++) {
			Chunk chunk = new Chunk();
			chunk.setChunkname("splitFileChunk.c" + i);
			chunk.setContent(splitted.get(i));
			chunks.add(chunk);
		}
		
		byte[] merged = s.mergeChunk(chunks);
		
		assertTrue(TestUtil.checkTest(in, merged));
	}
	
	@Test
	public void splitFileChunkFile() throws Exception {
		ArrayList<byte[]> splitted = s.split(TestUtil.filename);
		
		ArrayList<Chunk> chunks = new ArrayList<>();
		for(int i = 0; i < splitted.size(); i++) {
			Chunk chunk = new Chunk();
			chunk.setChunkname("splitFileChunkFile.c" + i);
			chunk.setContent(splitted.get(i));
			chunks.add(chunk);
		}
		
		s.mergeChunk(chunks, TestUtil.folder + "\\mergedFileChunkFile");
		
		assertTrue(TestUtil.checkTest(in, TestUtil.folder + "\\mergedFileChunkFile"));
	}
	
	@Test
	public void splitByteArrayFileByteArray() throws Exception {
		ArrayList<String> chunkNames = s.split(in, "ByteArrayFileByteArray", TestUtil.folder);
		
		ArrayList<byte[]> splitted = new ArrayList<>();
		for (String s : chunkNames)
			splitted.add(TestUtil.read(s));
		
		byte[] merged = s.merge(splitted);
		
		assertTrue(TestUtil.checkTest(in, merged));
	}
	
	@Test
	public void splitByteArrayFileFile() throws Exception {
		ArrayList<String> chunkNames = s.split(in, "ByteArrayFileFile", TestUtil.folder);
		
		ArrayList<byte[]> splitted = new ArrayList<>();
		for (String s : chunkNames)
			splitted.add(TestUtil.read(s));
		
		s.merge(splitted, TestUtil.folder + "\\mergedByteArrayFileFile");
		
		assertTrue(TestUtil.checkTest(in, TestUtil.folder + "\\mergedByteArrayFileFile"));
	}

	@Test
	public void splitByteArrayChunkByteArray() throws Exception {
		ArrayList<String> chunkNames = s.split(in, "ByteArrayChunkByteArray", TestUtil.folder);
		
		ArrayList<Chunk> chunks = new ArrayList<>();
		for(int i = 0; i < chunkNames.size(); i++) {
			Chunk chunk = new Chunk();
			chunk.setChunkname(chunkNames.get(i));
			chunk.setContent(TestUtil.read(chunkNames.get(i)));
			chunks.add(chunk);
		}
		
		byte[] merged = s.mergeChunk(chunks);
		
		assertTrue(TestUtil.checkTest(in, merged));
	}
	
	@Test
	public void splitByteArrayChunkFileByteArray() throws Exception {
		ArrayList<String> chunkNames = s.split(in, "ByteArrayChunkFileByteArray", TestUtil.folder);
		
		ArrayList<Chunk> chunks = new ArrayList<>();
		for(int i = 0; i < chunkNames.size(); i++) {
			Chunk chunk = new Chunk();
			chunk.setChunkname(chunkNames.get(i));
			chunk.setContent(TestUtil.read(chunkNames.get(i)));
			chunks.add(chunk);
		}
		
		s.mergeChunk(chunks, TestUtil.folder + "\\mergedByteArrayChunkFileByteArray");
		
		assertTrue(TestUtil.checkTest(in, TestUtil.folder + "\\mergedByteArrayChunkFileByteArray"));
	}
	
	@Test
	public void splitFileFileByteArray() throws Exception {
		ArrayList<String> chunkNames = s.split(TestUtil.filename, "FileFileByteArray", TestUtil.folder);
		
		ArrayList<byte[]> splitted = new ArrayList<>();
		for (String s : chunkNames)
			splitted.add(TestUtil.read(s));
		
		byte[] merged = s.merge(splitted);
		
		assertTrue(TestUtil.checkTest(in, merged));
	}
	
	@Test
	public void splitFileFileFile() throws Exception {
		ArrayList<String> chunkNames = s.split(TestUtil.filename, "FileFileByteArray", TestUtil.folder);
		
		ArrayList<byte[]> splitted = new ArrayList<>();
		for (String s : chunkNames)
			splitted.add(TestUtil.read(s));
		
		s.merge(splitted, TestUtil.folder + "\\mergedFileFileFile");
		
		assertTrue(TestUtil.checkTest(in, TestUtil.folder + "\\mergedFileFileFile"));
	}

	@Test
	public void splitFileChunkByteArray() throws Exception {
		ArrayList<String> chunkNames = s.split(TestUtil.filename, "FileFileByteArray", TestUtil.folder);
		
		ArrayList<Chunk> chunks = new ArrayList<>();
		for(int i = 0; i < chunkNames.size(); i++) {
			Chunk chunk = new Chunk();
			chunk.setChunkname(chunkNames.get(i));
			chunk.setContent(TestUtil.read(chunkNames.get(i)));
			chunks.add(chunk);
		}
		
		byte[] merged = s.mergeChunk(chunks);
		
		assertTrue(TestUtil.checkTest(in, merged));
	}
	
	@Test
	public void splitFileChunkFileByteArray() throws Exception {
		ArrayList<String> chunkNames = s.split(TestUtil.filename, "FileFileByteArray", TestUtil.folder);
		
		ArrayList<Chunk> chunks = new ArrayList<>();
		for(int i = 0; i < chunkNames.size(); i++) {
			Chunk chunk = new Chunk();
			chunk.setChunkname(chunkNames.get(i));
			chunk.setContent(TestUtil.read(chunkNames.get(i)));
			chunks.add(chunk);
		}
		
		s.mergeChunk(chunks, TestUtil.folder + "\\mergedFileChunkFileByteArray");
		
		assertTrue(TestUtil.checkTest(in, TestUtil.folder + "\\mergedFileChunkFileByteArray"));
	}
	
	@Test
	public void splitByteArrayChunkByteArray2() throws Exception {
		ArrayList<Chunk> chunks = s.splitChunk(in, "ByteArrayChunkByteArray2");
		
		ArrayList<byte[]> splitted = new ArrayList<>();
		for (Chunk c : chunks)
			splitted.add(c.getContent());
		
		byte[] merged = s.merge(splitted);
		
		assertTrue(TestUtil.checkTest(in, merged));
	}
	
	@Test
	public void splitByteArrayChunkByteArrayFile() throws Exception {
		ArrayList<Chunk> chunks = s.splitChunk(in, "ByteArrayChunkByteArrayFile");
		
		ArrayList<byte[]> splitted = new ArrayList<>();
		for (Chunk c : chunks)
			splitted.add(c.getContent());
		
		s.merge(splitted, TestUtil.folder + "\\mergedByteArrayChunkByteArrayFile");
		
		assertTrue(TestUtil.checkTest(in, TestUtil.folder + "\\mergedByteArrayChunkByteArrayFile"));
	}

	@Test
	public void splitChunkByteArray() throws Exception {
		ArrayList<Chunk> chunks = s.splitChunk(in, "ChunkByteArray");
		
		byte[] merged = s.mergeChunk(chunks);
		
		assertTrue(TestUtil.checkTest(in, merged));
	}
	
	@Test
	public void splitChunkFileByteArray() throws Exception {
		ArrayList<Chunk> chunks = s.splitChunk(in, "ChunkFileByteArray");
		
		s.mergeChunk(chunks, TestUtil.folder + "\\mergedChunkFileByteArray");
		
		assertTrue(TestUtil.checkTest(in, TestUtil.folder + "\\mergedChunkFileByteArray"));
	}
	
	@Test
	public void splitFileChunkByteArray2() throws Exception {
		ArrayList<Chunk> chunks = s.splitChunk(TestUtil.filename, "FileChunkByteArray2");
		
		ArrayList<byte[]> splitted = new ArrayList<>();
		for (Chunk c : chunks)
			splitted.add(c.getContent());
		
		byte[] merged = s.merge(splitted);
		
		assertTrue(TestUtil.checkTest(in, merged));
	}
	
	@Test
	public void splitFileChunkByteArrayFile() throws Exception {
		ArrayList<Chunk> chunks = s.splitChunk(TestUtil.filename, "FileChunkByteArrayFile");
		
		ArrayList<byte[]> splitted = new ArrayList<>();
		for (Chunk c : chunks)
			splitted.add(c.getContent());
		
		s.merge(splitted, TestUtil.folder + "\\mergedFileChunkByteArrayFile");
		
		assertTrue(TestUtil.checkTest(in, TestUtil.folder + "\\mergedFileChunkByteArrayFile"));
	}

	@Test
	public void splitFileChunkByteArray3() throws Exception {
		ArrayList<Chunk> chunks = s.splitChunk(TestUtil.filename, "FileChunkByteArray3");
		
		byte[] merged = s.mergeChunk(chunks);
		
		assertTrue(TestUtil.checkTest(in, merged));
	}
	
	@Test
	public void splitFileChunkFileByteArray2() throws Exception {
		ArrayList<Chunk> chunks = s.splitChunk(TestUtil.filename, "FileChunkFileByteArray2");
		
		s.mergeChunk(chunks, TestUtil.folder + "\\mergedFileChunkFileByteArray2");
		
		assertTrue(TestUtil.checkTest(in, TestUtil.folder + "\\mergedFileChunkFileByteArray2"));
	}
}
