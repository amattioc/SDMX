package it.bancaditalia.oss.sdmx.helper;

import static it.bancaditalia.oss.sdmx.client.SdmxClientHandler.getCodes;
import static it.bancaditalia.oss.sdmx.client.SdmxClientHandler.getFlow;
import static it.bancaditalia.oss.sdmx.client.SdmxClientHandler.getTimeSeries;
import static java.lang.Integer.MAX_VALUE;
import static java.util.Collections.singletonList;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.SortOrder.ASCENDING;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
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
import javax.swing.event.ListSelectionEvent;
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
import it.bancaditalia.oss.sdmx.exceptions.SdmxResponseException;
import it.bancaditalia.oss.sdmx.util.Configuration;

public class SDMXHelper extends JFrame
{
	public static final Action COPY_ACTION = new DefaultEditorKit.CopyAction();

	static final Image ICON_MIN;
	static final Image ICON_MAX;

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
		
		try
		{
			ICON_MIN = ImageIO.read(SDMXHelper.class.getResourceAsStream("min.png"));
			ICON_MAX = ImageIO.read(SDMXHelper.class.getResourceAsStream("max.png"));
		}
		catch (Exception e)
		{
			final AtomicBoolean finished = new AtomicBoolean(false);
			if (!GraphicsEnvironment.isHeadless())
				SwingUtilities.invokeLater(() -> {
						JOptionPane.showMessageDialog(null, "Cannot load resources from jar file.", "SDMX Helper", JOptionPane.ERROR_MESSAGE);
						finished.set(true);
					});
			else
				finished.set(true);
			
			while (!finished.get() && !Thread.interrupted())
				try
				{
					Thread.sleep(500);
				}
				catch (InterruptedException e1)
				{
					Thread.currentThread().interrupt();
				}

			throw new ExceptionInInitializerError(e);
		}
	}
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Configuration.getSdmxLogger();
	private static final String[] HELP = {
			"Obtains a token for accessing Thomson Reuters Datastream Webservices, and print it to standard output.",
			"", "java " + SDMXHelper.class.getName() + " [-s <provider>]", "",
			"    -s    Enable \"print query\" button and lock SDMXHelper on the specified provider." };


	private final JLabel queryLabel;
	private final JTextField sdmxQueryTextField = new JTextField();
	private final JTextField codesFilterTextField = new JTextField();
	private final JTextField dataflowFilterTextField;
	private final JTable dataflowsTable = new JTable();
	private final JLabel codelistLabel;
	private final JTable dimensionsTable;
	private final JTable codesTable;
	private final JButton checkQueryButton;
	private final JButton btnPrintQuery;
	private final ButtonGroup selectedProviderGroup = new ButtonGroup();
	private final DataflowsModel dataflowsTableModel = new DataflowsModel();
	private final EnumedListTableModel<Dimension> dimsTableModel = new EnumedListTableModel<>(
			"Dimension", "Description");
	private final HashMap<String, TableRowSorter<CheckboxListTableModel<String>>> codelistSortersMap = new HashMap<>();
	private final JCheckBox regexSearchCLCheckbox = new JCheckBox("Regular expression");
	private final JCheckBox caseSearchCLCheckbox = new JCheckBox("Case sensitive");
	private final JCheckBox wholeWordCLCheckbox = new JCheckBox("Whole word");
	private final JRadioButton searchCodeCLRadio = new JRadioButton("Code");
	private final JRadioButton searchDescCLRadio = new JRadioButton("Description");
	private final JRadioButton searchBothCLRadio = new JRadioButton("Search Both");
	private final ButtonGroup codeSearchRadioGroup = new ButtonGroup();
	private final JRadioButton searchCodeFlowRadio = new JRadioButton("Search Dataflow Code");
	private final JRadioButton searchDSDFlowRadio = new JRadioButton("Search DSD Code");
	private final JRadioButton searchDescFlowRadio = new JRadioButton("Search Description");
	private final JRadioButton searchAllFlowRadio = new JRadioButton("Search all fields");
	private final JCheckBox wholeWordFlowCheckbox = new JCheckBox("Whole word");
	private final JCheckBox caseSearchFlowCheckbox = new JCheckBox("Case sensitive");
	private final JCheckBox regexSearchFlowCheckbox = new JCheckBox("Regular expression");
	private final ButtonGroup flowSearchRadioGroup = new ButtonGroup();
	private final JTextField dimensionFilterTextField = new JTextField();
	private final TableRowSorter<EnumedListTableModel<Dimension>> dimTableSorter = new TableRowSorter<>(dimsTableModel);

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
		}
		else
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

	public String getCurrentProvider()
	{
		return selectedProviderGroup.getSelection().getActionCommand();
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
		mntmBuildCommands.addActionListener(paramActionEvent -> {
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
			});
		mnActions.add(mntmBuildCommands);

		JSeparator separator = new JSeparator();
		mnActions.add(separator);

		JMenuItem mntmAddProvider = new JMenuItem("Add provider...");
		mntmAddProvider.addActionListener(event -> {
					NewProviderDialog newProviderDialog = new NewProviderDialog();
					if (newProviderDialog.getResult() == JOptionPane.OK_OPTION)
						try
						{
							String name = newProviderDialog.getName();
							String description = newProviderDialog.getDescription();
							String sdmxVersion = newProviderDialog.getSdmxVersion();
							URI endpoint = new URI(newProviderDialog.getURL());
							SDMXClientFactory.addProvider(name, endpoint, false, false, true, description, false, sdmxVersion);
							providersMenu.removeAll();
							providersSetup(providersMenu);
						} 
						catch (SdmxException | URISyntaxException e)
						{
							e.printStackTrace();
						}
				});
		mntmAddProvider.setMnemonic(KeyEvent.VK_P);
		mnActions.add(mntmAddProvider);

		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);

		JMenuItem mntmAboutSdmxConnectors = new JMenuItem("About SDMX Connectors...");
		mntmAboutSdmxConnectors.addActionListener(event -> new AboutContentFrame());
		mnHelp.add(mntmAboutSdmxConnectors);

		final JSplitPane controlsPane = new JSplitPane();
		controlsPane.setBorder(null);
		controlsPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		
		JPanel queryAndFlowPanel = new JPanel();
		queryAndFlowPanel.setMinimumSize(new java.awt.Dimension(10, 250));
		controlsPane.setLeftComponent(queryAndFlowPanel);
		queryAndFlowPanel.setLayout(new BoxLayout(queryAndFlowPanel, BoxLayout.Y_AXIS));

		JPanel finalqueryPanel = new JPanel();
		finalqueryPanel.setMaximumSize(new java.awt.Dimension(32767, 70));
		finalqueryPanel.setMinimumSize(new java.awt.Dimension(10, 70));
		finalqueryPanel.setPreferredSize(new java.awt.Dimension(10, 60));
		finalqueryPanel.setLayout(new BoxLayout(finalqueryPanel, BoxLayout.X_AXIS));
		finalqueryPanel.setBorder(new CompoundBorder(
				new EmptyBorder(5, 5, 0, 5), new CompoundBorder(
					new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Query",
						TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)),
					new EmptyBorder(5, 5, 5, 5))));
		queryAndFlowPanel.add(finalqueryPanel);

		queryLabel = new JLabel();
		queryLabel.setText("Result query:");
		queryLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		queryLabel.setPreferredSize(new java.awt.Dimension(150, 14));
		queryLabel.setMinimumSize(new java.awt.Dimension(200, 14));
		queryLabel.setMaximumSize(new java.awt.Dimension(200, 14));
		queryLabel.setSize(new java.awt.Dimension(200, 14));
		finalqueryPanel.add(queryLabel);

		Component horizontalStrut = Box.createHorizontalStrut(10);
		finalqueryPanel.add(horizontalStrut);

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
		checkQueryButton.addActionListener(e -> displayQueryResults());
		finalqueryPanel.add(checkQueryButton);

		Component horizontalStrut_5 = Box.createHorizontalStrut(10);

		btnPrintQuery = new JButton("Print query & exit");
		btnPrintQuery.setEnabled(false);
		btnPrintQuery.setPreferredSize(new java.awt.Dimension(120, 23));
		btnPrintQuery.setMinimumSize(new java.awt.Dimension(120, 23));
		btnPrintQuery.setMaximumSize(new java.awt.Dimension(120, 23));
		btnPrintQuery.addActionListener(e -> {
				System.out.println(sdmxQueryTextField.getText());
				System.exit(0);
			});

		Component verticalStrut_1 = Box.createVerticalStrut(20);
		verticalStrut_1.setPreferredSize(new java.awt.Dimension(0, 10));
		verticalStrut_1.setMinimumSize(new java.awt.Dimension(0, 10));
		verticalStrut_1.setMaximumSize(new java.awt.Dimension(32767, 10));
		queryAndFlowPanel.add(verticalStrut_1);

		JPanel dataflowsPanel = new JPanel();
		dataflowsPanel.setPreferredSize(new java.awt.Dimension(10, 150));
		dataflowsPanel.setMinimumSize(new java.awt.Dimension(10, 150));
		dataflowsPanel.setBorder(new CompoundBorder(
				new EmptyBorder(0, 5, 5, 5), new CompoundBorder(
					new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Dataflow selection",
						TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)),
					new EmptyBorder(10, 10, 10, 10))));
		queryAndFlowPanel.add(dataflowsPanel);
		dataflowsPanel.setLayout(new BoxLayout(dataflowsPanel, BoxLayout.Y_AXIS));

		JPanel dataflowFilterPanel = new JPanel();
		dataflowFilterPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		dataflowsPanel.add(dataflowFilterPanel);
		dataflowFilterPanel.setLayout(new BoxLayout(dataflowFilterPanel, BoxLayout.X_AXIS));

		// Dataflow selection filter
		JLabel dataflowFilterLabel = new JLabel("Filter flows by name or description:");
		dataflowFilterPanel.add(dataflowFilterLabel);

		Component horizontalStrut_2 = Box.createHorizontalStrut(10);
		dataflowFilterPanel.add(horizontalStrut_2);

		final TableRowSorter<DataflowsModel> dataflowsTableSorter = new TableRowSorter<>(dataflowsTableModel);
		dataflowsTableSorter.setSortKeys(singletonList(new RowSorter.SortKey(0, SortOrder.ASCENDING)));

		dataflowFilterTextField = new JTextField();
		dataflowFilterTextField.setPreferredSize(new java.awt.Dimension(6, 25));
		dataflowFilterTextField.setMaximumSize(new java.awt.Dimension(2147483647, 25));
		dataflowFilterTextField.setFont(new Font(null, Font.BOLD, 16));
		dataflowFilterTextField.setForeground(Color.RED);
			
		final DoFilterListener flowListener = new DoFilterListener(dataflowFilterTextField, text -> {
				String searchPattern = caseSearchFlowCheckbox.isSelected() ? "": "(?i)";
				searchPattern += regexSearchFlowCheckbox.isSelected() ? text : Pattern.quote(text);
				searchPattern = wholeWordFlowCheckbox.isSelected() ? "^" + searchPattern + "$" : searchPattern;  
				
				try 
				{
			        Pattern.compile(searchPattern);

					final int indices[];
					int indicesCode[] = { 0 };
					int indicesDSD[] = { 2 };
					int indicesDesc[] = { 5 };
					int indicesAll[] = { 0, 2, 5 };
					
					if (searchCodeFlowRadio.isSelected())
						indices = indicesCode;
					else if (searchDSDFlowRadio.isSelected())
						indices = indicesDSD;
					else if (searchDescFlowRadio.isSelected())
						indices = indicesDesc;
					else
						indices = indicesAll;

					((TableRowSorter<?>) dataflowsTable.getRowSorter()).setRowFilter(RowFilter.regexFilter(searchPattern, indices));
				} 
				catch (PatternSyntaxException e) 
				{
			        // don't do anything if the pattern is invalid
			    }
			});
		dataflowFilterTextField.getDocument().addDocumentListener(flowListener);
		dataflowFilterPanel.add(dataflowFilterTextField);

		JSplitPane horizontalSplitPane = new JSplitPane();
		horizontalSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		controlsPane.setRightComponent(horizontalSplitPane);

		JPanel dimensionsPanel = new JPanel();
		horizontalSplitPane.setLeftComponent(dimensionsPanel);
		dimensionsPanel.setPreferredSize(new java.awt.Dimension(400, 200));
		dimensionsPanel.setMinimumSize(new java.awt.Dimension(200, 150));
		dimensionsPanel.setBorder(new CompoundBorder(
				new EmptyBorder(5, 5, 5, 5), new CompoundBorder(
					new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Dimension selection",
						TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)),
					new EmptyBorder(10, 10, 10, 10))));
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
		horizontalSplitPane.setRightComponent(codesPanel);
		codesPanel.setPreferredSize(new java.awt.Dimension(400, 200));
		codesPanel.setMinimumSize(new java.awt.Dimension(200, 150));
		codesPanel.setBorder(new CompoundBorder(
				new EmptyBorder(5, 5, 5, 5), new CompoundBorder(
					new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Codelist selection",
						TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)),
					new EmptyBorder(10, 10, 10, 10))));
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
		
		Box codeOptionsPane = Box.createHorizontalBox();
		codeOptionsPane.setPreferredSize(new java.awt.Dimension(10, 25));
		codeOptionsPane.setMinimumSize(new java.awt.Dimension(10, 25));
		codeOptionsPane.setMaximumSize(new java.awt.Dimension(32768, 25));
		codesPanel.add(codeOptionsPane);

		codesTable = new JTable() {
			private static final long serialVersionUID = 1L;

			@Override
			public void editingStopped(ChangeEvent e)
			{
				super.editingStopped(e);

				updateCodelistCount();
			}
		};
		
		final DoFilterListener codeFilterListener = new DoFilterListener(codesFilterTextField, text -> {
				String searchPattern = caseSearchCLCheckbox.isSelected() ? "": "(?i)";
				searchPattern += regexSearchCLCheckbox.isSelected() ? text : Pattern.quote(text);
				searchPattern = wholeWordCLCheckbox.isSelected() ? "^" + searchPattern + "$" : searchPattern;  
				
				try 
				{
			        Pattern.compile(searchPattern);

					final int indices[];
					int indicesCode[] = { 1 };
					int indicesDesc[] = { 2 };
					int indicesCodeDesc[] = { 1, 2 };
					
					if (searchCodeCLRadio.isSelected())
						indices = indicesCode;
					else if (searchDescCLRadio.isSelected())
						indices = indicesDesc;
					else
						indices = indicesCodeDesc;

					((TableRowSorter<?>) codesTable.getRowSorter()).setRowFilter(RowFilter.regexFilter(searchPattern, indices));
					updateCodelistCount();
				} 
				catch (PatternSyntaxException e) 
				{
			        // don't do anything if the pattern is invalid
			    }
			});

		regexSearchCLCheckbox.addActionListener(e -> {
				wholeWordCLCheckbox.setSelected(false);
				codeFilterListener.filter();
			});
		codeOptionsPane.add(regexSearchCLCheckbox);
		
		Component horizontalStrut_4_1 = Box.createHorizontalStrut(10);
		codeOptionsPane.add(horizontalStrut_4_1);
		
		caseSearchCLCheckbox.addActionListener(e -> codeFilterListener.filter());
		codeOptionsPane.add(caseSearchCLCheckbox);
		
		Component horizontalStrut_4_1_1 = Box.createHorizontalStrut(10);
		codeOptionsPane.add(horizontalStrut_4_1_1);
		
		wholeWordCLCheckbox.addActionListener(e -> {
				regexSearchCLCheckbox.setSelected(false);
				codeFilterListener.filter();
			});
		codeOptionsPane.add(wholeWordCLCheckbox);
		
		Component horizontalStrut_4_2 = Box.createHorizontalStrut(10);
		codeOptionsPane.add(horizontalStrut_4_2);
		
		searchCodeCLRadio.addActionListener(e -> codeFilterListener.filter());
		codeSearchRadioGroup.add(searchCodeCLRadio);
		codeOptionsPane.add(searchCodeCLRadio);
		
		Component horizontalStrut_4_3 = Box.createHorizontalStrut(10);
		codeOptionsPane.add(horizontalStrut_4_3);
		
		searchDescCLRadio.addActionListener(e -> codeFilterListener.filter());
		codeSearchRadioGroup.add(searchDescCLRadio);
		codeOptionsPane.add(searchDescCLRadio);
		
		Component horizontalStrut_4_4 = Box.createHorizontalStrut(10);
		codeOptionsPane.add(horizontalStrut_4_4);
		
		searchBothCLRadio.addActionListener(e -> codeFilterListener.filter());
		codeSearchRadioGroup.add(searchBothCLRadio);
		searchBothCLRadio.setSelected(true);
		codeOptionsPane.add(searchBothCLRadio);

		JScrollPane codelistScrollPane = new JScrollPane();
		codesPanel.add(codelistScrollPane);

		codesFilterTextField.setForeground(Color.RED);
		codesFilterTextField.setFont(new Font("Dialog", Font.BOLD, 16));
		codesFilterTextField.getDocument().addDocumentListener(codeFilterListener);
		codesFilterBox.add(codesFilterTextField);

		JButton toggleVisibleButton = new JButton("Toggle unfiltered rows");
		toggleVisibleButton.addActionListener(e -> {
					for (int i = 0; i < codesTable.getRowCount(); i++)
						codesTable.setValueAt(
								!(Boolean) codesTable.getValueAt(i, codesTable.convertColumnIndexToView(0)), i,
								codesTable.convertColumnIndexToView(0));
					updateCodelistCount();
				});
		codesFilterBox.add(toggleVisibleButton);
			
		codesTable.setAutoCreateColumnsFromModel(false);
		DefaultTableColumnModel codesTableColumnModel = new DefaultTableColumnModel();
		TableColumn columns[] = new TableColumn[] { new TableColumn(0), new TableColumn(1), new TableColumn(2) };
		String columnHeaders[] = { "", "Code ID", "Code Description" };
		int minWidths[] =        { 30, 100,       200 };
		int maxWidths[] =        { 30, MAX_VALUE, MAX_VALUE };
		int preferredWidths[] =  { 30, 100,       400 };
		for (int i = 0; i < columns.length; i++)
		{
			columns[i].setHeaderValue(columnHeaders[i]);
			columns[i].setMinWidth(minWidths[i]);
			columns[i].setMaxWidth(maxWidths[i]);
			columns[i].setPreferredWidth(preferredWidths[i]);
			columns[i].setResizable(i != 0);
			codesTableColumnModel.addColumn(columns[i]);
		}
		codesTable.setColumnModel(codesTableColumnModel);
		codesTable.getTableHeader().setReorderingAllowed(false);
		codesTable.getTableHeader().setResizingAllowed(true);
		codelistScrollPane.setViewportView(codesTable);

		dimTableSorter.setSortKeys(singletonList(new SortKey(0, SortOrder.ASCENDING)));

		dimensionsTable = new JTable();
		dimensionsTable.setModel(dimsTableModel);
		dimensionsTable.getColumnModel().getColumn(0).setMinWidth(20);
		dimensionsTable.getColumnModel().getColumn(0).setMaxWidth(20);
		dimensionsTable.getColumnModel().getColumn(1).setMinWidth(150);
		dimensionsTable.getColumnModel().getColumn(1).setMaxWidth(Integer.MAX_VALUE);
		dimensionsTable.getColumnModel().getColumn(2).setMinWidth(200);
		dimensionsTable.getColumnModel().getColumn(2).setMaxWidth(Integer.MAX_VALUE);
		dimensionsTable.getColumnModel().getColumn(2).setPreferredWidth(400);
		dimensionsTable.setRowSorter(dimTableSorter);
		dimensionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		dimensionsTable.getSelectionModel().addListSelectionListener(this::dimSelListener);
		dimensionsScrollPane.setViewportView(dimensionsTable);

		dimensionFilterTextField.setFont(new Font(null, Font.BOLD, 16));
		dimensionFilterTextField.setForeground(Color.RED);
		dimensionFilterTextField.getDocument().addDocumentListener(new DoFilterListener(dimensionFilterTextField, 
				pattern -> dimTableSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(pattern)))));
		dimensionFilterBox.add(dimensionFilterTextField);

		// Dataflow table setup
		dataflowsTable.setModel(dataflowsTableModel);
		dataflowsTable.getColumnModel().getColumn(0).setMinWidth(120);
		dataflowsTable.getColumnModel().getColumn(0).setMaxWidth(Integer.MAX_VALUE);
		dataflowsTable.getColumnModel().getColumn(1).setMinWidth(60);
		dataflowsTable.getColumnModel().getColumn(1).setMaxWidth(60);
		dataflowsTable.getColumnModel().getColumn(2).setMinWidth(120);
		dataflowsTable.getColumnModel().getColumn(2).setMaxWidth(Integer.MAX_VALUE);
		dataflowsTable.getColumnModel().getColumn(3).setMinWidth(60);
		dataflowsTable.getColumnModel().getColumn(3).setMaxWidth(60);
		dataflowsTable.getColumnModel().getColumn(4).setMinWidth(60);
		dataflowsTable.getColumnModel().getColumn(4).setMaxWidth(60);
		dataflowsTable.getColumnModel().getColumn(5).setMinWidth(200);
		dataflowsTable.getColumnModel().getColumn(5).setMaxWidth(Integer.MAX_VALUE);
		dataflowsTable.getColumnModel().getColumn(5).setPreferredWidth(800);
		dataflowsTable.setRowSorter(dataflowsTableSorter);
		dataflowsTable.setAutoCreateColumnsFromModel(false);
		dataflowsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		dataflowsTable.getSelectionModel().addListSelectionListener(this::flowSelListener);
		
		Box flowOptionsPane = Box.createHorizontalBox();
		flowOptionsPane.setAlignmentY(Component.CENTER_ALIGNMENT);
		flowOptionsPane.setPreferredSize(new java.awt.Dimension(10, 25));
		flowOptionsPane.setMinimumSize(new java.awt.Dimension(10, 25));
		flowOptionsPane.setMaximumSize(new java.awt.Dimension(32768, 25));
		dataflowsPanel.add(flowOptionsPane);
		
		regexSearchFlowCheckbox.addActionListener(e -> {
				caseSearchFlowCheckbox.setEnabled(false);
				flowListener.filter();
			});
		flowOptionsPane.add(regexSearchFlowCheckbox);
		
		Component horizontalStrut_4_1_2 = Box.createHorizontalStrut(10);
		flowOptionsPane.add(horizontalStrut_4_1_2);
		
		caseSearchFlowCheckbox.addActionListener(e -> {
				regexSearchFlowCheckbox.setEnabled(false);
				flowListener.filter();
			});
		flowOptionsPane.add(caseSearchFlowCheckbox);
		
		Component horizontalStrut_4_1_1_1 = Box.createHorizontalStrut(10);
		flowOptionsPane.add(horizontalStrut_4_1_1_1);
		
		wholeWordFlowCheckbox.addActionListener(e -> flowListener.filter());
		flowOptionsPane.add(wholeWordFlowCheckbox);
		
		Component horizontalStrut_4_2_1 = Box.createHorizontalStrut(10);
		flowOptionsPane.add(horizontalStrut_4_2_1);
		
		searchCodeFlowRadio.addActionListener(e -> flowListener.filter());
		flowSearchRadioGroup.add(searchCodeFlowRadio);
		flowOptionsPane.add(searchCodeFlowRadio);
		
		Component horizontalStrut_4_3_1 = Box.createHorizontalStrut(10);
		flowOptionsPane.add(horizontalStrut_4_3_1);
		
		searchDSDFlowRadio.addActionListener(e -> flowListener.filter());
		flowSearchRadioGroup.add(searchDSDFlowRadio);
		flowOptionsPane.add(searchDSDFlowRadio);
		
		Component horizontalStrut_4_4_1 = Box.createHorizontalStrut(10);
		flowOptionsPane.add(horizontalStrut_4_4_1);
		
		searchDescFlowRadio.addActionListener(e -> flowListener.filter());
		flowSearchRadioGroup.add(searchDescFlowRadio);
		flowOptionsPane.add(searchDescFlowRadio);
		
		Component horizontalStrut_4_3_1_1 = Box.createHorizontalStrut(10);
		flowOptionsPane.add(horizontalStrut_4_3_1_1);
		
		searchAllFlowRadio.addActionListener(e -> flowListener.filter());
		flowSearchRadioGroup.add(searchAllFlowRadio);
		searchAllFlowRadio.setSelected(true);
		flowOptionsPane.add(searchAllFlowRadio);

		JScrollPane dataflowsScrollPane = new JScrollPane();
		dataflowsScrollPane.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		dataflowsScrollPane.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
		dataflowsScrollPane.setViewportView(dataflowsTable);
		dataflowsPanel.add(dataflowsScrollPane);

		JButton btnClearSelectedDimension = new JButton("Clear selected dimension");
		btnClearSelectedDimension.addActionListener(e -> {
					if (getSelectedDataflow() != null)
					{
						CheckboxListTableModel<?> model = (CheckboxListTableModel<?>) codesTable.getModel();
						model.uncheckAll();
						updateCodelistCount();
					}
				});
		btnClearSelectedDimension.setMaximumSize(new java.awt.Dimension(151, 32768));
		dimensionFilterBox.add(btnClearSelectedDimension);

		JTextPane loggingArea = new JTextPane();
		loggingArea.setBorder(new EmptyBorder(0, 0, 0, 0));
		loggingArea.setBackground(new Color(224, 224, 224));
		Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);
		loggingArea.setFont(font);
		loggingArea.setEditable(false);
		LOGGER.addHandler(new HelperHandler(loggingArea));

		JScrollPane loggingPane = new JScrollPane(loggingArea);
		loggingPane.setBorder(new EmptyBorder(0, 0, 0, 0));

		final JSplitPane mainSplitPane = new JSplitPane();
		mainSplitPane.setBorder(null);
		mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		mainSplitPane.setLeftComponent(controlsPane);
		mainSplitPane.setRightComponent(loggingPane);
		setContentPane(mainSplitPane);

		providersSetup(providersMenu);

		if (lockedProvider != null)
		{
			for (AbstractButton menuItem : Collections.list(selectedProviderGroup.getElements()))
				if (lockedProvider.equalsIgnoreCase(menuItem.getActionCommand()))
				{
					selectedProviderGroup.setSelected(menuItem.getModel(), true);
					menuItem.doClick();
				}

			finalqueryPanel.add(horizontalStrut_5);
			finalqueryPanel.add(btnPrintQuery);
			providersMenu.setEnabled(false);
			providersMenu.setText(providersMenu.getText() + ": " + lockedProvider);
		}

		
		setLocationRelativeTo(null);
		setVisible(true);
		SwingUtilities.invokeLater(() -> {
				controlsPane.setDividerLocation(0.75);
				mainSplitPane.setDividerLocation(0.8);
			});
	}

	private void flowSelListener(ListSelectionEvent e)
	{
		String dataflowID = getSelectedDataflow();
		if (!e.getValueIsAdjusting() && dataflowID != null)
		{
			updateDataflow(dataflowID);
			dimensionFilterTextField.setText("");
			dimTableSorter.setRowFilter(null);
			checkQueryButton.setEnabled(true);
			btnPrintQuery.setEnabled(true);
		}
	}

	private void dimSelListener(ListSelectionEvent e)
	{
		String selectedDimension = getSelectedDimension();
		String provider = selectedProviderGroup.getSelection().getActionCommand();

		if (e.getValueIsAdjusting() || selectedDimension == null)
			return;

		List<? extends SortKey> sortingKeys = codesTable.getRowSorter() == null ? null
					: codesTable.getRowSorter().getSortKeys();
		String selectedDataflow = getSelectedDataflow();
		AtomicBoolean interrupted = new AtomicBoolean(false);
		
//		if (!codelistSortersMap.containsKey(selectedDimension))
			new ProgressViewer<>(this, interrupted, () -> 
				SDMXClientFactory.getProviders().get(provider).getSdmxVersion().equals(SDMXClientFactory.SDMX_V3) ?
					SdmxClientHandler.filterCodes(provider, selectedDataflow, createAvailabilityFilter()).get(selectedDimension) 
					:
					getCodes(provider, selectedDataflow, selectedDimension)	
					,
				codes -> {
					CheckboxListTableModel<String> model = null;
					if(!codelistSortersMap.containsKey(selectedDimension)){
						model = new CheckboxListTableModel<String>("Code ID", "Code Description");
						model.addTableModelListener(event -> sdmxQueryTextField.setText(createSDMXQuery(selectedDataflow, dimsTableModel.getSource())));
					}
					else{
						model = codelistSortersMap.get(selectedDimension).getModel();
					}
					
					model.setItems(codes);
					
					TableRowSorter<CheckboxListTableModel<String>> sorter = new TableRowSorter<>(model);
					codelistSortersMap.put(selectedDimension, sorter);
				},
				ex -> {
					interrupted.set(true);
					LOGGER.severe("Exception. Class: " + ex.getClass().getName()
							+ " .Message: " + ex.getMessage());
					LOGGER.log(Level.FINER, "", ex);
				}
			).start();

		if (!interrupted.get())
			SwingUtilities.invokeLater(() -> {
				TableRowSorter<CheckboxListTableModel<String>> sorter = codelistSortersMap.get(selectedDimension);
				codesTable.setModel(sorter.getModel());
				codesTable.setRowSorter(sorter);
				if (sortingKeys == null || sortingKeys.isEmpty())
					sorter.setSortKeys(singletonList(new SortKey(1, ASCENDING)));
				else
					sorter.setSortKeys(sortingKeys);
				sorter.setSortable(codesTable.convertColumnIndexToView(0), false);
				sorter.setRowFilter(null);
				
				codesFilterTextField.setText("");
				codesTable.revalidate();
				updateCodelistCount();
			});
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
		// reset clean state for codesTable before
		codelistSortersMap.clear();
		codesTable.setModel(new CheckboxListTableModel<String>("Code ID", "Code Description"));
		updateCodelistCount();
		dimsTableModel.clear();
		// if this is not a provider switch
		new ProgressViewer<>(this, new AtomicBoolean(false),
				() -> SdmxClientHandler.getDimensions(selectedProviderGroup.getSelection().getActionCommand(), dataflowID),
				dims -> {
					dimsTableModel.setItems(dims, item -> new String[] { item.getId(), item.getName() });
					sdmxQueryTextField.setText(createSDMXQuery(dataflowID, dims));
				},
				ex -> {
						LOGGER.severe("Exception. Class: " + ex.getClass().getName() + " .Message: " + ex.getMessage());
						LOGGER.log(Level.FINER, "", ex);
					}).start();
	}

	private void displayQueryResults()
	{
		String query = sdmxQueryTextField.getText();
		ButtonModel providerMenu = selectedProviderGroup.getSelection();
		String selectedDataflow = getSelectedDataflow();
		String selectedProvider = providerMenu.getActionCommand();
		AtomicBoolean isCancelled = new AtomicBoolean(false);
		
		if (selectedDataflow != null && query != null && !query.isEmpty())
			new ProgressViewer<>(SDMXHelper.this, isCancelled, 
				() -> { 
					try
					{
						Dataflow dataflow = getFlow(selectedProvider, selectedDataflow);
						List<PortableTimeSeries<Double>> names = getTimeSeries(selectedProvider, null, query, null, null, null, true, null, false);
						return new SimpleEntry<>(dataflow, names);
					}
					catch (SdmxResponseException e)
					{
						if (e.getResponseCode() != 100)
							throw e;
						
						isCancelled.set(true);
						JOptionPane.showMessageDialog(null, "No results for selected query parameters.", "SDMX Helper", JOptionPane.WARNING_MESSAGE);
						return new SimpleEntry<Dataflow, List<PortableTimeSeries<Double>>>(null, null);
					}
				},
				entry -> {
					if (!isCancelled.get())
					{
						Dataflow df = entry.getKey();
						List<PortableTimeSeries<Double>> result = entry.getValue();
						
						// Open a new window to browse query results
						final JFrame wnd = new ResultsFrame(getCurrentProvider(), result);

						wnd.setTitle(result.size() + " results" + " - " + df.getDescription());
						wnd.setVisible(true);
						wnd.toFront();
					}
				},
				ex -> {
					LOGGER.severe("Exception. Class: " + ex.getClass().getName() + " .Message: " + ex.getMessage());
					LOGGER.log(Level.FINER, "", ex);
				}).start();
	}

	private void providersSetup(final JMenu providersMenu)
	{
		for (final Entry<String, Provider> providerEntry : SDMXClientFactory.getProviders().entrySet())
		{
			final String provider = providerEntry.getKey();
			final String sdmxVersion = providerEntry.getValue().getSdmxVersion();
			final JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(
					"[" + sdmxVersion + "] " + provider + ": " + providerEntry.getValue().getDescription());
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
	 * @param provider The selected provider
	 */
	private void updateSource(final String provider)
	{
		final AtomicBoolean isCancelled = new AtomicBoolean(false);
		new ProgressViewer<>(this, isCancelled, () -> SdmxClientHandler.getFlowObjects(provider, null),
			flows -> {
				if (!isCancelled.get())
				{
					codelistSortersMap.clear();
					codesTable.setModel(new CheckboxListTableModel<String>("Code ID", "Code Description"));
					dimsTableModel.clear();
					dataflowsTableModel.setItems(flows);
					TableRowSorter<DataflowsModel> rowSorter = new TableRowSorter<>(dataflowsTableModel);
					rowSorter.setSortKeys(singletonList(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
					dataflowsTable.setRowSorter(rowSorter);										
				}
			},
			ex -> {
				LOGGER.severe("Exception. Class: " + ex.getClass().getName() + " .Message: " + ex.getMessage());
				LOGGER.log(Level.FINER, "", ex);
			}).start();
	}

	private String createSDMXQuery(String dataflow, List<Dimension> dims)
	{
		StringBuffer query = new StringBuffer(dataflow + "/");
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

		return query.toString();
	}
	
	private String createAvailabilityFilter() throws SdmxException
	{
		StringBuffer query = new StringBuffer();
		boolean firstDim = true;
		List<Dimension> dims = SdmxClientHandler.getDimensions(getCurrentProvider(), getSelectedDataflow()); 
		for (Dimension dim : dims)
		{	
			if (codelistSortersMap.containsKey(dim.getId()))
			{
				CheckboxListTableModel<String> model = codelistSortersMap.get(dim.getId()).getModel();
				StringBuffer subquery = new StringBuffer(!firstDim ? "&" : "");
				subquery.append("c[" + dim.getId() + "]=");
				boolean firstCode = true;
				for (String code : model.getCheckedCodes())
				{
					if (!firstCode)
						subquery.append(",");
					subquery.append(code);
					firstCode = false;
				}
				if(!firstCode){
					query.append(subquery);
				}
			}
			firstDim = false;
		}

		return query.toString();
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
