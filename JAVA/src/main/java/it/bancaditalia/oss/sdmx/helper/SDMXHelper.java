package it.bancaditalia.oss.sdmx.helper;

import static it.bancaditalia.oss.sdmx.api.SDMXVersion.V3;
import static it.bancaditalia.oss.sdmx.client.Provider.AuthenticationMethods.NONE;
import static it.bancaditalia.oss.sdmx.client.SdmxClientHandler.getCodes;
import static it.bancaditalia.oss.sdmx.client.SdmxClientHandler.getFlow;
import static it.bancaditalia.oss.sdmx.client.SdmxClientHandler.getTimeSeries;
import static it.bancaditalia.oss.sdmx.client.SdmxClientHandler.getTimeSeries2;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Collections.singletonList;
import static java.util.Locale.forLanguageTag;
import static java.util.ResourceBundle.getBundle;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static java.util.stream.Collectors.joining;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.SortOrder.ASCENDING;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
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
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultEditorKit;

import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.Dimension;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.api.SDMXVersion;
import it.bancaditalia.oss.sdmx.client.Provider;
import it.bancaditalia.oss.sdmx.client.SDMXClientFactory;
import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxResponseException;
import it.bancaditalia.oss.sdmx.util.Configuration;

public class SDMXHelper extends JFrame
{
	private static final String TOOLTIP_FORMAT = "<html><p style='text-align: center'>%1$tF<br />%1$tT.%1$tL</p></html>";

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
			final ExceptionInInitializerError e1 = new ExceptionInInitializerError("Operating system must provide a valid graphic environment."); //$NON-NLS-1$
			e1.initCause(e);
			throw e1;
		}
		
		try
		{
			ICON_MIN = ImageIO.read(SDMXHelper.class.getResourceAsStream("min.png")); //$NON-NLS-1$
			ICON_MAX = ImageIO.read(SDMXHelper.class.getResourceAsStream("max.png")); //$NON-NLS-1$
		}
		catch (Exception e)
		{
			final AtomicBoolean finished = new AtomicBoolean(false);
			SwingUtilities.invokeLater(() -> {
					JOptionPane.showMessageDialog(null, "Cannot load resources from jar file.", "SDMXHelper", ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
					finished.set(true);
				});
			
			while (!finished.get() && !Thread.currentThread().isInterrupted())
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
			"Opens the SDMX Helper navigation tool window.", //$NON-NLS-1$
			"", "java " + SDMXHelper.class.getName() + " [-s <provider>]", "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"    -s    Enable \"print query\" button and lock SDMXHelper on the specified provider." }; //$NON-NLS-1$

	private final JLabel lblQuery = new JLabel();
	private final JTextField tfSdmxQuery = new JTextField();
	private final JTextField tfCodesFilter = new JTextField();
	private final JTextField tfDataflowFilter = new JTextField();
	private final JTable tblDataflows = new JTable();
	private final JLabel lblCodelist = new JLabel();
	private final JTable tblDimensions = new JTable();
	private final JTable tblCodes = new JTable();
	private final JButton btnCheckQuery = new JButton();
	private final JButton btnPrintQuery = new JButton();
	private final ButtonGroup selectedProviderGroup = new ButtonGroup();
	private final DataflowsModel dataflowsTableModel = new DataflowsModel();
	private final EnumedListTableModel<Dimension> dimsTableModel = new EnumedListTableModel<>();
	private final HashMap<String, TableRowSorter<CheckboxListTableModel<String>>> codelistSortersMap = new HashMap<>();
	private final JCheckBox cbRegexSearchCL = new JCheckBox();
	private final JCheckBox cbCaseSearchCL = new JCheckBox();
	private final JCheckBox cbWholeWordCL = new JCheckBox();
	private final JRadioButton rdSearchCodeCL = new JRadioButton();
	private final JRadioButton rdSearchDescCL = new JRadioButton();
	private final JRadioButton rdSearchBothCL = new JRadioButton();
	private final ButtonGroup codeSearchRadioGroup = new ButtonGroup();
	private final JRadioButton rdSearchCodeFlow = new JRadioButton();
	private final JRadioButton rdSearchDSDFlow = new JRadioButton();
	private final JRadioButton rdSearchDescFlow = new JRadioButton();
	private final JRadioButton rdSearchAllFlow = new JRadioButton();
	private final JCheckBox cbWholeWordFlow = new JCheckBox();
	private final JCheckBox cbCaseSearchFlow = new JCheckBox();
	private final JCheckBox cbRegexSearchFlow = new JCheckBox();
	private final ButtonGroup flowSearchRadioGroup = new ButtonGroup();
	private final JTextField tfDimensionFilter = new JTextField();
	private final TableRowSorter<EnumedListTableModel<Dimension>> dimTableSorter = new TableRowSorter<>(dimsTableModel);
	private final JMenuItem mntmAddProvider = new JMenuItem();
	private final JMenuItem mntmBuildCommands = new JMenuItem();
	private final JMenuItem mntmCopySelection = new JMenuItem(COPY_ACTION);
	private final JMenu mnActions = new JMenu();
	private final JMenu mnProviders = new JMenu();
	private final JMenu mnLanguage = new JMenu(); 
	private final JMenu mnLogLevel = new JMenu();
	private final JMenu mnHelp = new JMenu();
	private final JMenuItem mntmAboutSdmxConnectors = new JMenuItem();
	private final JLabel lblDataflowFilter = new JLabel();
	private final JLabel lblDimension = new JLabel();
	private final JButton btnToggleVisible = new JButton();
	private final JButton btnClearSelectedDimension = new JButton();
	private final TitledBorder brdDataflowsPanel = createTitledBorder();
	private final TitledBorder brdFinalqueryPanel = createTitledBorder();
	private final TitledBorder brdDimensionsPanel = createTitledBorder();
	private final TitledBorder brdCodesPanel = createTitledBorder();
	private final JRadioButtonMenuItem mntmLogLevels[] = {
			new JRadioButtonMenuItem(),
			new JRadioButtonMenuItem(),
			new JRadioButtonMenuItem("", true), //$NON-NLS-1$
			new JRadioButtonMenuItem(),
			new JRadioButtonMenuItem()
		};
	private String noResultsMessage;
	private String resultsCountMessage;

	/**
	  	if this variable is false, the updateSeriesCounts() does nothing, it must be set to false when clearing the tlbCodes
	 	to avoid uneccessary newtork call by the listener function.
	 	Apparently Swing doesn't allow to disable listener call temporarily unless you save the Listener Object somewhere
	 	and disable it by passing the Listener object by reference, which is incompatible to the current way we organize our table.
	 	setting a global kill switch is the only way to achieve this behaviour, the listener function must be aware of this
	 	variable and if set to false not execute the code.
	 	so everytime we modify and/or clear tlbCodes in a V3 provider we need to disable the listener so that updateSeriesCounts()
	 	does not call the webservice to update the seriesCount and the ObsCount for each code that we are clearing.
	 */
	private boolean isCodelistSortersMapTablesListenerActive;

	private static class SeriesCountPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		
		private final JLabel seriesCountLabel;
		private final JTextField seriesCount;
		private final JLabel obsCountLabel;
		private final JTextField obsCount;

		public SeriesCountPanel(int seriesCount, int obsCount)
		{
			this.seriesCountLabel = new JLabel();
			this.seriesCount = new JTextField(Integer.toString(seriesCount));
			this.seriesCount.setEditable(false);
			this.seriesCount.setMinimumSize(new java.awt.Dimension(100, 33));
			this.obsCountLabel = new JLabel();
			this.obsCount = new JTextField(Integer.toString(obsCount));
			this.obsCount.setEditable(false);
			this.obsCount.setMinimumSize(new java.awt.Dimension(100, 33));
			add(seriesCountLabel);
			add(this.seriesCount);
			add(obsCountLabel);
			add(this.obsCount);
		}

		public void updateCounts(int seriesCount, int obsCount)
		{
			this.seriesCount.setText(Integer.toString(seriesCount));
			this.seriesCount.setCaretPosition(0);
			this.obsCount.setText(Integer.toString(obsCount));
			this.obsCount.setCaretPosition(0);
			if (seriesCount <= 0)
				hideSeriesCount();
			else
				showSeriesCount();

			if (obsCount <= 0)
				hideObsCount();
			else
				showObsCount();
		}

		public void updateBundle(ResourceBundle b)
		{
			this.seriesCountLabel.setText(b.getString("SDMXHelper.105"));
			this.obsCountLabel.setText(b.getString("SDMXHelper.106"));
		}

		public void hidePanel()
		{
			this.seriesCount.setVisible(false);
			this.seriesCountLabel.setVisible(false);
			this.obsCount.setVisible(false);
			this.obsCountLabel.setVisible(false);
			this.setVisible(false);
		}

		public void showPanel() {
			this.seriesCount.setVisible(true);
			this.seriesCountLabel.setVisible(true);
			this.obsCount.setVisible(true);
			this.obsCountLabel.setVisible(true);
			this.setVisible(true);
		}

		public void hideSeriesCount() {
			this.seriesCountLabel.setVisible(false);
			this.seriesCount.setVisible(false);
		}

		public void showSeriesCount() {
			this.seriesCountLabel.setVisible(true);
			this.seriesCount.setVisible(true);
		}

		public void hideObsCount() {
			this.obsCountLabel.setVisible(false);
			this.obsCount.setVisible(false);
		}

		public void showObsCount() {
			this.obsCountLabel.setVisible(true);
			this.obsCount.setVisible(true);
		}

	}

	private final SeriesCountPanel seriesCountPanel;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		String lockedProvider = null;

		if (args.length > 0 && (args.length == 1 || args.length > 2 || !"-s".equals(args[0]))) //$NON-NLS-1$
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

	/**
	 * Create the frame.
	 */
	public SDMXHelper(boolean exitOnClose, String lockedProvider)
	{
		String preferredLanguage = Configuration.getLanguages().iterator().next().getRange();
		ResourceBundle bundle = getBundle("it.bancaditalia.oss.sdmx.helper.bundles.HelperResources", forLanguageTag(preferredLanguage));
		if (lockedProvider != null)
			if ("".equals(lockedProvider.trim())) //$NON-NLS-1$
				lockedProvider = null;
			else
				LOGGER.info(bundle.getString("SDMXHelper.28") + lockedProvider); //$NON-NLS-1$
		
		setSize(1024, 768);
		setDefaultCloseOperation(exitOnClose ? JFrame.EXIT_ON_CLOSE : JFrame.HIDE_ON_CLOSE);

		final JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		menuBar.add(mnProviders);

		mnActions.setMnemonic(KeyEvent.VK_A);
		menuBar.add(mnActions);

		mntmCopySelection.setActionCommand(bundle.getString("SDMXHelper.33")); //$NON-NLS-1$
		mntmCopySelection.setMnemonic(KeyEvent.VK_COPY);
		mnActions.add(mntmCopySelection);

		mntmBuildCommands.setMnemonic(KeyEvent.VK_B);
		mntmBuildCommands.addActionListener(paramActionEvent -> {
				try
				{
					if (selectedProviderGroup.getSelection() != null)
						new ToolCommandsFrame(getSelectedDataflow(), createFilter(),
								selectedProviderGroup.getSelection().getActionCommand());
				} 
				catch (SdmxException ex)
				{
					LOGGER.severe("Exception. Class: " + ex.getClass().getName() + " .Message: " + ex.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
					LOGGER.log(Level.FINER, "", ex); //$NON-NLS-1$
				}
			});

		mntmAddProvider.addActionListener(event -> {
					NewProviderDialog newProviderDialog = new NewProviderDialog();
					if (newProviderDialog.getResult() == JOptionPane.OK_OPTION)
						try
						{
							String name = newProviderDialog.getName();
							String description = newProviderDialog.getDescription();
							SDMXVersion sdmxVersion = newProviderDialog.getSdmxVersion();
							URI endpoint = new URI(newProviderDialog.getURL());
							boolean availabilityQueries = newProviderDialog.getAvailabilityFlag().equalsIgnoreCase("true");
							SDMXClientFactory.addProvider(name, endpoint, NONE, false, true, availabilityQueries, description, sdmxVersion);
							mnProviders.removeAll();
							providersSetup(mnProviders);
						} 
						catch (SdmxException | URISyntaxException e)
						{
							e.printStackTrace();
						}
				});
		mntmAddProvider.setMnemonic(KeyEvent.VK_P);

		ButtonGroup langGroup = new ButtonGroup();
		HashSet<String> guiLanguages = new HashSet<>();
		guiLanguages.add("en"); //$NON-NLS-1$
		guiLanguages.add("it"); //$NON-NLS-1$
		guiLanguages.add("fr"); //$NON-NLS-1$
		for (LanguageRange r: LanguageRange.parse("en,de,fr,it"))
		{
			final String langCode = r.getRange();
			Locale locale = Locale.forLanguageTag(langCode);
			ResourceBundle localBundle = getBundle("it.bancaditalia.oss.sdmx.helper.bundles.HelperResources", locale);
			String langName = locale.getDisplayLanguage(locale).substring(0, 1).toUpperCase() + locale.getDisplayLanguage(locale).substring(1);
			JRadioButtonMenuItem mntmLanguage = new JRadioButtonMenuItem(
					String.format(localBundle.getString("SDMXHelper.8"), langName), langCode.equals(preferredLanguage)); //$NON-NLS-1$
			mntmLanguage.addActionListener(e -> {
				LOGGER.info(format(localBundle.getString("SDMXHelper.7"), langName)); //$NON-NLS-1$
				updateBundle(localBundle);
			});
			langGroup.add(mntmLanguage);
			mnLanguage.add(mntmLanguage);
		}

		mnActions.add(mntmBuildCommands);
		mnActions.add(new JSeparator());
		mnActions.add(mntmAddProvider);
		mnActions.add(new JSeparator());
		mnActions.add(mnLanguage);

		Level levelVals[] = { SEVERE, WARNING, INFO, FINE, FINEST }; 
		ButtonGroup logGroup = new ButtonGroup();
		for (int i = 0; i < mntmLogLevels.length; i++)
		{
			JRadioButtonMenuItem level = mntmLogLevels[i];
			Level val = levelVals[i];
			level.addActionListener(e -> Configuration.getSdmxLogger().setLevel(val));
			logGroup.add(level);
			mnLogLevel.add(level);
		}
		mnActions.add(mnLogLevel);

		menuBar.add(mnHelp);

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
		finalqueryPanel.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 0, 5), 
				new CompoundBorder(brdFinalqueryPanel, new EmptyBorder(5, 5, 5, 5))));
		queryAndFlowPanel.add(finalqueryPanel);

		lblQuery.setHorizontalAlignment(SwingConstants.RIGHT);
		lblQuery.setPreferredSize(new java.awt.Dimension(150, 14));
		lblQuery.setMinimumSize(new java.awt.Dimension(200, 14));
		lblQuery.setMaximumSize(new java.awt.Dimension(200, 14));
		lblQuery.setSize(new java.awt.Dimension(200, 14));
		finalqueryPanel.add(lblQuery);

		Component horizontalStrut = Box.createHorizontalStrut(10);
		finalqueryPanel.add(horizontalStrut);

		lblQuery.setLabelFor(tfSdmxQuery);
		tfSdmxQuery.setFont(new Font(null, Font.BOLD, 16));
		tfSdmxQuery.setEditable(false);
		finalqueryPanel.add(tfSdmxQuery);

		Component horizontalStrut_1 = Box.createHorizontalStrut(10);
		finalqueryPanel.add(horizontalStrut_1);

		btnCheckQuery.setEnabled(false);
//		btnCheckQuery.setPreferredSize(new java.awt.Dimension(170, 23));
		btnCheckQuery.setMinimumSize(new java.awt.Dimension(100, 23));
		btnCheckQuery.setMaximumSize(new java.awt.Dimension(300, 23));
		btnCheckQuery.addActionListener(e -> displayQueryResults());
		finalqueryPanel.add(btnCheckQuery);

		Component horizontalStrut_5 = Box.createHorizontalStrut(10);

		btnPrintQuery.setEnabled(false);
		btnPrintQuery.setPreferredSize(new java.awt.Dimension(240, 23));
		btnPrintQuery.setMinimumSize(new java.awt.Dimension(240, 23));
		btnPrintQuery.setMaximumSize(new java.awt.Dimension(240, 23));
		btnPrintQuery.addActionListener(e -> {
				System.out.println(tfSdmxQuery.getText());
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
		dataflowsPanel.setBorder(new CompoundBorder( new EmptyBorder(0, 5, 5, 5), 
				new CompoundBorder(brdDataflowsPanel, new EmptyBorder(10, 10, 10, 10))));
		queryAndFlowPanel.add(dataflowsPanel);
		dataflowsPanel.setLayout(new BoxLayout(dataflowsPanel, BoxLayout.Y_AXIS));

		JPanel dataflowFilterPanel = new JPanel();
		dataflowFilterPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		dataflowsPanel.add(dataflowFilterPanel);
		dataflowFilterPanel.setLayout(new BoxLayout(dataflowFilterPanel, BoxLayout.X_AXIS));

		dataflowFilterPanel.add(lblDataflowFilter);

		Component horizontalStrut_2 = Box.createHorizontalStrut(10);
		dataflowFilterPanel.add(horizontalStrut_2);

		final TableRowSorter<DataflowsModel> dataflowsTableSorter = new TableRowSorter<>(dataflowsTableModel);
		dataflowsTableSorter.setSortKeys(singletonList(new RowSorter.SortKey(0, SortOrder.ASCENDING)));

		tfDataflowFilter.setPreferredSize(new java.awt.Dimension(6, 25));
		tfDataflowFilter.setMaximumSize(new java.awt.Dimension(2147483647, 25));
		tfDataflowFilter.setFont(new Font(null, Font.BOLD, 16));
		tfDataflowFilter.setForeground(Color.RED);
			
		final DoFilterListener flowListener = new DoFilterListener(tfDataflowFilter, text -> {
				String searchPattern = cbCaseSearchFlow.isSelected() ? "": "(?i)"; //$NON-NLS-1$ //$NON-NLS-2$
				searchPattern += cbRegexSearchFlow.isSelected() ? text : Pattern.quote(text);
				searchPattern = cbWholeWordFlow.isSelected() ? "^" + searchPattern + "$" : searchPattern;   //$NON-NLS-1$ //$NON-NLS-2$
				
				try 
				{
			        Pattern.compile(searchPattern);

					final int indices[];
					int indicesCode[] = { 0 };
					int indicesDSD[] = { 2 };
					int indicesDesc[] = { 5 };
					int indicesAll[] = { 0, 2, 5 };
					
					if (rdSearchCodeFlow.isSelected())
						indices = indicesCode;
					else if (rdSearchDSDFlow.isSelected())
						indices = indicesDSD;
					else if (rdSearchDescFlow.isSelected())
						indices = indicesDesc;
					else
						indices = indicesAll;

					((TableRowSorter<?>) tblDataflows.getRowSorter()).setRowFilter(RowFilter.regexFilter(searchPattern, indices));
				} 
				catch (PatternSyntaxException e) 
				{
			        // don't do anything if the pattern is invalid
			    }
			});
		tfDataflowFilter.getDocument().addDocumentListener(flowListener);
		dataflowFilterPanel.add(tfDataflowFilter);

		Box myDimensionBox = Box.createHorizontalBox();
		myDimensionBox.setPreferredSize(new java.awt.Dimension(100, 200));
		myDimensionBox.setMinimumSize(new java.awt.Dimension(100, 200));
		myDimensionBox.setMaximumSize(new java.awt.Dimension(100, 200));

		this.seriesCountPanel = new SeriesCountPanel(0,0);
		seriesCountPanel.setMinimumSize(new java.awt.Dimension(100, 25));
		seriesCountPanel.setMaximumSize(new java.awt.Dimension(MAX_VALUE, 25));
		seriesCountPanel.setPreferredSize(new java.awt.Dimension(400, 25));
		seriesCountPanel.hidePanel();


		JSplitPane horizontalSplitPane = new JSplitPane();
		horizontalSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		controlsPane.setRightComponent(horizontalSplitPane);

		JPanel dimensionsPanel = new JPanel();
		horizontalSplitPane.setLeftComponent(dimensionsPanel);
		dimensionsPanel.setPreferredSize(new java.awt.Dimension(400, 200));
		dimensionsPanel.setMinimumSize(new java.awt.Dimension(200, 150));
		dimensionsPanel.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), 
				new CompoundBorder(brdDimensionsPanel, new EmptyBorder(10, 10, 10, 10))));
		dimensionsPanel.setLayout(new BoxLayout(dimensionsPanel, BoxLayout.Y_AXIS));


		Box dimensionFilterBox = Box.createHorizontalBox();
		dimensionFilterBox.setPreferredSize(new java.awt.Dimension(10, 25));
		dimensionFilterBox.setMinimumSize(new java.awt.Dimension(10, 25));
		dimensionFilterBox.setMaximumSize(new java.awt.Dimension(32768, 25));
		dimensionsPanel.add(dimensionFilterBox);

		dimensionFilterBox.add(lblDimension);

		Component horizontalStrut_3 = Box.createHorizontalStrut(10);
		horizontalStrut_3.setPreferredSize(new java.awt.Dimension(10, 20));
		horizontalStrut_3.setMinimumSize(new java.awt.Dimension(10, 20));
		horizontalStrut_3.setMaximumSize(new java.awt.Dimension(10, 20));
		dimensionFilterBox.add(horizontalStrut_3);

		JScrollPane dimensionsScrollPane = new JScrollPane();
		dimensionsScrollPane.setPreferredSize(new java.awt.Dimension(200, 23));
		dimensionsScrollPane.setMinimumSize(new java.awt.Dimension(200, 23));
		dimensionsPanel.add(dimensionsScrollPane);

		Box obsCountBox = Box.createHorizontalBox();
		dimensionFilterBox.setPreferredSize(new java.awt.Dimension(10, 25));
		dimensionFilterBox.setMinimumSize(new java.awt.Dimension(10, 25));
		dimensionFilterBox.setMaximumSize(new java.awt.Dimension(32768, 25));
		obsCountBox.add(seriesCountPanel);
		dimensionsPanel.add(obsCountBox);


		JPanel codesPanel = new JPanel();
		codesPanel.setSize(new java.awt.Dimension(10, 150));
		horizontalSplitPane.setRightComponent(codesPanel);
		codesPanel.setPreferredSize(new java.awt.Dimension(400, 200));
		codesPanel.setMinimumSize(new java.awt.Dimension(200, 150));
		codesPanel.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), new CompoundBorder(
					brdCodesPanel, new EmptyBorder(10, 10, 10, 10))));
		codesPanel.setLayout(new BoxLayout(codesPanel, BoxLayout.Y_AXIS));

		Box codesFilterBox = Box.createHorizontalBox();
		codesFilterBox.setPreferredSize(new java.awt.Dimension(10, 25));
		codesFilterBox.setMinimumSize(new java.awt.Dimension(10, 25));
		codesFilterBox.setMaximumSize(new java.awt.Dimension(32768, 25));
		codesPanel.add(codesFilterBox);

		codesFilterBox.add(lblCodelist);

		Component horizontalStrut_4 = Box.createHorizontalStrut(10);
		codesFilterBox.add(horizontalStrut_4);
		
		Box codeOptionsPane = Box.createHorizontalBox();
		codeOptionsPane.setPreferredSize(new java.awt.Dimension(10, 25));
		codeOptionsPane.setMinimumSize(new java.awt.Dimension(10, 25));
		codeOptionsPane.setMaximumSize(new java.awt.Dimension(32768, 25));
		codesPanel.add(codeOptionsPane);

		final DoFilterListener codeFilterListener = new DoFilterListener(tfCodesFilter, text -> {
				String searchPattern = cbCaseSearchCL.isSelected() ? "": "(?i)"; //$NON-NLS-1$ //$NON-NLS-2$
				searchPattern += cbRegexSearchCL.isSelected() ? text : Pattern.quote(text);
				searchPattern = cbWholeWordCL.isSelected() ? "^" + searchPattern + "$" : searchPattern;   //$NON-NLS-1$ //$NON-NLS-2$
				
				try 
				{
			        Pattern.compile(searchPattern);

					final int indices[];
					int indicesCode[] = { 1 };
					int indicesDesc[] = { 2 };
					int indicesCodeDesc[] = { 1, 2 };
					
					if (rdSearchCodeCL.isSelected())
						indices = indicesCode;
					else if (rdSearchDescCL.isSelected())
						indices = indicesDesc;
					else
						indices = indicesCodeDesc;

					((TableRowSorter<?>) tblCodes.getRowSorter()).setRowFilter(RowFilter.regexFilter(searchPattern, indices));
					updateCodelistCount();
				} 
				catch (PatternSyntaxException e) 
				{
			        // don't do anything if the pattern is invalid
			    }
			});

		cbRegexSearchCL.addActionListener(e -> {
				cbWholeWordCL.setSelected(false);
				codeFilterListener.filter();
			});
		codeOptionsPane.add(cbRegexSearchCL);
		
		Component horizontalStrut_4_1 = Box.createHorizontalStrut(10);
		codeOptionsPane.add(horizontalStrut_4_1);
		
		cbCaseSearchCL.addActionListener(e -> codeFilterListener.filter());
		codeOptionsPane.add(cbCaseSearchCL);
		
		Component horizontalStrut_4_1_1 = Box.createHorizontalStrut(10);
		codeOptionsPane.add(horizontalStrut_4_1_1);
		
		cbWholeWordCL.addActionListener(e -> {
				cbRegexSearchCL.setSelected(false);
				codeFilterListener.filter();
			});
		codeOptionsPane.add(cbWholeWordCL);
		
		Component horizontalStrut_4_2 = Box.createHorizontalStrut(10);
		codeOptionsPane.add(horizontalStrut_4_2);
		
		rdSearchCodeCL.addActionListener(e -> codeFilterListener.filter());
		codeSearchRadioGroup.add(rdSearchCodeCL);
		codeOptionsPane.add(rdSearchCodeCL);
		
		Component horizontalStrut_4_3 = Box.createHorizontalStrut(10);
		codeOptionsPane.add(horizontalStrut_4_3);
		
		rdSearchDescCL.addActionListener(e -> codeFilterListener.filter());
		codeSearchRadioGroup.add(rdSearchDescCL);
		codeOptionsPane.add(rdSearchDescCL);
		
		Component horizontalStrut_4_4 = Box.createHorizontalStrut(10);
		codeOptionsPane.add(horizontalStrut_4_4);
		
		rdSearchBothCL.addActionListener(e -> codeFilterListener.filter());
		codeSearchRadioGroup.add(rdSearchBothCL);
		rdSearchBothCL.setSelected(true);
		codeOptionsPane.add(rdSearchBothCL);

		JScrollPane codelistScrollPane = new JScrollPane();
		codesPanel.add(codelistScrollPane);

		tfCodesFilter.setForeground(Color.RED);
		tfCodesFilter.setFont(new Font("Dialog", Font.BOLD, 16)); //$NON-NLS-1$
		tfCodesFilter.getDocument().addDocumentListener(codeFilterListener);
		codesFilterBox.add(tfCodesFilter);

		btnToggleVisible.addActionListener(e -> {
					for (int i = 0; i < tblCodes.getRowCount(); i++)
						tblCodes.setValueAt(
								!(Boolean) tblCodes.getValueAt(i, tblCodes.convertColumnIndexToView(0)), i,
								tblCodes.convertColumnIndexToView(0));
					updateCodelistCount();
				});
		codesFilterBox.add(btnToggleVisible);
			
		tblCodes.setAutoCreateColumnsFromModel(false);
		DefaultTableColumnModel codesTableColumnModel = new DefaultTableColumnModel();
		TableColumn columns[] = new TableColumn[] { new TableColumn(0), new TableColumn(1), new TableColumn(2) };
		int minWidths[] =        { 30, 100,       200 };
		int maxWidths[] =        { 30, MAX_VALUE, MAX_VALUE };
		int preferredWidths[] =  { 30, 100,       400 };
		for (int i = 0; i < columns.length; i++)
		{
			columns[i].setMinWidth(minWidths[i]);
			columns[i].setMaxWidth(maxWidths[i]);
			columns[i].setPreferredWidth(preferredWidths[i]);
			columns[i].setResizable(i != 0);
			codesTableColumnModel.addColumn(columns[i]);
		}
		tblCodes.setColumnModel(codesTableColumnModel);
		tblCodes.getTableHeader().setReorderingAllowed(false);
		tblCodes.getTableHeader().setResizingAllowed(true);
		tblCodes.getDefaultEditor(Boolean.class).addCellEditorListener(new CellEditorListener() {
			@Override
			public void editingStopped(ChangeEvent e)
			{
				updateCodelistCount();
			}
			
			@Override
			public void editingCanceled(ChangeEvent e)
			{

			}
		});
		codelistScrollPane.setViewportView(tblCodes);

		dimTableSorter.setSortKeys(singletonList(new SortKey(0, SortOrder.ASCENDING)));

		tblDimensions.setModel(dimsTableModel);
		tblDimensions.getColumnModel().getColumn(0).setMinWidth(20);
		tblDimensions.getColumnModel().getColumn(0).setMaxWidth(20);
		tblDimensions.getColumnModel().getColumn(1).setMinWidth(150);
		tblDimensions.getColumnModel().getColumn(1).setMaxWidth(Integer.MAX_VALUE);
		tblDimensions.getColumnModel().getColumn(2).setMinWidth(200);
		tblDimensions.getColumnModel().getColumn(2).setMaxWidth(Integer.MAX_VALUE);
		tblDimensions.getColumnModel().getColumn(2).setPreferredWidth(400);
		tblDimensions.setRowSorter(dimTableSorter);
		tblDimensions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblDimensions.getSelectionModel().addListSelectionListener(this::dimSelListener);
		dimensionsScrollPane.setViewportView(tblDimensions);

		tfDimensionFilter.setFont(new Font(null, Font.BOLD, 16));
		tfDimensionFilter.setForeground(Color.RED);
		tfDimensionFilter.getDocument().addDocumentListener(new DoFilterListener(tfDimensionFilter, 
				pattern -> dimTableSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(pattern))))); //$NON-NLS-1$
		dimensionFilterBox.add(tfDimensionFilter);

		// Dataflow table setup
		tblDataflows.setModel(dataflowsTableModel);
		tblDataflows.getColumnModel().getColumn(0).setMinWidth(120);
		tblDataflows.getColumnModel().getColumn(0).setMaxWidth(Integer.MAX_VALUE);
		tblDataflows.getColumnModel().getColumn(1).setMinWidth(60);
		tblDataflows.getColumnModel().getColumn(1).setMaxWidth(60);
		tblDataflows.getColumnModel().getColumn(2).setMinWidth(120);
		tblDataflows.getColumnModel().getColumn(2).setMaxWidth(Integer.MAX_VALUE);
		tblDataflows.getColumnModel().getColumn(3).setMinWidth(60);
		tblDataflows.getColumnModel().getColumn(3).setMaxWidth(60);
		tblDataflows.getColumnModel().getColumn(4).setMinWidth(60);
		tblDataflows.getColumnModel().getColumn(4).setMaxWidth(60);
		tblDataflows.getColumnModel().getColumn(5).setMinWidth(200);
		tblDataflows.getColumnModel().getColumn(5).setMaxWidth(Integer.MAX_VALUE);
		tblDataflows.getColumnModel().getColumn(5).setPreferredWidth(800);
		tblDataflows.setRowSorter(dataflowsTableSorter);
		tblDataflows.setAutoCreateColumnsFromModel(false);
		tblDataflows.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblDataflows.getSelectionModel().addListSelectionListener(this::flowSelListener);
		
		Box flowOptionsPane = Box.createHorizontalBox();
		flowOptionsPane.setAlignmentY(Component.CENTER_ALIGNMENT);
		flowOptionsPane.setPreferredSize(new java.awt.Dimension(10, 25));
		flowOptionsPane.setMinimumSize(new java.awt.Dimension(10, 25));
		flowOptionsPane.setMaximumSize(new java.awt.Dimension(32768, 25));
		dataflowsPanel.add(flowOptionsPane);

		cbRegexSearchFlow.addActionListener(e -> {
				cbCaseSearchFlow.setEnabled(false);
				flowListener.filter();
			});
		flowOptionsPane.add(cbRegexSearchFlow);
		
		Component horizontalStrut_4_1_2 = Box.createHorizontalStrut(10);
		flowOptionsPane.add(horizontalStrut_4_1_2);
		
		cbCaseSearchFlow.addActionListener(e -> {
				cbRegexSearchFlow.setEnabled(false);
				flowListener.filter();
			});
		flowOptionsPane.add(cbCaseSearchFlow);
		
		Component horizontalStrut_4_1_1_1 = Box.createHorizontalStrut(10);
		flowOptionsPane.add(horizontalStrut_4_1_1_1);
		
		cbWholeWordFlow.addActionListener(e -> flowListener.filter());
		flowOptionsPane.add(cbWholeWordFlow);
		
		Component horizontalStrut_4_2_1 = Box.createHorizontalStrut(10);
		flowOptionsPane.add(horizontalStrut_4_2_1);
		
		rdSearchCodeFlow.addActionListener(e -> flowListener.filter());
		flowSearchRadioGroup.add(rdSearchCodeFlow);
		flowOptionsPane.add(rdSearchCodeFlow);
		
		Component horizontalStrut_4_3_1 = Box.createHorizontalStrut(10);
		flowOptionsPane.add(horizontalStrut_4_3_1);
		
		rdSearchDSDFlow.addActionListener(e -> flowListener.filter());
		flowSearchRadioGroup.add(rdSearchDSDFlow);
		flowOptionsPane.add(rdSearchDSDFlow);
		
		Component horizontalStrut_4_4_1 = Box.createHorizontalStrut(10);
		flowOptionsPane.add(horizontalStrut_4_4_1);
		
		rdSearchDescFlow.addActionListener(e -> flowListener.filter());
		flowSearchRadioGroup.add(rdSearchDescFlow);
		flowOptionsPane.add(rdSearchDescFlow);
		
		Component horizontalStrut_4_3_1_1 = Box.createHorizontalStrut(10);
		flowOptionsPane.add(horizontalStrut_4_3_1_1);
		
		rdSearchAllFlow.addActionListener(e -> flowListener.filter());
		flowSearchRadioGroup.add(rdSearchAllFlow);
		rdSearchAllFlow.setSelected(true);
		flowOptionsPane.add(rdSearchAllFlow);

		JScrollPane dataflowsScrollPane = new JScrollPane();
		dataflowsScrollPane.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		dataflowsScrollPane.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
		dataflowsScrollPane.setViewportView(tblDataflows);
		dataflowsPanel.add(dataflowsScrollPane);

		btnClearSelectedDimension.addActionListener(e -> {
					if (getSelectedDataflow() != null)
					{
						isCodelistSortersMapTablesListenerActive = false;
						((CheckboxListTableModel<?>) tblCodes.getModel()).uncheckAll();
						updateCodelistCount();
						isCodelistSortersMapTablesListenerActive = true;
						if (V3 == SDMXClientFactory.getProviders().get(getSelectedProvider()).getSdmxVersion()) {
							updateSeriesCounts(getSelectedDataflow(), new ArrayList<>());
						}
					}
				});
		btnClearSelectedDimension.setMaximumSize(new java.awt.Dimension(151, 32768));
		dimensionFilterBox.add(btnClearSelectedDimension);

		JTextPane loggingArea = new JTextPane() {
				private static final long serialVersionUID = 1L;

				public String getToolTipText(MouseEvent e)
				{
					int pos = viewToModel(e.getPoint());
					if (pos >= 0)
					{
						AttributeSet attrs = getStyledDocument().getCharacterElement(pos).getAttributes();
						return attrs.getAttribute("TIME") == null ? null : String.format(TOOLTIP_FORMAT, new Date());
					}
					else
						return null;
				}
			};
		loggingArea.setBorder(new EmptyBorder(0, 0, 0, 0));
		loggingArea.setBackground(new Color(224, 224, 224));
		Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);
		loggingArea.setFont(font);
		loggingArea.setEditable(false);
		ToolTipManager.sharedInstance().registerComponent(loggingArea);
		loggingArea.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent e)
			{
				int pos = loggingArea.viewToModel(e.getPoint());
				if (pos >= 0)
				{
					AttributeSet attrs = loggingArea.getStyledDocument().getCharacterElement(pos).getAttributes();
					Object link = attrs.getAttribute("URL");
					if (link != null)
						try
						{
							Desktop.getDesktop().browse(new URI(link.toString()));
						}
						catch (IOException | URISyntaxException e1)
						{
							
						}
				}
			}
		});
		LOGGER.addHandler(new HelperHandler(loggingArea));

		JPanel noWrapPanel = new JPanel(new BorderLayout());
		noWrapPanel.add(loggingArea);
		
		JScrollPane loggingPane = new JScrollPane(noWrapPanel);
		loggingPane.setBorder(new EmptyBorder(0, 0, 0, 0));


		final JSplitPane mainSplitPane = new JSplitPane();
		mainSplitPane.setBorder(null);
		mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		mainSplitPane.setLeftComponent(controlsPane);
		mainSplitPane.setRightComponent(loggingPane);
		setContentPane(mainSplitPane);

		providersSetup(mnProviders);

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
			mnProviders.setEnabled(false);
			mnProviders.setText(mnProviders.getText() + ": " + lockedProvider); //$NON-NLS-1$
		}

		updateBundle(bundle);
		
		setLocationRelativeTo(null);
		setVisible(true);
		SwingUtilities.invokeLater(() -> {
				controlsPane.setDividerLocation(0.75);
				mainSplitPane.setDividerLocation(0.8);
			});
	}

	private void updateBundle(ResourceBundle b)
	{
		setTitle(b.getString("SDMXHelper.30")); //$NON-NLS-1$
		Configuration.setLanguages(b.getLocale().toLanguageTag() + ",en;q=0.8,*;q=0.6"); //$NON-NLS-1$
		brdCodesPanel.setTitle(b.getString("SDMXHelper.60")); //$NON-NLS-1$
		brdDataflowsPanel.setTitle(b.getString("SDMXHelper.52")); //$NON-NLS-1$
		brdDimensionsPanel.setTitle(b.getString("SDMXHelper.58")); //$NON-NLS-1$
		brdFinalqueryPanel.setTitle(b.getString("SDMXHelper.48")); //$NON-NLS-1$
		btnCheckQuery.putClientProperty("FORMAT", b.getString("SDMXHelper.50")); //$NON-NLS-1$
		btnCheckQuery.setText(String.format(b.getString("SDMXHelper.50"), "")); //$NON-NLS-1$ //$NON-NLS-2$
		btnClearSelectedDimension.setText(b.getString("SDMXHelper.72")); //$NON-NLS-1$
		btnPrintQuery.setText(b.getString("SDMXHelper.51")); //$NON-NLS-1$
		btnToggleVisible.setText(b.getString("SDMXHelper.67")); //$NON-NLS-1$
		cbCaseSearchCL.setText(b.getString("SDMXHelper.14")); //$NON-NLS-1$
		cbCaseSearchFlow.setText(b.getString("SDMXHelper.24")); //$NON-NLS-1$
		cbRegexSearchCL.setText(b.getString("SDMXHelper.13")); //$NON-NLS-1$
		cbRegexSearchFlow.setText(b.getString("SDMXHelper.25")); //$NON-NLS-1$
		cbWholeWordCL.setText(b.getString("SDMXHelper.15")); //$NON-NLS-1$
		cbWholeWordFlow.setText(b.getString("SDMXHelper.23")); //$NON-NLS-1$
		lblCodelist.setText(b.getString("SDMXHelper.61")); //$NON-NLS-1$
		lblDataflowFilter.setText(b.getString("SDMXHelper.53")); //$NON-NLS-1$
		lblDimension.setText(b.getString("SDMXHelper.59")); //$NON-NLS-1$
		lblQuery.setText(b.getString("SDMXHelper.49")); //$NON-NLS-1$
		mnActions.setText(b.getString("SDMXHelper.32")); //$NON-NLS-1$
		mnHelp.setText(b.getString("SDMXHelper.46")); //$NON-NLS-1$
		mnLanguage.setText(b.getString("SDMXHelper.6")); //$NON-NLS-1$
		mnLogLevel.setText(b.getString("SDMXHelper.40")); //$NON-NLS-1$
		mnProviders.setText(b.getString("SDMXHelper.31")); //$NON-NLS-1$
		mntmAddProvider.setText(b.getString("SDMXHelper.39")); //$NON-NLS-1$
		mntmAboutSdmxConnectors.setText(b.getString("SDMXHelper.47")); //$NON-NLS-1$
		mntmBuildCommands.setText(b.getString("SDMXHelper.35")); //$NON-NLS-1$
		mntmCopySelection.setText(b.getString("SDMXHelper.34")); //$NON-NLS-1$
		mntmLogLevels[0].setText(b.getString("SDMXHelper.41")); //$NON-NLS-1$
		mntmLogLevels[1].setText(b.getString("SDMXHelper.42")); //$NON-NLS-1$
		mntmLogLevels[2].setText(b.getString("SDMXHelper.43")); //$NON-NLS-1$
		mntmLogLevels[3].setText(b.getString("SDMXHelper.44")); //$NON-NLS-1$
		mntmLogLevels[4].setText(b.getString("SDMXHelper.45")); //$NON-NLS-1$
		rdSearchAllFlow.setText(b.getString("SDMXHelper.22")); //$NON-NLS-1$
		rdSearchBothCL.setText(b.getString("SDMXHelper.18")); //$NON-NLS-1$
		rdSearchCodeCL.setText(b.getString("SDMXHelper.16")); //$NON-NLS-1$
		rdSearchCodeFlow.setText(b.getString("SDMXHelper.19")); //$NON-NLS-1$
		rdSearchDescCL.setText(b.getString("SDMXHelper.17")); //$NON-NLS-1$
		rdSearchDescFlow.setText(b.getString("SDMXHelper.21")); //$NON-NLS-1$
		rdSearchDSDFlow.setText(b.getString("SDMXHelper.20")); //$NON-NLS-1$
		tblCodes.getColumnModel().getColumn(0).setHeaderValue(""); //$NON-NLS-1$
		tblCodes.getColumnModel().getColumn(1).setHeaderValue(b.getString("SDMXHelper.69")); //$NON-NLS-1$
		tblCodes.getColumnModel().getColumn(2).setHeaderValue(b.getString("SDMXHelper.70")); //$NON-NLS-1$
		tblDataflows.getColumnModel().getColumn(0).setHeaderValue(b.getString("SDMXHelper.0")); //$NON-NLS-1$
		tblDataflows.getColumnModel().getColumn(1).setHeaderValue(b.getString("SDMXHelper.1")); //$NON-NLS-1$
		tblDataflows.getColumnModel().getColumn(2).setHeaderValue(b.getString("SDMXHelper.2")); //$NON-NLS-1$
		tblDataflows.getColumnModel().getColumn(3).setHeaderValue(b.getString("SDMXHelper.3")); //$NON-NLS-1$
		tblDataflows.getColumnModel().getColumn(4).setHeaderValue(b.getString("SDMXHelper.4")); //$NON-NLS-1$
		tblDataflows.getColumnModel().getColumn(5).setHeaderValue(b.getString("SDMXHelper.5")); //$NON-NLS-1$
		tblDimensions.getColumnModel().getColumn(0).setHeaderValue(""); //$NON-NLS-1$
		tblDimensions.getColumnModel().getColumn(1).setHeaderValue(b.getString("SDMXHelper.11")); //$NON-NLS-1$
		tblDimensions.getColumnModel().getColumn(2).setHeaderValue(b.getString("SDMXHelper.12")); //$NON-NLS-1$
		noResultsMessage = b.getString("SDMXHelper.89"); //$NON-NLS-1$
		resultsCountMessage = b.getString("SDMXHelper.91"); //$NON-NLS-1$
		seriesCountPanel.updateBundle(b);
	}

	private void flowSelListener(ListSelectionEvent e)
	{
		String dataflowID = getSelectedDataflow();
		if (!e.getValueIsAdjusting() && dataflowID != null)
		{
			updateDataflow(dataflowID);
			tfDimensionFilter.setText(""); //$NON-NLS-1$
			dimTableSorter.setRowFilter(null);
			btnCheckQuery.setEnabled(true);
			btnPrintQuery.setEnabled(true);
		}
	}

	private void dimSelListener(ListSelectionEvent e)
	{
		String selectedDimension = getSelectedDimension();
		String provider = selectedProviderGroup.getSelection().getActionCommand();

		if (e.getValueIsAdjusting() || selectedDimension == null)
			return;

		List<? extends SortKey> sortingKeys = tblCodes.getRowSorter() == null ? null
					: tblCodes.getRowSorter().getSortKeys();
		String selectedDataflow = getSelectedDataflow();
		AtomicBoolean interrupted = new AtomicBoolean(false);
		
		new ProgressViewer<>(this, interrupted, () -> 
			SDMXClientFactory.getProviders().get(provider).isSupportsAvailability() ?
				V3 == SDMXClientFactory.getProviders().get(provider).getSdmxVersion() ?
					SdmxClientHandler.filterCodes(provider, selectedDataflow, createAvailabilityFilter()).get(selectedDimension) 
					:
					SdmxClientHandler.filterCodes(provider, selectedDataflow, createFilter()).get(selectedDimension)
				:
				getCodes(provider, selectedDataflow, selectedDimension)	
				,
			codes -> {
				// Create new checkbox for codes given the selectedDimension
				// the new checkbox is stored in codelistSorterMap using the String selectedDimension as key.
				// If the checkbox is already present, the selected codes are updated and set to true
				CheckboxListTableModel<String> model = new CheckboxListTableModel<String>();
				model.addTableModelListener(event -> {
					tfSdmxQuery.setText(createQuery());
				});
				model.setItems(codes);
				if (codelistSortersMap.get(selectedDimension) != null) {
					Collection<String> checkedCodes = codelistSortersMap.get(selectedDimension).getModel().getCheckedCodes();
					model.updateCheckedCodes(checkedCodes);
				}
				TableRowSorter<CheckboxListTableModel<String>> sorter = new TableRowSorter<>(model);
				codelistSortersMap.put(selectedDimension, sorter);
				if (V3 == SDMXClientFactory.getProviders().get(provider).getSdmxVersion()) {
					TableModelListener listener = new TableModelListener() {
						@Override
						public void tableChanged(TableModelEvent e) {
							updateSeriesCounts(selectedDataflow, dimsTableModel.getSource());
						}
					};
					model.addTableModelListener(listener);
				}
			},
			ex -> {
				interrupted.set(true);
				LOGGER.severe(ex.getClass().getName() + ": " + ex.getMessage()); //$NON-NLS-1$
				LOGGER.log(Level.FINER, "", ex); //$NON-NLS-1$
			}
		).start();

		SwingUtilities.invokeLater(() -> {
			// Here the codelist checkbox get switched, it detects when the selectedDimension get changed
			// and from codelistSorterMap get the previously instantiated checkbox and display it.
				if (!interrupted.get())
				{
					TableRowSorter<CheckboxListTableModel<String>> sorter = codelistSortersMap.get(selectedDimension);
					tblCodes.setModel(sorter.getModel());
					tblCodes.setRowSorter(sorter);
					if (sortingKeys == null || sortingKeys.isEmpty())
						sorter.setSortKeys(singletonList(new SortKey(1, ASCENDING)));
					else
						sorter.setSortKeys(sortingKeys);
					sorter.setSortable(tblCodes.convertColumnIndexToView(0), false);
					sorter.setRowFilter(null);
					
					tfCodesFilter.setText(""); //$NON-NLS-1$
					tblCodes.revalidate();
					updateCodelistCount();
				}
			});
	}

	private void updateCodelistCount()
	{
		int selected = 0;
		for (int i = 0; i < tblCodes.getRowCount(); i++)
			selected += (Boolean) tblCodes.getModel().getValueAt(tblCodes.convertRowIndexToModel(i), 0) ? 1 : 0;

		lblCodelist.setText("Filter codes (" + selected + "/" + tblCodes.getRowCount() + "):"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	private void updateDataflow(final String dataflowID)
	{
		// reset clean state for tblCodes before
		isCodelistSortersMapTablesListenerActive = false;
		codelistSortersMap.clear();
		((CheckboxListTableModel<?>) tblCodes.getModel()).clear();
		seriesCountPanel.updateCounts(0, 0);
		dimsTableModel.clear();
		String formatted = (String) btnCheckQuery.getClientProperty("FORMAT"); //$NON-NLS-1$
		btnCheckQuery.setText(String.format(formatted, "")); //$NON-NLS-1$ //$NON-NLS-2$
		// if this is not a provider switch
		isCodelistSortersMapTablesListenerActive = true;
		new ProgressViewer<>(this, new AtomicBoolean(false),
				() -> SdmxClientHandler.getDimensions(selectedProviderGroup.getSelection().getActionCommand(), dataflowID),
				dims -> {
					dimsTableModel.setItems(dims, item -> new String[] { item.getId(), item.getName() });
					tfSdmxQuery.setText(createQuery());
				},
				ex -> {
						LOGGER.severe("Exception. Class: " + ex.getClass().getName() + " .Message: " + ex.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
						LOGGER.log(Level.FINER, "", ex); //$NON-NLS-1$
					}).start();
	}

	private void displayQueryResults()
	{
		String query = createFilter();
		String selectedDataflow = getSelectedDataflow();
		String selectedProvider = getSelectedProvider();
		AtomicBoolean isCancelled = new AtomicBoolean(false);
		
		if (selectedDataflow != null && query != null && !query.isEmpty())
			new ProgressViewer<>(SDMXHelper.this, isCancelled, 
				() -> { 
					try
					{
						Dataflow dataflow = getFlow(selectedProvider, selectedDataflow);
						List<PortableTimeSeries<Double>> names = null; 
						if (V3 == SDMXClientFactory.getProviders().get(selectedProvider).getSdmxVersion()) {
							names = getTimeSeries2(selectedProvider, dataflow.getId(), null, query, null, null, "none", "none", null, false);
						}
						else{
							names = getTimeSeries(selectedProvider, dataflow.getId() + "/" + query, null, null, true, null, false);
						}
						return new SimpleEntry<>(dataflow, names);
					}
					catch (SdmxResponseException e)
					{
						if (e.getResponseCode() != 100)
							throw e;
						
						isCancelled.set(true);
						JOptionPane.showMessageDialog(null, noResultsMessage, "SDMX Helper", JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
						return new SimpleEntry<Dataflow, List<PortableTimeSeries<Double>>>(null, null);
					}
				},
				entry -> {
					if (!isCancelled.get())
					{
						Dataflow df = entry.getKey();
						List<PortableTimeSeries<Double>> result = entry.getValue();
						
						// Open a new window to browse query results
						final JFrame wnd = new ResultsFrame(getSelectedProvider(), df.getId(), result);

						wnd.setTitle(String.format(resultsCountMessage, result.size(), df.getDescription())); //$NON-NLS-1$ //$NON-NLS-2$
						wnd.setVisible(true);
						wnd.toFront();
					}
				},
				ex -> {
					LOGGER.severe("Exception. Class: " + ex.getClass().getName() + " .Message: " + ex.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
					LOGGER.log(Level.FINER, "", ex); //$NON-NLS-1$
				}).start();
	}

	private void providersSetup(final JMenu providersMenu)
	{
		for (final Entry<String, Provider> providerEntry : SDMXClientFactory.getProviders().entrySet())
		{
			final String provider = providerEntry.getKey();
			final SDMXVersion sdmxVersion = providerEntry.getValue().getSdmxVersion();
			final JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(
					"[" + sdmxVersion + "] " + provider + ": " + providerEntry.getValue().getDescription()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			menuItem.setActionCommand(provider);
			menuItem.addActionListener(e -> {
					try
					{
						menuItem.setSelected(true);
						// Refresh dataflows on provider selection
						updateSource(provider);
						btnCheckQuery.setEnabled(false);
						btnPrintQuery.setEnabled(false);
						String formatted = (String) btnCheckQuery.getClientProperty("FORMAT"); //$NON-NLS-1$
						btnCheckQuery.setText(String.format(formatted, "")); //$NON-NLS-1$ //$NON-NLS-2$
						tfSdmxQuery.setText(""); //$NON-NLS-1$
						tfDataflowFilter.setText(""); //$NON-NLS-1$
						lblQuery.setText(lblQuery.getText().split(":")[0] + ": " + provider); //$NON-NLS-1$ //$NON-NLS-2$
						if (V3 == SDMXClientFactory.getProviders().get(provider).getSdmxVersion()) {
							seriesCountPanel.showPanel();
						} else {
							seriesCountPanel.hidePanel();
						}

					}
					catch (Exception ex)
					{
						ex.printStackTrace();
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
					isCodelistSortersMapTablesListenerActive = false;
					codelistSortersMap.clear();
					tblCodes.setRowSorter(null);
					tblCodes.setModel(new CheckboxListTableModel<String>());//$NON-NLS-1$ //$NON-NLS-2$
					dimsTableModel.clear();
					dataflowsTableModel.setItems(flows);
					TableRowSorter<DataflowsModel> rowSorter = new TableRowSorter<>(dataflowsTableModel);
					rowSorter.setSortKeys(singletonList(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
					tblDataflows.setRowSorter(rowSorter);
					seriesCountPanel.updateCounts(0, 0);
					isCodelistSortersMapTablesListenerActive = true;
				}
			},
			ex -> {
				LOGGER.severe("Exception. Class: " + ex.getClass().getName() + " .Message: " + ex.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
				LOGGER.log(Level.FINER, "", ex); //$NON-NLS-1$
			}).start();
	}

	/**
	 * createQuery() create the full sdmx Query from the selected options in the helper:
	 * in SDMX V2 is provider/filter
	 *
	 * @return String the resulting full query
	 */
	private String createQuery()
	{
		String result = "";
		try
		{
			if (V3 == SDMXClientFactory.getProviders().get(getSelectedProvider()).getSdmxVersion())
			{
				result = createAvailabilityFilter();
			} else
			{
				result = getSelectedDataflow() + "/" + createFilterV2();
			}
		} catch (SdmxException ex)
		{
			LOGGER.severe("Exception. Class: " + ex.getClass().getName() + " .Message: " + ex.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			LOGGER.log(Level.FINER, "", ex); //$NON-NLS-1$
		}
		return result;
	}

	/**
	 * createFilter() create the filter part for the SDMX Query from the selected option in the helper.
	 *
	 * @return
	 */
	private String createFilter()
	{
		String result = "";

		try
		{
			if (V3 == SDMXClientFactory.getProviders().get(getSelectedProvider()).getSdmxVersion())
			{
				result = createAvailabilityFilter();
			} else
			{
				result = createFilterV2();
			}
		} catch (SdmxException ex)
		{
			LOGGER.severe("Exception. Class: " + ex.getClass().getName() + " .Message: " + ex.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			LOGGER.log(Level.FINER, "", ex); //$NON-NLS-1$
		}
		return result;
	}


	private void updateSeriesCounts(String dataflow, List<Dimension> dims)
	{
		if (!isCodelistSortersMapTablesListenerActive) {
			return;
		}
		try
		{
			Map<String, Integer> countMap = SdmxClientHandler.getSeriesCount(getSelectedProvider(), dataflow, createFilter());
			int series_count = countMap.getOrDefault("series_count", 0);
			int obs_count = countMap.getOrDefault("obs_count", 0);
			seriesCountPanel.updateCounts(series_count, obs_count);

		}
		catch (SdmxException e)
		{
			e.printStackTrace();
		}
	}

	private String createAvailabilityFilter() throws SdmxException
	{
		List<Dimension> dims = SdmxClientHandler.getDimensions(getSelectedProvider(), getSelectedDataflow());
		return dims.stream()
			.map(Dimension::getId)
			.filter(codelistSortersMap::containsKey)
			.filter(id -> codelistSortersMap.get(id).getModel().getCheckedCodesCount() > 0)
			.map(id -> "c[" + id + "]=" + join(",", codelistSortersMap.get(id).getModel().getCheckedCodes())) 
			.collect(joining("&"));
	}

	private String getSelectedDataflow()
	{
		int rowSelected = tblDataflows.getSelectedRow();
		return rowSelected != -1
				? tblDataflows.getValueAt(rowSelected, tblDataflows.convertColumnIndexToView(0)).toString()
				: null;
	}

	private String getSelectedProvider()
	{
		ButtonModel providerMenu = selectedProviderGroup.getSelection();
		return(providerMenu.getActionCommand());
	}

	private String getSelectedDimension()
	{
		int rowSelected = tblDimensions.getSelectedRow();
		return rowSelected == -1 ? null : tblDimensions.getValueAt(rowSelected, tblDimensions.convertColumnIndexToView(1)).toString();
	}

	private TitledBorder createTitledBorder()
	{
		return new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), 
				"", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)); //$NON-NLS-1$
	}

	private String createFilterV2() throws SdmxException
	{
		List<Dimension> dims = SdmxClientHandler.getDimensions(getSelectedProvider(), getSelectedDataflow());
		return dims.stream()
				.map(Dimension::getId)
				.map(id -> codelistSortersMap.containsKey(id)
						? join("+", codelistSortersMap.get(id).getModel().getCheckedCodes())
						: "")
				.collect(joining("."));
	}

}
