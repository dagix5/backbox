package it.backbox.gui.model;

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import it.backbox.gui.model.FileBrowserTreeNode.TreeNodeType;
import it.backbox.gui.utility.BackBoxHelper;
import it.backbox.gui.utility.GuiUtility;

public class FileBrowserTreeSelectionListener implements TreeSelectionListener {

	private JFrame frame;
	private BackBoxHelper helper;
	private JTable table;

	public FileBrowserTreeSelectionListener(JFrame frame, BackBoxHelper helper, JTable table) {
		this.frame = frame;
		this.helper = helper;
		this.table = table;
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		JTree tree = (JTree) e.getSource();

		try {
			FileBrowserTreeNode node = (FileBrowserTreeNode) e.getPath().getLastPathComponent();
			if (node == null)
				return;

			FileBrowserTreeNode parent = (FileBrowserTreeNode) node.getParent();
			if (parent == null)
				return;
			
			tree.setEnabled(false);
			
			// update structure
			if (parent.isRoot() && node.isLeaf()) {
				String alias = (String) node.getUserObject();
				List<it.backbox.bean.File> list = helper.dbm.getFilesInFolder(alias);
				FileBrowserTreeModel model = (FileBrowserTreeModel) tree.getModel();
				node.removeAllChildren();
				model.reload(node);

				for (it.backbox.bean.File f : list) {
					// in DB files have always windows file separators
					String[] splitted = f.getFilename().split("\\\\");
					FileBrowserTreeNode cnode = node;
					int i = 0;
					for (; i < splitted.length - 1; i++) {
						Enumeration<FileBrowserTreeNode> cc = cnode.children();
						boolean found = false;
						while (cc.hasMoreElements()) {
							FileBrowserTreeNode n = cc.nextElement();
							Object o = n.getUserObject();
							if (o instanceof String) {
								String u = (String) o;
								if (u.equals(splitted[i])) {
									cnode = n;
									found = true;
									break;
								}
							}

						}
						if (!found) {
							FileBrowserTreeNode nnode = new FileBrowserTreeNode(splitted[i], TreeNodeType.FOLDER);
							model.insertNodeInto(nnode, cnode);
							cnode = nnode;
						}
					}
					FileBrowserTreeNode nnode = new FileBrowserTreeNode(f, TreeNodeType.FILE);
					model.insertNodeInto(nnode, cnode);
				}
			}

			updateTable((FileBrowserTableModel) table.getModel(), node);

		} catch (SQLException ex) {
			GuiUtility.handleException(frame, "Error updating table", ex);
		}

		tree.setEnabled(true);
	}

	private static void updateTable(final FileBrowserTableModel model, final FileBrowserTreeNode node) {
		GuiUtility.checkEDT(true);
		
		SwingWorker<Void, FileBrowserTreeNode> worker = new SwingWorker<Void, FileBrowserTreeNode>() {

			@Override
			protected Void doInBackground() throws Exception {
				while (model.getRowCount() > 0)
					model.removeRow(0);

				if (!((FileBrowserTreeNode) node.getParent()).isRoot()) {
					FileBrowserTreeNode fp = new FileBrowserTreeNode("..", TreeNodeType.PREV_FOLDER);
					fp.setParent((FileBrowserTreeNode) node.getParent());
					publish(fp);
				}
				
				Enumeration<FileBrowserTreeNode> cc = node.children();
				while (cc.hasMoreElements()) 
					publish(cc.nextElement());

				return null;
			}

			@Override
			protected void process(List<FileBrowserTreeNode> chunks) {
				for (FileBrowserTreeNode u : chunks)
					model.addRow(u);
			}
		};

		worker.execute();

	}
}
