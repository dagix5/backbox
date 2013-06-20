package it.backbox.transaction;

import java.util.concurrent.Phaser;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BBPhaser extends Phaser {
	private static Logger _log = Logger.getLogger(BBPhaser.class.getCanonicalName());
	
	/*
	 * (non-Javadoc)
	 * @see java.util.concurrent.Phaser#onAdvance(int, int)
	 */
	protected boolean onAdvance(int phase, int registeredParties) {
		if (_log.isLoggable(Level.FINE)) _log.fine(new StringBuilder("Phaser advancing to phase ").append(phase).append(" with ").append(registeredParties).append(" registered threads").toString());
		//the phaser won't terminate
		return false;
	}

}
