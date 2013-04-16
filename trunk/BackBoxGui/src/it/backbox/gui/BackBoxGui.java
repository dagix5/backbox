package it.backbox.gui;

import it.backbox.bean.File;
import it.backbox.gui.utility.GuiUtility;
import it.backbox.progress.ProgressListener;
import it.backbox.progress.ProgressManager;
import it.backbox.transaction.TransactionManager;
import it.backbox.transaction.task.Task;
import it.backbox.transaction.task.Transaction;
import it.backbox.utility.BackBoxHelper;
import it.backbox.utility.Utility;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.configuration.ConfigurationException;

public class BackBoxGui {

	protected static final String CONFIG_FILE = "config.xml";
	
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
	
	protected BackBoxHelper helper;
	private ArrayList<String> keys;
	
	private static BackBoxGui window;

	private boolean connected = false;
	private boolean running = false;
	
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
	
	public void update(boolean esito) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		try {
			while(model.getRowCount() > 0)
				model.removeRow(0);
			
			keys = new ArrayList<>();
			if (esito) {
				List<SimpleEntry<String, File>> map = helper.getRecords();
				if (map != null) {
					for (SimpleEntry<String, File> entry : map) {
						File f = entry.getValue();
						model.addRow(new Object[] { f.getFilename(), f.getHash(), Utility.humanReadableByteCount(f.getSize(), true),  f.isEncrypted(), f.isCompressed(), f.isSplitted() });
						keys.add(entry.getKey());
					}
				}
				
				backupFolder = helper.getConfiguration().getString(BackBoxHelper.BACKUP_FOLDER);
				chunkSize = helper.getConfiguration().getInt(BackBoxHelper.CHUNK_SIZE);
			}
			connected = esito;
			updateStatus();
		} catch (SQLException e) {
			GuiUtility.handleException(frmBackBox, "Error updating table", e);
		}
		if (pwdDialog != null)
			pwdDialog.setVisible(false);
		if (newConfDialog != null)
			newConfDialog.setVisible(false);
	}
	
	private void updatePreview(Iterable<Transaction> transactions) {
		DefaultTableModel model = (DefaultTableModel) tablePreview.getModel();
		while(model.getRowCount() > 0)
			model.removeRow(0);
		if (transactions != null) {
			for (Transaction tt : transactions)
				for (Task t : tt.getTasks())
					model.addRow(new Object[] { t.getDescription(), Utility.humanReadableByteCount(t.getWeight(), true)});
		}
	}
	
	private void updateStatus() {
		if (connected)
			lblStatus.setText("Connected");
		else
			lblStatus.setText("Not connected");
		btnConnect.setEnabled(!connected);
		btnBackup.setEnabled(connected);
		btnRestore.setEnabled(connected);
	}
	
	public void showResult() {
		ArrayList<Transaction> errors = TransactionManager.getInstance().getErrorTransactions();
		if (errors == null)
			JOptionPane.showMessageDialog(frmBackBox, "Transactions still in progress", "Error", JOptionPane.ERROR_MESSAGE);
		else if (errors.isEmpty())
			JOptionPane.showMessageDialog(frmBackBox, "Operation completed", "BackBox", JOptionPane.INFORMATION_MESSAGE);
		else {
			StringBuilder errDescriptions = new StringBuilder();
			for (Transaction t : errors) {
				errDescriptions.append(t.getResultDescription());
				errDescriptions.append("\n");
			}
			JOptionPane.showMessageDialog(frmBackBox, "Operation completed with errors: \n" + errDescriptions.toString(), "BackBox", JOptionPane.ERROR_MESSAGE);
			errors.clear();
		}
		update(true);
	}
	
	public void setPreferences(String backupFolder, Integer defaultUploadSpeed, int chunksize) {
		try {
			helper.getConfiguration().setProperty(BackBoxHelper.BACKUP_FOLDER, backupFolder);
			helper.getConfiguration().setProperty(BackBoxHelper.DEFAULT_UPLOAD_SPEED, defaultUploadSpeed);
			helper.getConfiguration().setProperty(BackBoxHelper.CHUNK_SIZE, chunksize);
			helper.saveConfiguration(CONFIG_FILE);
			
			this.backupFolder = backupFolder;
			this.chunkSize = chunksize;
			
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
		preferencesDialog = new PreferencesDialog(this);
		pwdDialog = new PasswordDialog(this);
		newConfDialog = new NewConfDialog(this);
		
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmBackBox = new JFrame();
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
		frmBackBox.setBounds(100, 100, 739, 692);
		frmBackBox.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		frmBackBox.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		mnFile.setMnemonic('F');
		menuBar.add(mnFile);
		
		JMenuItem mntmNewConfiguration = new JMenuItem("New configuration...");
		mntmNewConfiguration.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!running)
					newConfDialog.setVisible(true);
				else
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
				if (running) {
					JOptionPane.showMessageDialog(frmBackBox, "Transactions running", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				try {
					helper.uploadDB();
					connected=false;
					updateStatus();
				} catch (Exception e1) {
					GuiUtility.handleException(frmBackBox, "Error uploading database", e1);
				}
			}
		});
		mnFile.add(mntmUploadDb);
		
		JMenuItem mntmDownloadDb = new JMenuItem("Download DB");
		mntmDownloadDb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (running) {
					JOptionPane.showMessageDialog(frmBackBox, "Transactions running", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				try {
					helper.downloadDB();
					connected=false;
					updateStatus();
				} catch (Exception e1) {
					GuiUtility.handleException(frmBackBox, "Error downloading database", e1);
				}
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
				"Filename", "Hash", "Size"//, "Encrypted", "Compressed", "Splitted"
			}
		) {
			private static final long serialVersionUID = 1L;
			Class[] columnTypes = new Class[] {
				String.class, String.class, String.class//, Boolean.class, Boolean.class, Boolean.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
			boolean[] columnEditables = new boolean[] {
				false, false, false//, false, false, false
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
		
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setViewportBorder(null);
		frmBackBox.getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		final JButton btnStart = new JButton("Start");
		final JButton btnStop = new JButton("Stop");
		
		final JPopupMenu popupMenu = new JPopupMenu();
		addPopup(table, popupMenu);
		
		JMenuItem mntmDownload = new JMenuItem("Download");
		mntmDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (running) {
					JOptionPane.showMessageDialog(frmBackBox, "Transactions running", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				ArrayList<Transaction> tt = null;
				try {
					JFileChooser fc = new JFileChooser();
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int returnVal = fc.showSaveDialog(frmBackBox);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						tt = new ArrayList<Transaction>();
						int[] ii = table.getSelectedRows();
						for (int i : ii)
							tt.add(helper.downloadFile(keys.get(i), fc.getSelectedFile().getCanonicalPath(), false));
					}
					btnStart.setEnabled(true);
				} catch (Exception e1) {
					GuiUtility.handleException(frmBackBox, "Error building download transactions", e1);
				} finally {
					if (tt != null) {
						updatePreview(tt);
						if (tt.size() > 0)
							btnStart.setEnabled(true);
					}
				}
			}
		});
		
		JPanel panel = new JPanel();
		frmBackBox.getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new MigLayout("", "[70px][70px][35px][35px][117.00,grow][50.00][]", "[20px][282.00][]"));
		
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
					ArrayList<Transaction> tt = null;
					try {
						tt = helper.backup(backupFolder, chunkSize, false);
					} catch (Exception e) {
						GuiUtility.handleException(frmBackBox, "Error building backup transactions", e);
					} finally {
						if (tt != null) {
							updatePreview(tt);
							if (tt.size() > 0)
								btnStart.setEnabled(true);
						}
					}
				} else
					JOptionPane.showMessageDialog(frmBackBox, "Not connected", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		
		btnConnect = new JButton("Connect");
		btnConnect.setMnemonic('c');
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (helper.confExists(CONFIG_FILE))
					pwdDialog.setVisible(true);
				else
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
					JFileChooser fc = new JFileChooser();
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int returnVal = fc.showOpenDialog(frmBackBox);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						ArrayList<Transaction> tt = null;
						try {
							tt = helper.restore(fc.getSelectedFile().getCanonicalPath(), false);
						} catch (Exception e) {
							GuiUtility.handleException(frmBackBox, "Error building restore transactions", e);
						} finally {
							if (tt != null) {
								updatePreview(tt);
								if (tt.size() > 0)
									btnStart.setEnabled(true);
							}
						}
					}
				} else
					JOptionPane.showMessageDialog(frmBackBox, "Not connected", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		panel.add(btnRestore, "cell 2 0 2 1,grow");
		
		lblStatus = new JLabel("");
		panel.add(lblStatus, "cell 6 0,alignx right");
		
		JScrollPane scrollPanePreview = new JScrollPane();
		panel.add(scrollPanePreview, "cell 0 1 7 1,grow");
		
		tablePreview = new JTable();
		tablePreview.setModel(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				"Filename", "Size"
			}
		));
		tablePreview.getColumnModel().getColumn(0).setPreferredWidth(200);
		tablePreview.getColumnModel().getColumn(0).setMinWidth(200);
		tablePreview.getColumnModel().getColumn(1).setPreferredWidth(100);
		tablePreview.getColumnModel().getColumn(1).setMaxWidth(100);
		scrollPanePreview.setViewportView(tablePreview);
		scrollPanePreview.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPanePreview.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPanePreview.setViewportBorder(null);
		
		final JSpinner currentUploadSpeed = new JSpinner();
		currentUploadSpeed.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				ProgressManager.getInstance().setSpeed(ProgressManager.UPLOAD_ID, ((int) currentUploadSpeed.getValue()) * 1024);
			}
		});
		currentUploadSpeed.setValue(ProgressManager.getInstance().getSpeed(ProgressManager.UPLOAD_ID) / 1024);
		panel.add(currentUploadSpeed, "cell 2 2,growx");
		
		JLabel lblKbs = new JLabel("KB\\s");
		panel.add(lblKbs, "cell 3 2");

		final JProgressBar progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		panel.add(progressBar, "cell 4 2,grow");
		
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				TransactionManager.getInstance().stopTransactions();
				btnStop.setEnabled(false);
				btnStart.setEnabled(true);
				running = false;
			}
		});
		btnStop.setEnabled(false);
		panel.add(btnStop, "cell 1 2,grow");
		
		btnStart.setEnabled(false);
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnStop.setEnabled(true);
				btnStart.setEnabled(false);

				ProgressListener listener = new ProgressListener() {
					long partial = 0;
					long subpartial = 0;
					long speedSubpartial = 0;
					long lastTimestamp = 0;
					long averagespeed = 0;
					long speeds[] = new long[5];
					int i = 0;
					
					@Override
					public void update(long bytes) {
						subpartial += bytes;
						if (ProgressManager.getInstance().getSpeed(ProgressManager.UPLOAD_ID) == 0) {
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
							averagespeed = ProgressManager.getInstance().getSpeed(ProgressManager.UPLOAD_ID);
						
						if (averagespeed > 0) {
							partial += subpartial;
							subpartial = 0;
							
							long b = TransactionManager.getInstance().getAllTasks() - partial;
							lblEtaValue.setText(GuiUtility.getETAString(b, averagespeed));
						}
						
						TransactionManager.getInstance().taskCompleted(bytes);
					}
				};
				
				ProgressManager.getInstance().setListener(ProgressManager.UPLOAD_ID, listener);
				ProgressManager.getInstance().setListener(ProgressManager.DOWNLOAD_ID, listener);

				TransactionManager tm = TransactionManager.getInstance();
				tm.runTransactions();
				tm.shutdown();
				
				running = true;
				Runnable runnable = new Runnable() {
					
					@Override
					public void run() {
						int perc = helper.getProgressPercentage();
						progressBar.setValue(perc);
						while (perc > -1) {
							try {
								Thread.sleep(1000);
							} catch(Exception e) {}
							perc = helper.getProgressPercentage();
							progressBar.setValue(perc);
						}
						btnStop.setEnabled(false);
//						btnStart.setEnabled(true);
						running = false;
						showResult();
						updatePreview(null);
					}
				};
				Thread t = new Thread(runnable, "ProgressBar");
				t.start();
			}
		});
		panel.add(btnStart, "cell 0 2,grow");
		
		JLabel lblEta = new JLabel("ETA:");
		panel.add(lblEta, "cell 5 2,alignx right");
		
		lblEtaValue = new JLabel("");
		panel.add(lblEtaValue, "cell 6 2");
		
		popupMenu.add(mntmDownload);
		
		updateStatus();
	}

	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				int r = window.table.rowAtPoint(e.getPoint());
	            if (r >= 0 && r < window.table.getRowCount()) {
	            	window.table.addRowSelectionInterval(r, r);
	            } else {
	            	window.table.clearSelection();
	            }

	            int[] rowindex = window.table.getSelectedRows();
	            if (rowindex.length == 0)
	                return;
	            if (e.isPopupTrigger() && e.getComponent() instanceof JTable ) {
	            	popup.show(e.getComponent(), e.getX(), e.getY());
	            }
			}
		});
	}
}
