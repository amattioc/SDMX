package it.bancaditalia.oss.sdmx.helper;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultEditorKit;

import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;

class ResultsFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	public ResultsFrame(List<PortableTimeSeries<Double>> result) throws HeadlessException {
		setSize(800, 600);
		JScrollPane tsPane = new JScrollPane();
		int nRows = result.size();
		if (nRows > 0) {

			Vector<String> columnNames = new Vector<>();
			columnNames.add("Time Series");
			PortableTimeSeries<?> ts = result.get(0);
			for (String dim : ts.getDimensionsMap().keySet())
				columnNames.add(dim);

			Vector<Vector<String>> table = new Vector<>();

			for (PortableTimeSeries<?> series : result) {
				Vector<String> row = new Vector<>();
				row.add(series.getName());
				row.addAll(series.getDimensionsMap().values());
				table.add(row);
			}
			final JTable tsTable = new JTable(table, columnNames) {
				private static final long serialVersionUID = 1L;

				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
				}
			};
			tsTable.setAutoCreateRowSorter(true);
			tsTable.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (SwingUtilities.isRightMouseButton(e)) {
						final int row = tsTable.rowAtPoint(e.getPoint());
						final int column = tsTable.columnAtPoint(e.getPoint());
						if (tsTable.convertColumnIndexToModel(column) == 0)
						{
							JPopupMenu popupMenu = new JPopupMenu();
							JMenuItem mntmCopy = new JMenuItem(new DefaultEditorKit.CopyAction());
							mntmCopy.setText("Copy series name");
							mntmCopy.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
											new StringSelection(tsTable.getValueAt(row, column).toString()), null);
								}
							});
							popupMenu.add(mntmCopy);
							popupMenu.show(tsTable, e.getX(), e.getY());
						}
					}
				}
			});
			tsPane.setViewportView(tsTable);
			getContentPane().add(tsPane);
		}
	}
}