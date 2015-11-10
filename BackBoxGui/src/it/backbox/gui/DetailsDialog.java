package it.backbox.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

import it.backbox.gui.utility.GuiUtility;
import it.backbox.transaction.Task;
import it.backbox.transaction.Transaction;
import net.miginfocom.swing.MigLayout;

public class DetailsDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private final JPanel contentPanel = new JPanel();
	private JLabel lblTransactionIdValue;
	private JLabel lblTaskIdValue;
	private JLabel lblFilenameValue;
	private JLabel lblOperationValue;
	private JLabel lblSizeValue;
	private JLabel lblStatusValue;
	private JLabel lblTotalTimeValue;
	private JTextPane txtResult;
	private JButton btnNext;
	private JButton btnPrev;
	
	private List<Transaction> transactions;
	private List<Task> tasks;
	private List<Integer> allIndexes;
	private int selectedIndex;

	public void load(List<Transaction> transactions, List<Task> tasks, int selectedIndex, List<Integer> allIndexes) {
		GuiUtility.checkEDT(true);
		
		this.transactions = transactions;
		this.tasks = tasks;
		this.allIndexes = allIndexes;
		this.selectedIndex = selectedIndex;
		
		updateDetails(selectedIndex);
	}
	
	private void updateDetails(int selectedIndex) {
		Transaction transaction = transactions.get(allIndexes.get(selectedIndex));
		Task task = tasks.get(allIndexes.get(selectedIndex));
		lblTransactionIdValue.setText(transaction.getId());
		lblTaskIdValue.setText(task.getId());
		String fn = task.getDescription();
		if (fn.length() > 53)
			fn = new StringBuilder(fn.substring(0, 24)).append("...").append(fn.substring(fn.length() - 25)).toString();
		lblFilenameValue.setText(fn);
		lblOperationValue.setText(GuiUtility.getTaskType(task));
		lblSizeValue.setText(GuiUtility.getTaskSize(task).getHsize());
		txtResult.setText("");
		if (transaction.getResultCode() == Transaction.Result.NO_RESULT)
			lblStatusValue.setText("Not executed");
		else if (transaction.getResultCode() == Transaction.Result.KO) {
			lblStatusValue.setText("Error");
			txtResult.setText(transaction.getResultDescription());
		} else if (transaction.getResultCode() == Transaction.Result.ROLLBACK) {
			lblStatusValue.setText("Rollback");
			txtResult.setText(transaction.getResultDescription());
		} else if (transaction.getResultCode() == Transaction.Result.OK)
			lblStatusValue.setText("Success");
		lblTotalTimeValue.setText(GuiUtility.getTimeString(task.getTotalTime() / 1000));
		
		if (selectedIndex == 0)
			btnPrev.setEnabled(false);
		if (selectedIndex == allIndexes.size() - 1)
			btnNext.setEnabled(false);
	}
	
	private void prev() {
		btnNext.setEnabled(true);
		if (selectedIndex > 0)
			updateDetails(--selectedIndex);
	}
	
	private void next() {
		btnPrev.setEnabled(true);
		if (selectedIndex < allIndexes.size() - 1)
			updateDetails(++selectedIndex);
	}
	
	/**
	 * Create the dialog.
	 */
	public DetailsDialog(JFrame parent) {
		super(parent);
		
		GuiUtility.checkEDT(true);
		
		setResizable(false);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle("Details");
		setBounds(100, 100, 450, 302);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new MigLayout("", "[125.00:125.00][:322.00:322.00]", "[][][][][][][][][grow]"));
		
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
		
		JScrollPane scrollPane = new JScrollPane();
		contentPanel.add(scrollPane, "cell 0 8 2 1,grow");
		
		txtResult = new JTextPane();
		scrollPane.setViewportView(txtResult);
		txtResult.setEditable(false);
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
			}
		});
		
		btnNext = new JButton("Next");
		btnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				next();
			}
		});
		
		btnPrev = new JButton("Prev");
		btnPrev.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				prev();
			}
		});
		buttonPane.add(btnPrev);
		buttonPane.add(btnNext);
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);
	}

}
