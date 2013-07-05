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
		int speed = manager.getSpeed(id);
		if ((speed <= 0) || (speed > len)) {
			int r = super.read(b, off, len);
			manager.consume(id, r);
			if (r > 0)
				if (manager.getListener(id) != null) manager.getListener(id).update(id, r);
			return r;
		}
		int plen = 0;
		int r = 0;
		do {
			if (speed < (len - plen))
				r = super.read(b, (off + plen), speed);
			else
				r = super.read(b, (off + plen), (len - plen));
			manager.consume(id, r);
			if (r > 0) {
				plen += r;
				if (manager.getListener(id) != null) manager.getListener(id).update(id, r);
			}
		} while (r > 0);
		
		if (plen > 0)
			return plen;
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
