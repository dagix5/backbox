package it.backbox.transaction;

import it.backbox.exception.BackBoxException;
import it.backbox.transaction.task.Task;
import it.backbox.transaction.task.Transaction;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransactionThread implements Runnable {
	private static Logger _log = Logger.getLogger(TransactionThread.class.getCanonicalName());

	private TransactionManager tm;
	
	protected Transaction t;
	private Task currentTask;
	private boolean stop = false;

	/**
	 * Constructor
	 * 
	 * @param t
	 *            Transaction to run
	 */
	public TransactionThread(TransactionManager tm, Transaction t) {
		this.tm = tm;
		this.t = t;
	}

	/**
	 * Run all the tasks in a transaction
	 */
	@Override
	public void run() {
		for (Task task : t.getTasks()) {
			if (_log.isLoggable(Level.FINE)) _log.fine(task.getDescription() + "-> start");
			
			boolean inError = false;
			long start = new Date().getTime();
			try {
				if (stop)
					throw new BackBoxException("Interrupted");
				currentTask = task;
				
				currentTask.run();
				if (currentTask.isCountWeight())
					tm.taskCompleted(currentTask.getWeight());
			} catch (Exception e) {
				_log.log(Level.SEVERE, "Error", e);
				StringBuilder error = new StringBuilder("Error during execution task ");
//				error.append(t.getDescription()).append(" ");
				error.append(task.getDescription()).append(": ").append(e.toString());
				t.setResultDescription(error.toString());
				t.setResultCode(Transaction.ESITO_KO);
				inError = true;
			}
			long finish = new Date().getTime();
			currentTask.setTotalTime(finish - start);
			
			if (inError)
				return;
			
			if (_log.isLoggable(Level.FINE)) _log.fine(task.getDescription() + "-> end");
		}
		t.setResultCode(Transaction.ESITO_OK);

	}
	
	/**
	 * Try to stop the running task
	 */
	public void stop() {
		if (currentTask != null)
			currentTask.stop();
		stop = true;
	}

}
