package it.backbox.gui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import it.backbox.gui.panel.ImagePanel;
import it.backbox.gui.utility.GuiUtility;
import net.miginfocom.swing.MigLayout;

public class LoadingDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private static LoadingDialog loadingDialog;
	
	private Frame owner;
	
	public static LoadingDialog getInstance() {
		return loadingDialog;
	}
	
	LoadingDialog(Frame owner) {
		super(owner, false);
		
		GuiUtility.checkEDT(true);
		
		this.owner = owner;
		setUndecorated(true);
		setResizable(false);
		setBounds(100, 100, 200, 150);
		getContentPane().setLayout(new MigLayout("", "[300px]", "[98.00px,center][21.00px]"));
		
		JLabel label = new JLabel("Please wait...");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		getContentPane().add(label, "cell 0 1,growx,aligny top");
		
		Image image = this.getToolkit().createImage(Thread.currentThread().getContextClassLoader().getResource("loader.gif"));
		ImagePanel imgPanel = new ImagePanel(image);
		imgPanel.setMaximumSize(new Dimension(66, 66));
		imgPanel.setMinimumSize(new Dimension(66, 66));
		getContentPane().add(imgPanel, "cell 0 0,alignx center,aligny center");
		
		loadingDialog = this;
	}
	
	public void showLoading() {
		GuiUtility.checkEDT(true);
		
		owner.setEnabled(false);
		loadingDialog.setLocationRelativeTo(owner);
		loadingDialog.setVisible(true);
	}
	
	public void hideLoading() {
		GuiUtility.checkEDT(true);
		
		loadingDialog.setVisible(false);
		owner.setEnabled(true);
		owner.toFront();
	}
}
