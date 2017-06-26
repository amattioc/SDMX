package it.bancaditalia.oss.sdmx.ut;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.Dimension;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;

public class FileTest {
	
	@BeforeClass
	public static void runOnceBeforeClass() throws SdmxException {
		SdmxClientHandler.addLocalProvider("TestFile", "FileProviderTestFiles", "Test provider for files");
	}
	
	@Test
	public void testFlow() throws SdmxException
	{
		Dataflow flow = SdmxClientHandler.getFlow("TestFile", "EXR");
		assertNotNull("Null getDimensions result", flow);
		assertEquals("Wrong flow for EXR", flow.getId(), "EXR");
		assertEquals("Wrong flow for EXR", flow.getFullIdentifier(), "ECB,EXR,1.0");
	}

	@Test
	public void testDSD() throws SdmxException
	{
		DataFlowStructure dsd = SdmxClientHandler.getDataFlowStructure("TestFile", "EXR");
		assertNotNull("Null getDimensions result", dsd);
		assertEquals("Wrong flow for EXR", dsd.getId(), "ECB_EXR1");
		assertEquals("Wrong flow for EXR", dsd.getFullIdentifier(), "ECB/ECB_EXR1/1.0");
	}

	@Test
	public void testDimensions() throws SdmxException
	{
		List<Dimension> dim = SdmxClientHandler.getDimensions("TestFile", "EXR");
		assertNotNull("Null getDimensions result", dim);
		String result = "[Dimension [id=FREQ, position=1, codelist=Codelist [id=ECB/CL_FREQ/1.0, codes={D=Daily, B=Business, A=Annual, W=Weekly, S=Half Yearly, semester (value H exists but change to S in 2009, move from H to this new value to be agreed in ESCB context), Q=Quarterly, N=Minutely, M=Monthly, H=Half-yearly, E=Event (not supported)}";
		assertEquals("Wrong dimensions for EXR", result, dim.toString().substring(0, result.length()));
	}

	@Test
	public void testCodes() throws SdmxException
	{
		Map<String, String> codes = SdmxClientHandler.getCodes("TestFile", "EXR", "FREQ");
		assertNotNull("Null getCodes result", codes);
		assertEquals("Wrong code for FREQ annual", codes.get("A"), "Annual");
	}
	
	@Test
	public void testSeries() throws SdmxException
	{
		List<PortableTimeSeries> res = SdmxClientHandler.getTimeSeries("TestFile","EXR/.GBP+USD.EUR.SP00.A", null, null);
		assertNotNull("Null time series result", res);
	}
}
