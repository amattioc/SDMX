package it.bancaditalia.oss.sdmx.client;

import it.bancaditalia.oss.sdmx.exceptions.SdmxException;

public class FileTest {
	public static void main(String[] args) throws SdmxException {
		SdmxClientHandler.addLocalProvider("TestFile", "D:/Dati/Profili/e922480/Desktop/FILETEST", "Test provider for files");
		System.err.println( SdmxClientHandler.getCodes("TestFile", "EXR", "FREQ"));
		//System.err.println( SdmxClientHandler.getTimeSeries("TestFile","EXR/.GBP+USD.EUR.SP00.A", null, null));
		
		SdmxClientHandler.addLocalProvider("TestFile2", "src/it/bancaditalia/oss/sdmx/client/custom", "Test provider for files");
		System.err.println( SdmxClientHandler.getTimeSeries("TestFile2","EXR/.GBP+USD.EUR.SP00.A", null, null));
	}
}
