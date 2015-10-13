package it.backbox.gui.model;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.commons.io.FilenameUtils;

import it.backbox.bean.File;
import it.backbox.gui.utility.FileUtility;

public class FileBrowserTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = 1L;
	
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {
		
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		Object obj = node.getUserObject();

		if (obj instanceof File) {
			File f = (File) obj;
			String name = FilenameUtils.getName(f.getFilename());
			String ext = FilenameUtils.getExtension(f.getFilename());
			setText(name);
			setIcon(FileUtility.getIcon(ext));
		} else
			setIcon(FileUtility.getFolderIcon());
		
        return this;
	}
	
	

}
