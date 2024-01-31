package it.bancaditalia.oss.sdmx.helper;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultEditorKit;

import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.util.Configuration;

class ResultsFrame extends JFrame 
{
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Configuration.getSdmxLogger();

	private final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

	public ResultsFrame(final String provider, String dataflow, final List<PortableTimeSeries<Double>> result) throws HeadlessException 
	{
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(800, 600);
		setLocationRelativeTo(null);
		JScrollPane tsPane = new JScrollPane();
		int nRows = result.size();
		if (nRows == 0)
			return;

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
			public void mouseClicked(MouseEvent event)
			{
				int row = tsTable.rowAtPoint(event.getPoint());
				int column = tsTable.columnAtPoint(event.getPoint());
				if (tsTable.convertColumnIndexToModel(column) == 0)
					try
					{
						if (SwingUtilities.isRightMouseButton(event)) 
						{
							JPopupMenu popupMenu = createPopupMenu(provider, dataflow, tsTable, row);
							popupMenu.show(tsTable, event.getX(), event.getY());
						} 
						else if (column == 0 && SwingUtilities.isLeftMouseButton(event) && event.getClickCount() >= 2)
							new SeriesViewer(provider, tsTable.getValueAt(row, 0).toString()).setVisible(true);
					}
					catch (SdmxException e)
					{
						LOGGER.severe("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
						LOGGER.log(Level.FINER, "", e);
					}
			}

		});
		
		tsPane.setViewportView(tsTable);
		getContentPane().add(tsPane);
	}
	
	private String[] getSelectedSeries(JTable tsTable, int defRow)
	{
		int[] rows = tsTable.getSelectedRows();
		if (rows.length == 0)
			rows = new int[] { defRow };
		String[] result = new String[rows.length];
		for (int i = 0; i < rows.length; i++)
			result[i] = tsTable.getValueAt(rows[i], tsTable.convertColumnIndexToView(0)).toString();
		return result;
	}

	private boolean isSameFreqSelectedSeries(JTable tsTable, int defRow)
	{
		int[] rows = tsTable.getSelectedRows();
		if (rows.length == 0)
			rows = new int[] { defRow };
		Object freq = tsTable.getValueAt(rows[0], tsTable.convertColumnIndexToView(1));
		for (int i = 1; i < rows.length; i++)
			if (!freq.equals(tsTable.getValueAt(rows[i], tsTable.convertColumnIndexToView(1))))
				return false;
		
		return true;
	}


	private JPopupMenu createPopupMenu(final String provider, String dataflow, final JTable tsTable, int defRow)
	{
		final String names[] = getSelectedSeries(tsTable, defRow);
		boolean sameFreq = isSameFreqSelectedSeries(tsTable, defRow);
		
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem copyMenu = new JMenuItem(new DefaultEditorKit.CopyAction());
		copyMenu.setText("Copy " + (sameFreq ? "selected " : "") + "series name" + (sameFreq ? "s" : ""));
		copyMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				String selection = Arrays.toString(names).replaceAll("\\[|\\s", "");
				clipboard.setContents(new StringSelection(selection), null);
			}
		});
		popupMenu.add(copyMenu);
		
		ActionListener viewListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				for (final String name: names)
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run()
						{
							try
							{
								new SeriesViewer(provider, name).setVisible(true);
							}
							catch (SdmxException e1)
							{
								LOGGER.severe("Exception. Class: " + e1.getClass().getName() + " .Message: " + e1.getMessage());
								LOGGER.log(Level.FINER, "", e1);
							}
						}
					});
			}
		};

		JMenuItem compMenu = new JMenuItem();
		compMenu.setText("Tabulate series");
		compMenu.setEnabled(sameFreq && names.length <= 10);
		compMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run()
					{
						try
						{
							new SeriesViewer(provider, dataflow, names).setVisible(true);
						}
						catch (Exception e1)
						{
							LOGGER.severe("Exception. Class: " + e1.getClass().getName() + " .Message: " + e1.getMessage());
							LOGGER.log(Level.FINER, "", e1);
						}
					}
				});
			}
		});
		popupMenu.add(compMenu);
		
		JMenuItem viewMenu = new JMenuItem();
		viewMenu.setText("View series data");
		viewMenu.setEnabled(names.length <= 5);
		viewMenu.addActionListener(viewListener);
		popupMenu.add(viewMenu);
		
		popupMenu.addSeparator();

		JMenuItem countMenu = new JMenuItem();
		countMenu.setText("Selected series: " + names.length);
		countMenu.setEnabled(false);
		popupMenu.add(countMenu);

		return popupMenu;
	}
}