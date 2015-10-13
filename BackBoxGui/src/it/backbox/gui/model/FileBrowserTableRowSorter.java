package it.backbox.gui.model;

import java.text.Collator;
import java.util.Comparator;

import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import it.backbox.gui.bean.Filename;
import it.backbox.gui.bean.Size;

public class FileBrowserTableRowSorter extends TableRowSorter<TableModel> {
	
	private Comparator<Size> sizeComparator;
	private Comparator<Filename> nameComparator;
	
	public FileBrowserTableRowSorter(TableModel model) {
		super(model);
		this.sizeComparator =  new Comparator<Size>() {
			
			@Override
			public int compare(Size arg0, Size arg1) {
				return Long.compare(arg0.getSize(), arg1.getSize());
			}
		};
		this.nameComparator = new Comparator<Filename>() {

			@Override
			public int compare(Filename o1, Filename o2) {
				int c = Integer.compare(o1.getType(), o2.getType());
				if (c == 0)
					return Collator.getInstance().compare(o1.getName(), o2.getName());
				return c;
			}
			
		};
	}

	@Override
	public Comparator<?> getComparator(int column) {
		if (column == FileBrowserTableModel.SIZE_COLUMN_INDEX)
			return sizeComparator;
		if (column == FileBrowserTableModel.NAME_COLUMN_INDEX)
			return nameComparator;
		return super.getComparator(column);
	}

	@Override
	protected boolean useToString(int column) {
		if (column == FileBrowserTableModel.SIZE_COLUMN_INDEX
				|| column == FileBrowserTableModel.NAME_COLUMN_INDEX)
			return false;
		return super.useToString(column);
	}

}
