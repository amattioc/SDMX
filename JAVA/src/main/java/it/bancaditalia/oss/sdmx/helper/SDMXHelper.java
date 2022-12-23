package it.bancaditalia.oss.sdmx.helper;

import static it.bancaditalia.oss.sdmx.client.SdmxClientHandler.getCodes;
import static it.bancaditalia.oss.sdmx.client.SdmxClientHandler.getFlow;
import static it.bancaditalia.oss.sdmx.client.SdmxClientHandler.getTimeSeries;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;
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
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import javax.swing.text.AttributeSet;
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

	private final JLabel queryLabel = new JLabel();
	private final JTextField sdmxQueryTextField = new JTextField();
	private final JTextField codesFilterTextField = new JTextField();
	private final JTextField dataflowFilterTextField = new JTextField();
	private final JTable dataflowsTable = new JTable();
	private final JLabel codelistLabel = new JLabel();
	private final JTable dimensionsTable = new JTable();
	private final JTable codesTable = new JTable();
	private final JButton checkQueryButton = new JButton();
	private final JButton btnPrintQuery = new JButton();
	private final ButtonGroup selectedProviderGroup = new ButtonGroup();
	private final DataflowsModel dataflowsTableModel = new DataflowsModel();
	private final EnumedListTableModel<Dimension> dimsTableModel = new EnumedListTableModel<>();
	private final HashMap<String, TableRowSorter<CheckboxListTableModel<String>>> codelistSortersMap = new HashMap<>();
	private final JCheckBox regexSearchCLCheckbox = new JCheckBox();
	private final JCheckBox caseSearchCLCheckbox = new JCheckBox();
	private final JCheckBox wholeWordCLCheckbox = new JCheckBox();
	private final JRadioButton searchCodeCLRadio = new JRadioButton();
	private final JRadioButton searchDescCLRadio = new JRadioButton();
	private final JRadioButton searchBothCLRadio = new JRadioButton();
	private final ButtonGroup codeSearchRadioGroup = new ButtonGroup();
	private final JRadioButton searchCodeFlowRadio = new JRadioButton();
	private final JRadioButton searchDSDFlowRadio = new JRadioButton();
	private final JRadioButton searchDescFlowRadio = new JRadioButton();
	private final JRadioButton searchAllFlowRadio = new JRadioButton();
	private final JCheckBox wholeWordFlowCheckbox = new JCheckBox();
	private final JCheckBox caseSearchFlowCheckbox = new JCheckBox();
	private final JCheckBox regexSearchFlowCheckbox = new JCheckBox();
	private final ButtonGroup flowSearchRadioGroup = new ButtonGroup();
	private final JTextField dimensionFilterTextField = new JTextField();
	private final TableRowSorter<EnumedListTableModel<Dimension>> dimTableSorter = new TableRowSorter<>(dimsTableModel);
	private final JMenuItem mntmAddProvider = new JMenuItem();
	private final JMenuItem mntmBuildCommands = new JMenuItem();
	private final JMenuItem mntmCopySelection = new JMenuItem(COPY_ACTION);
	private final JMenu mnActions = new JMenu();
	private final JMenu providersMenu = new JMenu();
	private final JMenu mnLanguage = new JMenu(); 
	private final JMenu mnLogLevel = new JMenu();
	private final JMenu mnHelp = new JMenu();
	private final JMenuItem mntmAboutSdmxConnectors = new JMenuItem();
	private final JLabel dataflowFilterLabel = new JLabel();
	private final JLabel dimensionLabel = new JLabel();
	private final JButton toggleVisibleButton = new JButton();
	private final JButton btnClearSelectedDimension = new JButton();
	private final TitledBorder dataflowsPanelBorder = createTitledBorder();
	private final TitledBorder finalqueryPanelBorder = createTitledBorder();
	private final TitledBorder dimensionsPanelBorder = createTitledBorder();
	private final TitledBorder codesPanelBorder = createTitledBorder();
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

	public String getCurrentProvider()
	{
		return selectedProviderGroup.getSelection().getActionCommand();
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

		menuBar.add(providersMenu);

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
						new ToolCommandsFrame(sdmxQueryTextField.getText(),
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
				new CompoundBorder(finalqueryPanelBorder, new EmptyBorder(5, 5, 5, 5))));
		queryAndFlowPanel.add(finalqueryPanel);

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

		checkQueryButton.setEnabled(false);
//		checkQueryButton.setPreferredSize(new java.awt.Dimension(170, 23));
		checkQueryButton.setMinimumSize(new java.awt.Dimension(100, 23));
		checkQueryButton.setMaximumSize(new java.awt.Dimension(300, 23));
		checkQueryButton.addActionListener(e -> displayQueryResults());
		finalqueryPanel.add(checkQueryButton);

		Component horizontalStrut_5 = Box.createHorizontalStrut(10);

		btnPrintQuery.setEnabled(false);
		btnPrintQuery.setPreferredSize(new java.awt.Dimension(240, 23));
		btnPrintQuery.setMinimumSize(new java.awt.Dimension(240, 23));
		btnPrintQuery.setMaximumSize(new java.awt.Dimension(240, 23));
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
		dataflowsPanel.setBorder(new CompoundBorder( new EmptyBorder(0, 5, 5, 5), 
				new CompoundBorder(dataflowsPanelBorder, new EmptyBorder(10, 10, 10, 10))));
		queryAndFlowPanel.add(dataflowsPanel);
		dataflowsPanel.setLayout(new BoxLayout(dataflowsPanel, BoxLayout.Y_AXIS));

		JPanel dataflowFilterPanel = new JPanel();
		dataflowFilterPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		dataflowsPanel.add(dataflowFilterPanel);
		dataflowFilterPanel.setLayout(new BoxLayout(dataflowFilterPanel, BoxLayout.X_AXIS));

		dataflowFilterPanel.add(dataflowFilterLabel);

		Component horizontalStrut_2 = Box.createHorizontalStrut(10);
		dataflowFilterPanel.add(horizontalStrut_2);

		final TableRowSorter<DataflowsModel> dataflowsTableSorter = new TableRowSorter<>(dataflowsTableModel);
		dataflowsTableSorter.setSortKeys(singletonList(new RowSorter.SortKey(0, SortOrder.ASCENDING)));

		dataflowFilterTextField.setPreferredSize(new java.awt.Dimension(6, 25));
		dataflowFilterTextField.setMaximumSize(new java.awt.Dimension(2147483647, 25));
		dataflowFilterTextField.setFont(new Font(null, Font.BOLD, 16));
		dataflowFilterTextField.setForeground(Color.RED);
			
		final DoFilterListener flowListener = new DoFilterListener(dataflowFilterTextField, text -> {
				String searchPattern = caseSearchFlowCheckbox.isSelected() ? "": "(?i)"; //$NON-NLS-1$ //$NON-NLS-2$
				searchPattern += regexSearchFlowCheckbox.isSelected() ? text : Pattern.quote(text);
				searchPattern = wholeWordFlowCheckbox.isSelected() ? "^" + searchPattern + "$" : searchPattern;   //$NON-NLS-1$ //$NON-NLS-2$
				
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
		dimensionsPanel.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), 
				new CompoundBorder(dimensionsPanelBorder, new EmptyBorder(10, 10, 10, 10))));
		dimensionsPanel.setLayout(new BoxLayout(dimensionsPanel, BoxLayout.Y_AXIS));

		Box dimensionFilterBox = Box.createHorizontalBox();
		dimensionFilterBox.setPreferredSize(new java.awt.Dimension(10, 25));
		dimensionFilterBox.setMinimumSize(new java.awt.Dimension(10, 25));
		dimensionFilterBox.setMaximumSize(new java.awt.Dimension(32768, 25));
		dimensionsPanel.add(dimensionFilterBox);

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
		codesPanel.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), new CompoundBorder(
					codesPanelBorder, new EmptyBorder(10, 10, 10, 10))));
		codesPanel.setLayout(new BoxLayout(codesPanel, BoxLayout.Y_AXIS));

		Box codesFilterBox = Box.createHorizontalBox();
		codesFilterBox.setPreferredSize(new java.awt.Dimension(10, 25));
		codesFilterBox.setMinimumSize(new java.awt.Dimension(10, 25));
		codesFilterBox.setMaximumSize(new java.awt.Dimension(32768, 25));
		codesPanel.add(codesFilterBox);

		codesFilterBox.add(codelistLabel);

		Component horizontalStrut_4 = Box.createHorizontalStrut(10);
		codesFilterBox.add(horizontalStrut_4);
		
		Box codeOptionsPane = Box.createHorizontalBox();
		codeOptionsPane.setPreferredSize(new java.awt.Dimension(10, 25));
		codeOptionsPane.setMinimumSize(new java.awt.Dimension(10, 25));
		codeOptionsPane.setMaximumSize(new java.awt.Dimension(32768, 25));
		codesPanel.add(codeOptionsPane);

		final DoFilterListener codeFilterListener = new DoFilterListener(codesFilterTextField, text -> {
				String searchPattern = caseSearchCLCheckbox.isSelected() ? "": "(?i)"; //$NON-NLS-1$ //$NON-NLS-2$
				searchPattern += regexSearchCLCheckbox.isSelected() ? text : Pattern.quote(text);
				searchPattern = wholeWordCLCheckbox.isSelected() ? "^" + searchPattern + "$" : searchPattern;   //$NON-NLS-1$ //$NON-NLS-2$
				
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
		codesFilterTextField.setFont(new Font("Dialog", Font.BOLD, 16)); //$NON-NLS-1$
		codesFilterTextField.getDocument().addDocumentListener(codeFilterListener);
		codesFilterBox.add(codesFilterTextField);

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
		codesTable.setColumnModel(codesTableColumnModel);
		codesTable.getTableHeader().setReorderingAllowed(false);
		codesTable.getTableHeader().setResizingAllowed(true);
		codesTable.getDefaultEditor(Boolean.class).addCellEditorListener(new CellEditorListener() {
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
		codelistScrollPane.setViewportView(codesTable);

		dimTableSorter.setSortKeys(singletonList(new SortKey(0, SortOrder.ASCENDING)));

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
				pattern -> dimTableSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(pattern))))); //$NON-NLS-1$
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

		btnClearSelectedDimension.addActionListener(e -> {
					if (getSelectedDataflow() != null)
					{
						((CheckboxListTableModel<?>) codesTable.getModel()).uncheckAll();
						updateCodelistCount();
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
			providersMenu.setText(providersMenu.getText() + ": " + lockedProvider); //$NON-NLS-1$
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
		btnClearSelectedDimension.setText(b.getString("SDMXHelper.72")); //$NON-NLS-1$
		btnPrintQuery.setText(b.getString("SDMXHelper.51")); //$NON-NLS-1$
		caseSearchCLCheckbox.setText(b.getString("SDMXHelper.14")); //$NON-NLS-1$
		caseSearchFlowCheckbox.setText(b.getString("SDMXHelper.24")); //$NON-NLS-1$
		checkQueryButton.putClientProperty("FORMAT", b.getString("SDMXHelper.50")); //$NON-NLS-1$
		checkQueryButton.setText(String.format(b.getString("SDMXHelper.50"), "")); //$NON-NLS-1$ //$NON-NLS-2$
		codelistLabel.setText(b.getString("SDMXHelper.61")); //$NON-NLS-1$
		codesPanelBorder.setTitle(b.getString("SDMXHelper.60")); //$NON-NLS-1$
		codesTable.getColumnModel().getColumn(0).setHeaderValue(""); //$NON-NLS-1$
		codesTable.getColumnModel().getColumn(1).setHeaderValue(b.getString("SDMXHelper.69")); //$NON-NLS-1$
		codesTable.getColumnModel().getColumn(2).setHeaderValue(b.getString("SDMXHelper.70")); //$NON-NLS-1$
		dataflowFilterLabel.setText(b.getString("SDMXHelper.53")); //$NON-NLS-1$
		dataflowsPanelBorder.setTitle(b.getString("SDMXHelper.52")); //$NON-NLS-1$
		dataflowsTable.getColumnModel().getColumn(0).setHeaderValue(b.getString("SDMXHelper.0")); //$NON-NLS-1$
		dataflowsTable.getColumnModel().getColumn(1).setHeaderValue(b.getString("SDMXHelper.1")); //$NON-NLS-1$
		dataflowsTable.getColumnModel().getColumn(2).setHeaderValue(b.getString("SDMXHelper.2")); //$NON-NLS-1$
		dataflowsTable.getColumnModel().getColumn(3).setHeaderValue(b.getString("SDMXHelper.3")); //$NON-NLS-1$
		dataflowsTable.getColumnModel().getColumn(4).setHeaderValue(b.getString("SDMXHelper.4")); //$NON-NLS-1$
		dataflowsTable.getColumnModel().getColumn(5).setHeaderValue(b.getString("SDMXHelper.5")); //$NON-NLS-1$
		dimensionLabel.setText(b.getString("SDMXHelper.59")); //$NON-NLS-1$
		dimensionsPanelBorder.setTitle(b.getString("SDMXHelper.58")); //$NON-NLS-1$
		dimensionsTable.getColumnModel().getColumn(0).setHeaderValue(""); //$NON-NLS-1$
		dimensionsTable.getColumnModel().getColumn(1).setHeaderValue(b.getString("SDMXHelper.11")); //$NON-NLS-1$
		dimensionsTable.getColumnModel().getColumn(2).setHeaderValue(b.getString("SDMXHelper.12")); //$NON-NLS-1$
		finalqueryPanelBorder.setTitle(b.getString("SDMXHelper.48")); //$NON-NLS-1$
		mnActions.setText(b.getString("SDMXHelper.32")); //$NON-NLS-1$
		mnHelp.setText(b.getString("SDMXHelper.46")); //$NON-NLS-1$
		mnLanguage.setText(b.getString("SDMXHelper.6")); //$NON-NLS-1$
		mnLogLevel.setText(b.getString("SDMXHelper.40")); //$NON-NLS-1$
		mntmAddProvider.setText(b.getString("SDMXHelper.39")); //$NON-NLS-1$
		mntmAboutSdmxConnectors.setText(b.getString("SDMXHelper.47")); //$NON-NLS-1$
		mntmBuildCommands.setText(b.getString("SDMXHelper.35")); //$NON-NLS-1$
		mntmCopySelection.setText(b.getString("SDMXHelper.34")); //$NON-NLS-1$
		mntmLogLevels[0].setText(b.getString("SDMXHelper.41")); //$NON-NLS-1$
		mntmLogLevels[1].setText(b.getString("SDMXHelper.42")); //$NON-NLS-1$
		mntmLogLevels[2].setText(b.getString("SDMXHelper.43")); //$NON-NLS-1$
		mntmLogLevels[3].setText(b.getString("SDMXHelper.44")); //$NON-NLS-1$
		mntmLogLevels[4].setText(b.getString("SDMXHelper.45")); //$NON-NLS-1$
		providersMenu.setText(b.getString("SDMXHelper.31")); //$NON-NLS-1$
		queryLabel.setText(b.getString("SDMXHelper.49")); //$NON-NLS-1$
		regexSearchCLCheckbox.setText(b.getString("SDMXHelper.13")); //$NON-NLS-1$
		regexSearchFlowCheckbox.setText(b.getString("SDMXHelper.25")); //$NON-NLS-1$
		searchAllFlowRadio.setText(b.getString("SDMXHelper.22")); //$NON-NLS-1$
		searchBothCLRadio.setText(b.getString("SDMXHelper.18")); //$NON-NLS-1$
		searchCodeCLRadio.setText(b.getString("SDMXHelper.16")); //$NON-NLS-1$
		searchCodeFlowRadio.setText(b.getString("SDMXHelper.19")); //$NON-NLS-1$
		searchDescCLRadio.setText(b.getString("SDMXHelper.17")); //$NON-NLS-1$
		searchDescFlowRadio.setText(b.getString("SDMXHelper.21")); //$NON-NLS-1$
		searchDSDFlowRadio.setText(b.getString("SDMXHelper.20")); //$NON-NLS-1$
		toggleVisibleButton.setText(b.getString("SDMXHelper.67")); //$NON-NLS-1$
		wholeWordCLCheckbox.setText(b.getString("SDMXHelper.15")); //$NON-NLS-1$
		wholeWordFlowCheckbox.setText(b.getString("SDMXHelper.23")); //$NON-NLS-1$
		noResultsMessage = b.getString("SDMXHelper.89"); //$NON-NLS-1$
		resultsCountMessage = b.getString("SDMXHelper.91"); //$NON-NLS-1$
	}

	private void flowSelListener(ListSelectionEvent e)
	{
		String dataflowID = getSelectedDataflow();
		if (!e.getValueIsAdjusting() && dataflowID != null)
		{
			updateDataflow(dataflowID);
			dimensionFilterTextField.setText(""); //$NON-NLS-1$
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
		
		new ProgressViewer<>(this, interrupted, () -> 
			SDMXClientFactory.getProviders().get(provider).getSdmxVersion().equals(SDMXClientFactory.SDMX_V3) ?
				SdmxClientHandler.filterCodes(provider, selectedDataflow, createAvailabilityFilter()).get(selectedDimension) 
				:
				getCodes(provider, selectedDataflow, selectedDimension)	
				,
			codes -> {
				CheckboxListTableModel<String> model = null;
				if(!codelistSortersMap.containsKey(selectedDimension)){
					model = new CheckboxListTableModel<String>();
					model.addTableModelListener(event -> sdmxQueryTextField.setText(createQuery(selectedDataflow, dimsTableModel.getSource())));
					if (SDMXClientFactory.SDMX_V3.equals(SDMXClientFactory.getProviders().get(provider).getSdmxVersion()))
						model.addTableModelListener(event -> formatQueryButton(selectedDataflow, dimsTableModel.getSource()));
				}
				else
					model = codelistSortersMap.get(selectedDimension).getModel();
				
				model.setItems(codes);
				
				TableRowSorter<CheckboxListTableModel<String>> sorter = new TableRowSorter<>(model);
				codelistSortersMap.put(selectedDimension, sorter);
			},
			ex -> {
				interrupted.set(true);
				LOGGER.severe("Exception " + ex.getClass().getName() + ": " + ex.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
				LOGGER.log(Level.FINER, "", ex); //$NON-NLS-1$
			}
		).start();

		SwingUtilities.invokeLater(() -> {
				if (!interrupted.get())
				{
					TableRowSorter<CheckboxListTableModel<String>> sorter = codelistSortersMap.get(selectedDimension);
					codesTable.setModel(sorter.getModel());
					codesTable.setRowSorter(sorter);
					if (sortingKeys == null || sortingKeys.isEmpty())
						sorter.setSortKeys(singletonList(new SortKey(1, ASCENDING)));
					else
						sorter.setSortKeys(sortingKeys);
					sorter.setSortable(codesTable.convertColumnIndexToView(0), false);
					sorter.setRowFilter(null);
					
					codesFilterTextField.setText(""); //$NON-NLS-1$
					codesTable.revalidate();
					updateCodelistCount();
				}
			});
	}

	private void updateCodelistCount()
	{
		int selected = 0;
		for (int i = 0; i < codesTable.getRowCount(); i++)
			selected += (Boolean) codesTable.getModel().getValueAt(codesTable.convertRowIndexToModel(i), 0) ? 1 : 0;

		codelistLabel.setText("Filter codes (" + selected + "/" + codesTable.getRowCount() + "):"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	private void updateDataflow(final String dataflowID)
	{
		// reset clean state for codesTable before
		codelistSortersMap.clear();
		((CheckboxListTableModel<?>) codesTable.getModel()).clear();
		dimsTableModel.clear();

		// if this is not a provider switch
		new ProgressViewer<>(this, new AtomicBoolean(false),
				() -> SdmxClientHandler.getDimensions(selectedProviderGroup.getSelection().getActionCommand(), dataflowID),
				dims -> {
					dimsTableModel.setItems(dims, item -> new String[] { item.getId(), item.getName() });
					sdmxQueryTextField.setText(createQuery(dataflowID, dims));
				},
				ex -> {
						LOGGER.severe("Exception. Class: " + ex.getClass().getName() + " .Message: " + ex.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
						LOGGER.log(Level.FINER, "", ex); //$NON-NLS-1$
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
						final JFrame wnd = new ResultsFrame(getCurrentProvider(), result);

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
			final String sdmxVersion = providerEntry.getValue().getSdmxVersion();
			final JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(
					"[" + sdmxVersion + "] " + provider + ": " + providerEntry.getValue().getDescription()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			menuItem.setActionCommand(provider);
			menuItem.addActionListener(e -> {
					try
					{
						menuItem.setSelected(true);
						// Refresh dataflows on provider selection
						updateSource(provider);
						checkQueryButton.setEnabled(false);
						btnPrintQuery.setEnabled(false);
						sdmxQueryTextField.setText(""); //$NON-NLS-1$
						dataflowFilterTextField.setText(""); //$NON-NLS-1$
						queryLabel.setText(queryLabel.getText().split(":")[0] + ": " + provider); //$NON-NLS-1$ //$NON-NLS-2$
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
					codelistSortersMap.clear();
					codesTable.setModel(new CheckboxListTableModel<String>()); //$NON-NLS-1$ //$NON-NLS-2$
					dimsTableModel.clear();
					dataflowsTableModel.setItems(flows);
					TableRowSorter<DataflowsModel> rowSorter = new TableRowSorter<>(dataflowsTableModel);
					rowSorter.setSortKeys(singletonList(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
					dataflowsTable.setRowSorter(rowSorter);										
				}
			},
			ex -> {
				LOGGER.severe("Exception. Class: " + ex.getClass().getName() + " .Message: " + ex.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
				LOGGER.log(Level.FINER, "", ex); //$NON-NLS-1$
			}).start();
	}

	private String createQuery(String dataflow, List<Dimension> dims)
	{
		return dataflow + "/" + createFilter(dims);
	}

	private String createFilter(List<Dimension> dims)
	{
		return dims.stream()
			.map(Dimension::getId)
			.map(id -> codelistSortersMap.containsKey(id) ? join("+", codelistSortersMap.get(id).getModel().getCheckedCodes()) : "") 
			.collect(joining("."));
	}
	
	private void formatQueryButton(String dataflow, List<Dimension> dims)
	{
		try
		{
			int count = SdmxClientHandler.getSeriesCount(selectedProviderGroup.getSelection().getActionCommand(), dataflow, createFilter(dims));
			String formatted = (String) checkQueryButton.getClientProperty("FORMAT"); //$NON-NLS-1$
			checkQueryButton.setText(String.format(formatted, count <= 0 ? "" : ": " + count)); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch (SdmxException e)
		{
			e.printStackTrace();
		}
	}

	private String createAvailabilityFilter() throws SdmxException
	{
		List<Dimension> dims = SdmxClientHandler.getDimensions(getCurrentProvider(), getSelectedDataflow());
		return dims.stream()
			.map(Dimension::getId)
			.filter(codelistSortersMap::containsKey)
			.filter(id -> codelistSortersMap.get(id).getModel().getCheckedCodesCount() > 0)
			.map(id -> "c[" + id + "]=" + join(",", codelistSortersMap.get(id).getModel().getCheckedCodes())) 
			.collect(joining("&"));
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

	private TitledBorder createTitledBorder()
	{
		return new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), 
				"", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)); //$NON-NLS-1$
	}
}
