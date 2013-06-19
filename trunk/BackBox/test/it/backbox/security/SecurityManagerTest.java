package it.backbox.security;

import static org.junit.Assert.assertTrue;
import it.backbox.util.TestUtil;
import it.backbox.utility.Utility;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SecurityManagerTest {

	private static SecurityManager sm;
	private static byte[] plain;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		plain = Utility.read(TestUtil.filename);
	}
	
	@Before
	public void setUpBefore() throws Exception {
		sm = new SecurityManager("passwordTest", DigestUtils.sha1Hex("passwordTest"), "1a700f3a263da985");
	}
	
	@Test
	public void testEncryptByteArray1() throws Exception {
		SecurityManager sm = new SecurityManager("passwordTest", DigestUtils.sha1Hex("passwordTest"), "1a700f3a263da985");
		byte[] cypher = sm.encrypt(plain);
		
		byte[] newplain = sm.decrypt(cypher);
		
		assertTrue(TestUtil.checkTest(plain, newplain));
	}
	
	@Test
	public void testEncryptByteArray2() throws Exception {
		byte[] cypher = sm.encrypt(plain);
		
		TestUtil.write(cypher, TestUtil.folder + "testEncryptByteArray2c");
		byte[] newplain = sm.decrypt(TestUtil.folder + "testEncryptByteArray2c");
		
		assertTrue(TestUtil.checkTest(plain, newplain));
	}
	
	@Test
	public void testEncryptByteArray3() throws Exception {
		byte[] cypher = sm.encrypt(plain);
		
		sm.decrypt(cypher, TestUtil.folder + "testEncryptByteArray3c");
		
		assertTrue(TestUtil.checkTest(plain, TestUtil.folder + "testEncryptByteArray3c"));
	}
	
	@Test
	public void testEncryptByteArray4() throws Exception {
		byte[] cypher = sm.encrypt(plain);
		
		TestUtil.write(cypher, TestUtil.folder + "testEncryptByteArray4c");
		sm.decrypt(TestUtil.folder + "testEncryptByteArray4c", TestUtil.folder + "testEncryptByteArray4p");
		
		assertTrue(TestUtil.checkTest(plain, TestUtil.folder + "testEncryptByteArray4p"));
	}

	@Test
	public void testEncryptString1() throws Exception {
		byte[] cypher = sm.encrypt(TestUtil.filename);
		
		byte[] newplain = sm.decrypt(cypher);
		
		assertTrue(TestUtil.checkTest(plain, newplain));
	}
	
	@Test
	public void testEncryptString2() throws Exception {
		byte[] cypher = sm.encrypt(TestUtil.filename);
		TestUtil.write(cypher, TestUtil.folder + "testEncryptString2c");
		
		byte[] newplain = sm.decrypt(TestUtil.folder + "testEncryptString2c");
		
		assertTrue(TestUtil.checkTest(plain, newplain));
	}
	
	@Test
	public void testEncryptString3() throws Exception {
		byte[] cypher = sm.encrypt(TestUtil.filename);
		
		sm.decrypt(cypher, TestUtil.folder + "testEncryptString3c");
		
		assertTrue(TestUtil.checkTest(plain, TestUtil.folder + "testEncryptString3c"));
	}
	
	@Test
	public void testEncryptString4() throws Exception {
		byte[] cypher = sm.encrypt(TestUtil.filename);
		
		TestUtil.write(cypher, TestUtil.folder + "testEncryptString4c");
		sm.decrypt(TestUtil.folder + "testEncryptString4c", TestUtil.folder + "testEncryptString4p");
		
		assertTrue(TestUtil.checkTest(plain, TestUtil.folder + "testEncryptString4p"));
	}

	@Test
	public void testEncryptStringString1() throws Exception {
		sm.encrypt(TestUtil.filename, TestUtil.folder + "testEncryptStringString1c");
		
		byte[] newplain = sm.decrypt(Utility.read(TestUtil.folder + "testEncryptStringString1c"));
		
		assertTrue(TestUtil.checkTest(plain, newplain));
	}
	
	@Test
	public void testEncryptStringString2() throws Exception {
		sm.encrypt(TestUtil.filename, TestUtil.folder + "testEncryptStringString2c");
		
		byte[] newplain = sm.decrypt(TestUtil.folder + "testEncryptStringString2c");
		
		assertTrue(TestUtil.checkTest(plain, newplain));
	}
	
	@Test
	public void testEncryptStringString3() throws Exception {
		sm.encrypt(TestUtil.filename, TestUtil.folder + "testEncryptStringString3c");
		
		sm.decrypt(Utility.read(TestUtil.folder + "testEncryptStringString3c"), TestUtil.folder + "testEncryptStringString3p");
		
		assertTrue(TestUtil.checkTest(plain, TestUtil.folder + "testEncryptStringString3p"));	
	}
	
	@Test
	public void testEncryptStringString4() throws Exception {
		sm.encrypt(TestUtil.filename, TestUtil.folder + "testEncryptStringString4c");
		
		sm.decrypt(TestUtil.folder + "testEncryptStringString4c", TestUtil.folder + "testEncryptStringString4p");
		
		assertTrue(TestUtil.checkTest(plain, TestUtil.folder + "testEncryptStringString4p"));
	}

	@Test
	public void testEncryptByteArrayString1() throws Exception {
		sm.encrypt(plain, TestUtil.folder + "testEncryptByteArrayString1c");
		
		byte[] newplain = sm.decrypt(Utility.read(TestUtil.folder + "testEncryptByteArrayString1c"));
		
		assertTrue(TestUtil.checkTest(plain, newplain));
	}
	
	@Test
	public void testEncryptByteArrayString2() throws Exception {
		sm.encrypt(plain, TestUtil.folder + "testEncryptByteArrayString2c");
		
		byte[] newplain = sm.decrypt(TestUtil.folder + "testEncryptByteArrayString2c");
		
		assertTrue(TestUtil.checkTest(plain, newplain));
	}
	
	@Test
	public void testEncryptByteArrayString3() throws Exception {
		sm.encrypt(plain, TestUtil.folder + "testEncryptByteArrayString3c");
		
		sm.decrypt(Utility.read(TestUtil.folder + "testEncryptByteArrayString3c"), TestUtil.folder + "testEncryptByteArrayString3p");
		
		assertTrue(TestUtil.checkTest(plain, TestUtil.folder + "testEncryptByteArrayString3p"));
	}
	
	@Test
	public void testEncryptByteArrayString4() throws Exception {
		sm.encrypt(plain, TestUtil.folder + "testEncryptByteArrayString4c");
		
		sm.decrypt(TestUtil.folder + "testEncryptByteArrayString4c", TestUtil.folder + "testEncryptByteArrayString4p");
		
		assertTrue(TestUtil.checkTest(plain, TestUtil.folder + "testEncryptByteArrayString4p"));
	}

}
