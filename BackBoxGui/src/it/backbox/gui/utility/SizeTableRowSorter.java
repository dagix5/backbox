package it.backbox.gui.utility;

import it.backbox.gui.bean.Size;

import java.util.Comparator;

import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class SizeTableRowSorter extends TableRowSorter<TableModel> {
	
	private static final int SIZE_COLUM_INDEX = 2;
	
	private Comparator<Size> sizeComparator;
	
	public SizeTableRowSorter(TableModel model) {
		super(model);
		this.sizeComparator =  new Comparator<Size>() {
			
			@Override
			public int compare(Size arg0, Size arg1) {
				return Long.compare(arg0.getSize(), arg1.getSize());
			}
		};
	}

	@Override
	public Comparator<?> getComparator(int column) {
		if (column == SIZE_COLUM_INDEX)
			return sizeComparator;
		return super.getComparator(column);
	}

	@Override
	protected boolean useToString(int column) {
		if (column == SIZE_COLUM_INDEX)
			return false;
		return super.useToString(column);
	}

}
