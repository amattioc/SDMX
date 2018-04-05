package it.bancaditalia.oss.sdmx.ut;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;

import java.util.List;
import java.util.Map;

import org.junit.Test;

public class ISTATTest {
	@Test
	public void testGetFlows() throws SdmxException {
		Map<String, String> f = SdmxClientHandler.getFlows("ISTAT", null);
		assertNotNull("Null getFlows result", f);
		String descr = f.get("144_125");
		assertEquals("Wrong description for 144_125", "Consumer price index for the whole nation - annual average (NIC - until 2010)", descr);
	}

	@Test
	public void testGetTimeSeriesFromID() throws SdmxException {
		List<PortableTimeSeries<Double>> res = SdmxClientHandler.getTimeSeries("ISTAT", "115_362/M....", null, null);
		assertNotNull("Null time series result", res);
		
		//warning: they depend on eventual order
		String monthly = res.get(0).getName();
		assertEquals("Wrong name for first time series", "115_362.M.F.N.IT.CONS_PROD", monthly);
		//System.out.println(res);
	}
	
	public static void main(String[] args) throws SdmxException
	{
		System.out.println(SdmxClientHandler.getTimeSeries("ISTAT", "117_262/M.0017.TOTAL.N.IT.D283_10+D281_10", null, null));
	}

}
