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

public class Dimension extends SdmxMetaElement
{
	private final int position;

	/**
	 * Creates a Dimension with given attributes.
	 * 
	 * @param id       The dimension id
	 * @param name     The description
	 * @param codeList the Codelist object associated with this dimension.
	 * @param position The dimension ordinality in the dataflow structure
	 */
	public Dimension(String id, String name, Codelist codeList, int position)
	{
		super(id, name, codeList);
		this.position = position;
	}

	/**
	 * @return The dimension ordinality in the dataflow structure.
	 */
	public int getPosition()
	{
		return position;
	}

	@Override
	public String toString()
	{
		return String.format("Dimension [id=%s, position=%d, codelist=%s]\n", getId(), position, getCodeList());
	}
}
