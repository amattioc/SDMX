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

/**
 * 
 * @author Valentino Pinna
 */
public abstract class SdmxMetaElement
{
	private final String id;
	private final String name;
	private final Codelist codeList;

	/**
	 * Creates a sdmx metadata element with id, description and codelist
	 * 
	 * @param id The id of this metadata element
	 * @param name The description
	 * @param codeList the codelist
	 */
	public SdmxMetaElement(String id, String name, Codelist codeList)
	{
		this.id = id;
		this.name = name;
		this.codeList = codeList;
	}

	/**
	 * @return The id of this metadata element
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * @return The codelist of this metadata element
	 */
	public Codelist getCodeList()
	{
		return codeList;
	}

	/**
	 * @return The name of this metadata element
	 */
	public String getName()
	{
		return name;
	}
//
//	@Override
//	public String toString()
//	{
//		StringBuilder builder = new StringBuilder();
//		builder.append("Attribute [id=").append(id).append(", name=").append(name).append(", codelist=")
//				.append(codeList).append("]\n");
//		return builder.toString();
//	}
}
