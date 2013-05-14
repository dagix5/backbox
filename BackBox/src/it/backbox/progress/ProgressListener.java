package it.backbox.progress;

public interface ProgressListener {
	
	/**
	 * Method called when an update (read/write) is performed in a stream
	 * 
	 * @param id
	 *            ID of the stream
	 * @param bytes
	 *            Bytes read or written
	 */
	void update(String id, long bytes);

}
