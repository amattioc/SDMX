/* Copyright 2010,2014 Bank Of Italy
*
* Licensed under the EUPL, Version 1.1 or - as soon they
* will be approved by the European Commission - subsequent
* versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the
* Licence.
* You may obtain a copy of the Licence at:
*
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in
* writing, software distributed under the Licence is
* distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
* express or implied.
* See the Licence for the specific language governing
* permissions and limitations under the Licence.
*/

package it.bancaditalia.oss.sdmx.ut;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import it.bancaditalia.oss.sdmx.api.DSDIdentifier;
import it.bancaditalia.oss.sdmx.api.Dimension;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;

import java.util.List;
import java.util.Map;

import org.junit.Test;

public class UISTest {
	@Test
	public void getDSDIdentifier() throws SdmxException {
		DSDIdentifier keyF = SdmxClientHandler.getDSDIdentifier("UIS", "CE");
		assertNotNull("Null key family for CE", keyF);
		assertEquals("Wrong Key Family", "CE", keyF.getId());
		assertEquals("Wrong agency", "UNESCO", keyF.getAgency());
	}

	@Test
	public void testGetFlows() throws SdmxException {
		Map<String, String> f = SdmxClientHandler.getFlows("UIS", null);
		assertNotNull("Null getFlows result", f);
		String descr = f.get("CE");
		assertEquals("Wrong description for CE", "Cultural employment", descr);
	}

	@Test
	public void testGetDimensionsAndCodes() throws SdmxException {
		Map<String, String> codes = SdmxClientHandler.getCodes("UIS", "CE", "REF_AREA");
		assertNotNull("Null getCodes result", codes);
		assertEquals("Wrong code for REF_AREA italy", codes.get("IT"), "Italy");
		List<Dimension> dim = SdmxClientHandler.getDimensions("UIS", "CE");
		assertNotNull("Null getDimensions result", dim);
		String result = "[Dimension [id=FCS_DOMAIN, position=1, codelist=Codelist [id=UNESCO/CL_FCS_DOMAIN/1.0, codes=null]], Dimension [id=CULT_TYPE_IND2, position=2, codelist=Codelist [id=UNESCO/CL_CULT_TYPE/1.0, codes=null]], Dimension [id=CULT_TYPE_OCC2, position=3, codelist=Codelist [id=UNESCO/CL_CULT_TYPE/1.0, codes=null]], Dimension [id=CULT_TYPE_IND1, position=4, co";
		assertEquals("Wrong dimensions for CE", result, dim.toString().substring(0, result.length()));
	}

	@Test
	public void testGetTimeSeriesFromID() throws SdmxException {
		List<PortableTimeSeries>  res = SdmxClientHandler.getTimeSeries("UIS", "CE/............IT..", null, "2015");
		assertNotNull("Null time series result", res);
		assertEquals("Wrong result size", 285, res.size());
	}

}
