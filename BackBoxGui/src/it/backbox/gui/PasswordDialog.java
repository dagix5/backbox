package it.backbox.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import it.backbox.exception.BackBoxWrongPasswordException;
import it.backbox.gui.utility.BackBoxHelper;
import it.backbox.gui.utility.GuiUtility;
import it.backbox.gui.utility.ThreadActionListener;
import net.miginfocom.swing.MigLayout;

public class PasswordDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	
	private int mode = GuiConstant.LOGIN_MODE;
	
	private final JPanel contentPanel = new JPanel();
	private JPasswordField passwordField;
	private JButton okButton;
	private JLabel lblPasswordErrata;
	
	/**
	 * Create the dialog.
	 */
	public PasswordDialog(final BackBoxGui main, JFrame parent) {
		super(parent);
		
		GuiUtility.checkEDT(true);
		
		setModalityType(ModalityType.APPLICATION_MODAL);
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
		
		lblPasswordErrata = new JLabel("Wrong password");
		lblPasswordErrata.setForeground(Color.RED);
		lblPasswordErrata.setVisible(false);
		contentPanel.add(lblPasswordErrata, "cell 1 1,growy");
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		okButton = new JButton("OK");
		okButton.addActionListener(new ThreadActionListener() {
			
			@Override
			protected boolean preaction(ActionEvent e) {
				setVisible(false);
				lblPasswordErrata.setVisible(false);
				return true;
			}
			
			@Override
			protected void action(ActionEvent ev) {
				try {
					if (mode == GuiConstant.LOGIN_MODE) {
						BackBoxHelper.getInstance().login(new String(passwordField.getPassword()));
						main.connect();
					} else if (mode == GuiConstant.BUILDDB_MODE)
						BackBoxHelper.getInstance().buildDB(new String(passwordField.getPassword()));
				} catch (BackBoxWrongPasswordException e) {
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							lblPasswordErrata.setVisible(true);
							passwordField.setText("");
							setVisible(true);
						}
					});
				} catch (Exception e) {
					hideLoadingLater(ev);
					GuiUtility.handleException(contentPanel, "Error logging in", e);
				}
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
	
	public void load(int mode) {
		GuiUtility.checkEDT(true);
		this.mode = mode;
		lblPasswordErrata.setVisible(false);
		passwordField.setText("");
	}
	
}
