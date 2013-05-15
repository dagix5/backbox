package it.backbox;

public interface ISecurityManager {

	/**
	 * Encrypt a byte array
	 * 
	 * @param src
	 *            Byte array to encrypt
	 * @return Encrypted byte array
	 * @throws Exception 
	 */
	public byte[] encrypt(byte[] src) throws Exception;

	/**
	 * Encrypt a file
	 * 
	 * @param filename
	 *            Name of the file to encrypt
	 * @return Encrypted file content byte array
	 * @throws Exception 
	 */
	public byte[] encrypt(String filename) throws Exception;

	/**
	 * Encrypt a file
	 * 
	 * @param srcfilename
	 *            File to encrypt
	 * @param destfilename
	 *            Encrypted file
	 * @throws Exception 
	 */
	public void encrypt(String srcfilename, String destfilename) throws Exception;

	/**
	 * Encrypt a byte array content in a file
	 * 
	 * @param src
	 *            Byte array to encrypt
	 * @param destfilename
	 *            Encrypted file
	 * @throws Exception 
	 */
	public void encrypt(byte[] src, String destfilename) throws Exception;

	/**
	 * Decrypt a byte array
	 * 
	 * @param src
	 *            Byte array to decrypt
	 * @return Decrypted byte array
	 * @throws Exception 
	 */
	public byte[] decrypt(byte[] src) throws Exception;

	/**
	 * Decrypt a file
	 * 
	 * @param filename
	 *            File to decrypt
	 * @return Decrypted byte array
	 * @throws Exception 
	 */
	public byte[] decrypt(String filename) throws Exception;

	/**
	 * Decrypt a file
	 * 
	 * @param srcfilename
	 *            File to decrypt
	 * @param destfilename
	 *            Decrypted file
	 * @throws Exception 
	 */
	public void decrypt(String srcfilename, String destfilename) throws Exception;

	/**
	 * Decrypt a byte array content in a file
	 * 
	 * @param src
	 *            Byte array to decrypt
	 * @param destfilename
	 *            Decrypted file
	 * @throws Exception 
	 */
	public void decrypt(byte[] src, String destfilename) throws Exception;
}
