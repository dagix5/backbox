package it.backbox.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

import it.backbox.gui.model.PreviewTableModel;
import it.backbox.gui.utility.GuiUtility;
import it.backbox.transaction.Task;
import it.backbox.transaction.Transaction;
import net.miginfocom.swing.MigLayout;

public class DetailsDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private final JPanel contentPanel = new JPanel();
	private JTextArea txtTransactionId;
	private JTextArea txtTaskId;
	private JTextArea txtFilename;
	private JTextArea txtOperation;
	private JTextArea txtSize;
	private JTextArea txtStatus;
	private JTextArea txtTotalTime;
	private JTextPane txtResult;
	private JButton btnNext;
	private JButton btnPrev;
	private JTable tablePreview;
	
	private List<Transaction> transactions;
	private List<Task> tasks;
	private List<Integer> allIndexes;
	private int selectedIndex;

	public void load(JTable tablePreview) {
		GuiUtility.checkEDT(true);
		
		this.tablePreview = tablePreview;
		
		allIndexes = new ArrayList<>();
		selectedIndex = 0;
		for (int i = 0; i < tablePreview.getRowCount(); i++) {
			int c = tablePreview.convertRowIndexToModel(i);
			allIndexes.add(c);
			if (i == tablePreview.getSelectedRow())
				selectedIndex = i;
		}
		PreviewTableModel model = (PreviewTableModel) tablePreview.getModel();
		Vector<Vector<Object>> vv = model.getDataVector();
		transactions = new ArrayList<>();
		tasks = new ArrayList<>();
		Iterator<Vector<Object>> i = vv.iterator();
		while (i.hasNext()) {
			Vector<Object> v = i.next();
			transactions.add((Transaction) v.elementAt(PreviewTableModel.TRANSACTION_COLUMN_INDEX));
			tasks.add((Task) v.elementAt(PreviewTableModel.TASK_COLUMN_INDEX));
		}
		
		updateDetails(selectedIndex);
	}
	
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
		txtTransactionId.setText(transaction.getId());
		txtTaskId.setText(task.getId());
		String fn = task.getDescription();
		if (fn.length() > 53)
			fn = new StringBuilder(fn.substring(0, 24)).append("...").append(fn.substring(fn.length() - 25)).toString();
		txtFilename.setText(fn);
		txtOperation.setText(GuiUtility.getTaskType(task));
		txtSize.setText(GuiUtility.getTaskSize(task).getHsize());
		txtResult.setText("");
		if (transaction.getResultCode() == Transaction.Result.NO_RESULT)
			txtStatus.setText("Not executed");
		else if (transaction.getResultCode() == Transaction.Result.KO) {
			txtStatus.setText("Error");
			txtResult.setText(transaction.getResultDescription());
		} else if (transaction.getResultCode() == Transaction.Result.ROLLBACK) {
			txtStatus.setText("Rollback");
			txtResult.setText(transaction.getResultDescription());
		} else if (transaction.getResultCode() == Transaction.Result.OK)
			txtStatus.setText("Success");
		txtTotalTime.setText(GuiUtility.getTimeString(task.getTotalTime() / 1000));
		
		if (selectedIndex == 0)
			btnPrev.setEnabled(false);
		if (selectedIndex == allIndexes.size() - 1)
			btnNext.setEnabled(false);
	}
	
	private void prev() {
		btnNext.setEnabled(true);
		if (selectedIndex > 0) {
			int selectedIndexView = tablePreview.convertColumnIndexToView(--selectedIndex);
			updateDetails(selectedIndex);
			tablePreview.setRowSelectionInterval(selectedIndexView, selectedIndexView);
		}
	}
	
	private void next() {
		btnPrev.setEnabled(true);
		if (selectedIndex < allIndexes.size() - 1) {
			int selectedIndexView = tablePreview.convertColumnIndexToView(++selectedIndex);
			updateDetails(selectedIndex);
			tablePreview.setRowSelectionInterval(selectedIndexView, selectedIndexView);
		}
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
		
		txtTransactionId = new JTextArea("");
		txtTransactionId.setBackground(lblTransactionId.getBackground());
		txtTransactionId.setFont(lblTransactionId.getFont());
		txtTransactionId.setEditable(false);
		contentPanel.add(txtTransactionId, "cell 1 0,alignx left,growy");
		
		JLabel lblTaskId = new JLabel("Task ID: ");
		contentPanel.add(lblTaskId, "cell 0 1,alignx right,growy");
		
		txtTaskId = new JTextArea("");
		txtTaskId.setBackground(lblTaskId.getBackground());
		txtTaskId.setFont(lblTaskId.getFont());
		txtTaskId.setEditable(false);
		contentPanel.add(txtTaskId, "cell 1 1,alignx left,growy");
		
		JLabel lblFilename = new JLabel("Filename: ");
		contentPanel.add(lblFilename, "cell 0 2,alignx right,growy");
		
		txtFilename = new JTextArea("");
		txtFilename.setBackground(lblFilename.getBackground());
		txtFilename.setFont(lblFilename.getFont());
		txtFilename.setEditable(false);
		contentPanel.add(txtFilename, "cell 1 2,alignx left,growy");
		
		JLabel lblOperation = new JLabel("Operation: ");
		contentPanel.add(lblOperation, "cell 0 3,alignx right,growy");
		
		txtOperation = new JTextArea("");
		txtOperation.setBackground(lblOperation.getBackground());
		txtOperation.setFont(lblOperation.getFont());
		txtOperation.setEditable(false);
		contentPanel.add(txtOperation, "cell 1 3,alignx left,growy");
		
		JLabel lblSize = new JLabel("Size: ");
		contentPanel.add(lblSize, "cell 0 4,alignx right,growy");
		
		txtSize = new JTextArea("");
		txtSize.setBackground(lblSize.getBackground());
		txtSize.setFont(lblSize.getFont());
		txtSize.setEditable(false);
		contentPanel.add(txtSize, "cell 1 4,alignx left,growy");
		
		JLabel lblStatus = new JLabel("Status: ");
		contentPanel.add(lblStatus, "cell 0 5,alignx right,growy");
		
		txtStatus = new JTextArea("");
		txtStatus.setBackground(lblStatus.getBackground());
		txtStatus.setFont(lblStatus.getFont());
		txtStatus.setEditable(false);
		contentPanel.add(txtStatus, "cell 1 5,alignx left,growy");
		
		JLabel lblTotalTime = new JLabel("Total time: ");
		contentPanel.add(lblTotalTime, "cell 0 6,alignx right,growy");
		
		txtTotalTime = new JTextArea("");
		txtTotalTime.setBackground(lblTotalTime.getBackground());
		txtTotalTime.setFont(lblTotalTime.getFont());
		txtTotalTime.setEditable(false);
		contentPanel.add(txtTotalTime, "cell 1 6,alignx left,growy");
		
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
