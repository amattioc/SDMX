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

/*
 *      Execute getTimeSeries call. Parameters:
 *
 *      provider: the id of the data provider
 *      tskey: the SDMX identifier of the resource to be retrieved
 *      start: start time
 *      end: end time
 *      meta: handle or not metadata
 *	      0: only data (default)
 *	      1: data and metadata (can be very resource consuming)
 *	      2: only metadata
 *      force: if set to 1, eventual data in memory will be cleared
 *
 */

program getTimeSeries
	version 17
	args provider tskey start end meta force

	if _N > 0 {
		if "`force'" != "1" {
			display "Data would be lost. Please clear the dataset and retry."
			exit
		}
		else {
			clear
		}
	}

	java, shared(BItools): SgetTimeSeries("`provider'", "`tskey'", "`start'", "`end'", "`meta'");
end

quietly initSDMX

java, shared(BItools):
	/open GetTimeSeries.java
end
