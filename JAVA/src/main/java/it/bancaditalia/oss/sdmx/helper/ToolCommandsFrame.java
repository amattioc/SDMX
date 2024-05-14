package it.bancaditalia.oss.sdmx.helper;

import static java.awt.Color.WHITE;
import static java.awt.Font.PLAIN;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import it.bancaditalia.oss.sdmx.api.SDMXVersion;
import it.bancaditalia.oss.sdmx.client.SDMXClientFactory;
import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxInvalidParameterException;

class ToolCommandsFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	private final JTextField sasCommandText;
	private final JTextField stataCommandText;
	private final JTextField matlabCommandText;
	private final JTextField rCommandText;
	private final JTextField urlText;
	private final JLabel lblNewLabel;
	
	public ToolCommandsFrame(String dataflow, String queryString, String provider) throws SdmxException
	{
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setExtendedState(Frame.MAXIMIZED_HORIZ);
		if (queryString == null || queryString.isEmpty())
			throw new SdmxInvalidParameterException("The sdmx query is not valid yet: '" + queryString + "'");
		if (dataflow == null || dataflow.isEmpty())
			throw new SdmxInvalidParameterException("The dataflow is not selected yet");

		setResizable(false);
		setSize(800, 340);
		setLocationRelativeTo(null);
		setTitle("Commands in statistical tools");
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu actionMenu = new JMenu("File");
		menuBar.add(actionMenu);
		
		JMenuItem closeMenuItem = new JMenuItem("Close");
		closeMenuItem.setHorizontalAlignment(SwingConstants.LEFT);
		closeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_MASK));
		actionMenu.add(closeMenuItem);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(30, 20, 15, 20));
		getContentPane().add(panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] {30, 0};
		gbl_panel.rowHeights = new int[] {0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0};
		gbl_panel.rowWeights = new double[]{1.0, 1.0, 1.0, 1.0, 1.0};
		panel.setLayout(gbl_panel);
		
		JLabel rCommandLabel = new JLabel("R:");
		rCommandLabel.setDisplayedMnemonic('R');
		rCommandLabel.setDisplayedMnemonicIndex(0);
		rCommandLabel.setDisplayedMnemonic(KeyEvent.VK_R);
		rCommandLabel.setFont(new Font("Calibri", Font.PLAIN, 13));
		rCommandLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_rCommandLabel = new GridBagConstraints();
		gbc_rCommandLabel.gridy = 0;
		gbc_rCommandLabel.anchor = GridBagConstraints.EAST;
		gbc_rCommandLabel.insets = new Insets(5, 5, 5, 10);
		gbc_rCommandLabel.gridx = 0;
		panel.add(rCommandLabel, gbc_rCommandLabel);
		
		if(SDMXVersion.V2 == SDMXClientFactory.getProviders().get(provider).getSdmxVersion())
			rCommandText = new JTextField("result <- getTimeSeries(provider='" + provider + "', id='" + dataflow + "/" + queryString + "');");
		else
			rCommandText = new JTextField("result <- getTimeSeries2(provider='" + provider + "', dataflow='" + dataflow + "', filter='" + queryString + "');");
		rCommandLabel.setLabelFor(rCommandText);
		GridBagConstraints gbc_rCommandText = new GridBagConstraints();
		gbc_rCommandText.fill = GridBagConstraints.BOTH;
		gbc_rCommandText.insets = new Insets(0, 0, 5, 0);
		gbc_rCommandText.gridx = 1;
		gbc_rCommandText.gridy = 0;
		panel.add(rCommandText, gbc_rCommandText);
		
		JLabel matlabCommandLabel = new JLabel("MATLAB:");
		matlabCommandLabel.setDisplayedMnemonic('M');
		matlabCommandLabel.setFont(new Font("Calibri", Font.PLAIN, 13));
		matlabCommandLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_matlabCommandLabel = new GridBagConstraints();
		gbc_matlabCommandLabel.anchor = GridBagConstraints.EAST;
		gbc_matlabCommandLabel.insets = new Insets(5, 5, 5, 10);
		gbc_matlabCommandLabel.gridx = 0;
		gbc_matlabCommandLabel.gridy = 1;
		panel.add(matlabCommandLabel, gbc_matlabCommandLabel);
		
		if(SDMXVersion.V2 == SDMXClientFactory.getProviders().get(provider).getSdmxVersion())
			matlabCommandText = new JTextField("result = getTimeSeries('" + provider + "', '" + dataflow + "/" + queryString + "');");
		else
			matlabCommandText = new JTextField("result = getTimeSeriesTable2('" + provider + "', '" + dataflow + "', '', '" + queryString + "');");
		matlabCommandLabel.setLabelFor(matlabCommandText);
		GridBagConstraints gbc_matlabCommandText = new GridBagConstraints();
		gbc_matlabCommandText.fill = GridBagConstraints.BOTH;
		gbc_matlabCommandText.insets = new Insets(0, 0, 5, 0);
		gbc_matlabCommandText.gridx = 1;
		gbc_matlabCommandText.gridy = 1;
		panel.add(matlabCommandText, gbc_matlabCommandText);
		
		JLabel sasCommandLabel = new JLabel("SAS:");
		sasCommandLabel.setDisplayedMnemonic('S');
		sasCommandLabel.setFont(new Font("Calibri", Font.PLAIN, 13));
		sasCommandLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_sasCommandLabel = new GridBagConstraints();
		gbc_sasCommandLabel.anchor = GridBagConstraints.EAST;
		gbc_sasCommandLabel.insets = new Insets(5, 5, 5, 10);
		gbc_sasCommandLabel.gridx = 0;
		gbc_sasCommandLabel.gridy = 2;
		panel.add(sasCommandLabel, gbc_sasCommandLabel);
		
		if(SDMXVersion.V2 == SDMXClientFactory.getProviders().get(provider).getSdmxVersion())
			sasCommandText = new JTextField("%gettimeseries(provider=\"" + provider + "\", tsKey=\"" + dataflow + "/" + queryString + "\", metadata=1);");
		else
			sasCommandText = new JTextField("NOT AVAILABLE");
		sasCommandLabel.setLabelFor(sasCommandText);
		GridBagConstraints gbc_sasCommandText = new GridBagConstraints();
		gbc_sasCommandText.fill = GridBagConstraints.BOTH;
		gbc_sasCommandText.insets = new Insets(0, 0, 5, 0);
		gbc_sasCommandText.gridx = 1;
		gbc_sasCommandText.gridy = 2;
		panel.add(sasCommandText, gbc_sasCommandText);
		
		JLabel stataCommandLabel = new JLabel("STATA:");
		stataCommandLabel.setDisplayedMnemonic('A');
		stataCommandLabel.setFont(new Font("Calibri", Font.PLAIN, 13));
		stataCommandLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_stataCommandLabel = new GridBagConstraints();
		gbc_stataCommandLabel.anchor = GridBagConstraints.EAST;
		gbc_stataCommandLabel.insets = new Insets(5, 5, 5, 10);
		gbc_stataCommandLabel.gridx = 0;
		gbc_stataCommandLabel.gridy = 3;
		panel.add(stataCommandLabel, gbc_stataCommandLabel);
		
		if(SDMXVersion.V2 == SDMXClientFactory.getProviders().get(provider).getSdmxVersion())
			stataCommandText = new JTextField("getTimeSeries " + provider + " " + dataflow + "/" + queryString + " \"\" \"\" 0 0");
		else
			stataCommandText = new JTextField("NOT AVAILABLE");
		stataCommandLabel.setLabelFor(stataCommandText);
		GridBagConstraints gbc_stataCommandText = new GridBagConstraints();
		gbc_stataCommandText.insets = new Insets(0, 0, 5, 0);
		gbc_stataCommandText.fill = GridBagConstraints.BOTH;
		gbc_stataCommandText.gridx = 1;
		gbc_stataCommandText.gridy = 3;
		panel.add(stataCommandText, gbc_stataCommandText);
		
		JLabel urlLabel = new JLabel("URL:");
		urlLabel.setDisplayedMnemonic('U');
		urlLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		urlLabel.setFont(new Font("Calibri", Font.PLAIN, 13));
		GridBagConstraints gbc_urlLabel = new GridBagConstraints();
		gbc_urlLabel.insets = new Insets(5, 5, 5, 10);
		gbc_urlLabel.anchor = GridBagConstraints.EAST;
		gbc_urlLabel.gridx = 0;
		gbc_urlLabel.gridy = 4;
		panel.add(urlLabel, gbc_urlLabel);
		
		if(SDMXVersion.V2 == SDMXClientFactory.getProviders().get(provider).getSdmxVersion())
			urlText = new JTextField(SdmxClientHandler.getDataURL(provider, dataflow, queryString, null, null, false, null, false));
		else
			urlText = new JTextField(SdmxClientHandler.getDataURL(provider, dataflow, queryString, null, null, false, null, false));
		urlLabel.setLabelFor(urlText);
		GridBagConstraints gbc_urlText = new GridBagConstraints();
		gbc_urlText.fill = GridBagConstraints.BOTH;
		gbc_urlText.gridx = 1;
		gbc_urlText.gridy = 4;
		panel.add(urlText, gbc_urlText);

		JPanel bottom = new JPanel();
		bottom.setPreferredSize(new Dimension(200, 60));
		bottom.setMinimumSize(new Dimension(0, 60));
		bottom.setMaximumSize(new Dimension(32767, 60));
		bottom.setBorder(new EmptyBorder(20, 20, 20, 20));
		getContentPane().add(bottom, BorderLayout.SOUTH);
		bottom.setLayout(new BorderLayout(0, 0));
		
		lblNewLabel = new JLabel("Copied to clipboard");
		lblNewLabel.setVisible(false);
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setForeground(Color.RED);
		lblNewLabel.setFont(new Font("Arial", Font.BOLD, 20));
		bottom.add(lblNewLabel, BorderLayout.CENTER);
		
		Component rigidArea = Box.createRigidArea(new Dimension(20, 20));
		bottom.add(rigidArea, BorderLayout.WEST);
		
		Component rigidArea_1 = Box.createRigidArea(new Dimension(20, 20));
		bottom.add(rigidArea_1, BorderLayout.EAST);

		for (JTextField field: new JTextField[] { rCommandText, matlabCommandText, sasCommandText, stataCommandText, urlText })
		{
			addListeners(field);
			setVisuals(field);
		}
		
	    setVisible(true);
	}
	
	public void addListeners(final JTextField textField)
	{
		CaretListener caretListener = new CaretListener() {
			public void caretUpdate(CaretEvent event) {
				String selection;
				int start = event.getDot();
				int stop = event.getMark();
				String text = textField.getText();
				if (start > stop)
					selection = text.substring(stop, start);
				else if (start < stop)
					selection = text.substring(start, stop);
				else
					selection = "";
				if (!selection.isEmpty())
				{
					clipboard.setContents(new StringSelection(selection), null);
					lblNewLabel.setVisible(true);
					Timer timer = new Timer(1000, new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent var1)
						{
							lblNewLabel.setVisible(false);
						}
					});
			        timer.setRepeats(false);
			        timer.start();
				}
			}
		};

		FocusAdapter focusAdapter = new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent event) {
				textField.selectAll();
			}
		};

		textField.addCaretListener(caretListener);
		textField.addFocusListener(focusAdapter);
	}

	public void setVisuals(JTextField textField)
	{
		textField.setMinimumSize(new Dimension(7, 25));
		textField.setBackground(WHITE);
		textField.setEditable(false);
		textField.setFont(new Font("Consolas", PLAIN, 12));
		textField.setColumns(10);
	}
}