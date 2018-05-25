package it.bancaditalia.oss.sdmx.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import it.bancaditalia.oss.sdmx.api.Dimension;
import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.client.custom.IMF2;
import it.bancaditalia.oss.sdmx.client.custom.NBB;
import it.bancaditalia.oss.sdmx.client.custom.OECD;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;

@RunWith(Parameterized.class)
public class DataflowsIT
{
	@Parameters(name="{0} - {1}")
    public static Collection<Object[]> data() {
        return FilterProvidersToTest.filter(new Object[][] 
    		{
				{ "ABS",              null,                              "ATSI_BIRTHS_SUMM",              "Aboriginal and Torres Strait Islander births and confinements, summary, by state" },
				{ "ECB",              "*Exchange*",                      "EXR",                           "Exchange Rates" },
				{ "EUROSTAT",         "prc_hicp_midx",                   "prc_hicp_midx",                 "HICP (2015 = 100) - monthly data (index)" },
				{ "ILO",              "*DF_YI_ALL_EMP_TEMP_SEX_AGE_NB*", "DF_YI_ALL_EMP_TEMP_SEX_AGE_NB", "Employment by sex and age" },
				{ "IMF2",             "DS-WHDREO",                       "DS-WHDREO",                     "Western Hemisphere Regional Economic Outlook (WHDREO)" },
				{ "IMF_SDMX_CENTRAL", "SPI",                             "SPI",                           "Stock Market: Share Price Index" },
				{ "INEGI",            "DF_STEI",                         "DF_STEI",                       "Dataflow Short Term Economic Indicators" },
				{ "INSEE",            null,                              "CNA-2010-CONSO-SI",             "Final consumption expenditure by institutional sectors" },
				{ "WITS",             "*Tariff*",                        "DF_WITS_Tariff_TRAINS",         "WITS - UNCTAD TRAINS Tariff Data" },
				{ "WB",               "WDI",                             "WDI",                           "World Development Indicators" },
				{ "UNDATA",           null,                              "DF_UNDATA_COUNTRYDATA",         "SDMX-CountryData" },
				{ "UIS",              null,                              "CE",                            "Cultural employment" },
				{ "OECD",             "AEO",                             "AEO",                           "African Economic Outlook" },
				{ "NBB",              "AFCSURV",                         "AFCSURV",                       "Quarterly survey on the assessment of financing conditions" },
				{ "ISTAT",            null,                              "144_125",                       "Consumer price index for the whole nation - annual average (NIC - until 2010)" },
    		}, 0);
    }

	@Parameter(0) public String provider;
    @Parameter(1) public String dataflowPattern;
    @Parameter(2) public String testDataflow;
    @Parameter(3) public String expectedDescription;

	@Test
	public void getDataflows() throws SdmxException 
	{
		Map<String, String> f = SdmxClientHandler.getFlows(provider, dataflowPattern);
		assertNotNull("Null getFlows result", f);
		assertEquals("Wrong dataflow description", expectedDescription, f.get(testDataflow)); 
	}
}
