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
package it.bancaditalia.oss.sdmx.api;

import java.util.Map;

public class Codelist {
	private String id = null;
	private String agency = null;
	private String version = null;
	// code id --> code description
	private Map<String, String> codes;

	public Codelist(String id, String agency, String version) {
		super();
		this.id = id;
		this.agency = agency;
		this.version = version;
	}
	
	public Codelist() {
		super();
		this.id = null;
		this.agency = null;
		this.version = null; 
	}

	public void setCodes(Map<String, String> codes) {
		this.codes = codes;
	}

	public Map<String, String> getCodes() {
		return codes;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getAgency() {
		return agency;
	}
	public void setAgency(String agency) {
		this.agency = agency;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getFullIdentifier() {
		String codelist = id;
		if(agency!=null){
			codelist = agency + "/" + codelist;
		}
		if(version!=null){
			codelist = codelist +  "/" + version;
		}
		return codelist;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Codelist [id=").append(getFullIdentifier())
			.append(", codes=").append(codes).append("]");
		return builder.toString();
	}

}
