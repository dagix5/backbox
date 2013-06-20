package it.backbox.security;

import it.backbox.ISecurityManager;
import it.backbox.exception.BackBoxException;
import it.backbox.exception.BackBoxWrongPasswordException;
import it.backbox.utility.Utility;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

public class SecurityManager implements ISecurityManager{
	private static Logger _log = Logger.getLogger(SecurityManager.class.getCanonicalName());

	private static final String ENCRYPT_ALGO = "AES/CBC/PKCS5Padding";
	private static final String ENCRYPT_ALGO_GEN_KEY = "AES";
	private static final String GEN_KEY_ALGO = "PBKDF2WithHmacSHA1";
	private static final String RNG_ALGO = "SHA1PRNG";
	private static final int GEN_KEY_ITERATIONS = 65536;
	private static final int GEN_KEY_LENGTH = 128;
	private static final int SALT_LENGTH = 8;
	private static final int IV_LENGTH = 16;
	private static final String CHARSET = "UTF-8";
	
	private Key key;
	private byte[] salt;
	private String pwdDigest;

	/**
	 * Constructor
	 * 
	 * @param password
	 *            User password
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 * @throws IOException 
	 * @throws BackBoxException 
	 * @throws DecoderException 
	 * @throws BackBoxWrongPasswordException 
	 */
	public SecurityManager(String password) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, BackBoxException, DecoderException, BackBoxWrongPasswordException {
		this(password, null, null);
	}

	/**
	 * Constructor
	 * 
	 * @param password
	 *            User password
	 * @param pwdDigest
	 *            Saved user password
	 * @param salt
	 *            Salt
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws BackBoxException
	 * @throws DecoderException
	 * @throws BackBoxWrongPasswordException 
	 */
	public SecurityManager(String password, String pwdDigest, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, BackBoxException, DecoderException, BackBoxWrongPasswordException {
		if ((pwdDigest != null) && (salt != null)) { 
			this.pwdDigest = pwdDigest;
			this.salt = Hex.decodeHex(salt.toCharArray());
			if (!checkPassword(password))
				throw new BackBoxWrongPasswordException("Wrong Password");
			generateKey(password, this.salt);
		} else {
			this.pwdDigest = DigestUtils.sha1Hex(password.getBytes(CHARSET));
			generateKey(password);
		}
	}
	

	/**
	 * Check if the password is correct
	 * 
	 * @param password
	 *            Password to check
	 * @return true if the password is correct, false otherwise
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	private boolean checkPassword(String password) throws NoSuchAlgorithmException, IOException {
		if (pwdDigest == null)
			return false;
		Charset cs = Charset.forName(CHARSET);
		String pwdDigestCalc = DigestUtils.sha1Hex(password.getBytes(cs));
		return pwdDigestCalc.equals(pwdDigest);
	}
	
	/**
	 * Generate a algorithm specific key from a password
	 * 
	 * @param password
	 *            User password
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeySpecException 
	 */
	private void generateKey(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
		if (password != null) {
			SecureRandom random = SecureRandom.getInstance(RNG_ALGO);
			salt = new byte[SALT_LENGTH];
			random.nextBytes(salt);
			generateKey(password, salt);
		} else
			_log.severe("Password null: key not generated");
	}
	
	/**
	 * Generate a algorithm specific key from a password
	 * 
	 * @param password
	 *            User password
	 * @param salt
	 *            Pre-generated salt
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	private void generateKey(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
		if ((password != null) || (salt != null)) {
			SecretKeyFactory factory = SecretKeyFactory.getInstance(GEN_KEY_ALGO);
			KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, GEN_KEY_ITERATIONS, GEN_KEY_LENGTH);
			SecretKey tmp = factory.generateSecret(spec);
			key = new SecretKeySpec(tmp.getEncoded(), ENCRYPT_ALGO_GEN_KEY);
			_log.fine("Key generated");
		} else
			_log.severe("Password/Salt null: key not generated");
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.ISecurityManager#encrypt(java.io.InputStream, java.io.OutputStream)
	 */
	@Override
	public void encrypt(InputStream in, OutputStream out) throws Exception {
		try {
			Cipher c = Cipher.getInstance(ENCRYPT_ALGO);
			c.init(Cipher.ENCRYPT_MODE, key);
			
			AlgorithmParameters params = c.getParameters();
			byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();

			out.write(iv);
			
			byte[] buf = new byte[Utility.BUFFER];
			int count = in.read(buf);
			while (count >= 0) {
				out.write(c.update(buf, 0, count)); 
				count = in.read(buf);
			}
			out.write(c.doFinal());
			out.flush();

			_log.fine("encrypt ok");
		} finally {
			if (in != null)
				in.close();
			if (out != null)
				out.close();
		}
	}

	
	/*
	 * (non-Javadoc)
	 * @see it.backbox.ISecurityManager#encrypt(byte[])
	 */
	@Override
	public byte[] encrypt(byte[] src) throws Exception {
		InputStream in = new BufferedInputStream(new ByteArrayInputStream(src));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encrypt(in, out);
		return out.toByteArray();
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.ISecurityManager#ecnrypt(java.lang.String)
	 */
	@Override
	public byte[] encrypt(String filename) throws Exception {
		InputStream in = new BufferedInputStream(new FileInputStream(filename));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encrypt(in, out);
		return out.toByteArray();
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.ISecurityManager#encrypt(java.lang.String, java.lang.String)
	 */
	@Override
	public void encrypt(String srcfilename, String destfilename) throws Exception {
		InputStream in = new BufferedInputStream(new FileInputStream(srcfilename));
		OutputStream out = Utility.getOutputStream(destfilename);
		encrypt(in, out);
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.ISecurityManager#decrypt(java.io.InputStream, java.io.OutputStream)
	 */
	@Override
	public void decrypt(InputStream in, OutputStream out) throws Exception {
		try {
			byte[] iv = new byte[IV_LENGTH];
			int count = in.read(iv, 0, IV_LENGTH);
			if (count < IV_LENGTH)
				throw new IllegalBlockSizeException("IV not found");
			
			Cipher c = Cipher.getInstance(ENCRYPT_ALGO);
			c.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
			
			byte[] buf = new byte[Utility.BUFFER];
			count = in.read(buf);

			while (count >= 0) {
				out.write(c.update(buf, 0, count)); 
				count = in.read(buf);
			}
			out.write(c.doFinal());
			out.flush();
			
			_log.fine("decrypt ok");
		} finally {
			if (in != null)
				in.close();
			if (out != null)
				out.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.ISecurityManager#decrypt(byte[])
	 */
	@Override
	public byte[] decrypt(byte[] src) throws Exception {
		InputStream in = new BufferedInputStream(new ByteArrayInputStream(src));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		decrypt(in, out);
		return out.toByteArray();
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.ISecurityManager#decrypt(java.lang.String)
	 */
	@Override
	public byte[] decrypt(String filename) throws Exception {
		InputStream in = new BufferedInputStream(new FileInputStream(filename));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		decrypt(in, out);
		return out.toByteArray();
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.ISecurityManager#decrypt(java.lang.String, java.lang.String)
	 */
	@Override
	public void decrypt(String srcfilename, String destfilename) throws Exception {
		InputStream in = new BufferedInputStream(new FileInputStream(srcfilename));
		OutputStream out = Utility.getOutputStream(destfilename);
		decrypt(in, out);
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.ISecurityManager#decrypt(byte[], java.lang.String)
	 */
	@Override
	public void decrypt(byte[] src, String destfilename) throws Exception {
		InputStream in = new BufferedInputStream(new ByteArrayInputStream(src));
		OutputStream out = Utility.getOutputStream(destfilename);
		decrypt(in, out);
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.ISecurityManager#getSalt()
	 */
	@Override
	public byte[] getSalt() {
		return salt;
	}

	/*
	 * (non-Javadoc)
	 * @see it.backbox.ISecurityManager#getPwdDigest()
	 */
	@Override
	public String getPwdDigest() {
		return pwdDigest;
	}

}
