package it.backbox.progress.stream;

import it.backbox.progress.ProgressManager;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamCounter extends FilterInputStream {

	private ProgressManager manager;
	private String id;

	public InputStreamCounter(final InputStream in, String id) {
		super(in);
		this.id = id;
		manager = ProgressManager.getInstance();
	}

	public int read(byte[] b, int off, int len) throws IOException {
		int r = 0;
		int speed = manager.getSpeed(id);
		int plen = 0;
		if (speed > 0) {
			while (plen < (len - speed)) {
				manager.consume(id, speed);
				r += super.read(b, (off + plen), speed);
				plen += speed;
				if (manager.getListener(id) != null) manager.getListener(id).update(speed);
			}
		}
		manager.consume(id, len - plen);
		r += super.read(b, (off + plen), (len - plen));
		if (manager.getListener(id) != null) manager.getListener(id).update(len - plen);
		return r;
	}

	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

}
