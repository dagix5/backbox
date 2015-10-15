package it.backbox.gui.model;

import it.backbox.gui.GuiConstant;
import it.backbox.gui.utility.GuiUtility;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class PreviewTableCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;
	
	@Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		GuiUtility.checkEDT(true);
		
		Component cr = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        cr.setForeground(Color.BLACK);
        if (value != null) {
	        if (column == PreviewTableModel.RESULT_COLUMN_INDEX)
	        	if (((String) value).equals(GuiConstant.RESULT_ERROR))
	        		cr.setForeground(Color.RED);
	            else if (((String) value).equals(GuiConstant.RESULT_SUCCESS))
	            	cr.setForeground(Color.GREEN);
        }
        if (isSelected)
        	cr.setForeground(Color.WHITE);

        return cr;
    }
}
