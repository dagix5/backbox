package it.backbox.gui.panel;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

import it.backbox.gui.utility.GuiUtility;

public class ImagePanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private final Image image;

    public ImagePanel(Image image) {
        super();
        this.image = image;
    }

    @Override
    protected void paintComponent(Graphics g) {
    	GuiUtility.checkEDT(true);
    	
        super.paintComponent(g);
        g.drawImage(this.image, 0, 0, getWidth(), getHeight(), this);
    }
}
