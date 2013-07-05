package it.backbox.gui;

import it.backbox.bean.ProxyConfiguration;
import it.backbox.gui.utility.GuiUtility;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.ConfigurationException;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

import net.miginfocom.swing.MigLayout;

public class PreferencesDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private JSpinner defaultUploadSpeed;
	private JComboBox<Level> comboBox;
	private JTextField txtProxyPort;
	private JTextField txtProxyAddress;
	private JCheckBox chckbxProxy;
	
	private Level newLevel = null;

	/**
	 * Create the dialog.
	 * @throws ConfigurationException 
	 */
	public PreferencesDialog(final BackBoxGui main) {
		setModalityType(ModalityType.APPLICATION_MODAL);
		
		setTitle("Preferences");
		setBounds(100, 100, 450, 220);
		getContentPane().setLayout(new BorderLayout());
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
			}
		});
		buttonPane.add(btnCancel);

		final JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new MigLayout("", "[141.00px][49.00px,grow][250.00]", "[][][][][][][][][][][][][]"));
		
		JLabel lblDefaultUploadSpeed = new JLabel("Default upload speed");
		panel.add(lblDefaultUploadSpeed, "cell 0 0,alignx right");
		
		defaultUploadSpeed = new JSpinner();
		panel.add(defaultUploadSpeed, "cell 1 0,growx");
		
		JLabel lblKbsset = new JLabel("KB\\s (set 0 for unlmited)");
		panel.add(lblKbsset, "cell 2 0");
		
		JLabel lblLogLevel = new JLabel("Log level");
		panel.add(lblLogLevel, "cell 0 1,alignx trailing");
		
		comboBox = new JComboBox<Level>();
		comboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				newLevel = (Level) arg0.getItem();
			}
		});
		comboBox.addItem(Level.ALL);
		comboBox.addItem(Level.FINE);
		comboBox.addItem(Level.INFO);
		comboBox.addItem(Level.WARNING);
		comboBox.addItem(Level.SEVERE);
		comboBox.addItem(Level.OFF);
		
		panel.add(comboBox, "cell 1 1 2 1,growx");
		
		JSeparator separator = new JSeparator();
		panel.add(separator, "cell 0 2 3 1");
		
		JLabel lblProxyAddress = new JLabel("Proxy Address");
		panel.add(lblProxyAddress, "cell 0 5,alignx trailing");
		
		txtProxyAddress = new JTextField();
		txtProxyAddress.setEnabled(false);
		panel.add(txtProxyAddress, "cell 1 5 2 1,growx");
		txtProxyAddress.setColumns(10);
		
		JLabel lblProxyPort = new JLabel("Proxy Port");
		panel.add(lblProxyPort, "cell 0 6,alignx trailing");
		
		txtProxyPort = new JTextField();
		txtProxyPort.setEnabled(false);
		panel.add(txtProxyPort, "cell 1 6 2 1,growx");
		Document doc = txtProxyPort.getDocument();
		if (doc instanceof AbstractDocument) {
			AbstractDocument abDoc = (AbstractDocument) doc;
			abDoc.setDocumentFilter(new DocumentInputFilter());
		}
		
		chckbxProxy = new JCheckBox("Proxy");
		chckbxProxy.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				txtProxyAddress.setEnabled(chckbxProxy.isSelected());
				txtProxyPort.setEnabled(chckbxProxy.isSelected());
			}
		});
		panel.add(chckbxProxy, "cell 0 4,alignx right");
		
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				main.showLoading();
				setVisible(false);
				Thread worker = new Thread() {
					public void run() {
						try {
							main.setPreferences(((Integer) defaultUploadSpeed.getValue()) * 1024);
							if (newLevel != null) {
								Logger.getLogger("it.backbox").setLevel(newLevel);
								main.helper.getConfiguration().setLogLevel(newLevel);
							}
							
							ProxyConfiguration pc = new ProxyConfiguration(chckbxProxy.isSelected(), txtProxyAddress.getText(), (txtProxyPort.getText().isEmpty() ? 0 : Integer.parseInt(txtProxyPort.getText())));
							main.helper.setProxyConfiguration(pc);
							
							main.helper.getConfiguration().setProxyConfiguration(pc);
							main.helper.saveConfiguration();
							
							setVisible(false);
						} catch (Exception e) {
							setVisible(true);
							GuiUtility.handleException(panel, "Error setting preferences", e);
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
	}

	public void load(int uploadSpeed, ProxyConfiguration pc, boolean proxyChcbxEnabled, Level logLevel) {
		defaultUploadSpeed.setModel(new SpinnerNumberModel(uploadSpeed / 1024, new Integer(0), null, new Integer(1)));
		chckbxProxy.setEnabled(proxyChcbxEnabled);
		if (pc != null) {
			chckbxProxy.setSelected(pc.isEnabled());
			txtProxyAddress.setText(pc.getAddress());
			txtProxyPort.setText(String.valueOf(pc.getPort()));
		} else {
			chckbxProxy.setSelected(false);
			txtProxyAddress.setText("");
			txtProxyPort.setText("");
		}
		
		if (logLevel.equals(Level.ALL))
			comboBox.setSelectedIndex(0);
		else if (logLevel.equals(Level.FINE))
			comboBox.setSelectedIndex(1);
		else if (logLevel.equals(Level.INFO))
			comboBox.setSelectedIndex(2);
		else if (logLevel.equals(Level.WARNING))
			comboBox.setSelectedIndex(3);
		else if (logLevel.equals(Level.SEVERE))
			comboBox.setSelectedIndex(4);
		else if (logLevel.equals(Level.OFF))
			comboBox.setSelectedIndex(5);
	}
	
	class DocumentInputFilter extends DocumentFilter {
		public void insertString(FilterBypass fb, int offset, String text,
				AttributeSet as) throws BadLocationException {
			int len = text.length();
			if (len > 0) {
				if (Character.isDigit(text.charAt(len - 1)))
					super.insertString(fb, offset, text, as);
				else {
					Toolkit.getDefaultToolkit().beep();
				}
			}
		}

		public void replace(FilterBypass fb, int offset, int length,
				String text, AttributeSet as) throws BadLocationException {
			int len = text.length();
			if (len > 0) {
				if (Character.isDigit(text.charAt(len - 1)))
					super.replace(fb, offset, length, text, as);
				else {
					Toolkit.getDefaultToolkit().beep();
				}
			}
		}
	}
}
