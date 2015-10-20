package it.backbox.transaction;

import java.util.concurrent.Phaser;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BBPhaser extends Phaser {
	private static final Logger _log = Logger.getLogger(BBPhaser.class.getCanonicalName());
	
	/*
	 * (non-Javadoc)
	 * @see java.util.concurrent.Phaser#onAdvance(int, int)
	 */
	protected boolean onAdvance(int phase, int registeredParties) {
		if (_log.isLoggable(Level.FINE)) 
			_log.fine("Phaser advancing to phase " + phase + " with " + registeredParties + " registered threads");
		//the phaser won't terminate
		return false;
	}

}
