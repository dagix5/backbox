package it.backbox.gui.model;

import javax.swing.Icon;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.io.FilenameUtils;

import it.backbox.bean.File;
import it.backbox.gui.bean.Size;
import it.backbox.gui.model.FileBrowserTreeNode.TreeNodeType;
import it.backbox.gui.bean.Filename;
import it.backbox.gui.utility.FileUtility;

public class FileBrowserTableModel extends DefaultTableModel {

	private static final long serialVersionUID = 1L;

	public static final int NAME_COLUMN_INDEX = 1;
	public static final int SIZE_COLUMN_INDEX = 2;
	public static final int ID_COLUMN_INDEX = 5;
	public static final int NODE_COLUMN_INDEX = 6;

	private Class[] columnTypes = new Class[] { Icon.class, Filename.class, Size.class, String.class, String.class,
			String.class, DefaultMutableTreeNode.class };

	public FileBrowserTableModel() {
		super(new Object[][] {}, new String[] { "", "Filename", "Size", "Last Modified", "Type", "ID", "Node" });
	}

	public Class getColumnClass(int columnIndex) {
		return columnTypes[columnIndex];
	}

	public boolean isCellEditable(int row, int column) {
		return false;
	}

	public String getId(int row) {
		return (String) getValueAt(row, ID_COLUMN_INDEX);
	}

	public Filename getName(int row) {
		return (Filename) getValueAt(row, NAME_COLUMN_INDEX);
	}

	public Size getSize(int row) {
		return (Size) getValueAt(row, SIZE_COLUMN_INDEX);
	}

	public FileBrowserTreeNode getNode(int row) {
		return (FileBrowserTreeNode) getValueAt(row, NODE_COLUMN_INDEX);
	}

	public void addRow(FileBrowserTreeNode node) {
		Object o = node.getUserObject();
		if ((node.getType() == TreeNodeType.FOLDER) || (node.getType() == TreeNodeType.PREV_FOLDER))
			addRow(new Object[] { FileUtility.getFolderIcon(), new Filename((String) o, Filename.DIRECTORY_TYPE), null,
					null, "", "", node });
		else if (node.getType() == TreeNodeType.FILE) {
			File f = (File) o;
			String ext = FilenameUtils.getExtension(f.getFilename());
			addRow(new Object[] { FileUtility.getIcon(ext),
					new Filename(FilenameUtils.getName(f.getFilename()), Filename.FILE_TYPE), new Size(f.getSize()),
					f.getTimestamp().toString(), FileUtility.getType(ext), f.getHash(), node });
		}
	}

}
