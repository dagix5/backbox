package it.backbox.gui;

import it.backbox.bean.Folder;
import it.backbox.exception.BackBoxException;
import it.backbox.exception.RestException;
import it.backbox.gui.utility.FoldersPanel;
import it.backbox.gui.utility.GuiUtility;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

public class ConfigurationDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private FoldersPanel foldersPanel;

	/**
	 * Create the dialog.
	 */
	public ConfigurationDialog(final BackBoxGui main) {
		setModalityType(ModalityType.APPLICATION_MODAL);
		
		setTitle("Configuration");
		setBounds(100, 100, 450, 274);
		getContentPane().setLayout(new BorderLayout());
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.NORTH);
		panel.setLayout(new MigLayout("", "[89px]", "[14px]"));
		
		JLabel lblFoldersToBackup = new JLabel("Folders to backup:");
		lblFoldersToBackup.setHorizontalAlignment(SwingConstants.LEFT);
		lblFoldersToBackup.setVerticalAlignment(SwingConstants.BOTTOM);
		panel.add(lblFoldersToBackup, "cell 0 0,alignx left,aligny bottom");
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				main.showLoading();
				setVisible(false);
				Thread worker = new Thread() {
					public void run() {
						try {
							main.helper.updateBackupFolders(foldersPanel.getFolders());
							main.clearMenu();
							main.updateMenu();
						} catch (IOException | RestException | BackBoxException e) {
							main.hideLoading();
							GuiUtility.handleException(getContentPane(), "Error updating configuration", e);
							setVisible(true);
						}
						
						SwingUtilities.invokeLater(new Runnable() {
		                    public void run() {
		                    	main.hideLoading();
		                    }
		                });
					}
				};
				worker.start();
			}
		});
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
			}
		});
		buttonPane.add(btnCancel);
		
		foldersPanel = new FoldersPanel(getContentPane());
		lblFoldersToBackup.setLabelFor(foldersPanel);
		getContentPane().add(foldersPanel, BorderLayout.CENTER);
	}
	
	public void load(List<Folder> backupFolders) {
		foldersPanel.load(backupFolders);
	}

}
