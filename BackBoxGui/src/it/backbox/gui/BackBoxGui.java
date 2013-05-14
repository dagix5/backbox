package it.backbox.gui;

import it.backbox.bean.File;
import it.backbox.gui.bean.TableTask;
import it.backbox.gui.utility.GuiUtility;
import it.backbox.progress.ProgressListener;
import it.backbox.progress.ProgressManager;
import it.backbox.transaction.TransactionManager;
import it.backbox.transaction.task.Task;
import it.backbox.transaction.task.Transaction;
import it.backbox.utility.BackBoxHelper;
import it.backbox.utility.Utility;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
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
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.configuration.ConfigurationException;

public class BackBoxGui {
	private static Logger _log = Logger.getLogger("it.backbox");

	protected static final String CONFIG_FILE = "config.xml";
	protected static final String LOG_FILE = "backbox.log";
	
	private static BackBoxGui window;
	private JFrame frmBackBox;
	private JTable table;
	private PasswordDialog pwdDialog;
	private PreferencesDialog preferencesDialog;
	private JLabel lblStatus;
	private NewConfDialog newConfDialog;
	private JTable tablePreview;
	private JButton btnConnect;
	private JLabel lblEtaValue;
	private JButton btnBackup;
	private JButton btnRestore;
	private LoadingDialog loadingDialog;
	private JButton btnClear;
	private JButton btnStart;
	private JButton btnStop;
	private DetailsDialog detailsDialog;
	
	protected BackBoxHelper helper;
	private ArrayList<String> fileKeys;
	private Map<String, Integer> taskKeys;
	private List<TableTask> tasksPending;
	private boolean connected = false;
	private boolean running = false;
	private boolean pending = false;
	private boolean pendingDone = false;
	private String backupFolder;
	private int chunkSize;

	/**
	 * Launch the application.
	 * @throws Exception 
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					window = new BackBoxGui();
					window.frmBackBox.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public void connect() {
		connected = true;
		
		if (connected) {
			backupFolder = helper.getConfiguration().getString(BackBoxHelper.BACKUP_FOLDER);
			chunkSize = helper.getConfiguration().getInt(BackBoxHelper.CHUNK_SIZE);
			updateTable();
		}
		
		if (pwdDialog != null)
			pwdDialog.setVisible(false);
		if (newConfDialog != null)
			newConfDialog.setVisible(false);
		preferencesDialog = new PreferencesDialog(this);
		
		updateStatus();
	}
	
	public void disconnect() {
		connected = false;
		TransactionManager.getInstance().clear();
		clearTable();
		clearPreviewTable();
		updateStatus();
	}
	
	private void updateTable() {
		try {
			List<SimpleEntry<String, File>> map = helper.getRecords();
			fileKeys = new ArrayList<>();
			
			clearTable();
			if (map == null) 
				return;
			
			DefaultTableModel model = (DefaultTableModel) table.getModel();
			for (SimpleEntry<String, File> entry : map) {
				File f = entry.getValue();
				model.addRow(new Object[] { f.getFilename(), f.getHash(), Utility.humanReadableByteCount(f.getSize(), true),  ((f.getChunks() != null) ? f.getChunks().size() : 0)});
				fileKeys.add(entry.getKey());
			}
		} catch (SQLException e) {
			GuiUtility.handleException(frmBackBox, "Error updating table", e);
		}
	}
	
	private void clearTable() {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		while(model.getRowCount() > 0)
			model.removeRow(0);
	}
	
	private void updatePreviewTable(List<Transaction> transactions) {
		DefaultTableModel model = (DefaultTableModel) tablePreview.getModel();
		taskKeys = new HashMap<String, Integer>();
		if (tasksPending == null)
			tasksPending = new ArrayList<TableTask>();
		if (transactions != null) {
			for (Transaction tt : transactions)
				for (Task t : tt.getTasks()) {
					model.addRow(new Object[] {tt.getId(), t.getDescription(), GuiUtility.getTaskSize(t), GuiUtility.getTaskType(t)});
					taskKeys.put(t.getId(), model.getRowCount() - 1);
					
					TableTask tbt = new TableTask();
					tbt.setTableIndex(model.getRowCount() - 1);
					tbt.setTransaction(tt);
					tbt.setTask(t);
					tasksPending.add(tbt);
				}
		}
		pending = ((transactions != null) && !transactions.isEmpty());
	}
	
	private void clearPreviewTable() {
		DefaultTableModel model = (DefaultTableModel) tablePreview.getModel();
		while(model.getRowCount() > 0)
			model.removeRow(0);
		if (tasksPending != null)
			tasksPending.clear();
	}
	
	private void showTableResult(List<Transaction> transactions) {
		DefaultTableModel model = (DefaultTableModel) tablePreview.getModel();
		if (transactions == null)
			return;
		for (Transaction tt : transactions) {
			for (Task t : tt.getTasks()) {
				if (!taskKeys.containsKey(t.getId()))
					break;
				if (tt.getResultCode() == Transaction.ESITO_KO)
					model.setValueAt("Error", taskKeys.get(t.getId()), 4);
				else if (tt.getResultCode() == Transaction.ESITO_OK)
					model.setValueAt("Success", taskKeys.get(t.getId()), 4);
				tasksPending.get(taskKeys.get(t.getId())).setTask(t);
				tasksPending.get(taskKeys.get(t.getId())).setTransaction(tt);
			}
		}
	}
	
	private void updateStatus() {
		if (running)
			lblStatus.setText("Running...");
		else if (pending && !pendingDone)
			lblStatus.setText("Operations pending");
		else if (connected)
			lblStatus.setText("Connected");
		else
			lblStatus.setText("Not connected");
		btnConnect.setEnabled(!connected);
		btnBackup.setEnabled(connected && !running);
		btnRestore.setEnabled(connected && !running);
		btnStart.setEnabled(connected && !running && pending && !pendingDone);
		btnStop.setEnabled(connected && running);
		btnClear.setEnabled(connected && !running && pending);
	}
	
	private void showResult(List<Transaction> errors) {
		if (errors == null)
			JOptionPane.showMessageDialog(frmBackBox, "Transactions still in progress", "Error", JOptionPane.ERROR_MESSAGE);
		else if (errors.isEmpty())
			JOptionPane.showMessageDialog(frmBackBox, "Operation completed", "BackBox", JOptionPane.INFORMATION_MESSAGE);
		else {
			StringBuilder errDescriptions = new StringBuilder();
			for (Transaction t : errors) {
				if (t.getResultDescription().length() > 100)
					errDescriptions.append(t.getResultDescription().substring(0, 99));
				else
					errDescriptions.append(t.getResultDescription());
				errDescriptions.append("\n");
			}
			JOptionPane.showMessageDialog(frmBackBox, "Operation completed with errors: \n" + errDescriptions.toString(), "BackBox", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void setPreferences(String backupFolder, Integer defaultUploadSpeed, int chunksize) {
		try {
			helper.getConfiguration().setProperty(BackBoxHelper.BACKUP_FOLDER, backupFolder);
			helper.getConfiguration().setProperty(BackBoxHelper.DEFAULT_UPLOAD_SPEED, defaultUploadSpeed);
			helper.getConfiguration().setProperty(BackBoxHelper.CHUNK_SIZE, chunksize);
			helper.saveConfiguration(CONFIG_FILE);
		} catch (ConfigurationException e) {
			GuiUtility.handleException(frmBackBox, "Error saving preferences", e);
		}
	}
	
	public void savePreferences(String backupFolder, Integer defaultUploadSpeed) {
		try {
			helper.getConfiguration().setProperty(BackBoxHelper.BACKUP_FOLDER, backupFolder);
			helper.getConfiguration().setProperty(BackBoxHelper.DEFAULT_UPLOAD_SPEED, defaultUploadSpeed);
			helper.saveConfiguration(CONFIG_FILE);
			
			this.backupFolder = backupFolder;

			if (preferencesDialog != null)
				preferencesDialog.setVisible(false);
		} catch (ConfigurationException e) {
			GuiUtility.handleException(frmBackBox, "Error saving preferences", e);
		}
	}
	
	/**
	 * Create the application.
	 * @throws Exception 
	 */
	public BackBoxGui() {
		helper = new BackBoxHelper();
		
		initialize();
		
		pwdDialog = new PasswordDialog(this);
		newConfDialog = new NewConfDialog(this);
		loadingDialog = new LoadingDialog();
		detailsDialog = new DetailsDialog();
	}

	public void showLoading() {
		loadingDialog.setLocationRelativeTo(frmBackBox);
		loadingDialog.setVisible(true);
        frmBackBox.setEnabled(false);
	}
	
	public void hideLoading() {
		loadingDialog.setVisible(false);
        frmBackBox.setEnabled(true);
        frmBackBox.toFront();
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		ConsoleHandler ch = new ConsoleHandler();
		ch.setLevel(Level.ALL);
		_log.addHandler(ch);
		try {
			FileHandler fh = new FileHandler(LOG_FILE, 10240, 3, true);
			fh.setFormatter(new SimpleFormatter());
			fh.setLevel(Level.ALL);
			_log.addHandler(fh);
		} catch (SecurityException | IOException e2) {
			GuiUtility.handleException(frmBackBox, "Error open logging file", e2);
		}
		_log.setLevel(Level.SEVERE);
		
		frmBackBox = new JFrame();
		frmBackBox.setLocationRelativeTo(null);
		frmBackBox.setSize(750, 700);
		frmBackBox.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					if (connected)
						helper.logout();
				} catch (Exception e1) {
					GuiUtility.handleException(frmBackBox, "Error in logout", e1);
				}
			}
		});
		frmBackBox.setTitle("BackBox");
		frmBackBox.setBounds(100, 100, 732, 692);
		frmBackBox.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		frmBackBox.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		mnFile.setMnemonic('F');
		menuBar.add(mnFile);
		
		JMenuItem mntmNewConfiguration = new JMenuItem("New configuration...");
		mntmNewConfiguration.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!running) {
					newConfDialog.setLocationRelativeTo(frmBackBox);
					newConfDialog.setVisible(true);
				} else
					JOptionPane.showMessageDialog(frmBackBox, "Transactions running", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		mntmNewConfiguration.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		mnFile.add(mntmNewConfiguration);
		
		JSeparator separator = new JSeparator();
		mnFile.add(separator);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (running) {
					JOptionPane.showMessageDialog(frmBackBox, "Transactions running", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				try {
					if (connected)
						helper.logout();
					System.exit(0);
				} catch (Exception e) {
					GuiUtility.handleException(frmBackBox, "Error in logout", e);
				}
			}
		});
		
		JMenuItem mntmUploadDb = new JMenuItem("Upload DB");
		mntmUploadDb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!connected) {
					JOptionPane.showMessageDialog(frmBackBox, "Not connected", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (running) {
					JOptionPane.showMessageDialog(frmBackBox, "Transactions running", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				showLoading();
				Thread worker = new Thread() {
					public void run() {
						try {
							helper.uploadDB();
							disconnect();
						} catch (Exception e1) {
							hideLoading();
							GuiUtility.handleException(frmBackBox, "Error uploading database", e1);
						}
						
						SwingUtilities.invokeLater(new Runnable() {
		                    public void run() {
		                    	hideLoading();
		                    }
		                });
					}
				};
				worker.start();
			}
		});
		mnFile.add(mntmUploadDb);
		
		JMenuItem mntmDownloadDb = new JMenuItem("Download DB");
		mntmDownloadDb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!connected) {
					JOptionPane.showMessageDialog(frmBackBox, "Not connected", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (running) {
					JOptionPane.showMessageDialog(frmBackBox, "Transactions running", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				int s = JOptionPane.showConfirmDialog(frmBackBox, "Are you sure? This will overwrite local DB.", "Download DB", JOptionPane.YES_NO_OPTION);
				if (s != JOptionPane.OK_OPTION)
					return;
				showLoading();
				Thread worker = new Thread() {
					public void run() {
						try {
							helper.downloadDB();
							disconnect();
						} catch (Exception e1) {
							hideLoading();
							GuiUtility.handleException(frmBackBox, "Error downloading database", e1);
						}
						
						SwingUtilities.invokeLater(new Runnable() {
		                    public void run() {
		                    	hideLoading();
		                    }
		                });
					}
				};
				worker.start();
			}
		});
		mnFile.add(mntmDownloadDb);
		
		JSeparator separator_1 = new JSeparator();
		mnFile.add(separator_1);
		mntmExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_MASK));
		mnFile.add(mntmExit);
		
		JMenu mnEdit = new JMenu("Edit");
		menuBar.add(mnEdit);
		
		JMenuItem mntmPreferences = new JMenuItem("Preferences...");
		mntmPreferences.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (running) {
					JOptionPane.showMessageDialog(frmBackBox, "Transactions running", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (connected) {
					preferencesDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					preferencesDialog.setLocationRelativeTo(frmBackBox);
					preferencesDialog.setVisible(true);
				} else
					JOptionPane.showMessageDialog(frmBackBox, "Not connected", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		mnEdit.add(mntmPreferences);
		
		table = new JTable();
		table.setFillsViewportHeight(true);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setModel(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				"Filename", "Hash", "Size", "Chunks"
			}
		) {
			private static final long serialVersionUID = 1L;
			Class[] columnTypes = new Class[] {
				String.class, String.class, String.class, Integer.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
			boolean[] columnEditables = new boolean[] {
				false, false, false, false
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		table.getColumnModel().getColumn(0).setPreferredWidth(200);
		table.getColumnModel().getColumn(0).setMinWidth(200);
		table.getColumnModel().getColumn(1).setPreferredWidth(250);
		table.getColumnModel().getColumn(1).setMinWidth(200);
		table.getColumnModel().getColumn(2).setPreferredWidth(15);
		table.getColumnModel().getColumn(2).setMinWidth(5);
		table.getColumnModel().getColumn(3).setPreferredWidth(15);
		table.getColumnModel().getColumn(3).setMinWidth(5);
		
		table.setAutoCreateRowSorter(true);
		
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setViewportBorder(null);
		frmBackBox.getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		btnStart = new JButton("Start");
		btnStop = new JButton("Stop");
		
		final JPopupMenu popupMenu = new JPopupMenu();
		GuiUtility.addPopup(table, popupMenu);
		
		JMenuItem mntmDownload = new JMenuItem("Download");
		mntmDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (running) {
					JOptionPane.showMessageDialog(frmBackBox, "Transactions running", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				final JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (fc.showSaveDialog(frmBackBox) == JFileChooser.APPROVE_OPTION) {
					showLoading();
					Thread worker = new Thread() {
						public void run() {
							List<Transaction> tt = new ArrayList<Transaction>();
							try {
								int[] ii = table.getSelectedRows();
								for (int i : ii)
									tt.add(helper.downloadFile(fileKeys.get(table.convertRowIndexToModel(i)), fc.getSelectedFile().getCanonicalPath(), false));
							} catch (Exception e1) {
								hideLoading();
								GuiUtility.handleException(frmBackBox, "Error building download transactions", e1);
							} finally {
								updatePreviewTable(tt);
								pending = ((tt != null) &&	!tt.isEmpty());
								pendingDone = false;
								updateStatus();
							}
							SwingUtilities.invokeLater(new Runnable() {
			                    public void run() {
			                    	hideLoading();
			                    }
			                });
						}
					};
					worker.start();
				}		
			}
		});
		
		JMenuItem mntmDelete = new JMenuItem("Delete");
		mntmDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (running) {
					JOptionPane.showMessageDialog(frmBackBox, "Transactions running", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				showLoading();
				Thread worker = new Thread() {
					public void run() {
						List<Transaction> tt = new ArrayList<Transaction>();
						try {
							int[] ii = table.getSelectedRows();
							for (int i : ii)
								tt.add(helper.delete(fileKeys.get(table.convertRowIndexToModel(i)), false));
						} catch (Exception e1) {
							hideLoading();
							GuiUtility.handleException(frmBackBox, "Error building download transactions", e1);
						} finally {
							updatePreviewTable(tt);
							pending = ((tt != null) &&	!tt.isEmpty());
							pendingDone = false;
							updateStatus();
						}
						
						SwingUtilities.invokeLater(new Runnable() {
		                    public void run() {
		                    	hideLoading();
		                    }
		                });
					}
				};
				worker.start();
			}
		});
		popupMenu.add(mntmDelete);
		
		JPanel panel = new JPanel();
		frmBackBox.getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new MigLayout("", "[70px][70px][70px][35px][35px][117.00,grow][31.00][70px]", "[20px][282.00][]"));
		
		btnBackup = new JButton("Backup");
		btnBackup.setMnemonic('B');
		btnBackup.setEnabled(connected);
		btnBackup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (running) {
					JOptionPane.showMessageDialog(frmBackBox, "Transactions running", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (connected) {
					TransactionManager.getInstance().clear();
					showLoading();
					Thread worker = new Thread() {
						public void run() {
							ArrayList<Transaction> tt = null;
							try {
								tt = helper.backup(backupFolder, chunkSize, false);
							} catch (Exception e) {
								hideLoading();
								GuiUtility.handleException(frmBackBox, "Error building backup transactions", e);
							} finally {
								clearPreviewTable();
								updatePreviewTable(tt);
								if ((tt == null) ||	tt.isEmpty()) {
									hideLoading();
									JOptionPane.showMessageDialog(frmBackBox, "No files to backup", "Info", JOptionPane.INFORMATION_MESSAGE);
								} else {
									pending = true;
									pendingDone = false;
								}
								updateStatus();
							}
							
							SwingUtilities.invokeLater(new Runnable() {
			                    public void run() {
			                    	hideLoading();
			                    }
			                });
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
				if (helper.confExists(CONFIG_FILE)) {
					pwdDialog.setLocationRelativeTo(frmBackBox);
					pwdDialog.setVisible(true);
				} else
					JOptionPane.showMessageDialog(frmBackBox, "Configuration not found", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		panel.add(btnConnect, "cell 0 0,grow");
		panel.add(btnBackup, "cell 1 0,grow");
		
		btnRestore = new JButton("Restore");
		btnRestore.setMnemonic('R');
		btnRestore.setEnabled(connected);
		btnRestore.addActionListener(new ActionListener() {
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
						TransactionManager.getInstance().clear();
						showLoading();
						Thread worker = new Thread() {
							public void run() {
								ArrayList<Transaction> tt = null;
								try {
									tt = helper.restore(fc.getSelectedFile().getCanonicalPath(), false);
								} catch (Exception e) {
									hideLoading();
									GuiUtility.handleException(frmBackBox, "Error building restore transactions", e);
								} finally {
									clearPreviewTable();
									updatePreviewTable(tt);
									if ((tt == null) || tt.isEmpty()) {
										hideLoading();
										JOptionPane.showMessageDialog(frmBackBox, "No files to restore", "Info", JOptionPane.INFORMATION_MESSAGE);
									} else {
										pending = true;
										pendingDone = false;
									}
									updateStatus();
								}
								
								SwingUtilities.invokeLater(new Runnable() {
				                    public void run() {
				                    	hideLoading();
				                    }
				                });
							}
						};
						worker.start();
					}
				} else
					JOptionPane.showMessageDialog(frmBackBox, "Not connected", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		panel.add(btnRestore, "cell 2 0,grow");
		
		lblStatus = new JLabel("");
		panel.add(lblStatus, "cell 5 0 3 1,alignx right");
		
		JScrollPane scrollPanePreview = new JScrollPane();
		panel.add(scrollPanePreview, "cell 0 1 8 1,grow");
		
		tablePreview = new JTable();
		tablePreview.setAutoCreateRowSorter(true);
		tablePreview.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tablePreview.setModel(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				"Transaction", "Filename", "Size", "Operation", "Result"
			}
		) {			
			private static final long serialVersionUID = 1L;
			boolean[] columnEditables = new boolean[] {
				false, false, false, false, false
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		}
		);
		tablePreview.getColumnModel().getColumn(0).setPreferredWidth(150);
		tablePreview.getColumnModel().getColumn(0).setMaxWidth(100);
		tablePreview.getColumnModel().getColumn(1).setPreferredWidth(200);
		tablePreview.getColumnModel().getColumn(1).setMinWidth(200);
		tablePreview.getColumnModel().getColumn(2).setPreferredWidth(100);
		tablePreview.getColumnModel().getColumn(2).setMaxWidth(100);
		tablePreview.getColumnModel().getColumn(3).setPreferredWidth(100);
		tablePreview.getColumnModel().getColumn(3).setMaxWidth(100);
		tablePreview.getColumnModel().getColumn(4).setPreferredWidth(100);
		tablePreview.getColumnModel().getColumn(4).setMaxWidth(100);
		scrollPanePreview.setViewportView(tablePreview);
		scrollPanePreview.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPanePreview.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPanePreview.setViewportBorder(null);
		
		final JPopupMenu popupPreviewMenu = new JPopupMenu();
		GuiUtility.addPopup(tablePreview, popupPreviewMenu);
		
		JMenuItem mntmDetails = new JMenuItem("Details");
		mntmDetails.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (running) {
					JOptionPane.showMessageDialog(frmBackBox, "Transactions running", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				showLoading();
				Thread worker = new Thread() {
					public void run() {
						try {
							int i = tablePreview.convertRowIndexToModel(tablePreview.getSelectedRow());
							detailsDialog.updateDetails(tasksPending.get(i));
							detailsDialog.setLocationRelativeTo(frmBackBox);
							detailsDialog.setVisible(true);
						} catch (Exception e1) {
							hideLoading();
							GuiUtility.handleException(frmBackBox, "Error loading details", e1);
						}
						
						SwingUtilities.invokeLater(new Runnable() {
		                    public void run() {
		                    	hideLoading();
		                    }
		                });
					}
				};
				worker.start();
			}
		});
		popupPreviewMenu.add(mntmDetails);
		
		final JSpinner currentUploadSpeed = new JSpinner();
		currentUploadSpeed.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				ProgressManager.getInstance().setSpeed(ProgressManager.UPLOAD_ID, ((int) currentUploadSpeed.getValue()) * 1024);
				ProgressManager.getInstance().setSpeed(ProgressManager.DOWNLOAD_ID, ((int) currentUploadSpeed.getValue()) * 1024);
			}
		});
		
		btnClear = new JButton("Clear");
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				pending = false;
				pendingDone = false;
				TransactionManager.getInstance().clear();
				clearPreviewTable();
				updateStatus();
			}
		});
		btnClear.setEnabled(false);
		panel.add(btnClear, "cell 2 2,grow");
		currentUploadSpeed.setValue(ProgressManager.getInstance().getSpeed(ProgressManager.UPLOAD_ID) / 1024);
		panel.add(currentUploadSpeed, "cell 3 2,growx");
		
		JLabel lblKbs = new JLabel("KB\\s");
		panel.add(lblKbs, "cell 4 2");

		final JProgressBar progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		panel.add(progressBar, "cell 5 2,grow");
		
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				TransactionManager.getInstance().stopTransactions();
				updateStatus();
				running = false;
				pendingDone = true;
			}
		});
		btnStop.setEnabled(false);
		panel.add(btnStop, "cell 1 2,grow");
		
		btnStart.setEnabled(false);
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				final TransactionManager tm = TransactionManager.getInstance();
				final ProgressManager pm = ProgressManager.getInstance();
				
				ProgressListener listener = new ProgressListener() {
					long partial = 0;
					long subpartial = 0;
					long speedSubpartial = 0;
					long lastTimestamp = 0;
					long averagespeed = 0;
					long speeds[] = new long[5];
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
								
								if (i == speeds.length) {
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
							
							long b = tm.getAllTasks() - partial;
							lblEtaValue.setText(GuiUtility.getETAString(b, averagespeed));
						}
						
						tm.taskCompleted(bytes);
					}
				};
				
				pm.setListener(ProgressManager.UPLOAD_ID, listener);
				pm.setListener(ProgressManager.DOWNLOAD_ID, listener);

				tm.runTransactions();
				tm.shutdown();
				
				running = true;
				updateStatus();
				Runnable runnable = new Runnable() {
					
					@Override
					public void run() {
						progressBar.setValue(0);
						while (tm.isRunning()) {
							if (_log.isLoggable(Level.FINEST)) _log.finest(new StringBuilder("TaskCompleted/AllTask: ").append(tm.getCompletedTasks()).append("/").append(tm.getAllTasks()).toString());
							if (tm.getAllTasks() > 0) {
								int perc = (int) ((tm.getCompletedTasks() * 100) / tm.getAllTasks());
								if ((perc > progressBar.getValue()) && (perc < 99))
									progressBar.setValue(perc);
							}
							try { Thread.sleep(1000); } catch (InterruptedException e) {}
						}
						running = false;
						pendingDone = true;
						progressBar.setValue(100);
						List<Transaction> result = tm.getResult();
						updateTable();
						showResult(null);
						showTableResult(result);
						updateStatus();
					}
				};
				Thread t = new Thread(runnable, "ProgressBar");
				t.start();
			}
		});
		panel.add(btnStart, "cell 0 2,grow");
		
		JLabel lblEta = new JLabel("ETA:");
		panel.add(lblEta, "cell 6 2,alignx right");
		
		lblEtaValue = new JLabel("");
		panel.add(lblEtaValue, "cell 7 2");
		
		popupMenu.add(mntmDownload);
		
		updateStatus();
	}

}
