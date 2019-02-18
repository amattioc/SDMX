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
						new String[][] { { "19", "Births where only mother is Aboriginal or Torres Strait Islander" },
								{ "18", "Births where both parents are Aboriginal or Torres Strait Islander" }, { "15", "Median age of father" },
								{ "13", "Confinements" }, { "14", "Median age of mother" },
								{ "20", "Births where only father is Aboriginal or Torres Strait Islander" }, { "1", "Births" },
								{ "10", "Ex-nuptial, paternity not acknowledged births" }, { "7", "Nuptial births" }, { "5", "Female births" },
								{ "4", "Male births" }, { "9", "Ex-nuptial, paternity acknowledged births" }, { "8", "Ex-nuptial births" } } },
				{ "ECB", "EXR", 0, new String[][] { { "D", "Daily" }, { "E", "Event (not supported)" }, { "W", "Weekly" },
						{ "Q", "Quarterly" }, { "A", "Annual" }, { "B", "Business" },
						{ "S", "Half Yearly, semester (value H exists but change to S in 2009, move from H to this new value to be agreed in ESCB context)" },
						{ "M", "Monthly" }, { "N", "Minutely" }, { "H", "Half-yearly" } } },
				{ "WITS", "DF_WITS_Tariff_TRAINS", 0, new String[][] {} },
				{ "UNDATA", "DF_UNDATA_COUNTRYDATA", 0,
						new String[][] { { "3A", "Three-year average" }, { "Q", "Quarterly" }, { "A", "Annual" }, { "S", "Half-yearly, semester" },
								{ "2A", "Two-year average" }, { "M", "Monthly" } } },
				{ "UIS", "CE", 0, new String[][] { { "DESIGN", "Design and creative services" },
						{ "CULT_NH", "Cultural and natural heritage" }, { "V_ARTS", "Visual arts and crafts" }, { "PERF", "Performance and celebration" },
						{ "AV_MEDIA", "Audio-visual and interactive media" }, { "EDU", "Education and training" }, { "PRINT", "Books and press" },
						{ "ICH", "Intangible cultural heritage" }, { "_T_ICH", "Cultural domain or intangible cultural heritage" }, { "_T", "Total" } } },
				{ "ISTAT", "144_125", 0,
						new String[][] { { "D", "daily" }, { "E", "event (not supported)" }, { "W", "weekly" }, { "Q", "quarterly" }, { "A", "annual" },
								{ "B", "business (not supported)" }, { "M", "monthly" }, { "H", "half-yearly" } } },
				{ "INSEE", "CNA-2010-CONSO-SI", 0,
						new String[][] { { "T", "Quarterly" }, { "A", "Annual" }, { "B", "Two-monthly" }, { "S", "Semi-annual" }, { "M", "Monthly" } } },
				{ "INEGI", "DF_STEI", 0, new String[][] { { "VU", "Vanuatu" }, { "EC", "Ecuador" }, { "VN", "Viet Nam" },
						{ "VI", "U.S.\"" }, { "DZ", "Algeria" }, { "VG", "British\"" }, { "DM", "Dominica" }, { "VE", "Venezuela" },
						{ "DO", "Dominican Republic" }, { "VC", "Saint Vincent And The Grenadines" }, { "VA", "Holy See (Vatican City State)" },
						{ "DE", "Germany" }, { "UZ", "Uzbekistan" }, { "UY", "Uruguay" }, { "DK", "Denmark" }, { "DJ", "Djibouti" }, { "US", "United States" },
						{ "UM", "United States Minor Outlying Islands" }, { "UG", "Uganda" }, { "UA", "Ukraine" }, { "ET", "Ethiopia" }, { "ES", "Spain" },
						{ "ER", "Eritrea" }, { "EH", "Western Sahara" }, { "EG", "Egypt" }, { "EE", "Estonia" }, { "TZ", "United Republic Of\"" },
						{ "TT", "Trinidad And Tobago" }, { "TW", "Province Of China\"" }, { "TV", "Tuvalu" }, { "GD", "Grenada" }, { "GE", "Georgia" },
						{ "GF", "French Guiana" }, { "GA", "Gabon" }, { "GB", "United Kingdom" }, { "FR", "France" }, { "FO", "Faroe Islands" },
						{ "FK", "Falkland Islands (Malvinas)" }, { "FJ", "Fiji" }, { "FM", "Federated States Of\"" }, { "FI", "Finland" },
						{ "Z", "Not Applicable" }, { "WS", "Samoa" }, { "GY", "Guyana" }, { "GW", "Guinea-Bissau" }, { "GU", "Guam" }, { "GT", "Guatemala" },
						{ "GS", "South Georgia And The South Sandwich Islands" }, { "GR", "Greece" }, { "GQ", "Equatorial Guinea" }, { "GP", "Guadeloupe" },
						{ "WF", "Wallis And Futuna" }, { "GN", "Guinea" }, { "GM", "Gambia" }, { "GL", "Greenland" }, { "GI", "Gibraltar" }, { "GH", "Ghana" },
						{ "GG", "Guernsey" }, { "RE", "Réunion" }, { "RO", "Romania" }, { "AT", "Austria" }, { "AS", "American Samoa" }, { "AR", "Argentina" },
						{ "AQ", "Antarctica" }, { "AX", "Åland Islands" }, { "AW", "Aruba" }, { "QA", "Qatar" }, { "AU", "Australia" }, { "AZ", "Azerbaijan" },
						{ "BA", "Bosnia And Herzegovina" }, { "PT", "Portugal" }, { "AD", "Andorra" }, { "PW", "Palau" }, { "AG", "Antigua And Barbuda" },
						{ "PR", "Puerto Rico" }, { "AE", "United Arab Emirates" }, { "AF", "Afghanistan" }, { "PS", "Occupied\"" }, { "AL", "Albania" },
						{ "AI", "Anguilla" }, { "AO", "Angola" }, { "PY", "Paraguay" }, { "AM", "Armenia" }, { "AN", "Netherlands Antilles" },
						{ "BW", "Botswana" }, { "TG", "Togo" }, { "BV", "Bouvet Island" }, { "TF", "French Southern Territories" }, { "BY", "Belarus" },
						{ "TD", "Chad" }, { "BS", "Bahamas" }, { "TK", "Tokelau" }, { "BR", "Brazil" }, { "TJ", "Tajikistan" }, { "BT", "Bhutan" },
						{ "TH", "Thailand" }, { "TO", "Tonga" }, { "TN", "Tunisia" }, { "TM", "Turkmenistan" }, { "CA", "Canada" }, { "TL", "Timor-Leste" },
						{ "BZ", "Belize" }, { "TR", "Turkey" }, { "BF", "Burkina Faso" }, { "BG", "Bulgaria" }, { "SV", "El Salvador" }, { "BH", "Bahrain" },
						{ "BI", "Burundi" }, { "ST", "Sao Tome And Principe" }, { "BB", "Barbados" }, { "SY", "Syrian Arab Republic" }, { "SZ", "Swaziland" },
						{ "BD", "Bangladesh" }, { "BE", "Belgium" }, { "BN", "Brunei Darussalam" }, { "BO", "Bolivia" }, { "BJ", "Benin" },
						{ "TC", "Turks And Caicos Islands" }, { "BL", "Saint Barthélemy" }, { "BM", "Bermuda" }, { "CZ", "Czech Republic" }, { "SD", "Sudan" },
						{ "CY", "Cyprus" }, { "SC", "Seychelles" }, { "CX", "Christmas Island" }, { "SE", "Sweden" }, { "CV", "Cape Verde" },
						{ "SH", "Saint Helena" }, { "CU", "Cuba" }, { "SG", "Singapore" }, { "SJ", "Svalbard And Jan Mayen" }, { "SI", "Slovenia" },
						{ "SL", "Sierra Leone" }, { "SK", "Slovakia" }, { "SN", "Senegal" }, { "SM", "San Marino" }, { "SO", "Somalia" }, { "SR", "Suriname" },
						{ "CI", "Côte D´Ivoire" }, { "RS", "Serbia" }, { "CG", "Congo" }, { "RU", "Russian Federation" }, { "CH", "Switzerland" },
						{ "CF", "Central African Republic" }, { "RW", "Rwanda" }, { "CC", "Cocos (Keeling) Islands" },
						{ "CD", "The Democratic Republic Of The\"" }, { "CR", "Costa Rica" }, { "CO", "Colombia" }, { "CM", "Cameroon" }, { "CN", "China" },
						{ "CK", "Cook Islands" }, { "SA", "Saudi Arabia" }, { "CL", "Chile" }, { "SB", "Solomon Islands" }, { "LV", "Latvia" },
						{ "LU", "Luxembourg" }, { "LT", "Lithuania" }, { "LY", "Libyan Arab Jamahiriya" }, { "LS", "Lesotho" }, { "LR", "Liberia" },
						{ "MG", "Madagascar" }, { "MH", "Marshall Islands" }, { "ME", "Montenegro" }, { "MF", "Saint Martin" },
						{ "MK", "The Former Yugoslav Republic Of\"" }, { "ML", "Mali" }, { "MC", "Monaco" }, { "MD", "Republic Of\"" }, { "MA", "Morocco" },
						{ "MV", "Maldives" }, { "MU", "Mauritius" }, { "MX", "Mexico" }, { "MW", "Malawi" }, { "MZ", "Mozambique" }, { "MY", "Malaysia" },
						{ "MN", "Mongolia" }, { "MM", "Myanmar" }, { "MP", "Northern Mariana Islands" }, { "MO", "Macao" }, { "MR", "Mauritania" },
						{ "MQ", "Martinique" }, { "MT", "Malta" }, { "MS", "Montserrat" }, { "NF", "Norfolk Island" }, { "NG", "Nigeria" },
						{ "NI", "Nicaragua" }, { "NL", "Netherlands" }, { "NA", "Namibia" }, { "NC", "New Caledonia" }, { "NE", "Niger" },
						{ "NZ", "New Zealand" }, { "NU", "Niue" }, { "NR", "Nauru" }, { "NP", "Nepal" }, { "NO", "Norway" }, { "OM", "Oman" },
						{ "PL", "Poland" }, { "PM", "Saint Pierre And Miquelon" }, { "PN", "Pitcairn" }, { "PH", "Philippines" }, { "PK", "Pakistan" },
						{ "PE", "Peru" }, { "PF", "French Polynesia" }, { "PG", "Papua New Guinea" }, { "PA", "Panama" }, { "HK", "Hong Kong" },
						{ "ZA", "South Africa" }, { "HN", "Honduras" }, { "HM", "Heard Island And Mcdonald Islands" }, { "HR", "Croatia" }, { "HT", "Haiti" },
						{ "HU", "Hungary" }, { "ZM", "Zambia" }, { "ID", "Indonesia" }, { "ZW", "Zimbabwe" }, { "IE", "Ireland" }, { "IL", "Israel" },
						{ "IM", "Isle Of Man" }, { "IN", "India" }, { "IO", "British Indian Ocean Territory" }, { "IQ", "Iraq" },
						{ "IR", "Islamic Republic Of\"" }, { "IS", "Iceland" }, { "YE", "Yemen" }, { "IT", "Italy" }, { "JE", "Jersey" }, { "YT", "Mayotte" },
						{ "JP", "Japan" }, { "JO", "Jordan" }, { "JM", "Jamaica" }, { "KI", "Kiribati" }, { "KH", "Cambodia" }, { "KG", "Kyrgyzstan" },
						{ "KE", "Kenya" }, { "KP", "Democratic People´s Republic Of\"" }, { "KR", "Republic Of\"" }, { "KM", "Comoros" },
						{ "KN", "Saint Kitts And Nevis" }, { "KW", "Kuwait" }, { "KY", "Cayman Islands" }, { "KZ", "Kazakhstan" },
						{ "LA", "Lao People´s Democratic Republic" }, { "LC", "Saint Lucia" }, { "LB", "Lebanon" }, { "LI", "Liechtenstein" },
						{ "LK", "Sri Lanka" } } },
				{ "ILO", "DF_YI_ALL_EMP_TEMP_SEX_AGE_NB", 0, new String[][] { { "YI", "Yearly indicators" } } },
				{ "EUROSTAT", "prc_hicp_midx", 0,
						new String[][] { { "D", "Daily" }, { "W", "Weekly" }, { "Q", "Quarterly" }, { "A", "Annual" }, { "S", "Semi-annual" },
								{ "M", "Monthly" }, { "H", "Half-year" } } },
				{ "IMF2", "DS-WHDREO", 0, 
						new String[][] { { "D", "Daily" }, { "W", "Weekly" }, { "Q", "Quarterly" }, { "A", "Annual" }, { "B", "Bi-annual" },
								{ "M", "Monthly" } } },
				{ "IMF_SDMX_CENTRAL", "SPI", 0, 
						new String[][] { { "SPI", "Share price index" }, { "DSE", "Debt Securities" }, { "DOTS", "Direction of Trade Statistics" },
								{ "MSG", "Monetary and Financial Statistics Aggregates" },
								{ "COF", "COFER: Currency Composition of Foreign Exchange Reserves" }, { "FAS", "Financial Access Survey" },
								{ "IIP6", "International Investment Position (BPM6)" }, { "IIP5", "International Investment Position (BPM5)" },
								{ "IND", "Ind. Of Economic Activity" }, { "EXD", "External debt" }, { "FS1", "FSI: Institution of Deposit Takers and OFCs" },
								{ "FS2", "FSI: Sectoral Financial Statements" }, { "01R", "Exchange Rates and International Reserves" },
								{ "EXR", "Exchange rates" }, { "POP", "Population" }, { "CPI", "Consumer price indices" }, { "FSD", "FSI: Data Report Form" },
								{ "CGO", "Central government operations" }, { "OFS", "Other Financial Corporations (OFC) survey" },
								{ "ILV2", "Template on International Reserves and Foreign Currency Liquidity " }, { "ILV1", "Official reserve assets" },
								{ "CGD", "Central government debt" }, { "FSI", "Financial Soundness Indicators" },
								{ "GFB", "GFS: Budgetary Central Govt: Operational Statement, Cash Statement, Balance Sheet" },
								{ "CPS", "Coordinated Portfolio Investment Survey" }, { "GFC", "GFS: Consolidated Central Govt" },
								{ "RTD", "Reserves Template" }, { "GFG", "GFS: Consolidated General Govt" }, { "CDS", "Coordinated Direct Investment Survey" },
								{ "SRE", "COFER: Currency Composition of Foreign Exchange Reserves" }, { "GBD", "Budgetary Central Government Gross Debt" },
								{ "DCS", "Depository Corporations Survey" }, { "PPI", "Producer price indices" }, { "BOP5", "Balance of Payments (BPM5)" },
								{ "INR", "Interest rates" }, { "BOP6", "Balance of Payments (BPM6)" }, { "LMI", "Labor Market Indicators" },
								{ "INF", "INFER: Instrument Composition of Transactions in Foreign Exchange Reserves" },
								{ "CPIH", "Consumer Price Index Harmonized" }, { "UEM", "Unemployment" }, { "4SR", "M&B: Other Financial Corporations" },
								{ "WOE", "Wages/earnings" }, { "6SR", "M&B: Interest Rates and Share Prices" }, { "GGD", "General government gross debt" },
								{ "SOC", "Socio-Demographic" }, { "MET", "Merchandise trade " }, { "EMP", "Employment" }, { "5SR", "M&B: Monetary Aggregates" },
								{ "90R", "RES: National Accounts" }, { "GGO", "General Government Operations" },
								{ "2SR", "M&B: Other Depository Corporations" }, { "NAG", "National Accounts - GDP" }, { "FLI", "Forward looking Indicators" },
								{ "SBS", "Accumulation Accounts (Sectoral Balance Sheets)" }, { "60R", "RES: Prices, Production, Labor and Trade" },
								{ "1PI", "Coordinated Portfolio Investment Survey" }, { "1DI", "Coordinated Direct Investment Survey" },
								{ "GYQ", "GFS: Yearkbook Questionnaire" }, { "1SR", "M&B: Central Bank" }, { "CBS", "Central Bank Survey" } } },
				{ "NBB", "AFCSURV", 0, new String[][] {} },
				{ "OECD", "QNA", 0, 
						new String[][] { { "G-7", "G7" }, { "AUS", "Australia" }, { "PRT", "Portugal" }, { "ISR", "Israel" }, { "ISL", "Iceland" },
								{ "KOR", "Korea" }, { "EU15", "European Union (15 countries)" }, { "DNK", "Denmark" }, { "NOR", "Norway" },
								{ "DEU", "Germany" }, { "ITA", "Italy" }, { "FIN", "Finland" }, { "JPN", "Japan" }, { "MEX", "Mexico" }, { "CAN", "Canada" },
								{ "G-20", "G20" }, { "IDN", "Indonesia" }, { "NMEC", "Non-OECD Economies" }, { "IND", "India" }, { "OECDE", "OECD - Europe" },
								{ "ARG", "Argentina" }, { "COL", "Colombia" }, { "IRL", "Ireland" }, { "TUR", "Turkey" },
								{ "EU28", "European Union (28 countries)" }, { "POL", "Poland" }, { "EA19", "Euro area (19 countries)" }, { "AUT", "Austria" },
								{ "EST", "Estonia" }, { "ESP", "Spain" }, { "OTF", "OECD - FORMER TOTAL" }, { "ZAF", "South Africa" }, { "GRC", "Greece" },
								{ "FRA", "France" }, { "LUX", "Luxembourg" }, { "HUN", "Hungary" }, { "GBR", "United Kingdom" }, { "CHE", "Switzerland" },
								{ "LVA", "Latvia" }, { "SWE", "Sweden" }, { "SAU", "Saudi Arabia" }, { "CHL", "Chile" }, { "CZE", "Czech Republic" },
								{ "CHN", "China (People's Republic of)" }, { "NLD", "Netherlands" }, { "LTU", "Lithuania" }, { "NZL", "New Zealand" },
								{ "USA", "United States" }, { "OECD", "OECD - Total" }, { "RUS", "Russia" }, { "CRI", "Costa Rica" }, { "BRA", "Brazil" },
								{ "SVK", "Slovak Republic" }, { "NAFTA", "NAFTA" }, { "BEL", "Belgium" }, { "SVN", "Slovenia" } } },
				{ "WB", "WDI", 1, new String[][] { { "SM_POP_NETM", "Net migration" },
						{ "GC_TAX_GSRV_CN", "Taxes on goods and services (current LCU)" },
						{ "SI_POV_NAHC", "Poverty headcount ratio at national poverty lines (% of population)" },
						{ "SH_SGR_CRSK_ZS", "Risk of catastrophic expenditure for surgical care (% of people at risk)" },
						{ "SL_IND_EMPL_MA_ZS", "Employment in industry, male (% of male employment)" },
						{ "NV_IND_MANF_KD_ZG", "Manufacturing, value added (annual % growth)" },
						{ "DT_TDS_DIMF_CD", "IMF repurchases and charges (TDS, current US$)" },
						{ "NY_ADJ_DMIN_CD", "Adjusted savings: mineral depletion (current US$)" },
						{ "DT_GPA_DPPG", "Average grace period on new external debt commitments (years)" },
						{ "SP_DYN_WFRT", "Wanted fertility rate (births per woman)" },
						{ "NY_GNP_PCAP_PP_KD", "GNI per capita, PPP (constant 2011 international $)" },
						{ "SI_POV_URHC", "Urban poverty headcount ratio at national poverty lines (% of urban population)" },
						{ "IT_CEL_SETS", "Mobile cellular subscriptions" }, { "DT_TDS_DPPG_GN_ZS", "Public and publicly guaranteed debt service (% of GNI)" },
						{ "IS_SHP_GCNW_XQ", "Liner shipping connectivity index (maximum value in 2004 = 100)" },
						{ "DT_INR_OFFT", "Average interest on new external debt commitments, official (%)" },
						{ "DT_NFL_BOND_CD", "Portfolio investment, bonds (PPG + PNG) (NFL, current US$)" },
						{ "NY_GDP_DISC_KN", "Discrepancy in expenditure estimate of GDP (constant LCU)" },
						{ "SP_POP_1564_TO_ZS", "Population, ages 15-64 (% of total)" }, { "IC_CRD_PUBL_ZS", "Public credit registry coverage (% of adults)" },
						{ "SL_TLF_0714_FE_ZS", "Children in employment, female (% of female children ages 7-14)" },
						{ "SE_PRM_REPT_MA_ZS", "Percentage of repeaters in primary education, all grades, male (%)" },
						{ "SL_TLF_ACTI_1524_FE_ZS", "Labor force participation rate for ages 15-24, female (%) (modeled ILO estimate)" },
						{ "NY_GDP_COAL_RT_ZS", "Coal rents (% of GDP)" }, { "SL_EMP_SELF_MA_ZS", "Self-employed, male (% of males employed)" },
						{ "MS_MIL_TOTL_TF_ZS", "Armed forces personnel (% of total labor force)" },
						{ "SE_XPD_CPRM_ZS", "Current education expenditure, primary (% of total expenditure in primary public institutions)" },
						{ "DT_AMT_MIDA_CD", "PPG, IDA (AMT, current US$)" },
						{ "SL_EMP_INSV_FE_ZS", "Share of women in wage employment in the nonagricultural sector (% of total nonagricultural employment)" },
						{ "DT_TDS_DECT_GN_ZS", "Total debt service (% of GNI)" },
						{ "DC_DAC_ESPL_CD", "Net bilateral aid flows from DAC donors, Spain (current US$)" },
						{ "SH_STA_ORTH", "Diarrhea treatment (% of children under 5 who received ORS packet)" },
						{ "SH_DYN_NMRT", "Mortality rate, neonatal (per 1,000 live births)" },
						{ "DT_AMT_DLTF_CD", "Principal repayments on external debt, long-term + IMF (AMT, current US$)" },
						{ "SH_ANM_ALLW_ZS", "Prevalence of anemia among women of reproductive age (% of women ages 15-49)" },
						{ "EN_ATM_CO2E_GF_KT", "CO2 emissions from gaseous fuel consumption (kt)" },
						{ "GC_XPN_GSRV_ZS", "Goods and services expense (% of expense)" }, { "NV_IND_TOTL_ZS", "Industry, value added (% of GDP)" },
						{ "SL_UEM_TOTL_FE_ZS", "Unemployment, female (% of female labor force)" }, { "AG_SRF_TOTL_K2", "Surface area (sq. km)" },
						{ "EN_CO2_TRAN_ZS", "CO2 emissions from transport (% of total fuel combustion)" },
						{ "NV_IND_MANF_CD", "Manufacturing, value added (current US$)" }, { "FS_AST_CGOV_GD_ZS", "Claims on central government, etc. (% GDP)" },
						{ "ER_LND_PTLD_ZS", "Terrestrial protected areas (% of total land area)" }, { "AG_LND_AGRI_ZS", "Agricultural land (% of land area)" },
						{ "DT_DOD_MLTC_CD", "PPG, multilateral concessional (DOD, current US$)" },
						{ "NY_TTF_GNFS_KN", "Terms of trade adjustment (constant LCU)" },
						{ "ER_PTD_TOTL_ZS", "Terrestrial and marine protected areas (% of total territorial area)" },
						{ "SE_XPD_SECO_ZS", "Expenditure on secondary as % of government expenditure on education (%)" },
						{ "SE_PRM_REPT_ZS", "Percentage of repeaters in primary education, all grades, both sexes (%)" },
						{ "FM_LBL_BMNY_ZG", "Broad money growth (annual %)" }, { "SE_PRM_TENR", "Adjusted net enrolment rate, primary, both sexes (%)" },
						{ "DT_DOD_PROP_CD", "PPG, other private creditors (DOD, current US$)" }, { "NV_SRV_TETC_ZS", "Services, etc., value added (% of GDP)" },
						{ "SH_STA_WAST_ZS", "Prevalence of wasting, weight for height (% of children under 5)" },
						{ "AG_LND_ARBL_ZS", "Arable land (% of land area)" },
						{ "TX_VAL_MRCH_R4_ZS",
								"Merchandise exports to low- and middle-income economies in Middle East & North Africa (% of total merchandise exports)" },
						{ "IQ_CPA_DEBT_XQ", "CPIA debt policy rating (1=low to 6=high)" },
						{ "ST_INT_XPND_MP_ZS", "International tourism, expenditures (% of total imports)" },
						{ "SL_SLF_0714_ZS", "Children in employment, self-employed (% of children in employment, ages 7-14)" } } } };

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
