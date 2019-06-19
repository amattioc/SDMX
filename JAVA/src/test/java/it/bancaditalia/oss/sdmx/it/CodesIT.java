package it.bancaditalia.oss.sdmx.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import it.bancaditalia.oss.sdmx.api.Codelist;
import it.bancaditalia.oss.sdmx.api.Dimension;
import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;

@RunWith(Parameterized.class)
public class CodesIT
{
	@Parameters(name = "{0} - {1} - {5}")
	public static Collection<Object[]> data()
	{
		Object[][] paramsInit = {
				{ "ABS", "ATSI_BIRTHS_SUMM", 0,
						new String[][] { { "19", "Births where only mother is Aboriginal or Torres Strait Islander" } } },
				{ "ECB", "EXR", 0, new String[][] { { "D", "Daily" } } },
				{ "WITS", "DF_WITS_Tariff_TRAINS", 0, new String[][] {} },
				{ "UNDATA", "DF_UNDATA_COUNTRYDATA", 0,
						new String[][] { { "3A", "Three-year average" } } },
				{ "UIS", "CE", 0, new String[][] { { "DESIGN", "Design and creative services" } } },
				{ "ISTAT", "144_125", 0,
						new String[][] { { "D", "daily" } } },
				{ "INSEE", "CNA-2010-CONSO-SI", 0,
						new String[][] { { "T", "Quarterly" } } },
				{ "INEGI", "DF_STEI", 0, new String[][] { { "VU", "Vanuatu" } } },
				{ "ILO", "DF_YI_ALL_EMP_TEMP_SEX_AGE_NB", 0, new String[][] { { "YI", "Yearly indicators" } } },
				{ "EUROSTAT", "prc_hicp_midx", 0,
						new String[][] { { "D", "Daily" } } },
				{ "IMF2", "DS-WHDREO", 0, 
						new String[][] { { "D", "Daily" } } },
				{ "IMF_SDMX_CENTRAL", "SPI", 0, 
						new String[][] { { "SPI", "Share price index" } } },
				{ "NBB", "AFCSURV", 0, new String[][] {} },
				{ "OECD", "QNA", 0, 
						new String[][] { { "G-7", "G7" } } },
				{ "WB", "WDI", 1, new String[][] { { "SM_POP_NETM", "Net migration" } } } };

		List<Object[]> params = new ArrayList<>();
		for (Object[] paramInit : paramsInit)
		{
			String[][] codesValues = (String[][]) paramInit[3];
			for (String[] codeValue : codesValues)
				params.add(new Object[] { paramInit[0], paramInit[1], paramInit[2], codeValue[0], codeValue[1] });
		}

		return FilterProvidersToTest.filter(params.toArray(new Object[0][0]), 0);
	}

	@Parameter(0) public String	provider;
	@Parameter(1) public String	dataflow;
	@Parameter(2) public int	position;
	@Parameter(3) public String	expectedCode;
	@Parameter(4) public String	expectedValue;

	@Test
	public void testCodes() throws SdmxException
	{
		List<Dimension> dimensions = SdmxClientHandler.getDimensions(provider, dataflow);
		Codelist codelist = dimensions.get(position).getCodeList();
		assertTrue("Code not found in codelist " + codelist.getFullIdentifier(), codelist.containsKey(expectedCode));
		assertEquals("Wrong code description for code " + expectedCode + " in codelist " + codelist.getFullIdentifier(), expectedValue,
				codelist.get(expectedCode));
	}
}
