package it.backbox.progress.stream;

import it.backbox.progress.ProgressManager;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamCounter extends FilterInputStream {

	private ProgressManager manager;
	private String id;

	/**
	 * Constructor
	 * 
	 * @param in
	 *            Original InputStream
	 * @param id
	 *            Stream ID
	 */
	public InputStreamCounter(final InputStream in, String id) {
		super(in);
		this.id = id;
		manager = ProgressManager.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.FilterInputStream#read(byte[], int, int)
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int r = 0;
		int speed = manager.getSpeed(id);
		int plen = 0;
		if (speed > 0) {
			while (plen < (len - speed)) {
				manager.consume(id, speed);
				r += super.read(b, (off + plen), speed);
				plen += speed;
				if (manager.getListener(id) != null) manager.getListener(id).update(id, speed);
			}
		}
		manager.consume(id, len - plen);
		r += super.read(b, (off + plen), (len - plen));
		if (manager.getListener(id) != null) manager.getListener(id).update(id, len - plen);
		return r;
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.FilterInputStream#read(byte[])
	 */
	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

}
