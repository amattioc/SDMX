package it.bancaditalia.oss.sdmx.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
public class DimensionsIT
{
	@Parameters(name="{0} - {1}")
    public static Collection<Object[]> data() {
        return FilterProvidersToTest.filter(new Object[][] 
    		{
    			{ "ABS",              "ATSI_BIRTHS_SUMM",              0, "MEASURE",       "ABS/CL_BIRTHS_MEASURE/1.0.0" 	},
    			{ "ECB",              "EXR",                           0, "FREQ",          "ECB/CL_FREQ/1.0",                },
    			{ "DEMO_SDMXV3",      "EXR",                           0, "FREQ",          "ECB/CL_FREQ/1.0",                },
    			{ "WITS",             "DF_WITS_Tariff_TRAINS",         0, "FREQ",          "WBG_WITS/CL_FREQ_WITS/1.0",      },
    			{ "UNDATA",           "DF_UNDATA_COUNTRYDATA",         0, "FREQ",          "IAEG/CL_FREQ_MDG/1.0",           },
    			{ "ISTAT",            "144_125",                       0, "FREQ",          "IT1/CL_FREQ/1.0",                },
    			{ "ISTAT_RI",         "101_1015",                      0, "FREQ",          "IT1/CL_FREQ/1.0",                },
    			{ "INSEE",            "CNA-2010-CONSO-SI",             0, "FREQ",          "FR1/CL_PERIODICITE/1.0",         },
    			{ "INEGI",            "DF_STEI",                       0, "REF_AREA",      "SDMX/CL_AREA/1.0",               },
    			{ "ILO",              "DF_EMP_TEMP_SEX_AGE_NB", 	   0, "REF_AREA",      "ILO/CL_AREA/1.0",          },
    			{ "EUROSTAT",         "prc_hicp_midx",                 0, "freq",          "ESTAT/FREQ/3.9",              },
    			{ "IMF2",             "DS-WHDREO",                     0, "FREQ",          "IMF/CL_FREQ",                    },
    			{ "IMF",              "CPI",                           0, "COUNTRY",       "IMF/CL_COUNTRY/1.5.1",                    },
    			{ "OECD",             "QNA",                           0, "LOCATION",      "OECD/CL_QNA_LOCATION",           },
    			{ "OECD_NEW",         "DSD_NAMAIN1@DF_QNA_EXPENDITURE_CAPITA",0, "FREQ",   "SDMX/CL_FREQ/2.1",           },
    			{ "OECD_SDMXV3",      "DSD_NAMAIN1@DF_QNA_EXPENDITURE_CAPITA",0, "FREQ",   "SDMX/CL_FREQ/2.1",           },
    			{ "WB",               "WDI",                           1, "SERIES",        "WB/CL_SERIES_WDI/1.0",           },
    			{ "BBK",              "BBASV",                         0, "BBK_STD_FREQ",  "BBK/CL_BBK_STD_FREQ/1.0",        }
    		}, 0);
    }

	@Parameter(0) public String provider;
    @Parameter(1) public String dataflow;
    @Parameter(2) public int position;
    @Parameter(3) public String expectedDimensionID;
    @Parameter(4) public String expectedCodelistId;

	@Test
	public void testDimensionsAndCodes() throws SdmxException 
	{
		List<Dimension> dimensions = SdmxClientHandler.getDimensions(provider, dataflow);
		assertNotNull("Null getDimensions result", dimensions);
		assertTrue("Not enough dimensions in result", dimensions.size() > position);
		Dimension dim = dimensions.get(position);
		assertEquals("Wrong dimension id in position " + position, expectedDimensionID, dim.getId());
		Codelist codelist = (Codelist) dim.getCodeList();
		assertNotNull("Null Codelist for dimension " + dim.getId(), codelist);
		assertEquals("Wrong codelist for dimension " + dim.getId(), expectedCodelistId, codelist.getFullIdentifier());
	}
}
