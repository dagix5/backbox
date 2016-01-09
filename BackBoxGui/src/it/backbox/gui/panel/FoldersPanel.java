package it.backbox.gui.panel;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import it.backbox.bean.Folder;
import it.backbox.gui.utility.GuiUtility;
import net.miginfocom.swing.MigLayout;

public class FoldersPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private DefaultListModel<Folder> listModel;
	
	public FoldersPanel(final Container container) {
		GuiUtility.checkEDT(true);
		
		setLayout(new MigLayout("", "[279.00px,grow][90.00:90.00:90.00][90.00:90.00:90.00]", "[grow][]"));
		
		listModel = new DefaultListModel<Folder>();
		
		final JButton btnRemove = new JButton("Remove");
		btnRemove.setEnabled(false);
		
		final JButton btnEdit = new JButton("Edit");
		btnEdit.setEnabled(false);
		
		final JList<Folder> list = new JList<Folder>(listModel);
		list.setVisibleRowCount(5);
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					btnRemove.setEnabled((list.getSelectedIndex() != -1) && (listModel.getSize() > 1));
					btnEdit.setEnabled((list.getSelectedIndex() != -1));
				}
			}
		});
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setCellRenderer(new DefaultListCellRenderer() {

			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				
				if (value instanceof Folder)
					setText(((Folder) value).getPath());
				
				return this;
			}

		});
		
		add(new JScrollPane(list), "cell 0 0 3 1,grow");
		
		btnEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int index = list.getSelectedIndex();
				Folder f = listModel.get(index);
				
				String folder = null;
				
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(container);
				if (returnVal == JFileChooser.CANCEL_OPTION)
					return;
				try {
					folder = fc.getSelectedFile().getCanonicalPath();
				} catch (IOException e) {
					GuiUtility.handleException(container, "Error getting canonical path", e);
					return;
				}
				if ((folder == null) || folder.isEmpty() || !Files.exists(Paths.get(folder))) {
					JOptionPane.showMessageDialog(container, "Folder not found", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				if (listModel.contains(folder)) {
					JOptionPane.showMessageDialog(container, "Folder already in list", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				f.setPath(folder);
				listModel.set(index, f);
				
				list.setSelectedIndex(index);
	            list.ensureIndexIsVisible(index);
			}
		});
		
		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int index = list.getSelectedIndex();
				listModel.remove(index);
				
				int size = listModel.getSize();

				if (size <= 1)
					btnRemove.setEnabled(false);
				else {
					if (index == listModel.getSize())
						index--;

					list.setSelectedIndex(index);
					list.ensureIndexIsVisible(index);
				}
			}
		});
		
		JButton btnAdd = new JButton("Add...");
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String folder = null;
				
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(container);
				if (returnVal == JFileChooser.CANCEL_OPTION)
					return;
				try {
					folder = fc.getSelectedFile().getCanonicalPath();
				} catch (IOException e) {
					GuiUtility.handleException(container, "Error getting canonical path", e);
					return;
				}
				if ((folder == null) || folder.isEmpty() || !Files.exists(Paths.get(folder))) {
					JOptionPane.showMessageDialog(container, "Folder not found", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				if (listModel.contains(folder)) {
					JOptionPane.showMessageDialog(container, "Folder already in list", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				String alias = null;
				do {
					alias = JOptionPane.showInputDialog(container, "Set an alias for folder: " + folder, "Folders to backup", JOptionPane.QUESTION_MESSAGE);
					if (checkAlias(alias))
						JOptionPane.showMessageDialog(container, "Alias duplicated", "Error", JOptionPane.ERROR_MESSAGE);
				} while (checkAlias(alias) || alias.isEmpty());
				
				int index = listModel.getSize();
				listModel.insertElementAt(new Folder(folder, null, alias), index);
				
				list.setSelectedIndex(index);
	            list.ensureIndexIsVisible(index);
			}
		});
		add(btnAdd, "cell 1 1,grow");
		add(btnEdit, "cell 2 1,grow");
		add(btnRemove, "cell 3 1,grow");
	}
	
	private boolean checkAlias(String alias) {
		for (int i = 0; i < listModel.size(); i++)
			if (listModel.get(i).getAlias().equals(alias))
				return true;
		return false;
	}
	
	public List<Folder> getFolders() {
		return Collections.list(listModel.elements());
	}
	
	public void load(List<Folder> backupFolders) {
		listModel.clear();
		
		for (Folder f : backupFolders)
			listModel.addElement(f);//.getPath());
	}

}
