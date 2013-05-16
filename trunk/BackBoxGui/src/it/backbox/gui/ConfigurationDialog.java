package it.backbox.gui;

import it.backbox.utility.BackBoxHelper;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.configuration.ConfigurationException;

public class ConfigurationDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private JTextField backupFolder;

	/**
	 * Create the dialog.
	 * @throws ConfigurationException 
	 */
	public ConfigurationDialog(final BackBoxGui main) {
		setModalityType(ModalityType.APPLICATION_MODAL);
		
		setTitle("Configuration");
		setBounds(100, 100, 450, 144);
		getContentPane().setLayout(new BorderLayout());
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				main.setConfiguration(backupFolder.getText());
				setVisible(false);
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

		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new MigLayout("", "[141.00px][49.00px,grow][250.00]", "[28.00px]"));

		JLabel lblFolderToBackup = new JLabel("Folder to backup");
		panel.add(lblFolderToBackup, "cell 0 0,alignx trailing,aligny center");

		backupFolder = new JTextField();
		backupFolder.setColumns(20);
		backupFolder.setText(main.helper.getConfiguration().getString(BackBoxHelper.BACKUP_FOLDER));
		panel.add(backupFolder, "cell 1 0 2 1,growx");
	}
	
	public void loadConf(String backupFolder) {
		this.backupFolder.setText(backupFolder);
	}

}
