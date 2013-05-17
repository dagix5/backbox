package it.backbox.gui;

import it.backbox.gui.utility.GuiUtility;
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
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;

public class PasswordDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	
	public static final int LOGIN_MODE = 0;
	public static final int BUILDDB_MODE = 1;
	
	private final JPanel contentPanel = new JPanel();
	private JPasswordField passwordField;
	private JButton okButton;
	private int mode = LOGIN_MODE;
	
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
				main.showLoading();
				setVisible(false);
				Thread worker = new Thread() {
					public void run() {
						if (mode == LOGIN_MODE) {
							boolean pwd = main.helper.login(new String(passwordField.getPassword()));
							if (main.helper.getConfiguration().containsKey(BackBoxHelper.DEFAULT_UPLOAD_SPEED))
								ProgressManager.getInstance().setSpeed(ProgressManager.UPLOAD_ID, main.helper.getConfiguration().getInt(BackBoxHelper.DEFAULT_UPLOAD_SPEED));
							lblPasswordErrata.setVisible(!pwd);
							passwordField.setText("");
							if (pwd)
								main.connect();
							else
								setVisible(true);
						} else if (mode == BUILDDB_MODE) {
							try {
								main.helper.buildDB(new String(passwordField.getPassword()));
							} catch (Exception e) {
								main.hideLoading();
								GuiUtility.handleException(contentPanel, "Error building database", e);
							}
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

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}
	
}
