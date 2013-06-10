package it.backbox.transaction.task;

import it.backbox.IBoxManager;
import it.backbox.exception.RestException;

import java.util.concurrent.Phaser;

import com.google.api.client.http.HttpResponseException;

public abstract class BoxTask extends Task {

protected abstract void boxMethod() throws Exception;
	
	protected void callBox() throws Exception {
		IBoxManager bm = getBoxManager();
		Phaser p = getPhaser();
		
		boolean registered = false;
		
		if (!bm.isAccessTokenValid()) {
			if (p.getRegisteredParties() > 0) {
				int f = p.getPhase();
				_log.fine("Phaser - waiting -> " + f);
				int c = p.awaitAdvance(f);
				_log.fine("Phaser - continuing -> " + c);
			} else
				_log.fine("Phaser - no one registered -> not waiting");
		} else {
			int r = p.register();
			_log.fine("Phaser - register -> " + r);
			registered = true;
		}
		
		try {
			try {
				boxMethod();
			} catch (RestException e) {
				HttpResponseException httpe = e.getHttpException();
				if ((httpe != null) && (httpe.getStatusCode() == 401)) {
					_log.fine("Unauthorized");
					//retry because at this point the token should already be refreshed
					if (bm.isAccessTokenValid())
						boxMethod();
				} else
					throw e;
			}
		} finally {
			if (registered) {
				int a = p.arriveAndDeregister();
				_log.fine("Phaser - arrive -> " + a);
			}
		}
	}

}
