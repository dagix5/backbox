package it.backbox.gui;

import it.backbox.gui.utility.ImagePanel;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;

public class LoadingDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	/**
	 * Create the dialog.
	 */
	public LoadingDialog() {
		addWindowFocusListener(new WindowFocusListener() {

			@Override
			public void windowLostFocus(WindowEvent arg0) {
				requestFocus();
			}
			
			@Override
			public void windowGainedFocus(WindowEvent arg0) {
				//nothing to do
			}
		});
		setUndecorated(true);
		setResizable(false);
		setBounds(100, 100, 200, 150);
		getContentPane().setLayout(new MigLayout("", "[300px]", "[98.00px,center][21.00px]"));
		
		JLabel label = new JLabel("Please wait...");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		getContentPane().add(label, "cell 0 1,growx,aligny top");
		
		Image image = this.getToolkit().createImage(getClass().getResource(GuiConstant.LOADING_IMG));
		ImagePanel imgPanel = new ImagePanel(image);
		imgPanel.setMaximumSize(new Dimension(66, 66));
		imgPanel.setMinimumSize(new Dimension(66, 66));
		imgPanel.setPreferredSize(new Dimension(66, 66));
		getContentPane().add(imgPanel, "cell 0 0,alignx center,aligny center");

	}

}
