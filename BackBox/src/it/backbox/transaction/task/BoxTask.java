package it.backbox.transaction.task;

import it.backbox.IBoxManager;
import it.backbox.exception.RestException;

import java.util.concurrent.Callable;
import java.util.concurrent.Phaser;
import java.util.logging.Level;

import com.google.api.client.http.HttpResponseException;

public abstract class BoxTask extends Task {

	protected void callBox(Callable<Void> callable) throws Exception {
		IBoxManager bm = getBoxManager();
		Phaser p = getPhaser();
		
		boolean registered = false;
		
		if (!bm.isAccessTokenValid()) {
			if (p.getRegisteredParties() > 0) {
				int f = p.getPhase();
				if (_log.isLoggable(Level.FINE)) _log.fine("Phaser - waiting -> " + f);
				int c = p.awaitAdvance(f);
				if (_log.isLoggable(Level.FINE)) _log.fine("Phaser - continuing -> " + c);
			} else
				if (_log.isLoggable(Level.FINE)) _log.fine("Phaser - no one registered -> not waiting");
		} else {
			int r = p.register();
			if (_log.isLoggable(Level.FINE)) _log.fine("Phaser - register -> " + r);
			registered = true;
		}
		
		try {
			try {
				callable.call();
			} catch (RestException e) {
				HttpResponseException httpe = e.getHttpException();
				if ((httpe != null) && (httpe.getStatusCode() == 401)) {
					if (_log.isLoggable(Level.FINE)) _log.fine("Unauthorized");
					//retry because at this point the token should already be refreshed
					if (bm.isAccessTokenValid())
						callable.call();
				} else
					throw e;
			}
		} finally {
			if (registered) {
				int a = p.arriveAndDeregister();
				if (_log.isLoggable(Level.FINE)) _log.fine("Phaser - arrive -> " + a);
			}
		}
	}

}
