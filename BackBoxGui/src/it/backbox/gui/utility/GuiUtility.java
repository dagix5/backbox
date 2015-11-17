package it.backbox.gui.utility;

import it.backbox.gui.BackBoxGui;
import it.backbox.gui.bean.Size;
import it.backbox.transaction.CopyTask;
import it.backbox.transaction.DeleteBoxTask;
import it.backbox.transaction.DeleteDBTask;
import it.backbox.transaction.DeleteRemoteTask;
import it.backbox.transaction.DeleteTask;
import it.backbox.transaction.DownloadTask;
import it.backbox.transaction.InsertTask;
import it.backbox.transaction.Task;
import it.backbox.transaction.UploadTask;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

public class GuiUtility {
	
	private static final Logger _log = Logger.getLogger(BackBoxGui.class.getCanonicalName());
	
	public static void handleException(Component parent, String message, Exception e) {
		_log.log(Level.SEVERE, message, e);
		JOptionPane.showMessageDialog(parent, message + ": " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	private static int[] getETA(long bytes, long speed) {
		long s = bytes / speed;
	    return getTimeArray(s);
	}
	
	public static String getETAString(long bytes, long speed) {
		int[] t = getETA(bytes, speed);
	    StringBuilder eta = new StringBuilder();
	    if (t[0] > 0) {
	    	eta.append(t[0]);
	    	eta.append("h ");
	    }
	    if ((t[1] > 0) || (t[0] > 0)) {
	    	eta.append(t[1]);
	    	eta.append("m ");
	    }
    	eta.append((t[2] >= 0) ? t[2] : 0);
    	eta.append("s");
	    return eta.toString();
	}
	
	public static String getTimeString(long time) {
		int[] t = getTimeArray(time);
		StringBuilder eta = new StringBuilder();
	    if (t[0] > 0) {
	    	eta.append(t[0]);
	    	eta.append("h ");
	    }
	    if ((t[1] > 0) || (t[0] > 0)) {
	    	eta.append(t[1]);
	    	eta.append("m ");
	    }
    	eta.append((t[2] >= 0) ? t[2] : 0);
    	eta.append("s");
	    return eta.toString();
	}
	
	private static int[] getTimeArray(long time) {
		int hours = (int) time / 3600;
		int remainder = (int) time - hours * 3600;
		int mins = remainder / 60;
	    remainder = remainder - mins * 60;
	    int secs = remainder;
	    return new int[] {hours, mins, secs};
	}

	public static String getTaskType(Task t) {
		if (t instanceof CopyTask)
			return "Local copy";
		if (t instanceof DeleteBoxTask)
			return "Delete";
		if (t instanceof DeleteRemoteTask)
			return "Remote delete";
		if (t instanceof DeleteDBTask)
			return "DB delete";
		if (t instanceof DeleteTask)
			return "Local delete";
		if (t instanceof DownloadTask)
			return "Download";
		if (t instanceof InsertTask)
			return "DB insert";
		if (t instanceof UploadTask)
			return "Upload";
		return "";
	}
	
	public static Size getTaskSize(Task t) {
		if (t instanceof DownloadTask) {
			DownloadTask dt = (DownloadTask) t;
			if (dt.getSize() != null)
				return new Size(dt.getSize());
		}
		if (t instanceof UploadTask) {
			UploadTask ut = (UploadTask) t;
			if (ut.getSize() != null)
				return new Size(ut.getSize());
		}
		return new Size(-1);
	}
	
	public static void addPopup(Component component, final JPopupMenu popup) {
		GuiUtility.checkEDT(true);
		
		final JTable table = (JTable) component;
		
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				int r = table.rowAtPoint(e.getPoint());
	            if (r >= 0 && r < table.getRowCount()) {
	            	table.addRowSelectionInterval(r, r);
	            } else {
	            	table.clearSelection();
	            }

	            int[] rowindex = table.getSelectedRows();
	            if (rowindex.length == 0)
	                return;
	            if (e.isPopupTrigger() && e.getComponent() instanceof JTable ) {
	            	popup.show(e.getComponent(), e.getX(), e.getY());
	            }
			}
		});
	}
	
	public static void checkEDT(boolean shouldbe) {
		boolean isEDT = SwingUtilities.isEventDispatchThread();
		assert(!isEDT ^ shouldbe);
	}
}
