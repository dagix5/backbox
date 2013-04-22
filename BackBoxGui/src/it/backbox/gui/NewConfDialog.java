package it.backbox.gui;

import it.backbox.gui.utility.GuiUtility;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;

public class NewConfDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JPasswordField passwordField;
	private JPasswordField passwordField_1;
	private JTextField textField;

	/**
	 * Create the dialog.
	 * @param BackboxGui 
	 */
	public NewConfDialog(final BackBoxGui main) {
		setTitle("New configuration");
		setBounds(100, 100, 450, 255);
		getContentPane().setLayout(new MigLayout("", "[434px,grow]", "[185px][31.00,grow][33px]"));
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, "cell 0 0,grow");
		contentPanel.setLayout(new MigLayout("", "[82.00px][93.00][172.00][]", "[][][][][][][]"));
		
		JLabel label = new JLabel("Password:");
		contentPanel.add(label, "cell 0 0,alignx trailing");
	
		passwordField = new JPasswordField();
		contentPanel.add(passwordField, "cell 1 0 3 1,growx");
	
		JLabel labelC = new JLabel("Confirm password:");
		contentPanel.add(labelC, "cell 0 1,alignx trailing");
	
		passwordField_1 = new JPasswordField();
		contentPanel.add(passwordField_1, "cell 1 1 3 1,growx");
		
		final JLabel lblPasswordErrata = new JLabel("Passwords do not match");
		lblPasswordErrata.setForeground(Color.RED);
		lblPasswordErrata.setVisible(false);
		contentPanel.add(lblPasswordErrata, "cell 1 2");
	
		JLabel lblFolderToBackup = new JLabel("Folder to backup:");
		contentPanel.add(lblFolderToBackup, "cell 0 4,alignx trailing");
		
		textField = new JTextField();
		contentPanel.add(textField, "cell 1 4 2 1,growx");
		textField.setColumns(10);

		JButton btnNewButton = new JButton("Browse...");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(contentPanel);
				if (returnVal == JFileChooser.APPROVE_OPTION)
					try {
						textField.setText(fc.getSelectedFile().getCanonicalPath());
					} catch (IOException e) {
						GuiUtility.handleException(contentPanel, "Error getting canonical path", e);
					}
			}
		});
		contentPanel.add(btnNewButton, "cell 3 4");
		
		final JLabel lblSetFolderTo = new JLabel("Set folder to backup");
		lblSetFolderTo.setForeground(Color.RED);
		lblSetFolderTo.setVisible(false);
		contentPanel.add(lblSetFolderTo, "cell 1 5");
		
		JLabel lblChunksize = new JLabel("Chunk size");
		contentPanel.add(lblChunksize, "cell 0 6,alignx right");
		
		final JSpinner spinnerChunksize = new JSpinner();
		spinnerChunksize.setModel(new SpinnerNumberModel(new Integer(1024), new Integer(0), null, new Integer(1)));
		contentPanel.add(spinnerChunksize, "cell 1 6,growx");
		
		JLabel lblKb = new JLabel("KB");
		contentPanel.add(lblKb, "cell 2 6");
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, "cell 0 2,growx,aligny top");

		final JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					lblSetFolderTo.setVisible(false);
					lblPasswordErrata.setVisible(false);
					if (textField.getText().isEmpty() || !Files.exists(Paths.get(textField.getText()))) {
						lblSetFolderTo.setVisible(true);
						return;
					}
					if ((passwordField.getPassword().length == 0) || !Arrays.equals(passwordField.getPassword(), passwordField_1.getPassword())) {
						lblPasswordErrata.setVisible(true);
						return;
					}
					main.setPreferences(textField.getText(), 0, (int) spinnerChunksize.getValue() * 1024);
					
					main.helper.register(BackBoxGui.CONFIG_FILE, new String(passwordField.getPassword()));
					passwordField.setText("");
					passwordField_1.setText("");
					lblPasswordErrata.setVisible(false);
					main.update(true);	
				} catch (Exception e) {
					GuiUtility.handleException(contentPanel, "Error registering new configuration", e);
				}
			}
		});
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				main.update(false);
			}
		});
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);
	}

}
