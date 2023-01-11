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

@RunWith(Parameterized.class)
public class DataflowsIT
{
	@Parameters(name="{0} - {1}")
    public static Collection<Object[]> data() {
        return FilterProvidersToTest.filter(new Object[][] 
    		{
				{ "ABS",              null,                              "ATSI_BIRTHS_SUMM",              "Aboriginal and Torres Strait Islander births and confinements, summary, by state" },
				{ "ECB",              "*Exchange*",                      "EXR",                           "Exchange Rates" },
				{ "EUROSTAT",         "prc_hicp_midx",                   "prc_hicp_midx",                 "HICP - monthly data (index)" },
				{ "ILO",              "*DF_EMP_TEMP_SEX_AGE_NB*", "DF_EMP_TEMP_SEX_AGE_NB", "Employment by sex and age" },
				{ "IMF2",             "DS-WHDREO",                       "DS-WHDREO",                     "Western Hemisphere Regional Economic Outlook (WHDREO)" },
				{ "INSEE",            "BALANCE-PAIEMENTS",               "BALANCE-PAIEMENTS",             "Balance of payments" },
				{ "WITS",             "*Tariff*",                        "DF_WITS_Tariff_TRAINS",         "WITS - UNCTAD TRAINS Tariff Data" },
				{ "WB",               "WDI",                             "WDI",                           "World Development Indicators" },
				{ "UNDATA",           null,                              "DF_UNDATA_COUNTRYDATA",         "SDMX-CountryData" },
				{ "OECD",             "AEO",                             "AEO",                           "African Economic Outlook" },
				{ "ISTAT_RI",            null,                              "163_24",                       "Principali aggregati trimestrali di Contabilità Nazionale" },
				{ "ISTAT",            null,                              "144_125",                       "Nic - annuali sino al  2010" },
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
