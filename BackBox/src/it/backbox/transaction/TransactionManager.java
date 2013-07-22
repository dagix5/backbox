package it.backbox.transaction;

import it.backbox.IBoxManager;
import it.backbox.ICompress;
import it.backbox.IDBManager;
import it.backbox.ISecurityManager;
import it.backbox.ISplitter;
import it.backbox.transaction.task.Task;
import it.backbox.transaction.task.Transaction;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransactionManager {
	private static Logger _log = Logger.getLogger(TransactionManager.class.getCanonicalName());
	
	private LinkedList<Transaction> transactions;
	private ExecutorService executor;
	private boolean running;
	private int completedTasksWeight;
	private long allTasksWeight;
	private BBPhaser phaser;
	
	private List<CompleteTransactionListener> listeners;
	
	private IBoxManager boxManager;
	private IDBManager dbManager;
	private ISecurityManager securityManager;
	private ISplitter splitter;
	private ICompress zipper;
	
	/**
	 * Constructor
	 * 
	 * @param dbManager
	 *            DBManager
	 * @param boxManager
	 *            BoxManager
	 * @param securityManager
	 *            SecurityManager
	 * @param splitter
	 *            Splitter
	 * @param zipper
	 *            Zipper
	 */
	public TransactionManager(IDBManager dbManager, IBoxManager boxManager, ISecurityManager securityManager, ISplitter splitter, ICompress zipper) {
		this.boxManager = boxManager;
		this.dbManager = dbManager;
		this.securityManager = securityManager;
		this.splitter = splitter;
		this.zipper = zipper;
		running = false;
		start();
	}

	/**
	 * Run a transaction
	 * 
	 * @param t
	 *            Transaction to run
	 */
	public void runTransaction(Transaction t) {
		addTransaction(t);
		startTransaction(t);
	}
	
	/**
	 * Add a transaction in queue
	 * 
	 * @param t
	 *            Transaction to add
	 */
	public void addTransaction(Transaction t) {
		if (executor.isShutdown())
			start();
		for (Task task : t.getTasks()) {
			allTasksWeight+=task.getWeight();
			
			task.setBoxManager(boxManager);
			task.setDbManager(dbManager);
			task.setSecurityManager(securityManager);
			task.setSplitter(splitter);
			task.setZipper(zipper);
			task.setPhaser(phaser);
		}

		getTransactions().add(t);
		if (_log.isLoggable(Level.INFO)) _log.info(t.getId() + " added");
	}
	
	/**
	 * Start a transaction
	 * 
	 * @param t
	 *            The transaction to start
	 */
	private void startTransaction(Transaction t) {
		Runnable tt = new TransactionThread(this, t);
		executor.execute(tt);
		running = true;
		if (_log.isLoggable(Level.INFO)) _log.info(t.getId() + " transaction thread running");
	}
	
	/**
	 * Try to stop the running transactions
	 * @throws InterruptedException 
	 */
	public void stopTransactions() throws InterruptedException {
		if (running) {
			List<Runnable> rr = executor.shutdownNow();
			for (Runnable r : rr) {
				TransactionThread tt = (TransactionThread) r;
				tt.stop();
			}
			running = false;
			executor.awaitTermination(5, TimeUnit.MINUTES);
		}
	}
	
	/**
	 * Run all transactions in queue
	 * 
	 * @param t
	 *            Transaction to run
	 */
	public void runTransactions() {
		for (Transaction t : getTransactions())
			startTransaction(t);
	}
	
	/**
	 * Callback method to increment weight of completed tasks
	 */
	public synchronized void weightCompleted(long weight) {
		completedTasksWeight+=weight;
	}
	
	/**
	 * Get the total weight of completed tasks
	 * 
	 * @return The weight of completed tasks
	 */
	public int getCompletedTasksWeight() {
		return completedTasksWeight;
	}
	
	/**
	 * Get the total weight of all tasks in all the transactions
	 * 
	 * @return The total weight of all tasks
	 */
	public long getAllTasksWeight() {
		return allTasksWeight;
	}

	/**
	 * Get the transactions terminated
	 * 
	 * @return List of transactions terminated
	 */
	public List<Transaction> getResult() {
		if (isRunning()) {
			_log.warning("Executor running");
			return null;
		}
		List<Transaction> tt = new ArrayList<>();
		while (!getTransactions().isEmpty()) {
			Transaction t = getTransactions().poll();
			if (t.getResultCode() != Transaction.NO_ESITO)
				tt.add(t);
		}
		clear();
		return tt;
	}

	/**
	 * Get the queue of transactions
	 * 
	 * @return List of all the transactions
	 */
	private LinkedList<Transaction> getTransactions() {
		if (transactions == null)
			transactions = new LinkedList<>();
		return transactions;
	}
	
	/**
	 * Clear all pending transactions
	 */
	public void clear() {
		allTasksWeight = 0;
		completedTasksWeight = 0;
		getTransactions().clear();
	}
	
	/**
	 * Check if there are transactions running
	 * 
	 * @return true if the are transactions running, false otherwise
	 */
	public boolean isRunning() {
		if (executor == null)
			return running;
		running = !executor.isTerminated();
		return running;
	}
	
	/**
	 * Shutdown executor
	 */
	public void shutdown() {
		executor.shutdown();
	}
	
	/**
	 * Start executor and reset tasks counters
	 */
	private void start() {
		clear();
		executor = new ThreadPoolExecutor(5, 5, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		((ThreadPoolExecutor) executor).allowCoreThreadTimeOut(true);
		
		phaser = new BBPhaser();
	}
	
	/**
	 * Callback method to call listener of completed transactions
	 */
	public synchronized void taskCompleted(Transaction t) {
		if ((listeners != null) && !listeners.isEmpty())
			for (CompleteTransactionListener listener : listeners)
				listener.transactionCompleted(t);
	}
	
	/**
	 * Add a listener to call when a task is completed
	 * 
	 * @param listener
	 *            Listener to call
	 */
	public void addListener(CompleteTransactionListener listener) {
		if (listeners == null)
			listeners = new ArrayList<>();
		listeners.add(listener);
	}

	/**
	 * Listener to call when a transaction is completed
	 */
	public interface CompleteTransactionListener {
		
		/**
		 * Callback method of completed transactions
		 */
		public void transactionCompleted(Transaction t);
	}
}
