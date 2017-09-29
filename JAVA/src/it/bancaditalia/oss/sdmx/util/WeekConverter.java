
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

package it.bancaditalia.oss.sdmx.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import it.bancaditalia.oss.sdmx.exceptions.SdmxInvalidParameterException;

// used in MATLAB
public class WeekConverter {
	public static String convert(String week) throws SdmxInvalidParameterException{
		// aaaa-Wn: e.g. 2010-W32
		SimpleDateFormat dayFormatter = new SimpleDateFormat("yyyy-MM-dd");
		String toks[] = week.split("-W");
		if(toks.length != 2){
			throw new SdmxInvalidParameterException("Invalid weekly date format: " + week);
		}
		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.YEAR, new Integer(toks[0]));
		calendar.set(Calendar.WEEK_OF_YEAR, new Integer(toks[1]));     
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY); //end of period
		
		return dayFormatter.format(calendar.getTime());
	}
}

