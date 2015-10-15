package it.backbox.gui.model;

import java.text.Collator;
import java.util.Enumeration;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.apache.commons.io.FilenameUtils;

import it.backbox.bean.File;
import it.backbox.gui.utility.GuiUtility;

public class FileBrowserTreeModel extends DefaultTreeModel {

	private static final long serialVersionUID = 1L;

	public FileBrowserTreeModel(TreeNode root) {
		super(root);
	}

	public void insertNodeInto(FileBrowserTreeNode newChild, FileBrowserTreeNode parent) {
		GuiUtility.checkEDT(true);
		
		int i = 0;
		Enumeration e = parent.children();
		while (e.hasMoreElements()) {
			Object objn = newChild.getUserObject();
			Object objc = ((FileBrowserTreeNode) e.nextElement()).getUserObject();
			if (objn instanceof String) {
				String n = (String) objn;
				if (objc instanceof String) {
					String c = (String) objc;
					if (Collator.getInstance().compare(n, c) < 0) {
						insertNodeInto(newChild, parent, i);
						return;
					}
				} else {
					insertNodeInto(newChild, parent, i);
					return;
				}
			} else {
				String n = FilenameUtils.getName(((File) objn).getFilename());
				if (objc instanceof File) {
					String c = FilenameUtils.getName(((File) objc).getFilename());
					if (Collator.getInstance().compare(n, c) < 0) {
						insertNodeInto(newChild, parent, i);
						return;
					}
				}
			}
			i++;
		}
		insertNodeInto(newChild, parent, i);
	}
	
}
