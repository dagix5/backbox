package it.backbox.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.naming.ConfigurationException;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

import it.backbox.bean.ProxyConfiguration;
import it.backbox.gui.utility.BackBoxHelper;
import it.backbox.gui.utility.GuiUtility;
import it.backbox.gui.utility.ThreadActionListener;
import net.miginfocom.swing.MigLayout;

public class PreferencesDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private JSpinner defaultUploadSpeed;
	private JSpinner defaultDownloadSpeed;
	private JTextField txtProxyPort;
	private JTextField txtProxyAddress;
	private JCheckBox chckbxProxy;
	private JCheckBox chckbxAutoUpload;
	
	/**
	 * Create the dialog.
	 * @throws ConfigurationException 
	 */
	public PreferencesDialog(JFrame parent) {
		super(parent);
		
		GuiUtility.checkEDT(true);
		
		setModalityType(ModalityType.APPLICATION_MODAL);
		
		setTitle("Preferences");
		setBounds(100, 100, 450, 283);
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
		panel.setLayout(new MigLayout("", "[120px:120px][90px:90px][150px:n,grow]", "[][][][][][][]"));
		
		JLabel lblDefaultUploadSpeed = new JLabel("Default upload speed");
		panel.add(lblDefaultUploadSpeed, "cell 0 0,alignx right");
		
		defaultUploadSpeed = new JSpinner();
		panel.add(defaultUploadSpeed, "cell 1 0,growx");
		
		JLabel lblKbsset = new JLabel("KB\\s (set 0 for unlimited)");
		panel.add(lblKbsset, "cell 2 0");
		
		JLabel lblDefaultDownloadSpeed = new JLabel("Default download speed");
		panel.add(lblDefaultDownloadSpeed, "cell 0 1,alignx right,growy");
		
		defaultDownloadSpeed = new JSpinner();
		panel.add(defaultDownloadSpeed, "cell 1 1,growx");
		
		JLabel lblKbsset2 = new JLabel("KB\\s (set 0 for unlimited)");
		panel.add(lblKbsset2, "cell 2 1");
		
		chckbxAutoUpload = new JCheckBox("Auto-Upload configuration on exit");
		panel.add(chckbxAutoUpload, "cell 1 2 2 1,alignx left,growy");
		
		chckbxProxy = new JCheckBox("Proxy");
		chckbxProxy.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				txtProxyAddress.setEnabled(chckbxProxy.isSelected());
				txtProxyPort.setEnabled(chckbxProxy.isSelected());
			}
		});
		panel.add(chckbxProxy, "cell 1 4,alignx left,growy");
		
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
					int upSpeed = ((int) defaultUploadSpeed.getValue()) * 1024;
					int downSpeed = ((int) defaultDownloadSpeed.getValue()) * 1024;
					
					BackBoxHelper helper = BackBoxHelper.getInstance();
					helper.getConfiguration().setDefaultUploadSpeed(upSpeed);
					helper.getConfiguration().setDefaultDownloadSpeed(downSpeed);
					
					helper.getConfiguration().setAutoUploadConf(chckbxAutoUpload.isSelected());
					
					ProxyConfiguration pc = new ProxyConfiguration(chckbxProxy.isSelected(), txtProxyAddress.getText(), (txtProxyPort.getText().isEmpty() ? 0 : Integer.parseInt(txtProxyPort.getText())));
					helper.setProxyConfiguration(pc);
					
					helper.getConfiguration().setProxyConfiguration(pc);
					helper.saveConfiguration();
					
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							setVisible(false);
						}
					});
				} catch (final Exception e) {
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							setVisible(true);
							GuiUtility.handleException(panel, "Error setting preferences", e);
						}
					});
					
				}
				
			}
		});
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);
	}

	public void load(int uploadSpeed, int downloadSpeed, ProxyConfiguration pc, boolean proxyChcbxEnabled, boolean autoUpload) {
		GuiUtility.checkEDT(true);
		
		defaultUploadSpeed.setModel(new SpinnerNumberModel(uploadSpeed / 1024, Integer.valueOf(0), null, Integer.valueOf(1)));
		defaultDownloadSpeed.setModel(new SpinnerNumberModel(downloadSpeed / 1024, Integer.valueOf(0), null, Integer.valueOf(1)));
		
		chckbxAutoUpload.setSelected(autoUpload);
		
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
		
	}
	
	static class DocumentInputFilter extends DocumentFilter {
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
