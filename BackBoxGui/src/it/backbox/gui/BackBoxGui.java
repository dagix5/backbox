package it.backbox.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.ScrollPaneConstants;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreePath;

import it.backbox.bean.File;
import it.backbox.bean.Folder;
import it.backbox.exception.BackBoxException;
import it.backbox.exception.RestException;
import it.backbox.gui.model.FileBrowserTableModel;
import it.backbox.gui.model.FileBrowserTableRowSorter;
import it.backbox.gui.model.FileBrowserTreeCellRenderer;
import it.backbox.gui.model.FileBrowserTreeModel;
import it.backbox.gui.model.FileBrowserTreeNode;
import it.backbox.gui.model.FileBrowserTreeNode.TreeNodeType;
import it.backbox.gui.model.FileBrowserTreeSelectionListener;
import it.backbox.gui.model.PreviewTableCellRenderer;
import it.backbox.gui.model.PreviewTableModel;
import it.backbox.gui.utility.BackBoxHelper;
import it.backbox.gui.utility.GuiUtility;
import it.backbox.gui.utility.ThreadActionListener;
import it.backbox.progress.ProgressListener;
import it.backbox.progress.ProgressManager;
import it.backbox.transaction.TransactionManager;
import it.backbox.transaction.TransactionManager.CompleteTransactionListener;
import it.backbox.transaction.task.Task;
import it.backbox.transaction.task.Transaction;
import it.backbox.utility.Utility;
import net.miginfocom.swing.MigLayout;

public class BackBoxGui {
	private static final Logger _log = Logger.getLogger("it.backbox");
	
	private JFrame frmBackBox;
	private JTable table;
	private JTree tree;
	private PasswordDialog pwdDialog;
	private ConfigurationDialog configurationDialog;
	private PreferencesDialog preferencesDialog;
	private JLabel lblStatus;
	private NewConfDialog newConfDialog;
	private JTable tablePreview;
	private JButton btnConnect;
	private JLabel lblEtaValue;
	private JButton btnBackupAll;
	private JButton btnRestoreAll;
	private LoadingDialog loadingDialog;
	private JButton btnClear;
	private JButton btnStart;
	private JButton btnStop;
	private DetailsDialog detailsDialog;
	private JMenuItem mntmUploadDb;
	private JMenuItem mntmDownloadDb;
	private JSpinner spnCurrentUploadSpeed;
	private JMenuItem mntmNewConfiguration;
	private JMenuItem mntmConfiguration;
	private JLabel lblFreeSpaceValue;
	private JMenuItem mntmCheck;
	private JMenu mnBackup;
	
	protected BackBoxHelper helper;
	private ProgressManager pm;
	private Map<String, Integer> taskKeys;
	private boolean connected = false;
	private boolean running = false;
	private boolean pending = false;
	private boolean pendingDone = false;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					BackBoxGui window = new BackBoxGui();
					window.frmBackBox.setVisible(true);
					if (window.helper.confExists()) {
						window.pwdDialog.setLocationRelativeTo(window.frmBackBox);
						window.pwdDialog.load(GuiConstant.LOGIN_MODE);
						window.pwdDialog.setVisible(true);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public void connect() {
		GuiUtility.checkEDT(false);
		
		connected = true;
		
		final int uploadSpeed = helper.getConfiguration().getDefaultUploadSpeed();
		pm.setSpeed(ProgressManager.UPLOAD_ID, uploadSpeed);
		pm.setSpeed(ProgressManager.DOWNLOAD_ID, helper.getConfiguration().getDefaultDownloadSpeed());
		
		helper.getTransactionManager().addListener(new CompleteTransactionListener() {
			
			@Override
			public void transactionCompleted(Transaction tt) {
				updateTableResult(tt);
			}
		});
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				spnCurrentUploadSpeed.setValue(uploadSpeed / 1024);
				updateFileBrowser();
				updateMenu();
				updateStatus();
			}
		});
	}
	
	public void disconnect(final boolean clear) {
		GuiUtility.checkEDT(false);
		
		try {
			if (helper.getTransactionManager() != null) {
				if (running) {
					helper.getTransactionManager().stopTransactions();
					pendingDone = true;
				}
				helper.getTransactionManager().clear();
			}
			
			if (connected)
				helper.logout();
			
			connected = false;
			running = false;
			
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					if (clear) {
						FileBrowserTableModel model = (FileBrowserTableModel) table.getModel();
						while(model.getRowCount() > 0)
							model.removeRow(0);
						
						clearPreviewTable();
						clearMenu();
					}
					
					updateStatus();
				}
			});
			
		} catch (Exception e) {
			GuiUtility.handleException(frmBackBox, "Error in logout", e);
		}
	}
	
	public void updateMenu() {
		GuiUtility.checkEDT(true);
		
		final List<Folder> folders = helper.getConfiguration().getBackupFolders();
		for (final Folder f : folders) {
			JMenuItem mntmFolder = new JMenuItem(f.getAlias());
			mntmFolder.addActionListener(new ThreadActionListener() {
				private List<Transaction> tt = null;
				
				@Override
				protected boolean preaction(ActionEvent event) {
					if (running) {
						JOptionPane.showMessageDialog(frmBackBox, "Transactions running", "Error", JOptionPane.ERROR_MESSAGE);
						return false;
					}
					
					if (!connected) {
						JOptionPane.showMessageDialog(frmBackBox, "Not connected", "Error", JOptionPane.ERROR_MESSAGE);
						return false;
					}
					return true;
				}
				
				@Override
				protected void action(ActionEvent event) {
					helper.getTransactionManager().clear();
					try {
						tt = helper.backup(f, false);
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								spnCurrentUploadSpeed.setValue(helper.getConfiguration().getDefaultUploadSpeed() / 1024);
							}
						});
					} catch (final Exception e) {
						SwingUtilities.invokeLater(new Runnable() {
							
							@Override
							public void run() {
								loadingDialog.hideLoading();
								GuiUtility.handleException(frmBackBox, "Error building backup transactions", e);
								
							}
						});
					}
					
				}
				
				@Override
				protected void postaction(ActionEvent event) {
					clearPreviewTable();
					updatePreviewTable(tt);
					
					if ((tt == null) ||	tt.isEmpty()) {
						loadingDialog.hideLoading();
						JOptionPane.showMessageDialog(frmBackBox, "No files to backup", "Info", JOptionPane.INFORMATION_MESSAGE);
					} else {
						pending = true;
						pendingDone = false;
					}
					updateStatus();
				}
			});
			mnBackup.add(mntmFolder);
		}
		
		mnBackup.setEnabled(true);
	}
	
	public void clearMenu() {
		GuiUtility.checkEDT(true);
		
		for (Component c : mnBackup.getComponents())
			mnBackup.remove(c);
		
		mnBackup.setEnabled(false);
	}
	
	private void updateFileBrowser() {
		GuiUtility.checkEDT(true);
		
		final List<Folder> folders = helper.getConfiguration().getBackupFolders();
		
		final FileBrowserTreeModel model = (FileBrowserTreeModel) tree.getModel();
		final FileBrowserTreeNode root = (FileBrowserTreeNode) model.getRoot();
		root.removeAllChildren();

		for (Folder f : folders)
			model.insertNodeInto(new FileBrowserTreeNode(f.getAlias(), TreeNodeType.FOLDER), root);
		
		model.reload(root);
	}
	
	private void updatePreviewTable(List<Transaction> transactions) {
		GuiUtility.checkEDT(true);		
		
		final DefaultTableModel model = (DefaultTableModel) tablePreview.getModel();
		taskKeys = new HashMap<String, Integer>();
		if (transactions != null) {
			for (Transaction tt : transactions)
				for (final Task t : tt.getTasks()) {
					model.addRow(new Object[] {t.getDescription(), GuiUtility.getTaskSize(t), GuiUtility.getTaskType(t), "", tt, t});
					
					taskKeys.put(t.getId(), model.getRowCount() - 1);
				}
		}
		pending = ((transactions != null) && !transactions.isEmpty());
	}
	
	private void clearPreviewTable() {
		GuiUtility.checkEDT(true);
		
		DefaultTableModel model = (DefaultTableModel) tablePreview.getModel();
		while (model.getRowCount() > 0)
			model.removeRow(0);
	}
	
	private void updateTableResult(Transaction transaction) {
		GuiUtility.checkEDT(true);
		
		if (transaction == null)
			return;
		
		DefaultTableModel model = (DefaultTableModel) tablePreview.getModel();
		for (Task task : transaction.getTasks()) {
			if (!taskKeys.containsKey(task.getId()))
				break;
			final Integer row = taskKeys.get(task.getId());
			final short resultCode = transaction.getResultCode();
			
			model.setValueAt(transaction, row, PreviewTableModel.TRANSACTION_COLUMN_INDEX);
			model.setValueAt(task, row, PreviewTableModel.TASK_COLUMN_INDEX);
			
			if (resultCode == Transaction.ESITO_KO)
				model.setValueAt(GuiConstant.RESULT_ERROR, row, PreviewTableModel.RESULT_COLUMN_INDEX);
			else if (resultCode == Transaction.ESITO_OK)
				model.setValueAt(GuiConstant.RESULT_SUCCESS, row, PreviewTableModel.RESULT_COLUMN_INDEX);
			
			
		}
	}
	
	private void updateStatus() {
		GuiUtility.checkEDT(true);
		
		if (running)
			lblStatus.setText("Running...");
		else if (pending && !pendingDone)
			lblStatus.setText("Operations pending");
		else if (connected)
			lblStatus.setText("Connected");
		else
			lblStatus.setText("Not connected");
		
		btnConnect.setEnabled(!connected);
		btnBackupAll.setEnabled(connected && !running);
		btnRestoreAll.setEnabled(connected && !running);
		btnStart.setEnabled(connected && !running && pending && !pendingDone);
		btnStop.setEnabled(connected && running);
		btnClear.setEnabled(connected && !running && pending);
		mntmUploadDb.setEnabled(!running);
		mntmDownloadDb.setEnabled(!running);
		mntmNewConfiguration.setEnabled(!running);
		mntmConfiguration.setEnabled(connected && !running);
		mntmCheck.setEnabled(connected && !running);
		
		if (connected && !running && !pending)
			try {
				lblFreeSpaceValue.setText(Utility.humanReadableByteCount(helper.getFreeSpace(), false));
			} catch (IOException | RestException | BackBoxException e) {
				lblFreeSpaceValue.setText("Error");
				_log.log(Level.WARNING, "Error retrieving free space", e);
			}
		else if (!connected)
			lblFreeSpaceValue.setText("");
	}
	
	/**
	 * Create the application.
	 */
	public BackBoxGui() {
		helper = BackBoxHelper.getInstance();
		pm = ProgressManager.getInstance();
		
		try {
			helper.loadConfiguration();
		} catch (IOException e) {
			GuiUtility.handleException(frmBackBox, "Error loading configuration file, using default configuration...", e);
		}
		
		initializeFrame();
		initializeMenu();
		initializeFileBrowser();
		initializeOp();

		updateStatus();
		
		pwdDialog = new PasswordDialog(this);
		newConfDialog = new NewConfDialog(this);
		loadingDialog = new LoadingDialog(frmBackBox);
		detailsDialog = new DetailsDialog();
		preferencesDialog = new PreferencesDialog();
		configurationDialog = new ConfigurationDialog(this);
	}

	private final Thread exitThread = new Thread() {
		public void run() {
			try {
				if (connected && helper.getConfiguration().isAutoUploadConf())
					helper.uploadConf(false);
				
				disconnect(false);
				System.exit(0);
			} catch (Exception e) {
				GuiUtility.handleException(frmBackBox, "Error in logout", e);
			}
			
            loadingDialog.hideLoading();
		}
	};
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initializeFrame() {
		GuiUtility.checkEDT(true);
		
		//TODO log4j.xml
		// Logger configuration
//		ConsoleHandler ch = new ConsoleHandler();
//		ch.setLevel(Level.ALL);
//		_log.addHandler(ch);
		try {
			FileHandler fh = new FileHandler("backbox.log", helper.getConfiguration().getLogSize(), 3, true);
			fh.setFormatter(new SimpleFormatter());
			fh.setLevel(Level.ALL);
			_log.addHandler(fh);
		} catch (SecurityException | IOException e) {
			GuiUtility.handleException(frmBackBox, "Error open logging file", e);
		}
		try {
			_log.setLevel(Level.parse(helper.getConfiguration().getLogLevel()));
		} catch (SecurityException | IllegalArgumentException e) {
			GuiUtility.handleException(frmBackBox, "Error setting logging level", e);
		}
		
		frmBackBox = new JFrame();
		frmBackBox.setLocationRelativeTo(null);
		frmBackBox.setSize(750, 700);
		frmBackBox.setTitle("BackBox");
		frmBackBox.setBounds(100, 100, 732, 692);
		frmBackBox.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		frmBackBox.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				loadingDialog.showLoading();
				exitThread.start();
			}
		});
		
	}

	private void initializeOp() {
		GuiUtility.checkEDT(true);
		
		JPanel pnlOp = new JPanel();
		frmBackBox.getContentPane().add(pnlOp, BorderLayout.SOUTH);
		pnlOp.setLayout(new MigLayout("", "[90px:n:90px][90px:n:90px][90px:n:90px][40px:40px][35px][201.00,grow][100px:100px:100px][35.00:35.00:35.00][90px:90px:90px]", "[20px][282.00][]"));
		
		btnBackupAll = new JButton("Backup All");
		btnBackupAll.setMnemonic('B');
		btnBackupAll.setEnabled(connected);
		btnBackupAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (running) {
					JOptionPane.showMessageDialog(frmBackBox, "Transactions running", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (connected) {
					helper.getTransactionManager().clear();
					loadingDialog.showLoading();
					Thread worker = new Thread() {
						public void run() {
							List<Transaction> tt = null;
							try {
								tt = helper.backupAll();
								
								spnCurrentUploadSpeed.setValue(helper.getConfiguration().getDefaultUploadSpeed() / 1024);
							} catch (Exception e) {
								loadingDialog.hideLoading();
								GuiUtility.handleException(frmBackBox, "Error building backup transactions", e);
							} finally {
								clearPreviewTable();
								updatePreviewTable(tt);
								
								if ((tt == null) ||	tt.isEmpty()) {
									loadingDialog.hideLoading();
									JOptionPane.showMessageDialog(frmBackBox, "No files to backup", "Info", JOptionPane.INFORMATION_MESSAGE);
								} else {
									pending = true;
									pendingDone = false;
								}
								updateStatus();
							}
							
	                    	loadingDialog.hideLoading();
						}
					};
					worker.start();
				} else
					JOptionPane.showMessageDialog(frmBackBox, "Not connected", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		
		btnConnect = new JButton("Connect");
		btnConnect.setMnemonic('c');
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (helper.confExists()) {
					pwdDialog.setLocationRelativeTo(frmBackBox);
					pwdDialog.load(GuiConstant.LOGIN_MODE);
					pwdDialog.setVisible(true);
				} else
					JOptionPane.showMessageDialog(frmBackBox, "Configuration not found", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		pnlOp.add(btnConnect, "cell 0 0,grow");
		pnlOp.add(btnBackupAll, "cell 1 0,grow");
		
		btnRestoreAll = new JButton("Restore All");
		btnRestoreAll.setMnemonic('R');
		btnRestoreAll.setEnabled(connected);
		btnRestoreAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (running) {
					JOptionPane.showMessageDialog(frmBackBox, "Transactions running", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (connected) {
					final JFileChooser fc = new JFileChooser();
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int returnVal = fc.showOpenDialog(frmBackBox);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						helper.getTransactionManager().clear();
						loadingDialog.showLoading();
						Thread worker = new Thread() {
							public void run() {
								List<Transaction> tt = null;
								try {
									tt = helper.restoreAll(fc.getSelectedFile().getCanonicalPath());
									
									spnCurrentUploadSpeed.setValue(helper.getConfiguration().getDefaultDownloadSpeed() / 1024);
								} catch (Exception e) {
									loadingDialog.hideLoading();
									GuiUtility.handleException(frmBackBox, "Error building restore transactions", e);
								} finally {
									clearPreviewTable();
									updatePreviewTable(tt);
									if ((tt == null) || tt.isEmpty()) {
										loadingDialog.hideLoading();
										JOptionPane.showMessageDialog(frmBackBox, "No files to restore", "Info", JOptionPane.INFORMATION_MESSAGE);
									} else {
										pending = true;
										pendingDone = false;
									}
									updateStatus();
								}
								
		                    	loadingDialog.hideLoading();
							}
						};
						worker.start();
					}
				} else
					JOptionPane.showMessageDialog(frmBackBox, "Not connected", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		pnlOp.add(btnRestoreAll, "cell 2 0,grow");
		
		JLabel lblFreeSpace = new JLabel("Free Space:");
		pnlOp.add(lblFreeSpace, "cell 5 0,alignx right,growy");
		
		lblFreeSpaceValue = new JLabel("");
		pnlOp.add(lblFreeSpaceValue, "cell 6 0,alignx left,growy");
		
		lblStatus = new JLabel("");
		pnlOp.add(lblStatus, "cell 7 0 2 1,alignx right");
		
		JScrollPane scrollPanePreview = new JScrollPane();
		pnlOp.add(scrollPanePreview, "cell 0 1 9 1,grow");
		
		tablePreview = new JTable();
		tablePreview.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tablePreview.setModel(new PreviewTableModel());
		TableColumnModel columnModel = tablePreview.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(300);
		columnModel.getColumn(0).setMinWidth(300);
		columnModel.getColumn(1).setPreferredWidth(100);
		columnModel.getColumn(1).setMinWidth(50);
		columnModel.getColumn(2).setPreferredWidth(100);
		columnModel.getColumn(2).setMaxWidth(100);
		columnModel.getColumn(3).setPreferredWidth(100);
		columnModel.getColumn(3).setMaxWidth(100);
		columnModel.removeColumn(columnModel.getColumn(4));
		columnModel.removeColumn(columnModel.getColumn(4));
		
		tablePreview.setRowSorter(new FileBrowserTableRowSorter(tablePreview.getModel()));
		tablePreview.setDefaultRenderer(String.class, new PreviewTableCellRenderer());

		tablePreview.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2)
					showDetails();
			}
		});
		
		scrollPanePreview.setViewportView(tablePreview);
		scrollPanePreview.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPanePreview.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPanePreview.setViewportBorder(null);
		
		final JPopupMenu popupPreviewMenu = new JPopupMenu();
		GuiUtility.addPopup(tablePreview, popupPreviewMenu);
		
		JMenuItem mntmDetails = new JMenuItem("Details");
		mntmDetails.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showDetails();
			}
		});
		popupPreviewMenu.add(mntmDetails);
		
		spnCurrentUploadSpeed = new JSpinner();
		spnCurrentUploadSpeed.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				pm.setSpeed(ProgressManager.UPLOAD_ID, ((int) spnCurrentUploadSpeed.getValue()) * 1024);
				pm.setSpeed(ProgressManager.DOWNLOAD_ID, ((int) spnCurrentUploadSpeed.getValue()) * 1024);
			}
		});
		
		btnStart = new JButton("Start");
		btnStop = new JButton("Stop");
		btnClear = new JButton("Clear");
		
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				pending = false;
				pendingDone = false;
				helper.getTransactionManager().clear();
				clearPreviewTable();
				updateStatus();
			}
		});
		btnClear.setEnabled(false);
		pnlOp.add(btnClear, "cell 2 2,grow");
		pnlOp.add(spnCurrentUploadSpeed, "cell 3 2,growx");
		
		JLabel lblKbs = new JLabel("KB\\s");
		pnlOp.add(lblKbs, "cell 4 2");

		final JProgressBar progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		pnlOp.add(progressBar, "cell 5 2 2 1,grow");
		
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				loadingDialog.showLoading();
				Thread worker = new Thread() {
					public void run() {
						try {
							helper.getTransactionManager().stopTransactions();
						} catch (Exception e1) {
							loadingDialog.hideLoading();
							GuiUtility.handleException(frmBackBox, "Error stopping transactions", e1);
						} finally {
							running = false;
							pendingDone = true;
							updateStatus();
						}

						loadingDialog.hideLoading();
					}
				};
				worker.start();
			}
		});
		btnStop.setEnabled(false);
		pnlOp.add(btnStop, "cell 1 2,grow");
		
		btnStart.setEnabled(false);
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				final TransactionManager tm = helper.getTransactionManager();
				
				ProgressListener listener = new ProgressListener() {
					long partial = 0;
					long subpartial = 0;
					long speedSubpartial = 0;
					long lastTimestamp = 0;
					long averagespeed = 0;
					long speeds[] = new long[10];
					int i = 0;
					
					@Override
					public void update(String id, long bytes) {
						subpartial += bytes;
						if (pm.getSpeed(id) == 0) {
							long timestamp = new Date().getTime();
							speedSubpartial += bytes;
							long elapsed = timestamp - lastTimestamp;
							
							if (elapsed > 1500) {
								long speed = (speedSubpartial * 1000) / elapsed;
								speeds[i++] = speed;
								lastTimestamp = timestamp;
								speedSubpartial = 0;
								
								if (i >= 5) {
									i = 0;
									averagespeed = 0;
									for (long a : speeds)
										averagespeed += a;
									averagespeed /= speeds.length;
								}
								
							}
						} else
							averagespeed = pm.getSpeed(id);
						
						if (averagespeed > 0) {
							partial += subpartial;
							subpartial = 0;
							
							long b = tm.getAllTasksWeight() - partial;
							lblEtaValue.setText(GuiUtility.getETAString(b, averagespeed));
						}
						
						tm.weightCompleted(bytes);
					}
				};
				
				pm.setListener(ProgressManager.UPLOAD_ID, listener);
				pm.setListener(ProgressManager.DOWNLOAD_ID, listener);

				tm.runTransactions();
				tm.shutdown();
				
				running = true;
				updateStatus();
				Thread t = new Thread("ProgressBar") {
					public void run() {
						progressBar.setValue(0);
						while (tm.isRunning()) {
							if (_log.isLoggable(Level.FINE)) _log.fine(new StringBuilder("TaskCompleted/AllTask: ").append(tm.getCompletedTasksWeight()).append('/').append(tm.getAllTasksWeight()).toString());
							if (tm.getAllTasksWeight() > 0) {
								long perc = (tm.getCompletedTasksWeight() * 100) / tm.getAllTasksWeight();
								if (_log.isLoggable(Level.FINE)) _log.fine(new StringBuilder("Perc: ").append(perc).append("% - ").append("Progress: ").append(progressBar.getValue()).append('%').toString());
								if ((perc > progressBar.getValue()) && (perc <= 99))
									progressBar.setValue((int)perc);
							}
							try { Thread.sleep(1000); } catch (InterruptedException e) {}
						}
						running = false;
						pendingDone = true;
						progressBar.setValue(100);
						
						updateFileBrowser();
						
						List<Transaction> result = tm.getResult();
						if (result != null) {
							boolean error = false;
							for (Transaction t : result) {
								updateTableResult(t);
								if (t.getResultCode() == Transaction.ESITO_KO)
									error = true;
							}
						
							if (error)
								JOptionPane.showMessageDialog(frmBackBox, "Operation completed with errors", "BackBox", JOptionPane.ERROR_MESSAGE);
							else
								JOptionPane.showMessageDialog(frmBackBox, "Operation completed", "BackBox", JOptionPane.INFORMATION_MESSAGE);
						
						} else
							JOptionPane.showMessageDialog(frmBackBox, "Transactions still in progress", "Error", JOptionPane.ERROR_MESSAGE);
						
						updateStatus();
					}
				};
				t.start();
			}
		});
		pnlOp.add(btnStart, "cell 0 2,grow");
		
		JLabel lblEta = new JLabel("ETA:");
		pnlOp.add(lblEta, "cell 7 2,alignx right");
		
		lblEtaValue = new JLabel("");
		pnlOp.add(lblEtaValue, "cell 8 2");
	}

	private void initializeFileBrowser() {
		GuiUtility.checkEDT(true);
		
		table = new JTable();
		table.setShowGrid(false);
		table.setFillsViewportHeight(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		table.setModel(new FileBrowserTableModel());
		TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(0).setMinWidth(20);
		columnModel.getColumn(0).setMaxWidth(20);
		columnModel.getColumn(0).setResizable(false);
		columnModel.getColumn(1).setPreferredWidth(200);
		columnModel.getColumn(1).setMinWidth(200);
		columnModel.getColumn(2).setPreferredWidth(50);
		columnModel.getColumn(2).setMinWidth(50);
		columnModel.getColumn(3).setPreferredWidth(100);
		columnModel.getColumn(3).setMinWidth(100);
		columnModel.getColumn(4).setPreferredWidth(100);
		columnModel.getColumn(4).setMinWidth(100);
		table.removeColumn(columnModel.getColumn(5));
		table.removeColumn(columnModel.getColumn(5));
		
		
		FileBrowserTableRowSorter sorter = new FileBrowserTableRowSorter(table.getModel());
		table.setRowSorter(sorter);
		List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
        sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);
		
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int row = table.rowAtPoint(e.getPoint());
					row = table.convertRowIndexToModel(row);
					FileBrowserTableModel tableModel = (FileBrowserTableModel) table.getModel();
					FileBrowserTreeNode node = tableModel.getNode(row);
					if (node.getType() == TreeNodeType.FOLDER) {
						TreePath path = new TreePath(node.getPath());
						tree.setSelectionPath(path);
						tree.scrollPathToVisible(path);
					} else if (node.getType() == TreeNodeType.PREV_FOLDER) {
						node = (FileBrowserTreeNode) node.getParent();
						TreePath path = new TreePath(node.getPath());
						tree.setSelectionPath(path);
						tree.scrollPathToVisible(path);
					} else if (node.getType() == TreeNodeType.FILE) {
						addDownload();
					}
				}
			}
		});
		
		JScrollPane scpTable = new JScrollPane(table);
		scpTable.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scpTable.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scpTable.setViewportBorder(null);
		
		tree = new JTree(new FileBrowserTreeModel(new FileBrowserTreeNode(TreeNodeType.FOLDER)));
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.addTreeSelectionListener(new FileBrowserTreeSelectionListener(frmBackBox, helper, table));
        tree.setCellRenderer(new FileBrowserTreeCellRenderer());
        JScrollPane treeScroll = new JScrollPane(tree);
        
		Dimension preferredSize = treeScroll.getPreferredSize();
		Dimension widePreferred = new Dimension(200, (int) preferredSize.getHeight());
		treeScroll.setPreferredSize(widePreferred);
        
		JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                treeScroll,
                scpTable);
		
		frmBackBox.getContentPane().add(splitPane, BorderLayout.CENTER);
		
		final JPopupMenu popupMenu = new JPopupMenu();
		GuiUtility.addPopup(table, popupMenu);
		
		JMenuItem mntmDownload = new JMenuItem("Download");
		mntmDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addDownload();		
			}
		});
		
		JMenuItem mntmDelete = new JMenuItem("Delete");
		mntmDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (running) {
					JOptionPane.showMessageDialog(frmBackBox, "Transactions running", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				loadingDialog.showLoading();
				Thread worker = new Thread() {
					public void run() {
						List<Transaction> tt = new ArrayList<Transaction>();
						try {
							List<String> ids = getSelectedFilesId();
							for (String id : ids)
								tt.add(helper.delete(id, false));
						} catch (Exception e1) {
							loadingDialog.hideLoading();
							GuiUtility.handleException(frmBackBox, "Error building download transactions", e1);
						} finally {
							updatePreviewTable(tt);
							pending = ((tt != null) &&	!tt.isEmpty());
							pendingDone = false;
							updateStatus();
						}
						
                    	loadingDialog.hideLoading();
					}
				};
				worker.start();
			}
		});
		popupMenu.add(mntmDownload);
		popupMenu.add(mntmDelete);
	}

	private void initializeMenu() {
		GuiUtility.checkEDT(true);
		
		JMenuBar menuBar = new JMenuBar();
		frmBackBox.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		mnFile.setMnemonic('F');
		menuBar.add(mnFile);
		
		mntmNewConfiguration = new JMenuItem("New configuration...");
		mntmNewConfiguration.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!running) {
					if (helper.confExists()) {
						int s = JOptionPane.showConfirmDialog(frmBackBox, "Are you sure? This will overwrite current configuration.", "New configuration", JOptionPane.YES_NO_OPTION);
						if (s != JOptionPane.OK_OPTION)
							return;
					}
					newConfDialog.setLocationRelativeTo(frmBackBox);
					newConfDialog.setVisible(true);
				} else
					JOptionPane.showMessageDialog(frmBackBox, "Transactions running", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		mntmNewConfiguration.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		mnFile.add(mntmNewConfiguration);
		
		mnBackup = new JMenu("Backup");
		mnBackup.setEnabled(false);
		mnFile.add(mnBackup);
		
		JSeparator separator = new JSeparator();
		mnFile.add(separator);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				loadingDialog.showLoading();
				exitThread.start();
			}
		});
		
		
		
		mntmUploadDb = new JMenuItem("Upload configuration");
		mntmUploadDb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (running) {
					JOptionPane.showMessageDialog(frmBackBox, "Transactions running", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				loadingDialog.showLoading();
				Thread worker = new Thread() {
					public void run() {
						try {
							helper.uploadConf(true);
							disconnect(true);
							JOptionPane.showMessageDialog(frmBackBox, "Configuration uploaded successfully", "Upload configuration", JOptionPane.INFORMATION_MESSAGE);
						} catch (Exception e1) {
							loadingDialog.hideLoading();
							GuiUtility.handleException(frmBackBox, "Error uploading configuration", e1);
						}
						
                    	loadingDialog.hideLoading();
					}
				};
				worker.start();
			}
		});
		mntmUploadDb.setEnabled(connected);
		mnFile.add(mntmUploadDb);
		
		mntmDownloadDb = new JMenuItem("Download configuration");
		mntmDownloadDb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (running) {
					JOptionPane.showMessageDialog(frmBackBox, "Transactions running", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				int s = JOptionPane.showConfirmDialog(frmBackBox, "Are you sure? This will overwrite local configuration.", "Download configuration", JOptionPane.YES_NO_OPTION);
				if (s != JOptionPane.OK_OPTION)
					return;
				loadingDialog.showLoading();
				Thread worker = new Thread() {
					public void run() {
						try {
							helper.downloadConf();
							disconnect(true);
							JOptionPane.showMessageDialog(frmBackBox, "Configuration downloaded successfully", "Download configuration", JOptionPane.INFORMATION_MESSAGE);
						} catch (Exception e1) {
							loadingDialog.hideLoading();
							GuiUtility.handleException(frmBackBox, "Error downloading configuration", e1);
						}
						
                    	loadingDialog.hideLoading();
					}
				};
				worker.start();
			}
		});
		mntmDownloadDb.setEnabled(connected);
		mnFile.add(mntmDownloadDb);
		
		JSeparator separator_1 = new JSeparator();
		mnFile.add(separator_1);
		mntmExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_MASK));
		mnFile.add(mntmExit);
		
		JMenu mnEdit = new JMenu("Edit");
		menuBar.add(mnEdit);
		
		mntmConfiguration = new JMenuItem("Configuration...");
		mntmConfiguration.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (running) {
					JOptionPane.showMessageDialog(frmBackBox, "Transactions running", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (connected) {
					try {
						configurationDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						configurationDialog.setLocationRelativeTo(frmBackBox);
						configurationDialog.load(helper.getConfiguration().getBackupFolders());
						configurationDialog.setVisible(true);
					} catch (Exception e1) {
						GuiUtility.handleException(frmBackBox, "Error loading configuration", e1);
					}
				} else
					JOptionPane.showMessageDialog(frmBackBox, "Not connected", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		
		JMenuItem mntmBuildDatabase = new JMenuItem("Build database");
		mntmBuildDatabase.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (running) {
					JOptionPane.showMessageDialog(frmBackBox, "Transactions running", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				int s = JOptionPane.showConfirmDialog(frmBackBox, "Are you sure? This will overwrite local database.", "Build database", JOptionPane.YES_NO_OPTION);
				if (s != JOptionPane.OK_OPTION)
					return;
				pwdDialog.setLocationRelativeTo(frmBackBox);
				pwdDialog.load(GuiConstant.BUILDDB_MODE);
				pwdDialog.setVisible(true);
			}
		});
		
		mntmCheck = new JMenuItem("Check database");
		mntmCheck.setEnabled(false);
		mntmCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (running) {
					JOptionPane.showMessageDialog(frmBackBox, "Transactions running", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				loadingDialog.showLoading();
				Thread worker = new Thread() {
					public void run() {
						try {
							List<File> deleted = helper.getRemotelyDeletedFiles(true);
							if (deleted == null)
								JOptionPane.showMessageDialog(frmBackBox, "Error checking the database", "Check database", JOptionPane.ERROR_MESSAGE);
							else if (deleted.isEmpty())
								JOptionPane.showMessageDialog(frmBackBox, "That's all right!", "Check database", JOptionPane.INFORMATION_MESSAGE);
							else {
								StringBuilder message = new StringBuilder("Deleted ").append(deleted.size()).append(" files");
								JOptionPane.showMessageDialog(frmBackBox, message.toString(), "Check database", JOptionPane.INFORMATION_MESSAGE);
							}
						} catch (Exception e1) {
							loadingDialog.hideLoading();
							GuiUtility.handleException(frmBackBox, "Error checking database", e1);
						}
						
		                loadingDialog.hideLoading();
					}
				};
				worker.start();
			}
		});
		mnEdit.add(mntmCheck);
		mnEdit.add(mntmBuildDatabase);
		mnEdit.add(mntmConfiguration);
		
		JMenuItem mntmPreferences = new JMenuItem("Preferences...");
		mntmPreferences.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					preferencesDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					preferencesDialog.setLocationRelativeTo(frmBackBox);
					preferencesDialog.load(helper.getConfiguration().getDefaultUploadSpeed(), 
											helper.getConfiguration().getDefaultDownloadSpeed(), 
											helper.getConfiguration().getProxyConfiguration(), 
											!running, 
											Level.parse(helper.getConfiguration().getLogLevel()), 
											helper.getConfiguration().getLogSize(),
											helper.getConfiguration().isAutoUploadConf());
					preferencesDialog.setVisible(true);
				} catch (Exception e1) {
					GuiUtility.handleException(frmBackBox, "Error loading configuration", e1);
				}
			}
		});
		
		JSeparator separator_2 = new JSeparator();
		mnEdit.add(separator_2);
		mnEdit.add(mntmPreferences);
	}
	
	private void showDetails() {
		GuiUtility.checkEDT(true);
		
		if (running) {
			JOptionPane.showMessageDialog(frmBackBox, "Transactions running", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		loadingDialog.showLoading();
		Thread worker = new Thread() {
			public void run() {
				List<Integer> indexes = new ArrayList<>();
				int selectedIndex = 0;
				for (int i = 0; i < tablePreview.getRowCount(); i++) {
					int c = tablePreview.convertRowIndexToModel(i);
					indexes.add(c);
					if (i == tablePreview.getSelectedRow())
						selectedIndex = i;
				}
				DefaultTableModel model = (DefaultTableModel) tablePreview.getModel();
				Vector vv = model.getDataVector();
				List<Transaction> transactions = new ArrayList<>();
				List<Task> tasks = new ArrayList<>();
				Iterator i = vv.iterator();
				while (i.hasNext()) {
					Vector v = (Vector) i.next();
					transactions.add((Transaction) v.elementAt(PreviewTableModel.TRANSACTION_COLUMN_INDEX));
					tasks.add((Task) v.elementAt(PreviewTableModel.TASK_COLUMN_INDEX));
				}
				detailsDialog.load(transactions, tasks, selectedIndex, indexes);
				detailsDialog.setLocationRelativeTo(frmBackBox);
				detailsDialog.setVisible(true);
				
				loadingDialog.hideLoading();
			}
		};
		worker.start();
	}
	
	private void listAllFiles(FileBrowserTreeNode node, List<File> list) {
		GuiUtility.checkEDT(false);
		
		Enumeration<FileBrowserTreeNode> e = node.children();
		while (e.hasMoreElements()) {
			FileBrowserTreeNode cnode = e.nextElement();
			Object obj = cnode.getUserObject();
			if (obj instanceof String)
				listAllFiles(cnode, list);
			else if (obj instanceof File)
				list.add((File) obj);
		}
	}
	
	private List<String> getSelectedFilesId() {
		GuiUtility.checkEDT(false);
		
		int[] selectedRows = table.getSelectedRows();
		FileBrowserTableModel model = (FileBrowserTableModel) table.getModel();
		List<String> res = new ArrayList<>();
		for (int i : selectedRows) {
			int row = table.convertRowIndexToModel(i);
			String id = model.getId(row);
			if ((id == null) || id.isEmpty()) {
				FileBrowserTreeNode node = model.getNode(row);
				List<File> list = new ArrayList<>();
				listAllFiles(node, list);
				
				for (File f : list)
					res.add(f.getHash());
			} else
				res.add(id);
		}
		return res;
	}

	private void addDownload() {
		if (running) {
			JOptionPane.showMessageDialog(frmBackBox, "Transactions running", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		final JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (fc.showSaveDialog(frmBackBox) == JFileChooser.APPROVE_OPTION) {
			loadingDialog.showLoading();
			Thread worker = new Thread() {
				public void run() {
					final List<Transaction> tt = new ArrayList<Transaction>();
					try {
						List<String> ids = getSelectedFilesId();
						for (String id : ids)
							tt.add(helper.downloadFile(id, fc.getSelectedFile().getCanonicalPath(), false));
					} catch (final Exception e1) {
						SwingUtilities.invokeLater(new Runnable() {
							
							@Override
							public void run() {
								loadingDialog.hideLoading();
								GuiUtility.handleException(frmBackBox, "Error building download transactions", e1);
							}
						});
					}
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							updatePreviewTable(tt);
							pending = ((tt != null) &&	!tt.isEmpty());
							pendingDone = false;
							updateStatus();
		                	loadingDialog.hideLoading();
						}
					});
                	
				}
			};
			worker.start();
		}
	}

}
