package it.backbox.gui;

import it.backbox.gui.bean.TableTask;
import it.backbox.gui.utility.GuiUtility;
import it.backbox.transaction.task.Transaction;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;

public class DetailsDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private static DetailsDialog dialog;
	private final JPanel contentPanel = new JPanel();
	private JLabel lblTransactionIdValue;
	private JLabel lblTaskIdValue;
	private JLabel lblFilenameValue;
	private JLabel lblOperationValue;
	private JLabel lblSizeValue;
	private JLabel lblStatusValue;
	private JLabel lblTotalTimeValue;
	private JTextPane txtResult;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			dialog = new DetailsDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateDetails(TableTask tbt) {
		lblTransactionIdValue.setText(tbt.getTransaction().getId());
		lblTaskIdValue.setText(tbt.getTask().getId());
		lblFilenameValue.setText(tbt.getTask().getDescription());
		lblOperationValue.setText(GuiUtility.getTaskType(tbt.getTask()));
		lblSizeValue.setText(GuiUtility.getTaskSize(tbt.getTask()));
		if (tbt.getTransaction().getResultCode() == Transaction.NO_ESITO)
			lblStatusValue.setText("Not executed");
		else if (tbt.getTransaction().getResultCode() == Transaction.ESITO_KO) {
			lblStatusValue.setText("Error");
			txtResult.setText(tbt.getTransaction().getResultDescription());
		} else if (tbt.getTransaction().getResultCode() == Transaction.ESITO_OK)
			lblStatusValue.setText("Success");
		lblTotalTimeValue.setText(GuiUtility.getTimeString(tbt.getTask().getTotalTime() / 1000));
	}
	
	/**
	 * Create the dialog.
	 */
	public DetailsDialog() {
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle("Details");
		setBounds(100, 100, 450, 302);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new MigLayout("", "[125.00,grow][322.00]", "[][][][][][][][][grow]"));
		
		JLabel lblTransactionId = new JLabel("Transaction ID: ");
		contentPanel.add(lblTransactionId, "cell 0 0,alignx right,growy");
		
		lblTransactionIdValue = new JLabel("");
		contentPanel.add(lblTransactionIdValue, "cell 1 0,alignx left,growy");
		
		JLabel lblTaskId = new JLabel("Task ID: ");
		contentPanel.add(lblTaskId, "cell 0 1,alignx right,growy");
		
		lblTaskIdValue = new JLabel("");
		contentPanel.add(lblTaskIdValue, "cell 1 1,alignx left,growy");
		
		JLabel lblFilename = new JLabel("Filename: ");
		contentPanel.add(lblFilename, "cell 0 2,alignx right,growy");
		
		lblFilenameValue = new JLabel("");
		contentPanel.add(lblFilenameValue, "cell 1 2,alignx left,growy");
		
		JLabel lblOperation = new JLabel("Operation: ");
		contentPanel.add(lblOperation, "cell 0 3,alignx right,growy");
		
		lblOperationValue = new JLabel("");
		contentPanel.add(lblOperationValue, "cell 1 3,alignx left,growy");
		
		JLabel lblSize = new JLabel("Size: ");
		contentPanel.add(lblSize, "cell 0 4,alignx right,growy");
		
		lblSizeValue = new JLabel("");
		contentPanel.add(lblSizeValue, "cell 1 4,alignx left,growy");
		
		JLabel lblStatus = new JLabel("Status: ");
		contentPanel.add(lblStatus, "cell 0 5,alignx right,growy");
		
		lblStatusValue = new JLabel("");
		contentPanel.add(lblStatusValue, "cell 1 5,alignx left,growy");
		
		JLabel lblTotalTime = new JLabel("Total time: ");
		contentPanel.add(lblTotalTime, "cell 0 6,alignx right,growy");
		
		lblTotalTimeValue = new JLabel("");
		contentPanel.add(lblTotalTimeValue, "cell 1 6,alignx left,growy");
		
		JLabel lblResultMessage = new JLabel("Result message: ");
		contentPanel.add(lblResultMessage, "cell 0 7,alignx left,growy");
		
		txtResult = new JTextPane();
		txtResult.setEditable(false);
		contentPanel.add(txtResult, "cell 0 8 2 1,grow");
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
			}
		});
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);
	}

}
