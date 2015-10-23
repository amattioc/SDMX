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

%macro gettimeseries (provider=, tsKey=, start="0001", end="9999", metadata=0 );
/*	options  nonotes; */

	/* 
	 * check  Provider & tsKey;
	 */
	%if ( &provider eq OR &provider eq '' OR &provider eq "" ) %then %do;
		%put 'ERROR: the provider parameter is missing or blank. Please set it';
		%return;
	%end;
	%if ( &tsKey eq OR &tsKey eq '' OR &tsKey eq "" ) %then %do;
		%put 'ERROR: the tsKey parameter is missing or blank. Please set it';
		%return;
	%end;
	
	/* check if metadata has to be processed */
	%if ( &metadata = 0 ) %then %do;
		%put 'INFO: the metadata retrieval is disabled (default)';
		%let sdmxmetadata = ;
		%let sdmxobservationsmetadata = ;
	%end;
	%else %do;
		%let sdmxmetadata = sdmxmetadata;
		%let sdmxobservationsmetadata = sdmxobservationsmetadata;
	%end;
	
	data sdmxdata &sdmxmetadata &sdmxobservationsmetadata;
		keep TS_NAME OBS_TIME OBS_VALUE OBS_STATUS META_KEY META_VALUE META_TYPE;
		length nSeries 8.;

		/* build java object ;*/
		declare javaobj jClient ( 'it.bancaditalia.oss.sdmx.client.SASClientHandler' );
		/* call the handler with parameters to get data and metadata */
		jClient.callStaticIntMethod( 'makeGetTimeSeries' , &provider, &tsKey , &start , &end , nSeries);
		jClient.exceptioncheck(e);
		if (e) then do;
			put 'ERROR: exception in calling makeGetTimeSeries';
			stop;
		end;
		jClient.exceptionclear();
		
		put  'INFO: the SDMX call has returned ' nSeries ' time series';
		
		if (nSeries > 0) then do;
			/*
			 *	Data Section
			 */
			/* call the handler to get the size of the internal data section */
			length nObs 8.;
			jClient.callStaticIntMethod( 'getNumberOfData' , nObs );
			put  'INFO: processing ' nObs ' data rows';

			if (nObs > 0) then do;
				length tsName $ 100;
				length period $ 18;
				length stat $ 5;
				length obs 8.;
				do num=0 to  nObs-1;
					/* call the handler to get series name, time, data, status */
					jClient.callStaticStringMethod( 'getDataName' , num, tsName );
					jClient.exceptioncheck(e);
					if (e) then do;
						put 'ERROR: exception in calling getDataName' e;
						stop;
					end;
					jClient.exceptionclear();
					
					jClient.callStaticStringMethod( 'getDataTimestamp' , num, period );
					jClient.exceptioncheck(e);
					if (e) then do;
						put 'ERROR: exception in calling getDataTimestamp';
						stop;
					end;
					jClient.exceptionclear();
					
					jClient.callStaticDoubleMethod( 'getDataObservation' , num, obs );
					jClient.exceptioncheck(e);
					if (e) then do;
						put 'ERROR: exception in calling getDataObservation';
						stop;
					end;
					jClient.exceptionclear();
					
					*put tsName= period= obs= stat= ;
					TS_NAME = tsName;
					OBS_TIME = period;
					OBS_VALUE = obs;
					output sdmxdata;
				end;
			end;
		
			/*
			 *	Metadata Section: optional, only if the name ot the dataset has been
			 *  explicitly provided.
			 */
			if( &metadata ) then do;			
				/* call the handler to get the size of the internal metadata section */
				length nMeta 8.;

				/* time series metadata */
				jClient.callStaticIntMethod('getNumberOfMeta', nMeta );
				put  'INFO: processing ' nMeta ' metadata rows';

				if (nMeta > 0) then do;
					length metaKey $ 35;
					length metaValue $ 250;
					length metaType $ 20;
					do num=0 to  nMeta-1;
						/* call the client to get series name, metadata name, value, type */
						jClient.callStaticStringMethod('getMetaName', num, tsName );
						jClient.exceptioncheck(e);
						if (e) then do;
							put 'ERROR: exception in calling getMetaName';
							stop;
						end;
						jClient.exceptionclear();

						jClient.callStaticStringMethod('getMetaKey', num, metaKey );
						jClient.exceptioncheck(e);
						if (e) then do;
							put 'ERROR: exception in calling getMetaKey' e;
							stop;
						end;
						jClient.exceptionclear();
						
						
						jClient.callStaticStringMethod( 'getMetaValue' , num, metaValue );
						jClient.exceptioncheck(e);
						if (e) then do;
							put 'ERROR: exception in calling getMetaValue';
							stop;
						end;
						jClient.exceptionclear();
						
						jClient.callStaticStringMethod('getMetaType', num, metaType );
						jClient.exceptioncheck(e);
						if (e) then do;
							put 'ERROR: exception in calling getMetaType';
							stop;
						end;
						jClient.exceptionclear();
						

						*put tsName= metaKey= metaValue= metaType= ;
						TS_NAME = tsName;
						META_KEY = metaKey;
						META_VALUE = metaValue;
						META_TYPE = metaType;
						output &sdmxmetadata;
					end; 	/* end for */
				end; 	/* end if nMeta > 0*/

				/* observation level metadata */
				jClient.callStaticIntMethod('getNumberOfObsMeta', nMeta );
				put  'INFO: processing ' nMeta ' observation level metadata rows';

				if (nMeta > 0) then do;
					length metaKey $ 35;
					length metaValue $ 250;
					length metaDate $ 18;
					do num=0 to  nMeta-1;
						/* call the client to get series name, metadata name, value, type */
						jClient.callStaticStringMethod('getObsMetaName', num, tsName );
						jClient.exceptioncheck(e);
						if (e) then do;
							put 'ERROR: exception in calling getObsMetaName';
							stop;
						end;
						jClient.exceptionclear();

						jClient.callStaticStringMethod('getObsMetaKey', num, metaKey );
						jClient.exceptioncheck(e);
						if (e) then do;
							put 'ERROR: exception in calling getObsMetaKey' e;
							stop;
						end;
						jClient.exceptionclear();
						
						
						jClient.callStaticStringMethod( 'getObsMetaValue' , num, metaValue );
						jClient.exceptioncheck(e);
						if (e) then do;
							put 'ERROR: exception in calling getObsMetaValue';
							stop;
						end;
						jClient.exceptionclear();
						
						jClient.callStaticStringMethod('getObsMetaDate', num, metaDate );
						jClient.exceptioncheck(e);
						if (e) then do;
							put 'ERROR: exception in calling getMetaType';
							stop;
						end;
						jClient.exceptionclear();
						

						*put tsName= metaKey= metaValue= metaType= ;
						TS_NAME = tsName;
						META_KEY = metaKey;
						META_VALUE = metaValue;
						OBS_TIME = metaDate;
						output &sdmxobservationsmetadata;
					end; 	/* end for */
				end; 	/* end if nMeta > 0*/

			end;	/* end if metadata enabled */
		end;	/* end if at least one series returned */
		else do;
			put  'INFO: no time series to be processed';
		end;
		jClient.delete();

	run; /* end data step */

	/* refine metadata output dataset only if requested */
	%if( &metadata ^= 0 ) %then %do;
		data &sdmxmetadata;
			set &sdmxmetadata;
			drop  OBS_TIME OBS_VALUE;
		run;
		data &sdmxobservationsmetadata;
			set &sdmxobservationsmetadata;
			drop  OBS_VALUE META_TYPE;
		run;
	%end;
	
	/* refine data output dataset */
	data sdmxdata;
		set sdmxdata;
		drop  META_KEY META_VALUE META_TYPE;
	run;
	
%mend gettimeseries;

/* example
*  %gettimeseries(provider="ECB", tsKey="EXR.A.USD.EUR.SP00.A", metadata=1);
*/