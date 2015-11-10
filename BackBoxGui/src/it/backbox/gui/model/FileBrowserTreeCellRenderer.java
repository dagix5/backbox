package it.backbox.gui.model;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.commons.io.FilenameUtils;

import it.backbox.gui.utility.FileUtility;
import it.backbox.gui.utility.GuiUtility;

public class FileBrowserTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = 1L;
	
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {
		GuiUtility.checkEDT(true);
		
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		Object obj = node.getUserObject();

		if (obj instanceof it.backbox.bean.File) {
			it.backbox.bean.File f = (it.backbox.bean.File) obj;
			String name = FilenameUtils.getName(f.getFilename());
			String ext = FilenameUtils.getExtension(f.getFilename());
			setText(name);
			setIcon(FileUtility.getIcon(ext));
		} else
			setIcon(FileUtility.getFolderIcon());
		
        return this;
	}
	
	

}
