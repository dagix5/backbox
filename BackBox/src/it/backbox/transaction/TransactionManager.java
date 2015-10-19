package it.backbox.transaction;

import it.backbox.IBoxManager;
import it.backbox.ICompress;
import it.backbox.IDBManager;
import it.backbox.ISecurityManager;
import it.backbox.ISplitter;
import it.backbox.transaction.task.Task;
import it.backbox.transaction.task.Transaction;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import com.google.common.io.Files;

public class TransactionManager {
	private static final Logger _log = Logger.getLogger(TransactionManager.class.getCanonicalName());
	
	private LinkedList<Transaction> transactions;
	private ExecutorService executor;
	private long completedTasksWeight;
	private long allTasksWeight;
	private BBPhaser phaser;
	
	private List<CompleteTransactionListener> listeners;
	
	private IBoxManager boxManager;
	private IDBManager dbManager;
	private ISecurityManager securityManager;
	private ISplitter splitter;
	private ICompress zipper;
	
	private File tempDir;
	
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
		
		this.tempDir = Files.createTempDir();
		_log.info("Created temp dir: " + tempDir.getAbsolutePath());
		
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
			task.setTempDir(tempDir);
		}

		getTransactions().add(t);
		if (_log.isLoggable(Level.FINE)) _log.fine(t.getId() + " added");
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
		if (_log.isLoggable(Level.FINE)) _log.fine(t.getId() + " transaction thread running");
	}
	
	/**
	 * Try to stop the running transactions
	 * @throws InterruptedException 
	 */
	public void stopTransactions() throws InterruptedException {
		if (!executor.isTerminated()) {
			BlockingQueue<Runnable> rr = ((ThreadPoolExecutor)executor).getQueue();
			for (Runnable r : rr) {
				TransactionThread tt = (TransactionThread) r;
				tt.stop();
			}
			executor.shutdownNow();
			executor.awaitTermination(15, TimeUnit.MINUTES);
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
	public long getCompletedTasksWeight() {
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
			if (t.getResultCode() != Transaction.Result.NO_RESULT)
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
			return false;
		
		if (_log.isLoggable(Level.INFO)) {
			ThreadPoolExecutor tpEx = (ThreadPoolExecutor) executor;
			_log.info(String.format("[Executor monitor] [%d/%d] Active: %d, Completed: %d, Task: %d, isShutdown: %s, isTerminated: %s",
					tpEx.getPoolSize(), 
					tpEx.getCorePoolSize(),
					tpEx.getActiveCount(),
					tpEx.getCompletedTaskCount(), 
					tpEx.getTaskCount(),
					tpEx.isShutdown(), 
					tpEx.isTerminated()));
			
			Runtime runtime = Runtime.getRuntime();
			int mb = 1024*1024;
			_log.info(String.format("[JVM monitor] Used Memory: %d, Free Memory: %d, Total Memory: %d, Max Memory: %d",
					(runtime.totalMemory() - runtime.freeMemory()) / mb,
					runtime.freeMemory() / mb,
					runtime.totalMemory() / mb,
					runtime.maxMemory() / mb));
		}
		
		return !executor.isTerminated();
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
	 * Free transaction manager resources
	 */
	public void close() {
		FileUtils.deleteQuietly(tempDir);
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
