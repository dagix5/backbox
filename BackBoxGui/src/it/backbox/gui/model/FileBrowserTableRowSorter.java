package it.backbox.gui.model;

import java.text.Collator;
import java.util.Comparator;

import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import it.backbox.gui.bean.Filename;
import it.backbox.gui.bean.Size;

public class FileBrowserTableRowSorter extends TableRowSorter<TableModel> {

	private TableModel model;
	private static Comparator<Size> sizeComparator = new Comparator<Size>() {

		@Override
		public int compare(Size arg0, Size arg1) {
			return Long.compare(arg0.getSize(), arg1.getSize());
		}
	};
	private static Comparator<Filename> nameComparator = new Comparator<Filename>() {

		@Override
		public int compare(Filename o1, Filename o2) {
			int c = Integer.compare(o1.getType(), o2.getType());
			if (c == 0)
				return Collator.getInstance().compare(o1.getName(), o2.getName());
			return c;
		}

	};

	public FileBrowserTableRowSorter(TableModel model) {
		super(model);

		this.model = model;
	}

	@Override
	public Comparator<?> getComparator(int column) {
		if (model.getColumnClass(column) == Size.class)
			return sizeComparator;
		if (model.getColumnClass(column) == Filename.class)
			return nameComparator;
		return super.getComparator(column);
	}

	@Override
	protected boolean useToString(int column) {
		if ((model.getColumnClass(column) == Size.class) || (model.getColumnClass(column) == Filename.class))
			return false;
		return super.useToString(column);
	}

}
