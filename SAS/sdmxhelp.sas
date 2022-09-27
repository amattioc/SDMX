/* Copyright 2010,2014 Bank Of Italy
*
* Licensed under the EUPL, Version 1.1 or newer as soon they
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
%macro sdmxhelp;

%if %sysfunc(getoption(terminal,keyword)) eq NOTERMINAL %then %do;
	%put The SDMX Helper can only be used in SAS Foundation Display Manager.;
	%abort cancel;
%end; %else %do;

data _null_;
declare javaobj jClient ( 'it/bancaditalia/oss/sdmx/helper/SDMXHelper' );
jClient.delete();
run;

%end;

%mend;
