package it.backbox.transaction;

import it.backbox.exception.BackBoxException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
	 * Run all the tasks in a transaction, sorted by priority
	 */
	@Override
	public void run() {
		boolean inError = false;
		ArrayList<Task> tasks = t.getTasks();
		Collections.sort(tasks, new Comparator<Task>() {

			@Override
			public int compare(Task t1, Task t2) {
				return Short.compare(t2.getPriority(), t1.getPriority());
			}
		});
		for (int i = 0; i < tasks.size(); i++) {
			currentTask = tasks.get(i);
			if (_log.isLoggable(Level.INFO)) _log.info("[" + t.getId() + "] [" + currentTask.getId() + "] " + currentTask.getDescription() + " started");
			
			long start = new Date().getTime();
			try {
				if (stop)
					throw new BackBoxException("Interrupted");
				
				currentTask.run();
				if (currentTask.isCountWeight())
					tm.weightCompleted(currentTask.getWeight());
			} catch (Exception e) {
				_log.log(Level.SEVERE, "[" + t.getId() + "] [" + currentTask.getId() + "] Error", e);
				int rollbackError = -1;
				for (int j = 0; j < i; j++) {
					Task task = tasks.get(j);
					if (!task.rollback()) {
						if (rollbackError > -1)
							rollbackError = j;
						if (_log.isLoggable(Level.WARNING)) 
							_log.log(Level.WARNING, "[" + t.getId() + "] [" + currentTask.getId() + "] Rollback failed");
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
			
			if (_log.isLoggable(Level.INFO)) _log.info("[" + t.getId() + "] [" + currentTask.getId() + "] " + currentTask.getDescription() + " end");
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
