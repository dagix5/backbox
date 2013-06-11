package it.backbox.gui.utility;

import it.backbox.gui.GuiConstant;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class ColorTableCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;
	
	@Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component cr = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if ((value != null) && (column == GuiConstant.RESULT_COLUM_INDEX))
        	if (((String) value).equals(GuiConstant.RESULT_ERROR))
        		cr.setForeground(Color.RED);
            else if (((String) value).equals(GuiConstant.RESULT_SUCCESS))
            	cr.setForeground(Color.GREEN);
        else
        	if (isSelected)
        		cr.setForeground(Color.WHITE);
        	else
        		cr.setForeground(Color.BLACK);

        return cr;
    }
}
