package it.bancaditalia.oss.sdmx.helper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.ScrollPaneConstants;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import javax.swing.text.DefaultEditorKit;

import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.Dimension;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.client.Provider;
import it.bancaditalia.oss.sdmx.client.SDMXClientFactory;
import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.Utils.Function;

public class SDMXHelper extends JFrame
{
	public static final Action COPY_ACTION = new DefaultEditorKit.CopyAction();
	
	static
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} 
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e)
		{
			throw new RuntimeException("Operating system must provide a valid graphic environment", e);
		}
	}
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Configuration.getSdmxLogger();
	private static final String[] HELP = {
			"Obtains a token for accessing Thomson Reuters Datastream Webservices, and print it to standard output.",
			"", "java " + SDMXHelper.class.getName() + " [-s <provider>]", "",
			"    -s    Enable \"print query\" button and lock SDMXHelper on the specified provider." };


	private final JLabel queryLabel;
	private final JTextField sdmxQueryTextField;
	private final JTextField codesFilterTextField;
	private final JTextField dataflowFilterTextField;
	private final JTable dataflowsTable;
	private final JLabel codelistLabel;
	private final JTable dimensionsTable;
	private final JTable codesTable;
	private final JButton checkQueryButton;
	private final JButton btnPrintQuery;
	private final ButtonGroup selectedProviderGroup = new ButtonGroup();
	private final DataflowsModel dataflowsTableModel = new DataflowsModel();
	private final EnumedListTableModel<Dimension> dimensionsTableModel = new EnumedListTableModel<>(
			"Dimension", "Description");
	private final HashMap<String, TableRowSorter<CheckboxListTableModel<String>>> codelistSortersMap = new HashMap<>();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		String lockedProvider = null;

		if (args.length > 0 && (args.length == 1 || args.length > 2 || !"-s".equals(args[0])))
		{
			for (String helpStr : HELP)
				System.err.println(helpStr);
			System.exit(1);
		} else
		{
			if (args.length == 2)
				lockedProvider = args[1]; // arg 1
		}

		start(true, lockedProvider);
	}

	// Required by SAS
	@SuppressWarnings("unused")
	private SDMXHelper() {
		this(false, null);
	}
	
	public static void start()
	{
		start(false);
	}

	public static void start(final boolean exitOnClose)
	{
		start(exitOnClose, null);
	}

	public static void start(final boolean exitOnClose, String lockedProvider)
	{
		new SDMXHelper(exitOnClose, lockedProvider);
	}

	/**
	 * Create the frame.
	 */
	public SDMXHelper(boolean exitOnClose, String lockedProvider)
	{
		if (lockedProvider != null)
			if ("".equals(lockedProvider.trim()))
				lockedProvider = null;
			else
				LOGGER.info("Picking provider " + lockedProvider);

		setTitle("SDMX Helper Tool");
		setSize(1024, 768);
		setDefaultCloseOperation(exitOnClose ? JFrame.EXIT_ON_CLOSE : JFrame.HIDE_ON_CLOSE);

		final JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		final JMenu providersMenu = new JMenu("Providers");
		menuBar.add(providersMenu);

		final JMenu mnActions = new JMenu("Actions");
		mnActions.setMnemonic(KeyEvent.VK_A);
		menuBar.add(mnActions);

		final JMenuItem mntmCopySelection = new JMenuItem(COPY_ACTION);
		mntmCopySelection.setActionCommand("Copy selection");
		mntmCopySelection.setMnemonic(KeyEvent.VK_COPY);
		mntmCopySelection.setText("Copy selection");
		mnActions.add(mntmCopySelection);

		final JMenuItem mntmBuildCommands = new JMenuItem("Build commands");
		mntmBuildCommands.setMnemonic(KeyEvent.VK_B);
		mntmBuildCommands.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent paramActionEvent)
				{
					try
					{
						if (selectedProviderGroup.getSelection() != null)
							new ToolCommandsFrame(sdmxQueryTextField.getText(),
									selectedProviderGroup.getSelection().getActionCommand());
					} 
					catch (SdmxException ex)
					{
						LOGGER.severe("Exception. Class: " + ex.getClass().getName() + " .Message: " + ex.getMessage());
						LOGGER.log(Level.FINER, "", ex);
					}
				}
			});
		mnActions.add(mntmBuildCommands);

		JSeparator separator = new JSeparator();
		mnActions.add(separator);

		JMenuItem mntmAddProvider = new JMenuItem("Add provider...");
		mntmAddProvider.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					NewProviderDialog newProviderDialog = new NewProviderDialog();
					if (newProviderDialog.getResult() == JOptionPane.OK_OPTION)
						try
						{
							String name = newProviderDialog.getName();
							String description = newProviderDialog.getDescription();
							URI endpoint = new URI(newProviderDialog.getURL());
							SDMXClientFactory.addProvider(name, endpoint, false, false, true, description, false);
							providersMenu.removeAll();
							providersSetup(providersMenu);
						} 
						catch (SdmxException | URISyntaxException e)
						{
							e.printStackTrace();
						}
				}
			});
		mntmAddProvider.setMnemonic(KeyEvent.VK_P);
		mnActions.add(mntmAddProvider);

		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);

		JMenuItem mntmAboutSdmxConnectors = new JMenuItem("About SDMX Connectors...");
		mntmAboutSdmxConnectors.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent paramActionEvent)
				{
					new AboutContentFrame();
				}
			});
		mnHelp.add(mntmAboutSdmxConnectors);
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel controlsPane = new JPanel();
		controlsPane.setBorder(new EmptyBorder(10, 10, 10, 10));
		controlsPane.setLayout(new BoxLayout(controlsPane, BoxLayout.Y_AXIS));

		JPanel finalqueryPanel = new JPanel();
		finalqueryPanel.setMaximumSize(new java.awt.Dimension(32767, 60));
		finalqueryPanel.setMinimumSize(new java.awt.Dimension(10, 60));
		finalqueryPanel.setPreferredSize(new java.awt.Dimension(10, 60));
		finalqueryPanel.setLayout(new BoxLayout(finalqueryPanel, BoxLayout.X_AXIS));
		finalqueryPanel.setBorder(new CompoundBorder(
				new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Query",
						TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)),
				new EmptyBorder(5, 5, 5, 5)));
		controlsPane.add(finalqueryPanel);

		queryLabel = new JLabel();
		queryLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		queryLabel.setPreferredSize(new java.awt.Dimension(150, 14));
		queryLabel.setMinimumSize(new java.awt.Dimension(200, 14));
		queryLabel.setMaximumSize(new java.awt.Dimension(200, 14));
		queryLabel.setSize(new java.awt.Dimension(200, 14));
		finalqueryPanel.add(queryLabel);

		Component horizontalStrut = Box.createHorizontalStrut(10);
		finalqueryPanel.add(horizontalStrut);

		sdmxQueryTextField = new JTextField();
		queryLabel.setLabelFor(sdmxQueryTextField);
		sdmxQueryTextField.setFont(new Font(null, Font.BOLD, 16));
		sdmxQueryTextField.setEditable(false);
		finalqueryPanel.add(sdmxQueryTextField);

		Component horizontalStrut_1 = Box.createHorizontalStrut(10);
		finalqueryPanel.add(horizontalStrut_1);

		checkQueryButton = new JButton("Check query");
		checkQueryButton.setEnabled(false);
		checkQueryButton.setPreferredSize(new java.awt.Dimension(120, 23));
		checkQueryButton.setMinimumSize(new java.awt.Dimension(120, 23));
		checkQueryButton.setMaximumSize(new java.awt.Dimension(120, 23));
		checkQueryButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					displayQueryResults();
				}
			});
		finalqueryPanel.add(checkQueryButton);

		Component horizontalStrut_5 = Box.createHorizontalStrut(10);

		btnPrintQuery = new JButton("Print query & exit");
		btnPrintQuery.setEnabled(false);
		btnPrintQuery.setPreferredSize(new java.awt.Dimension(120, 23));
		btnPrintQuery.setMinimumSize(new java.awt.Dimension(120, 23));
		btnPrintQuery.setMaximumSize(new java.awt.Dimension(120, 23));
		btnPrintQuery.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					System.out.println(sdmxQueryTextField.getText());
					System.exit(0);
				}
			});

		Component verticalStrut_1 = Box.createVerticalStrut(20);
		verticalStrut_1.setPreferredSize(new java.awt.Dimension(0, 10));
		verticalStrut_1.setMinimumSize(new java.awt.Dimension(0, 10));
		verticalStrut_1.setMaximumSize(new java.awt.Dimension(32767, 10));
		controlsPane.add(verticalStrut_1);

		JPanel dataflowsPanel = new JPanel();
		dataflowsPanel.setPreferredSize(new java.awt.Dimension(10, 150));
		dataflowsPanel.setMinimumSize(new java.awt.Dimension(10, 150));
		dataflowsPanel.setBorder(new CompoundBorder(
				new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Dataflow selection",
						TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)),
				new EmptyBorder(10, 10, 10, 10)));
		dataflowsPanel.setLayout(new BorderLayout(0, 0));
		controlsPane.add(dataflowsPanel);

		JPanel dataflowFilterPanel = new JPanel();
		dataflowsPanel.add(dataflowFilterPanel, BorderLayout.NORTH);
		dataflowFilterPanel.setLayout(new BoxLayout(dataflowFilterPanel, BoxLayout.X_AXIS));

		// Dataflow selection filter
		JLabel dataflowFilterLabel = new JLabel("Filter flows by name or description:");
		dataflowFilterPanel.add(dataflowFilterLabel);

		Component horizontalStrut_2 = Box.createHorizontalStrut(10);
		dataflowFilterPanel.add(horizontalStrut_2);

		final TableRowSorter<DataflowsModel> dataflowsTableSorter = new TableRowSorter<>(dataflowsTableModel);
		dataflowsTableSorter.setSortKeys(Collections.singletonList(new RowSorter.SortKey(0, SortOrder.ASCENDING)));

		dataflowFilterTextField = new JTextField();
		dataflowFilterTextField.setPreferredSize(new java.awt.Dimension(6, 25));
		dataflowFilterTextField.setMaximumSize(new java.awt.Dimension(2147483647, 25));
		dataflowFilterTextField.setFont(new Font(null, Font.BOLD, 16));
		dataflowFilterTextField.setForeground(Color.RED);
		dataflowFilterTextField.getDocument().addDocumentListener(new DocumentListener()
			{
				@Override
				public void insertUpdate(DocumentEvent e)
				{
					filter();
				}

				@Override
				public void removeUpdate(DocumentEvent e)
				{
					filter();
				}

				@Override
				public void changedUpdate(DocumentEvent e)
				{
					filter();
				}

				private void filter()
				{
					String pattern = dataflowFilterTextField.getText();
					if (pattern != null)
						dataflowsTableSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(pattern)));
				}
			});
		dataflowFilterPanel.add(dataflowFilterTextField);

		Component verticalStrut = Box.createVerticalStrut(10);
		verticalStrut.setPreferredSize(new java.awt.Dimension(0, 10));
		verticalStrut.setMinimumSize(new java.awt.Dimension(0, 10));
		verticalStrut.setMaximumSize(new java.awt.Dimension(32767, 10));
		controlsPane.add(verticalStrut);

		Box horizontalBox = Box.createHorizontalBox();
		controlsPane.add(horizontalBox);

		JPanel dimensionsPanel = new JPanel();
		horizontalBox.add(dimensionsPanel);
		dimensionsPanel.setPreferredSize(new java.awt.Dimension(400, 200));
		dimensionsPanel.setMinimumSize(new java.awt.Dimension(200, 150));
		dimensionsPanel.setBorder(new CompoundBorder(
				new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Dimension selection",
						TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)),
				new EmptyBorder(10, 10, 10, 10)));
		dimensionsPanel.setLayout(new BoxLayout(dimensionsPanel, BoxLayout.Y_AXIS));

		Box dimensionFilterBox = Box.createHorizontalBox();
		dimensionFilterBox.setPreferredSize(new java.awt.Dimension(10, 25));
		dimensionFilterBox.setMinimumSize(new java.awt.Dimension(10, 25));
		dimensionFilterBox.setMaximumSize(new java.awt.Dimension(32768, 25));
		dimensionsPanel.add(dimensionFilterBox);

		JLabel dimensionLabel = new JLabel("Dimension to edit:");
		dimensionFilterBox.add(dimensionLabel);

		Component horizontalStrut_3 = Box.createHorizontalStrut(10);
		horizontalStrut_3.setPreferredSize(new java.awt.Dimension(10, 20));
		horizontalStrut_3.setMinimumSize(new java.awt.Dimension(10, 20));
		horizontalStrut_3.setMaximumSize(new java.awt.Dimension(10, 20));
		dimensionFilterBox.add(horizontalStrut_3);

		JScrollPane dimensionsScrollPane = new JScrollPane();
		dimensionsScrollPane.setPreferredSize(new java.awt.Dimension(200, 23));
		dimensionsScrollPane.setMinimumSize(new java.awt.Dimension(200, 23));
		dimensionsPanel.add(dimensionsScrollPane);

		JPanel codesPanel = new JPanel();
		codesPanel.setSize(new java.awt.Dimension(10, 150));
		horizontalBox.add(codesPanel);
		codesPanel.setPreferredSize(new java.awt.Dimension(400, 200));
		codesPanel.setMinimumSize(new java.awt.Dimension(200, 150));
		codesPanel.setBorder(new CompoundBorder(
				new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Codelist selection",
						TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)),
				new EmptyBorder(10, 10, 10, 10)));
		codesPanel.setLayout(new BoxLayout(codesPanel, BoxLayout.Y_AXIS));

		Box codesFilterBox = Box.createHorizontalBox();
		codesFilterBox.setPreferredSize(new java.awt.Dimension(10, 25));
		codesFilterBox.setMinimumSize(new java.awt.Dimension(10, 25));
		codesFilterBox.setMaximumSize(new java.awt.Dimension(32768, 25));
		codesPanel.add(codesFilterBox);

		codelistLabel = new JLabel("Filter codes:");
		codesFilterBox.add(codelistLabel);

		Component horizontalStrut_4 = Box.createHorizontalStrut(10);
		codesFilterBox.add(horizontalStrut_4);

		JScrollPane codelistScrollPane = new JScrollPane();
		codelistScrollPane.setPreferredSize(new java.awt.Dimension(200, 23));
		codelistScrollPane.setMinimumSize(new java.awt.Dimension(200, 23));
		codesPanel.add(codelistScrollPane);

		codesFilterTextField = new JTextField();
		codesFilterTextField.setForeground(Color.RED);
		codesFilterTextField.setFont(new Font("Dialog", Font.BOLD, 16));
		codesFilterTextField.getDocument().addDocumentListener(new DocumentListener()
			{
				@Override
				public void insertUpdate(DocumentEvent e)
				{
					filter();
				}

				@Override
				public void removeUpdate(DocumentEvent e)
				{
					filter();
				}

				@Override
				public void changedUpdate(DocumentEvent e)
				{
					filter();
				}

				@SuppressWarnings("unchecked")
				private void filter()
				{
					String pattern = codesFilterTextField.getText();
					if (pattern != null)
					{
						((TableRowSorter<DataflowsModel>) codesTable.getRowSorter())
								.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(pattern)));
						updateCodelistCount();
					}
				}
			});
		codesFilterBox.add(codesFilterTextField);

		JButton toggleVisibleButton = new JButton("Toggle unfiltered rows");
		toggleVisibleButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent paramActionEvent)
				{
					for (int i = 0; i < codesTable.getRowCount(); i++)
						codesTable.setValueAt(
								!(Boolean) codesTable.getValueAt(i, codesTable.convertColumnIndexToView(0)), i,
								codesTable.convertColumnIndexToView(0));
					updateCodelistCount();
				}
			});
		codesFilterBox.add(toggleVisibleButton);

		codesTable = new JTable()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void editingStopped(ChangeEvent e)
				{
					super.editingStopped(e);

					updateCodelistCount();
				}
			};
		DefaultTableColumnModel fixedColumnModel = new DefaultTableColumnModel();
		TableColumn columns[] = new TableColumn[] { new TableColumn(0), new TableColumn(1), new TableColumn(2) };
		String columnHeaders[] = new String[] { "", "Code ID", "Code Description" };
		int minWidths[] = new int[] { 30, 100, 200 };
		int maxWidths[] = new int[] { 30, Integer.MAX_VALUE, Integer.MAX_VALUE };
		int preferredWidths[] = new int[] { 30, 100, 400 };
		for (int i = 0; i < columns.length; i++)
		{
			columns[i].setHeaderValue(columnHeaders[i]);
			columns[i].setMinWidth(minWidths[i]);
			columns[i].setMaxWidth(preferredWidths[i]);
			columns[i].setPreferredWidth(maxWidths[i]);
			fixedColumnModel.addColumn(columns[i]);
		}
		codesTable.setColumnModel(fixedColumnModel);
		codesTable.setAutoCreateColumnsFromModel(false);
		codesTable.getTableHeader().setReorderingAllowed(false);
		codelistScrollPane.setViewportView(codesTable);

		final TableRowSorter<EnumedListTableModel<Dimension>> dimensionsTableSorter = new TableRowSorter<>(
				dimensionsTableModel);
		dimensionsTableSorter.setSortKeys(Collections.singletonList(new RowSorter.SortKey(0, SortOrder.ASCENDING)));

		dimensionsTable = new JTable();
		dimensionsTable.setModel(dimensionsTableModel);
		dimensionsTable.getColumnModel().getColumn(0).setMinWidth(20);
		dimensionsTable.getColumnModel().getColumn(0).setMaxWidth(20);
		dimensionsTable.getColumnModel().getColumn(1).setMinWidth(150);
		dimensionsTable.getColumnModel().getColumn(1).setMaxWidth(Integer.MAX_VALUE);
		dimensionsTable.getColumnModel().getColumn(2).setMinWidth(200);
		dimensionsTable.getColumnModel().getColumn(2).setMaxWidth(Integer.MAX_VALUE);
		dimensionsTable.getColumnModel().getColumn(2).setPreferredWidth(400);
		dimensionsTable.setRowSorter(dimensionsTableSorter);
		dimensionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		dimensionsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
			{
				@Override
				public void valueChanged(ListSelectionEvent e)
				{
					final String selectedDimension = getSelectedDimension();
					final String selectedDataflow = getSelectedDataflow();

					if (!e.getValueIsAdjusting() && selectedDimension != null)
					{
						final List<? extends SortKey> sortingKeys = codesTable.getRowSorter() == null ? null
								: codesTable.getRowSorter().getSortKeys();
						final AtomicBoolean interrupted = new AtomicBoolean(false);

						if (!codelistSortersMap.containsKey(selectedDimension))
						{
							final ProgressViewer progress = new ProgressViewer(SDMXHelper.this, new Runnable()
								{
									@Override
									public void run()
									{
										synchronized (interrupted)
										{
											try
											{
												// No UI modification here, only build the model and the sorter
												Map<String, String> codes = SdmxClientHandler.getCodes(
														selectedProviderGroup.getSelection().getActionCommand(),
														selectedDataflow, selectedDimension);
												CheckboxListTableModel<String> codeTableModel = new CheckboxListTableModel<String>("Code ID", "Code Description");
												codeTableModel.addTableModelListener(new TableModelListener()
													{
														@Override
														public void tableChanged(TableModelEvent var1)
														{
															try
															{
																updateSDMXQuery(selectedProviderGroup.getSelection().getActionCommand(),
																		getSelectedDataflow());
															} 
															catch (Exception ex)
															{
																LOGGER.severe(
																		"Exception. Class: " + ex.getClass().getName()
																				+ " .Message: " + ex.getMessage());
																LOGGER.log(Level.FINER, "", ex);
															}
														}
													});
												codeTableModel.setItems(codes);
												if (!Thread.currentThread().isInterrupted())
													codelistSortersMap.put(selectedDimension,
															new TableRowSorter<>(
																	codeTableModel));
											} catch (Exception ex)
											{
												LOGGER.severe("Exception. Class: " + ex.getClass().getName()
														+ " .Message: " + ex.getMessage());
												LOGGER.log(Level.FINER, "", ex);
											} finally
											{
												if (Thread.currentThread().isInterrupted())
													interrupted.set(true);
											}
										}
									}
								});

							progress.setVisible(true);
						}

						if (!interrupted.get())
						{
							final TableRowSorter<CheckboxListTableModel<String>> sorter = codelistSortersMap
									.get(selectedDimension);
							final CheckboxListTableModel<String> model = sorter.getModel();
							codesTable.setModel(model);
							codesTable.setRowSorter(sorter);
							// replace model and sorter
							if (sortingKeys == null || sortingKeys.isEmpty())
								sorter.setSortKeys(
										(Collections.singletonList(new RowSorter.SortKey(1, SortOrder.ASCENDING))));
							else
								sorter.setSortKeys(sortingKeys);
							sorter.setSortable(codesTable.convertColumnIndexToView(0), false);
							sorter.setRowFilter(null);
							codesFilterTextField.setText("");
							codesTable.revalidate();
							updateCodelistCount();
						}
					}
				}
			});
		dimensionsScrollPane.setViewportView(dimensionsTable);

		final JTextField dimensionFilterTextField = new JTextField();
		dimensionFilterTextField.setFont(new Font(null, Font.BOLD, 16));
		dimensionFilterTextField.setForeground(Color.RED);
		dimensionFilterTextField.getDocument().addDocumentListener(new DocumentListener()
			{
				@Override
				public void insertUpdate(DocumentEvent e)
				{
					filter();
				}

				@Override
				public void removeUpdate(DocumentEvent e)
				{
					filter();
				}

				@Override
				public void changedUpdate(DocumentEvent e)
				{
					filter();
				}

				private void filter()
				{
					String pattern = dimensionFilterTextField.getText();
					if (pattern != null)
						dimensionsTableSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(pattern)));
				}
			});
		dimensionFilterBox.add(dimensionFilterTextField);

		// Dataflow table setup
		dataflowsTable = new JTable();
		dataflowsTable.setModel(dataflowsTableModel);
		dataflowsTable.getColumnModel().getColumn(0).setMinWidth(200);
		dataflowsTable.getColumnModel().getColumn(0).setMaxWidth(Integer.MAX_VALUE);
		dataflowsTable.getColumnModel().getColumn(1).setMinWidth(60);
		dataflowsTable.getColumnModel().getColumn(1).setMaxWidth(60);
		dataflowsTable.getColumnModel().getColumn(2).setMinWidth(200);
		dataflowsTable.getColumnModel().getColumn(2).setMaxWidth(Integer.MAX_VALUE);
		dataflowsTable.getColumnModel().getColumn(2).setPreferredWidth(800);
		dataflowsTable.setRowSorter(dataflowsTableSorter);
		dataflowsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		dataflowsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
			{
				@Override
				public void valueChanged(ListSelectionEvent e)
				{
					String dataflowID = getSelectedDataflow();
					if (!e.getValueIsAdjusting() && dataflowID != null)
					{
						updateDataflow(dataflowID);
						dimensionFilterTextField.setText("");
						dimensionsTableSorter.setRowFilter(null);
						checkQueryButton.setEnabled(true);
						btnPrintQuery.setEnabled(true);
					}
				}
			});

		JScrollPane dataflowsScrollPane = new JScrollPane();
		dataflowsScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		dataflowsScrollPane.setViewportView(dataflowsTable);
		dataflowsPanel.add(dataflowsScrollPane, BorderLayout.CENTER);

		JButton btnClearSelectedDimension = new JButton("Clear selected dimension");
		btnClearSelectedDimension.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent paramActionEvent)
				{
					if (getSelectedDataflow() != null)
					{
						CheckboxListTableModel<?> model = (CheckboxListTableModel<?>) codesTable.getModel();
						model.uncheckAll();
						updateCodelistCount();
					}
				}
			});
		btnClearSelectedDimension.setMaximumSize(new java.awt.Dimension(151, 32768));
		dimensionFilterBox.add(btnClearSelectedDimension);

		JTextArea loggingArea = new JTextArea();
		loggingArea.setBorder(new EmptyBorder(0, 0, 0, 0));
		loggingArea.setBackground(Color.LIGHT_GRAY);
		Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);
		loggingArea.setFont(font);
		loggingArea.setEditable(false);
		LOGGER.addHandler(new HelperHandler(loggingArea));

		JScrollPane loggingPane = new JScrollPane(loggingArea);
		loggingPane.setBorder(new EmptyBorder(0, 0, 0, 0));

		JSplitPane mainSplitPane = new JSplitPane();
		mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		mainSplitPane.setLeftComponent(controlsPane);
		mainSplitPane.setRightComponent(loggingPane);
		contentPane.add(mainSplitPane);

		providersSetup(providersMenu);

		if (lockedProvider != null)
		{
			for (AbstractButton menuItem : Collections.list(selectedProviderGroup.getElements()))
				if (lockedProvider.equalsIgnoreCase(menuItem.getActionCommand()))
				{
					selectedProviderGroup.setSelected(menuItem.getModel(), true);
					//((ButtonModel) menuItem).setArmed(true);
					menuItem.doClick();
				}

			finalqueryPanel.add(horizontalStrut_5);
			finalqueryPanel.add(btnPrintQuery);
			providersMenu.setEnabled(false);
			providersMenu.setText(providersMenu.getText() + ": " + lockedProvider);
		}

		mainSplitPane.setDividerLocation(0.8);
		
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void updateCodelistCount()
	{
		int selected = 0;
		for (int i = 0; i < codesTable.getRowCount(); i++)
			selected += (Boolean) codesTable.getModel().getValueAt(codesTable.convertRowIndexToModel(i), 0) ? 1 : 0;

		codelistLabel.setText("Filter codes (" + selected + "/" + codesTable.getRowCount() + "):");
	}

	private void updateDataflow(final String dataflowID)
	{
		// if this is not a provider switch
		final ProgressViewer progress = new ProgressViewer(this, new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						final List<Dimension> dims = SdmxClientHandler
								.getDimensions(selectedProviderGroup.getSelection().getActionCommand(), dataflowID);
						if (!Thread.currentThread().isInterrupted())
							SwingUtilities.invokeLater(new Runnable() {
								
								@Override
								public void run() 
								{
									dimensionsTableModel.setItems(dims, new Function<Dimension, String[]>()
									{
										@Override
										public String[] apply(Dimension item)
										{
											return new String[] { item.getId(), item.getName() };
										}
									});
								};
							});

							if (!Thread.currentThread().isInterrupted())
							{
								updateSDMXQuery(selectedProviderGroup.getSelection().getActionCommand(), dataflowID);
							}
					} catch (Exception ex)
					{
						LOGGER.severe("Exception. Class: " + ex.getClass().getName() + " .Message: " + ex.getMessage());
						LOGGER.log(Level.FINER, "", ex);
					}
				}
			});
		progress.setVisible(true);
		progress.setAlwaysOnTop(true);
	}

	private void displayQueryResults()
	{
		final ProgressViewer progress = new ProgressViewer(SDMXHelper.this, new Runnable()
			{
				@Override
				public void run()
				{
					String query = sdmxQueryTextField.getText();
					ButtonModel providerMenu = selectedProviderGroup.getSelection();
					String selectedDataflow = getSelectedDataflow();
					String selectedProvider = providerMenu.getActionCommand();

					try
					{
						if (selectedDataflow != null && query != null && !query.isEmpty())
						{
							Dataflow df = SdmxClientHandler.getFlow(selectedProvider, selectedDataflow);

							if (!Thread.currentThread().isInterrupted())
							{
								final List<PortableTimeSeries<Double>> result = SdmxClientHandler
										.getTimeSeriesNames(selectedProvider, query);

								if (!Thread.currentThread().isInterrupted())
								{
									// Open a new window to browse query results
									JFrame wnd = new ResultsFrame(result);

									wnd.setTitle(result.size() + " results" + " - " + df.getDescription());
									wnd.setVisible(true);
								}
							}
						}
					} catch (SdmxException ex)
					{
						LOGGER.severe("Exception. Class: " + ex.getClass().getName() + " .Message: " + ex.getMessage());
						LOGGER.log(Level.FINER, "", ex);
					}
				}
			});

		progress.setVisible(true);
	}

	private void providersSetup(final JMenu providersMenu)
	{
		for (final Entry<String, Provider> providerEntry : SDMXClientFactory.getProviders().entrySet())
		{
			final String provider = providerEntry.getKey();
			final JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(
					provider + ": " + providerEntry.getValue().getDescription());
			menuItem.setActionCommand(provider);
			menuItem.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent paramActionEvent)
					{
						try
						{
							menuItem.setSelected(true);
							// Refresh dataflows on provider selection
							updateSource(provider);
							checkQueryButton.setEnabled(false);
							btnPrintQuery.setEnabled(false);
							sdmxQueryTextField.setText("");
							dataflowFilterTextField.setText("");
							queryLabel.setText("Provider: " + provider);
						} catch (Exception ex)
						{
							ex.printStackTrace();
						}
					}
				});
			providersMenu.add(menuItem);
			selectedProviderGroup.add(menuItem);
		}
	}

	/**
	 * Loads and shows all dataflows from a provider.
	 * 
	 * @param provider
	 *            The selected provider
	 */
	private void updateSource(final String provider)
	{
		final ProgressViewer progress = new ProgressViewer(this, new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						final Map<String, Dataflow> flows = SdmxClientHandler.getFlowObjects(provider, null);
						// UI modification must be invoked later
						if (!Thread.currentThread().isInterrupted())
						{
							SwingUtilities.invokeLater(new Runnable()
								{
									@Override
									public void run()
									{
										codelistSortersMap.clear();
										codesTable.setModel(new CheckboxListTableModel<String>("Code ID", "Code Description"));
										dimensionsTableModel.clear();
										dataflowsTableModel.setItems(flows);
									}
								});
						}
					} catch (SdmxException ex)
					{
						LOGGER.severe("Exception. Class: " + ex.getClass().getName() + " .Message: " + ex.getMessage());
						LOGGER.log(Level.FINER, "", ex);
					}
				}
			});

		progress.setVisible(true);
	}

	private void updateSDMXQuery(String provider, String dataflow) throws SdmxException
	{
		String buf = dataflow + "/";
		StringBuffer query = new StringBuffer("");
		List<Dimension> dims = SdmxClientHandler.getDimensions(provider, dataflow);
		boolean first = true;
		for (Dimension dim : dims)
		{
			if (!first)
				query.append(".");

			if (codelistSortersMap.containsKey(dim.getId()))
			{
				CheckboxListTableModel<String> model = codelistSortersMap.get(dim.getId()).getModel();

				first = true;
				for (String code : model.getCheckedCodes())
				{
					if (!first)
						query.append("+");
					query.append(code);
					first = false;
				}
			}

			first = false;
		}

		// temporary workaround for issue #94.
		// The '..' path breaks some providers and the 'all' keyword is a good
		// replacement,
		// but it is not accepted by the Eurostat provider (that anyway is able to
		// handle the '..' path
		if (query.toString().equals("..") && !provider.equalsIgnoreCase("EUROSTAT"))
			query = new StringBuffer("all");

		buf += query.toString();
		sdmxQueryTextField.setText(buf);
	}

	private String getSelectedDataflow()
	{
		int rowSelected = dataflowsTable.getSelectedRow();
		return rowSelected != -1
				? dataflowsTable.getValueAt(rowSelected, dataflowsTable.convertColumnIndexToView(0)).toString()
				: null;
	}

	private String getSelectedDimension()
	{
		int rowSelected = dimensionsTable.getSelectedRow();
		return rowSelected != -1
				? dimensionsTable.getValueAt(rowSelected, dimensionsTable.convertColumnIndexToView(1)).toString()
				: null;
	}
}
