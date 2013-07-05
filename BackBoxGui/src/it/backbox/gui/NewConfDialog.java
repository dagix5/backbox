package it.backbox.gui;

import it.backbox.bean.Folder;
import it.backbox.gui.utility.FoldersPanel;
import it.backbox.gui.utility.GuiUtility;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;

public class NewConfDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	
	private JPanel contentPanel;
	private JPasswordField passwordField;
	private JPasswordField passwordField_1;
	private FoldersPanel foldersPanel;

	/**
	 * Create the dialog.
	 * @param BackboxGui 
	 */
	public NewConfDialog(final BackBoxGui main) {
		setResizable(false);
		contentPanel = new JPanel();
		
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("New configuration");
		setBounds(100, 100, 395, 408);
		getContentPane().setLayout(new MigLayout("", "[434px,grow]", "[320.00px][33px]"));
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, "cell 0 0,grow");
		contentPanel.setLayout(new MigLayout("", "[82.00px][90.00:90.00:90.00][:150.00:150.00]", "[][][][189.00][][]"));
		
		JLabel label = new JLabel("Password:");
		contentPanel.add(label, "cell 0 0,alignx trailing");
	
		passwordField = new JPasswordField();
		contentPanel.add(passwordField, "cell 1 0 2 1,growx");
	
		JLabel labelC = new JLabel("Confirm password:");
		contentPanel.add(labelC, "cell 0 1,alignx trailing");
	
		passwordField_1 = new JPasswordField();
		contentPanel.add(passwordField_1, "cell 1 1 2 1,growx");
		
		final JLabel lblPasswordErrata = new JLabel("Passwords do not match");
		lblPasswordErrata.setForeground(Color.RED);
		lblPasswordErrata.setVisible(false);
		contentPanel.add(lblPasswordErrata, "cell 1 2");
	
		JLabel lblFolderToBackup = new JLabel("Folders to backup:");
		contentPanel.add(lblFolderToBackup, "cell 0 3,alignx trailing");
		
		foldersPanel = new FoldersPanel(getContentPane());
		contentPanel.add(foldersPanel, "cell 1 3 2 1,grow");

		final JLabel lblSetFolderTo = new JLabel("Set folders to backup");
		lblSetFolderTo.setForeground(Color.RED);
		lblSetFolderTo.setVisible(false);
		contentPanel.add(lblSetFolderTo, "cell 1 4");
		
		JLabel lblChunksize = new JLabel("Chunk size");
		contentPanel.add(lblChunksize, "cell 0 5,alignx right");
		
		final JSpinner spinnerChunksize = new JSpinner();
		spinnerChunksize.setModel(new SpinnerNumberModel(new Integer(1024), new Integer(0), null, new Integer(1)));
		contentPanel.add(spinnerChunksize, "cell 1 5,growx");
		
		JLabel lblKb = new JLabel("KB");
		contentPanel.add(lblKb, "cell 2 5");
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, "cell 0 1,growx,aligny top");

		final JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				main.showLoading();
				setVisible(false);
				Thread worker = new Thread() {
					public void run() {
						try {
							lblSetFolderTo.setVisible(false);
							lblPasswordErrata.setVisible(false);
							
							if ((passwordField.getPassword().length == 0) || !Arrays.equals(passwordField.getPassword(), passwordField_1.getPassword())) {
								main.hideLoading();
								setVisible(true);
								lblPasswordErrata.setVisible(true);
								return;
							}
							
							List<Folder> folders = foldersPanel.getFolders();
							if (folders.isEmpty()) {
								main.hideLoading();
								setVisible(true);
								lblSetFolderTo.setVisible(true);
								return;
							}
							
							main.helper.register(new String(passwordField.getPassword()), folders, (int) spinnerChunksize.getValue() * 1024);
							passwordField.setText("");
							passwordField_1.setText("");
							lblPasswordErrata.setVisible(false);
							main.connect();	
						} catch (Exception e) {
							main.hideLoading();
							setVisible(true);
							GuiUtility.handleException(contentPanel, "Error registering new configuration", e);
							main.disconnect();
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

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
			}
		});
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);
	}

}
