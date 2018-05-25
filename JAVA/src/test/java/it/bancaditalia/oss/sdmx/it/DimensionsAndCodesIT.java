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
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;

@RunWith(Parameterized.class)
public class DimensionsAndCodesIT
{
	@Parameters(name="{0} - {1}")
    public static Collection<Object[]> data() {
        return FilterProvidersToTest.filter(new Object[][] 
    		{
    			{ "ABS",      "ATSI_BIRTHS_SUMM",              "FREQUENCY",  "A",   "Annual",                      "[Dimension [id=MEASURE, position=1, codelist=Codelist [id=ABS/CL_ATSI_BIRTHS_SUMM_MEASURE, codes={19=Births where only mother is Aboriginal or Torres Strait Islander, 18=Births where both parents are Aboriginal or Torres Strait Islander, 15=Median age of father, 13=Confinements, 14=Median age of mother, 20=Births where" },
    			{ "ECB",      "EXR",                           "FREQ",       "A",   "Annual",                      "[Dimension [id=FREQ, position=1, codelist=Codelist [id=ECB/CL_FREQ/1.0, codes={D=Daily, E=Event (not supported), W=Weekly, Q=Quarterly, A=Annual, B=Business, S=Half Yearly, semester (value H exists but change to S in 2009, move from H to this new value to be agreed in ESCB context), M=Monthly, N=Minutely, H=Half-yearly}" },
    			{ "WITS",     "DF_WITS_Tariff_TRAINS",         "FREQ",       "A",   "Annual",                      "[Dimension [id=FREQ, position=1, codelist=Codelist [id=WBG_WITS/CL_FREQ_WITS/1.0, codes={A=Annual}]]" },
    			{ "UNDATA",   "DF_UNDATA_COUNTRYDATA",         "FREQ",       "A",   "Annual",                      "[Dimension [id=FREQ, position=1, codelist=Codelist [id=IAEG/CL_FREQ_MDG/1.0, codes={3A=Three-year average, Q=Quarterly, A=Annual, S=Half-yearly, semester, 2A=Two-year average, M=Monthly}]]" },
    			{ "UIS",      "CE",                            "REF_AREA",   "IT",  "Italy",                       "[Dimension [id=FCS_DOMAIN, position=1, codelist=Codelist [id=UNESCO/CL_FCS_DOMAIN/1.0, codes={DESIG" },
    			{ "ISTAT",    "144_125",                       "FREQ",       "A",   "annual",                      "[Dimension [id=FREQ, position=1, codelist=Codelist [id=IT1/CL_FREQ/1.0, codes={D=daily, E=event (not supported), W=weekly, Q=quarterly, A=an" },
    			{ "INSEE",    "CNA-2010-CONSO-SI",             "FREQ",       "A",   "Annual",                      "[Dimension [id=FREQ, position=1, codelist=Codelist [id=FR1/CL_PERIODICITE/1.0, codes={T=Quarterly, A=Annual, B=Two-monthly, S=Semi-annual, M=Monthly}]]" },
    			{ "INEGI",    "DF_STEI",                       "REF_AREA",   "MX",  "Mexico",                      "[Dimension [id=REF_AREA, position=1, codelist=Codelist [id=SDMX/CL_AREA/1.0, codes={VU=Vanuatu, EC=Ecuador, VN=Viet Nam, VI=U.S.\", DZ=Algeria, VG=British\", DM=Dominica, VE=Venezuela, DO=Dominican " },
    			{ "ILO",      "DF_YI_ALL_EMP_TEMP_SEX_AGE_NB", "COLLECTION", "MIG", "Labour Migration Statistics", "[Dimension [id=COLLECTION, position=1, codelist=Codelist [id=ILO/CL_COLLECTION, codes={YTH=youthSTATS - discontinued (latest period: 2013), SSI=Social Security Indicators, KIST=Key I" },
    			{ "EUROSTAT", "prc_hicp_midx",                 "FREQ",       "A",   "Annual",                      "[Dimension [id=FREQ, position=1, codelist=Codelist [id=ESTAT/CL_FREQ/1.0, codes={D=Daily, W=Weekly, Q=Quarterly, A=Annual, S=Semi-annual, M=Monthly, H" },
    			{ "IMF2",     "DS-WHDREO",                     "FREQ",       "A",   "Annual",                      "[Dimension [id=FREQ, position=1, codelist=Codelist [id=IMF/CL_FREQ, codes={D=Daily, W=Weekly, Q=Quarterly, A=Annu" },
    			{ "IMF_SDMX_CENTRAL",     "SPI",               "FREQ",       "A",   "Annual",                      "[Dimension [id=DATA_DOMAIN, position=1, codelist=Codelist [id=IMF/CL_DATADOMAIN/1.0, codes={SPI=S" },
    			{ "NBB",      "AFCSURV",                       "FREQUENCY",  "M",   "Monthly",                     "[Dimension [id=AFCSURV_INDIC, position=1, codelist=Codelis" },
    			{ "OECD",     "QNA",                           "FREQUENCY",  "A",   "Annual",                      "[Dimension [id=LOCATION, position=1, codelist=Codelist [id=OECD/CL_QNA_LOCATION, codes={G-7=G7, AUS=Australia, P" },
    			{ "WB",       "WDI",                           "REF_AREA",   "GRL", "Greenland",                   "" }
    		}, 0);
    }

	@Parameter(0) public String provider;
    @Parameter(1) public String dataflow;
    @Parameter(2) public String dimension;
    @Parameter(3) public String code;
    @Parameter(4) public String expectedCodeDescription;
    @Parameter(5) public String expectedCodelist;

	@Test
	public void testDimensionsAndCodes() throws SdmxException {
		if (dimension != null)
		{
			Map<String, String> codes = SdmxClientHandler.getCodes(provider, dataflow, dimension);
			assertNotNull("Null getCodes result", codes);
			assertEquals("Wrong code", expectedCodeDescription, codes.get(code));
		}
		List<Dimension> dim = SdmxClientHandler.getDimensions(provider, dataflow);
		assertNotNull("Null getDimensions result", dim);
		assertEquals("Wrong dimensions", expectedCodelist, dim.toString().substring(0, expectedCodelist.length()));
	}
}
