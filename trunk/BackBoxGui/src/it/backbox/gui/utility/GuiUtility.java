package it.backbox.gui.utility;

import java.awt.Component;

import javax.swing.JOptionPane;

public class GuiUtility {
	
	public static void handleException(Component parent, String message, Exception e) {
		e.printStackTrace();
		JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	public static int[] getETA(long bytes, long speed) {
		long s = bytes / speed;
		int hours = (int) s / 3600;
		int remainder = (int) s - hours * 3600;
		int mins = remainder / 60;
	    remainder = remainder - mins * 60;
	    int secs = remainder;
	    return new int[] {hours, mins, secs};
	}
	
	public static String getETAString(long bytes, long speed) {
		int[] t = GuiUtility.getETA(bytes, speed);
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

}
