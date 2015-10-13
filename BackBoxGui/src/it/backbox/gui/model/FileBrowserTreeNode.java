package it.backbox.gui.model;

import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;

public class FileBrowserTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 1L;
	
	private TreeNodeType type;
	
	public FileBrowserTreeNode(TreeNodeType type) {
		super();
		this.type = type;
	}

	public FileBrowserTreeNode(Object userObject, boolean allowsChildren, TreeNodeType type) {
		super(userObject, allowsChildren);
		this.type = type;
	}

	public FileBrowserTreeNode(Object userObject, TreeNodeType type) {
		super(userObject);
		this.type = type;
	}
	
	public TreeNodeType getType() {
		return type;
	}

	@Override
	public int getChildCount() {
		if (children == null)
			return 0;
		
		int c = 0;
		@SuppressWarnings("rawtypes")
		Enumeration e = children.elements();
	    while (e.hasMoreElements()) {
	    	FileBrowserTreeNode n = (FileBrowserTreeNode) e.nextElement();
	    	if (n.getType() == TreeNodeType.FOLDER)
	    		c++;
	    }
	    return c;
	}

	public static enum TreeNodeType { FILE, FOLDER, PREV_FOLDER};
}
