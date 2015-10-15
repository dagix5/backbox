package it.backbox.gui.utility;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.SwingUtilities;

import it.backbox.gui.LoadingDialog;

public abstract class ThreadActionListener implements ActionListener {

	@Override
	public void actionPerformed(final ActionEvent event) {
		GuiUtility.checkEDT(true);
		
		LoadingDialog.getInstance().showLoading();
		if (preaction(event)) {
			Thread worker = new Thread() {
				public void run() {
					GuiUtility.checkEDT(false);
					
					action(event);
					hideLoadingLater(event);
				}
			};
			worker.start();
		}
	}

	protected abstract boolean preaction(ActionEvent event);
	protected void postaction(ActionEvent event) {}
	protected abstract void action(ActionEvent event);
	
	protected void hideLoadingLater(final ActionEvent event) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				postaction(event);
				LoadingDialog.getInstance().hideLoading();
			}
		});
	}
}
