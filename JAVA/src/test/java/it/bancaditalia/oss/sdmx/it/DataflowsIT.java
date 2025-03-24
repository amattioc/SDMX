package it.bancaditalia.oss.sdmx.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.util.Configuration;

@RunWith(Parameterized.class)
public class DataflowsIT
{
	@Parameters(name="{0} - {1}")
    public static Collection<Object[]> data() {
    	Configuration.setLanguages("en");
    	
        return FilterProvidersToTest.filter(new Object[][] 
    		{
				{ "ABS",              null,                              "ATSI_BIRTHS_SUMM",              "Aboriginal and Torres Strait Islander births and confinements, summary, by state" },
				{ "ECB",              "*Exchange*",                      "EXR",                           "Exchange Rates" },
				{ "DEMO_SDMXV3",      "*Exchange*",                      "EXR",                           "Exchange Rates" },
				{ "EUROSTAT",         "PRC_HICP_MIDX",                   "PRC_HICP_MIDX",                 "HICP - monthly data (index)" },
				{ "ILO",              "*DF_EMP_TEMP_SEX_AGE_NB*", 		 "DF_EMP_TEMP_SEX_AGE_NB", 		  "Employment by sex and age" },
				{ "IMF2",             "DS-WHDREO",                       "DS-WHDREO",                     "Western Hemisphere Regional Economic Outlook (WHDREO)" },
				{ "IMF",              "CPI",                             "CPI",                           "Consumer Price Index (CPI)" },
				{ "INSEE",            "BALANCE-PAIEMENTS",               "BALANCE-PAIEMENTS",             "Balance of payments" },
				{ "WITS",             "*Tariff*",                        "DF_WITS_Tariff_TRAINS",         "WITS - UNCTAD TRAINS Tariff Data" },
				{ "WB",               "WDI",                             "WDI",                           "World Development Indicators" },
				{ "UNDATA",           null,                              "DF_UNDATA_COUNTRYDATA",         "SDMX-CountryData" },
				{ "OECD",             "AEO",                             "AEO",                           "African Economic Outlook" },
				{ "OECD_NEW",         "*DF_QNA_EXPENDITURE_CAPITA","DSD_NAMAIN1@DF_QNA_EXPENDITURE_CAPITA","Quarterly GDP per capita" },
				{ "OECD_SDMXV3",      "*DF_QNA_EXPENDITURE_CAPITA","DSD_NAMAIN1@DF_QNA_EXPENDITURE_CAPITA","Quarterly GDP per capita" },
				{ "ISTAT_RI",         null,                              "163_24",                        "National Accounts quarterly main aggregates - editions from October 2019 to September 2024" },
				{ "ISTAT",            null,                              "144_125",                       "Nic - annual data until 2010" },
				{ "BBK",          	  null,                              "BBASV",                         "Deutsche Bundesbank, Statistics on Insurance Corporations (Solvency I + II)" }
    		}, 0);
    }

	@Parameter(0) public String provider;
    @Parameter(1) public String dataflowPattern;
    @Parameter(2) public String testDataflow;
    @Parameter(3) public String expectedDescription;

	@Test
	public void getDataflows() throws SdmxException 
	{
		Map<String, String> result = new HashMap<>();
		for (Entry<String, String> entry: SdmxClientHandler.getFlows(provider, dataflowPattern).entrySet())
			if (entry.getKey().split(",").length > 2)
				result.put(entry.getKey().split(",")[1], entry.getValue());
			else
				result.put(entry.getKey(), entry.getValue());
		assertNotNull("Null getFlows result", result);
		assertEquals("Wrong dataflow description", expectedDescription, result.get(testDataflow)); 
	}
}
