package it.bancaditalia.oss.sdmx.client;

import java.io.File;

import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.util.Configuration;

public class FileTest {
	public static void main(String[] args) throws SdmxException {
		String directory = "FileProviderTestFiles";
		File f = new File(directory);
		f.mkdir();
		Configuration.setDumpPrefix(directory);
		SdmxClientHandler.getTimeSeries("ECB", "EXR.M.USD.EUR.SP00.A",  null, null);
		
		SdmxClientHandler.addLocalProvider("TEST", "FileProviderTestFiles", "my test");
		System.err.println(SdmxClientHandler.getDimensions("TEST", "EXR"));
		System.err.println(SdmxClientHandler.getDataFlowStructure("TEST", "EXR"));
		System.err.println(SdmxClientHandler.getTimeSeries("TEST", "EXR.M.USD.EUR.SP00.A",  null, null));
		
		
	}
}
