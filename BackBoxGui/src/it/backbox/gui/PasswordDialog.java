package it.backbox.gui;

import it.backbox.progress.ProgressManager;
import it.backbox.utility.BackBoxHelper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;

public class PasswordDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JPasswordField passwordField;
	private JButton okButton;
	
	/**
	 * Create the dialog.
	 */
	public PasswordDialog(final BackBoxGui main) {
		setModalityType(ModalityType.APPLICATION_MODAL);
		setType(Type.POPUP);
		setResizable(false);
		setTitle("Connect");
		setBounds(100, 100, 234, 115);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new MigLayout("", "[50px][126.00px,grow]", "[20px][][]"));
		
		JLabel lblPassword = new JLabel("Password:");
		contentPanel.add(lblPassword, "cell 0 0,alignx left,aligny top");
		
		passwordField = new JPasswordField();
		contentPanel.add(passwordField, "cell 1 0,growx,aligny top");
		
		final JLabel lblPasswordErrata = new JLabel("Wrong password");
		lblPasswordErrata.setForeground(Color.RED);
		lblPasswordErrata.setVisible(false);
		contentPanel.add(lblPasswordErrata, "cell 1 1,growy");
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				boolean pwd = main.helper.login(BackBoxGui.CONFIG_FILE, new String(passwordField.getPassword()));
				ProgressManager.getInstance().setSpeed(ProgressManager.UPLOAD_ID, main.helper.getConfiguration().getInt(BackBoxHelper.DEFAULT_UPLOAD_SPEED));
				lblPasswordErrata.setVisible(!pwd);
				passwordField.setText("");
				if (pwd)
					main.update(true);
					
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
