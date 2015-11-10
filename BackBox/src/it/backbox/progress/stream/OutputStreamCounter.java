package it.backbox.progress.stream;

import it.backbox.progress.ProgressManager;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamCounter extends FilterOutputStream {

	private ProgressManager manager;
	private String id;
    
	/**
	 * Constructor
	 * 
	 * @param in
	 *            Original OutputStream
	 * @param id
	 *            Stream ID
	 */
    public OutputStreamCounter(final OutputStream out, String id) {
        super(out);
        this.id = id;
		manager = ProgressManager.getInstance();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
    	int speed = manager.getSpeed(id);
    	int plen = 0;
    	if (speed > 0) {
	    	while(plen < (len - speed)) {
	    		manager.consume(id, speed);
	    		super.write(b, (off + plen), speed);
	    		plen += speed;
	    		if (manager.getListener(id) != null) manager.getListener(id).update(id, speed);
	    	}
    	}
    	super.write(b, (off + plen), (len - plen));
        if (manager.getListener(id) != null) manager.getListener(id).update(id, len - plen);
    }

    @Override
    public void write(int b) throws IOException {
    	manager.consume(id, 1);
        super.write(b);
        if (manager.getListener(id) != null) manager.getListener(id).update(id, 1);
    }
}