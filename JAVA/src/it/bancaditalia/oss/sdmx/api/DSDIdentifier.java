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

public class DSDIdentifier {
	private String id = null;
	private String agency = null;
	private String version = null;
	
	/**
	 * @param id
	 * @param agency
	 * @param version
	 */
	public DSDIdentifier(String id, String agency, String version) {
		super();
		this.id = id;
		this.agency = agency;
		this.version = version;
	}
	
	/**
	 * 
	 */
	public DSDIdentifier() {
		super();
		this.id = null;
		this.agency = null;
		this.version = null; // for providers that are not handling versions
	}

	/**
	 * @return
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return
	 */
	public String getAgency() {
		return agency;
	}
	/**
	 * @param agency
	 */
	public void setAgency(String agency) {
		this.agency = agency;
	}
	/**
	 * @return
	 */
	public String getVersion() {
		return version;
	}
	/**
	 * @param version
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	
	/**
	 * @return
	 */
	public String getFullIdentifier() {
		String dsd = id;
		if(agency!=null){
			dsd = agency + "/" + dsd;
		}
		if(version!=null){
			dsd = dsd +  "/" + version;
		}
		return dsd;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getFullIdentifier();
	}

}
