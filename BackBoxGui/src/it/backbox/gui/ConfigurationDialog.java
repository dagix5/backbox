package it.backbox.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import it.backbox.bean.Folder;
import it.backbox.exception.BackBoxException;
import it.backbox.exception.RestException;
import it.backbox.gui.panel.FoldersPanel;
import it.backbox.gui.utility.BackBoxHelper;
import it.backbox.gui.utility.GuiUtility;
import it.backbox.gui.utility.ThreadActionListener;
import net.miginfocom.swing.MigLayout;

public class ConfigurationDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private FoldersPanel foldersPanel;

	/**
	 * Create the dialog.
	 */
	public ConfigurationDialog(final BackBoxGui main, JFrame parent) {
		super(parent);
		
		GuiUtility.checkEDT(true);
		
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
		okButton.addActionListener(new ThreadActionListener() {
			
			@Override
			protected boolean preaction(ActionEvent event) {
				setVisible(false);
				return true;
			}
			
			@Override
			protected void action(ActionEvent event) {
				try {
					BackBoxHelper.getInstance().updateBackupFolders(foldersPanel.getFolders());
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							main.clearMenu();
							main.updateMenu();
							
						}
					});
				} catch (IOException | RestException | BackBoxException e) {
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							LoadingDialog.getInstance().hideLoading();
							GuiUtility.handleException(getContentPane(), "Error updating configuration", e);
							setVisible(true);
						}
					});
				}
				
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
		GuiUtility.checkEDT(true);
		
		foldersPanel.load(backupFolders);
	}

}
