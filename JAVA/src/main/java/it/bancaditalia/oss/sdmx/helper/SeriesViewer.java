package it.bancaditalia.oss.sdmx.helper;

import static it.bancaditalia.oss.sdmx.api.SDMXVersion.V3;
import static it.bancaditalia.oss.sdmx.helper.SDMXHelper.ICON_MAX;
import static it.bancaditalia.oss.sdmx.helper.SDMXHelper.ICON_MIN;
import static java.awt.Image.SCALE_SMOOTH;
import static java.awt.event.KeyEvent.VK_P;
import static java.util.Locale.US;
import static javax.swing.SwingConstants.RIGHT;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import it.bancaditalia.oss.sdmx.api.BaseObservation;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.client.SDMXClientFactory;
import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;

public class SeriesViewer extends JFrame
{
	private static final long serialVersionUID = 1L;

	private final JTable table = new JTable();
	private final JSlider precisionSlider = new JSlider();
	private final DefaultTableModel model;

	private class IconRenderer implements TableCellRenderer, Serializable
	{
		private static final long serialVersionUID = 1L;
		private final Map<String, Double> maxes;
		private final Map<String, Double> mins;

		private final DefaultTableCellRenderer defRenderer = new DefaultTableCellRenderer();
		private final JPanel pane[] = { new JPanel(), new JPanel() };
		private final JLabel minLabel = new JLabel();
		private final JLabel maxLabel = new JLabel();
		
		public IconRenderer(Map<String, Double> maxes, Map<String, Double> mins)
		{
			this.maxes = maxes;
			this.mins = mins;

			pane[0].setLayout(new BorderLayout());
			pane[1].setLayout(new BorderLayout());
			pane[0].add(minLabel, BorderLayout.WEST);
			pane[1].add(maxLabel, BorderLayout.WEST);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int col)
		{
			String formatted = value != null ? value.toString() : "";
			if (value instanceof Double)
				formatted = String.format(US, "%." + precisionSlider.getValue() + "f", value);
			
			JLabel defLabel = (JLabel) defRenderer.getTableCellRendererComponent(table, formatted, isSelected, hasFocus, row, col);
			defLabel.setHorizontalAlignment(RIGHT);

			if (table.convertColumnIndexToModel(col) > 0 && value != null)
			{
				int width = table.getColumnModel().getColumn(col).getPreferredWidth();
				int height = table.getRowHeight();
				String time = table.getValueAt(table.convertRowIndexToModel(row), table.convertColumnIndexToModel(0)).toString();
				
				if (value.equals(mins.get(time)))
				{
					if (minLabel.getIcon() == null || minLabel.getIcon().getIconHeight() != height)
						minLabel.setIcon(new ImageIcon(ICON_MIN.getScaledInstance(height, height, SCALE_SMOOTH)));
					pane[0].add(defLabel, BorderLayout.EAST);
					pane[0].setForeground(defLabel.getForeground());
					pane[0].setBackground(defLabel.getBackground());
					pane[0].setPreferredSize(new Dimension(width, height));
					return pane[0];
				}
				else if (value.equals(maxes.get(time)))
				{
					if (maxLabel.getIcon() == null || maxLabel.getIcon().getIconHeight() != height)
						maxLabel.setIcon(new ImageIcon(ICON_MAX.getScaledInstance(height, height, SCALE_SMOOTH)));
					pane[1].add(defLabel, BorderLayout.EAST);
					pane[1].setForeground(defLabel.getForeground());
					pane[1].setBackground(defLabel.getBackground());
					pane[1].setPreferredSize(new Dimension(width, height));
					return pane[1];
				}

				defLabel.setPreferredSize(new Dimension(width, height));
			}

			return defLabel;
		}
	}

	/**
	 * @wbp.parser.constructor
	 */
	public SeriesViewer(String provider, String dataflow, String[] nameArray) throws SdmxException, IOException
	{
		super("Tabulate series");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu precisionMenu = new JMenu("Precision");
		precisionMenu.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent paramFocusEvent) {
				precisionSlider.requestFocus();
			}
		});
		precisionMenu.setMnemonic(VK_P);
		menuBar.add(precisionMenu);
		precisionSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				table.invalidate();
				table.repaint();
			}
		});
		
		precisionSlider.setMajorTickSpacing(1);
		precisionSlider.setSnapToTicks(true);
		precisionSlider.setPaintTicks(true);
		precisionSlider.setMaximum(14);
		precisionSlider.setValue(6);
		precisionMenu.add(precisionSlider);
		
		Set<String> names = new HashSet<>(Arrays.asList(nameArray)); 
		List<PortableTimeSeries<Double>> series = new ArrayList<>();
		for (String name: names)
			if (V3 == SDMXClientFactory.getProviders().get(provider).getSdmxVersion()) {
				series.addAll(SdmxClientHandler.getTimeSeries2(provider, dataflow, name, null, null, null, "all", "none", null, false));
			}
			else{
				series.addAll(SdmxClientHandler.getTimeSeries(provider, dataflow + "/" + name, null, null, false, null, false));
			}
		
		if (series.size() < names.size())
			throw new IllegalStateException("Couldn't download all series");
		
		model = new ComparatorModel(names);

		final Map<String, List<Object>> values = new TreeMap<>();
		final Map<String, Double> maxes = new HashMap<>();
		final Map<String, Double> mins = new HashMap<>();
		for (int col = 0; col < names.size(); col++)
			for(BaseObservation<? extends Double> obs: series.get(col))
			{
				List<Object> row = values.get(obs.getTimeslot());
				if (row == null)
					row = new ArrayList<>();
				for (int i = row.size(); i < col; i++)
					row.add(null);
				row.add(obs.getValue());
				values.put(obs.getTimeslot(), row);
			}

		for (Entry<String, List<Object>> entry: values.entrySet())
		{
			List<Object> row = entry.getValue();
			while (row.size() < names.size())
				row.add(null);
			String time = entry.getKey();
			Double max = null, min = null;
			for (int col = 0; col < row.size(); col++)
			{
				if (max == null || row.get(col) != null && max < (Double) row.get(col))
					max = (Double) row.get(col);
				if (min == null || row.get(col) != null && min > (Double) row.get(col))
					min = (Double) row.get(col);
				if (min != null)
					mins.put(time, min);
				if (max != null)
					maxes.put(time, max);
			}
			row.add(0, time);
			model.addRow(row.toArray());
		}

		initialize();

		TableCellRenderer minMaxCellRenderer = new IconRenderer(maxes, mins);

		for (int col = 1; col < model.getColumnCount(); col++)
			table.getColumnModel().getColumn(table.convertColumnIndexToView(col)).setCellRenderer(minMaxCellRenderer);
	}

	public SeriesViewer(String provider, String singleSeriesName) throws SdmxException
	{
		super(singleSeriesName);
		List<PortableTimeSeries<Double>> list = null; 
		list = SdmxClientHandler.getTimeSeries(provider, singleSeriesName, null, null, false, null, false);
		if (list.size() != 1)
			throw new IllegalStateException("Query must return exactly one time series");
		
		PortableTimeSeries<Double> series = list.iterator().next();
		Set<String> attrNames = series.getObsLevelAttributesNames();
		model = new SeriesModel(attrNames);

		for(BaseObservation<? extends Double> obs: series)
		{
			Vector<Object> row = new Vector<>();
			row.add(obs.getTimeslot());
			row.add(obs.getValue());
			for (String attrName: attrNames)
				row.add(obs.getAttributeValue(attrName));
			model.addRow(row);
		}
		
		initialize();
	}

	private void initialize()
	{
		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		table.setModel(model);
		scrollPane.setViewportView(table);

		setSize(450, 300);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
}
