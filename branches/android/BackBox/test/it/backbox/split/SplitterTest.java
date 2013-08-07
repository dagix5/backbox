package it.backbox.split;

import static org.junit.Assert.assertTrue;
import it.backbox.bean.Chunk;
import it.backbox.util.TestUtil;
import it.backbox.utility.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

public class SplitterTest {
	
	private static Splitter s;
	private static byte[] in;

	@BeforeClass
	public static void setUpBeforeClass() throws IOException {
		s = new Splitter(1024*1024);
		
		in = Utility.read(TestUtil.filename);
	}
	
	@Test
	public void splitByteArrayByteArray() throws Exception {
		List<byte[]> splitted = s.split(in);
		byte[] merged = s.merge(splitted);
		
		assertTrue(TestUtil.checkTest(in, merged));
	}
	
	@Test
	public void splitByteArrayFile() throws Exception {
		List<byte[]> splitted = s.split(in);
		
		for(int i = 0; i < splitted.size(); i++)
			TestUtil.write(splitted.get(i), TestUtil.folder + "\\splitByteArrayFile.c" + i);
		
		s.merge(splitted, TestUtil.folder + "\\mergedByteArrayFile");
		
		assertTrue(TestUtil.checkTest(in, TestUtil.folder + "\\mergedByteArrayFile"));
	}

	@Test
	public void splitByteArrayChunkFile() throws Exception {
		List<byte[]> splitted = s.split(in);
		
		List<Chunk> chunks = new ArrayList<Chunk>();
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
	public void splitByteArrayChunkByteArray2() throws Exception {
		List<Chunk> chunks = s.splitChunk(in, "ByteArrayChunkByteArray2");
		
		List<byte[]> splitted = new ArrayList<byte[]>();
		for (Chunk c : chunks)
			splitted.add(c.getContent());
		
		byte[] merged = s.merge(splitted);
		
		assertTrue(TestUtil.checkTest(in, merged));
	}
	
	@Test
	public void splitByteArrayChunkByteArrayFile() throws Exception {
		List<Chunk> chunks = s.splitChunk(in, "ByteArrayChunkByteArrayFile");
		
		List<byte[]> splitted = new ArrayList<byte[]>();
		for (Chunk c : chunks)
			splitted.add(c.getContent());
		
		s.merge(splitted, TestUtil.folder + "\\mergedByteArrayChunkByteArrayFile");
		
		assertTrue(TestUtil.checkTest(in, TestUtil.folder + "\\mergedByteArrayChunkByteArrayFile"));
	}

	@Test
	public void splitChunkFileByteArray() throws Exception {
		List<Chunk> chunks = s.splitChunk(in, "ChunkFileByteArray");
		
		s.mergeChunk(chunks, TestUtil.folder + "\\mergedChunkFileByteArray");
		
		assertTrue(TestUtil.checkTest(in, TestUtil.folder + "\\mergedChunkFileByteArray"));
	}
	
}
