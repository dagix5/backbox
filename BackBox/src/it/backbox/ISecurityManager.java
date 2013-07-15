package it.backbox;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public interface ISecurityManager {
	
	/**
	 * Get the user password digest
	 * 
	 * @return The user password digest
	 */
	public String getPwdDigest();
	
	/**
	 * Get pre-generated salt
	 * 
	 * @return The salt
	 */
	public byte[] getSalt();

	/**
	 * Encrypt a byte array
	 * 
	 * @param src
	 *            Byte array to encrypt
	 * @return Encrypted byte array
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidParameterSpecException
	 * @throws IOException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public byte[] encrypt(byte[] src) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidParameterSpecException, IOException, IllegalBlockSizeException, BadPaddingException;

	/**
	 * Encrypt a file
	 * 
	 * @param filename
	 *            Name of the file to encrypt
	 * @return Encrypted file content byte array
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidParameterSpecException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws IOException
	 */
	public byte[] encrypt(String filename) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException, IOException;

	/**
	 * Encrypt a file
	 * 
	 * @param srcfilename
	 *            File to encrypt
	 * @param destfilename
	 *            Encrypted file
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidParameterSpecException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws IOException
	 */
	public void encrypt(String srcfilename, String destfilename) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException, IOException;

	/**
	 * Decrypt a byte array
	 * 
	 * @param src
	 *            Byte array to decrypt
	 * @return Decrypted byte array
	 * @throws IOException
	 * @throws IllegalBlockSizeException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws BadPaddingException
	 */
	public byte[] decrypt(byte[] src) throws IOException, IllegalBlockSizeException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, BadPaddingException;

	/**
	 * Decrypt a file
	 * 
	 * @param filename
	 *            File to decrypt
	 * @return Decrypted byte array
	 * @throws IOException
	 * @throws IllegalBlockSizeException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws BadPaddingException 
	 */
	public byte[] decrypt(String filename) throws IOException, IllegalBlockSizeException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, BadPaddingException;

	/**
	 * Decrypt a file
	 * 
	 * @param srcfilename
	 *            File to decrypt
	 * @param destfilename
	 *            Decrypted file
	 * @throws IOException
	 * @throws IllegalBlockSizeException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws BadPaddingException
	 */
	public void decrypt(String srcfilename, String destfilename) throws IOException, IllegalBlockSizeException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, BadPaddingException;

	/**
	 * Decrypt a byte array content in a file
	 * 
	 * @param src
	 *            Byte array to decrypt
	 * @param destfilename
	 *            Decrypted file
	 * @throws IOException
	 * @throws IllegalBlockSizeException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws BadPaddingException
	 */
	public void decrypt(byte[] src, String destfilename) throws IOException, IllegalBlockSizeException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, BadPaddingException;
	
	/**
	 * Encrypt an InputStream to an OutputStream
	 * 
	 * @param in
	 *            InputStream to encrypt
	 * @param out
	 *            Encrypted OutputStream
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidParameterSpecException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws IOException
	 */
	public void encrypt(InputStream in, OutputStream out) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException, IOException;
	
	/**
	 * Decrypt an InputStream to an OutputStream
	 * 
	 * @param in
	 *            InputStream to decrypt
	 * @param out
	 *            Decrypted OutputStream
	 * @throws IOException
	 * @throws IllegalBlockSizeException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws BadPaddingException
	 */
	public void decrypt(InputStream in, OutputStream out) throws IOException, IllegalBlockSizeException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, BadPaddingException;
}
