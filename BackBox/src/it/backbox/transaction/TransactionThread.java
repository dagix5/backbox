package it.backbox.transaction;

import it.backbox.exception.BackBoxException;
import it.backbox.transaction.task.Task;
import it.backbox.transaction.task.Transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransactionThread implements Runnable {
	private static final Logger _log = Logger.getLogger(TransactionThread.class.getCanonicalName());

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
		boolean inError = false;
		ArrayList<Task> tasks = t.getTasks();
		for (int i = 0; i < tasks.size(); i++) {
			currentTask = tasks.get(i);
			if (_log.isLoggable(Level.INFO)) _log.info(currentTask.getDescription() + "-> start");
			
			long start = new Date().getTime();
			try {
				if (stop)
					throw new BackBoxException("Interrupted");
				
				currentTask.run();
				if (currentTask.isCountWeight())
					tm.weightCompleted(currentTask.getWeight());
			} catch (Exception e) {
				_log.log(Level.SEVERE, "Error occurred in transaction " + t.getId() + ", task " + currentTask.getId(), e);
				int rollbackError = -1;
				for (int j = 0; j < i; j++) {
					Task task = tasks.get(j);
					if (!task.rollback()) {
						if (rollbackError > -1)
							rollbackError = j;
						_log.log(Level.WARNING, "Rollback failed at task: " + task.getId());
					}
				}
				if (rollbackError == -1) {
					t.setResultDescription("Rollback at task " + currentTask.getDescription() + ": " + e.toString());
					t.setResultCode(Transaction.Result.ROLLBACK);
				} else {
					t.setResultDescription("Error during execution task " + currentTask.getDescription() + ": " + e.toString() + ". Rollback failed at task " + tasks.get(rollbackError).getDescription());
					t.setResultCode(Transaction.Result.KO);
				}
				inError = true;
			}
			long finish = new Date().getTime();
			currentTask.setTotalTime(finish - start);
			
			if (inError)
				break;
			
			if (_log.isLoggable(Level.INFO)) _log.info(currentTask.getDescription() + "-> end");
		}
		if (!inError)
			t.setResultCode(Transaction.Result.OK);
		tm.taskCompleted(t);
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
