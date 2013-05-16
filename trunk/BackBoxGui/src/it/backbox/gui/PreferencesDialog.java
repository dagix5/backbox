package it.backbox.gui;

import it.backbox.utility.BackBoxHelper;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.configuration.ConfigurationException;

public class PreferencesDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private JSpinner defaultUploadSpeed;
	private JComboBox<Level> comboBox;
	
	private Level newLevel = null;

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
				main.setPreferences(((Integer) defaultUploadSpeed.getValue()) * 1024);
				if (newLevel != null)
					Logger.getLogger("it.backbox").setLevel(newLevel);
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
		panel.setLayout(new MigLayout("", "[141.00px][49.00px,grow][250.00]", "[][][][][][]"));
		
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
		comboBox.addItem(Level.INFO);
		comboBox.addItem(Level.WARNING);
		comboBox.addItem(Level.SEVERE);
		comboBox.addItem(Level.OFF);
		
		panel.add(comboBox, "cell 1 1 2 1,growx");
	}

	public void loadPref(int uploadSpeed) {
		defaultUploadSpeed.setModel(new SpinnerNumberModel(uploadSpeed / 1024, new Integer(0), null, new Integer(1)));
		Level level = Logger.getLogger("it.backbox").getLevel();
		if (level.equals(Level.ALL))
			comboBox.setSelectedIndex(0);
		else if (level.equals(Level.INFO))
			comboBox.setSelectedIndex(1);
		else if (level.equals(Level.WARNING))
			comboBox.setSelectedIndex(2);
		else if (level.equals(Level.SEVERE))
			comboBox.setSelectedIndex(3);
		else if (level.equals(Level.OFF))
			comboBox.setSelectedIndex(4);
	}
}
