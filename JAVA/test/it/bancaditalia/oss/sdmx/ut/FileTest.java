package it.bancaditalia.oss.sdmx.ut;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.Dimension;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.util.Configuration;

public class FileTest {
	private static String directory = "FileProviderTestFiles";
	
	@BeforeClass
	public static void runOnceBeforeClass() throws SdmxException {
		File f = new File(directory);
		f.mkdir();
		Configuration.setDumpPrefix(directory);
		SdmxClientHandler.getTimeSeries("ECB", "EXR.M.USD.EUR.SP00.A",  null, null);
		SdmxClientHandler.addLocalProvider("TestFile", directory, "Test provider for files");
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
		String result = "[Dimension [id=FREQ, position=1, codelist=Codelist [id=ECB/CL_FREQ/1.0, codes=null";
		assertEquals("Wrong dimensions for EXR", result, dim.toString().substring(0, result.length()));
	}

	
	@Test
	public void testSeries() throws SdmxException
	{
		List<PortableTimeSeries> res = SdmxClientHandler.getTimeSeries("TestFile","EXR.M.USD.EUR.SP00.A", null, null);
		assertNotNull("Null time series result", res);
		assertEquals("Wrong ts number for EXR", res.size(), 1);
		assertEquals("Wrong ts name for EXR", res.get(0).getName(), "EXR.M.USD.EUR.SP00.A");
	}
	
}
