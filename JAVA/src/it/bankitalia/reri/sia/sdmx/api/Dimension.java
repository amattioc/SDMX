/* Copyright 2010,2014 Bank Of Italy
*
* Licensed under the EUPL, Version 1.1 or – as soon they
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
package it.bankitalia.reri.sia.sdmx.api;

import java.util.Map;


public class Dimension {
	
	String id = null;
	int position;
	String  codeList = null;
	// code id --> code description
	private Map<String, String> codes;
	
	public Dimension(String id, int position, String codeList) {
		super();
		this.id = id;
		this.position = position;
		this.codeList = codeList;
	}
	
	public Dimension() {
		super();
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public String  getCodeList() {
		return codeList;
	}
	public void setCodeList(String  codeList) {
		this.codeList = codeList;
	}
	
	public void setCodes(Map<String, String> codes) {
		this.codes = codes;
	}

	public Map<String, String> getCodes() {
		return codes;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Dimension [id=").append(id)
			.append(", position=").append(position)
			.append(", codelist=").append(codeList).append("]");
		return builder.toString();
	}

}
