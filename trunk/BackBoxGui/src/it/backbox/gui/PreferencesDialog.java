package it.backbox.gui;

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
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class PreferencesDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private JTextField backupFolder;
	private JSpinner defaultUploadSpeed;

	/**
	 * Create the dialog.
	 * @throws ConfigurationException 
	 */
	public PreferencesDialog(final BackBoxGui main) {
		setModalityType(ModalityType.APPLICATION_MODAL);
		
		setTitle("Preferences");
		setBounds(100, 100, 450, 144);
		getContentPane().setLayout(new BorderLayout());
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				main.savePreferences(backupFolder.getText(), ((Integer) defaultUploadSpeed.getValue()) * 1024);
			}
		});
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);

		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new MigLayout("", "[141.00px][49.00px,grow][250.00]", "[20px][][][][][]"));

		JLabel lblFolderToBackup = new JLabel("Folder to backup");
		panel.add(lblFolderToBackup, "cell 0 0,alignx trailing,aligny center");

		backupFolder = new JTextField();
		backupFolder.setColumns(20);
		backupFolder.setText(main.helper.getConfiguration().getString("backupFolder"));
		panel.add(backupFolder, "cell 1 0 2 1,growx,aligny top");
		
		JLabel lblDefaultUploadSpeed = new JLabel("Default upload speed");
		panel.add(lblDefaultUploadSpeed, "cell 0 1");
		
		defaultUploadSpeed = new JSpinner();
		defaultUploadSpeed.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
		panel.add(defaultUploadSpeed, "cell 1 1,growx");
		
		JLabel lblKbsset = new JLabel("KB\\s (set 0 for unlmited)");
		panel.add(lblKbsset, "cell 2 1");
	}

}
