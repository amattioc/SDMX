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
package it.bancaditalia.oss.sdmx.parser.v21;

import java.util.ArrayList;
import java.util.List;

import it.bancaditalia.oss.sdmx.api.Message;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;

public class DataParsingResult extends ArrayList<PortableTimeSeries<Double>> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Message message = null;
	
	public void setData(List<PortableTimeSeries<Double>> data) {
		clear();
		for (PortableTimeSeries<Double> ts: data)
			add(ts);
	}
	public Message getMessage() {
		return message;
	}
	public void setMessage(Message message) {
		this.message = message;
	}


}
