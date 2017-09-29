% Copyright 2010,2014 Bank Of Italy
%
% Licensed under the EUPL, Version 1.1 or - as soon they
% will be approved by the European Commission - subsequent
% versions of the EUPL (the "Licence");
% You may not use this work except in compliance with the
% Licence.
% You may obtain a copy of the Licence at:
%
%
% http://ec.europa.eu/idabc/eupl
%
% Unless required by applicable law or agreed to in
% writing, software distributed under the Licence is
% distributed on an "AS IS" basis,
% WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
% express or implied.
% See the Licence for the specific language governing
% permissions and limitations under the Licence.
%

function tsList = convert(list, iso8601Date) 

    %check arguments
    if nargin < 1
        error([ 'Usage: convert(list)\n' ...
                    'Arguments\n' ...
                    'list: a java.util.List of SDMX TimeSeries']);
    elseif nargin < 2
        iso8601Date = false;
    end
     
    %check class
    if (~ isa(list,'java.util.List'))
        error('SDMX convert(list) error: input list must be of class java.util.ArrayList.');
    end
    
    %create out cell array
	numOfTS = list.size();
	tsList = cell(1, numOfTS);
    
    %populate
	for i=0:numOfTS-1
		series = list.get(i);
        %matlab does not handle non numeric data in time series
        if ~series.isNumeric
            warning('The time series %s is not numeric and will be skipped. You can get it in a table.', series.getName);
            continue
        end
		ts = convertSeries(series, iso8601Date);
		tsList{i+1} = ts;
	end %for i
	
end % function convert       

function ts = convertSeries(series, iso8601Date)
	
    %check arguments
    if nargin < 1
        error([ 'Usage: convertSeries(series)\n' ...
                    'Arguments\n' ...
                    'series: a it.bancaditalia.oss.sdmx.api.PortableTimeSeries']);
    elseif nargin < 2
        iso8601Date = false;
    end
     
    %check class
    if (~ isa(series,'it.bancaditalia.oss.sdmx.api.PortableTimeSeries')) 
        error('SDMX convertSeries(series) error: input list must be of class it.bancaditalia.oss.sdmx.api.PortableTimeSeries.');
    end
    
    % get frequency
	freq = series.getFrequency();

	% get all attributes and put them to DataInfo.UserData field as a map
	cArrayMap= getMetaData(series);

   	% now set time and values
	timeSlots = series.getTimeSlotsArray();
	cArrayTimeSlots = cell(timeSlots);
	arrayTimeSlots = convertDates(freq, cArrayTimeSlots, iso8601Date);
	
	numOfTimes = length(cArrayTimeSlots);
	observations = series.getObservationsArray();
	cArrayObservations = cell(observations);
	numOfSamples = length(cArrayObservations);
	arrayObservations = cell2mat(cArrayObservations);

	% create name
	name = series.getName();

    if numOfSamples ~= 0
        if numOfSamples == numOfTimes
            startdate = arrayTimeSlots(1,:);
            if(numOfTimes == 1)
                %seems a matlab bug in the constructor of the timeseries
                arrayTimeSlots = cellstr(arrayTimeSlots);
            end
            ts = timeseries(arrayObservations, arrayTimeSlots);
            ts.timeinfo.startdate = startdate;
        else
            error(['Time series: ', char(name), '. Number of samples different from number of timeslots']);
        end   %if 
    else
        warning(['Time series: ', char(name), '. No observations found']);
        ts = timeseries();
    end
    
    %add metadata
	set(ts, 'UserData', cArrayMap);
	ts.timeinfo.units='days';
	set(ts, 'Name', char(name));
    
end

function dates = convertDates(freq, dates, iso8601Date)
    
    if(nargin == 3 && iso8601Date == true)
        dates = datestr(datetime(dates,'InputFormat','uuuu-MM-dd''T''HH:mm:ss','TimeZone','UTC'));
    elseif(strcmp(freq, 'Q'))
		dates=regexprep(dates, 'Q1', '03-31');
		dates=regexprep(dates, 'Q2', '06-30');
		dates=regexprep(dates, 'Q3', '09-30');
		dates=regexprep(dates, 'Q4', '12-31');   
		dates=(cell2mat(dates));
	elseif(strcmp(freq, 'A'))
		dates=strcat(cell2mat(dates), '-12-31');
	elseif(strcmp(freq, 'H'))
		dates=regexprep(dates, 'S1', '06-30');   
		dates=regexprep(dates, 'S2', '12-31'); 
		dates=(cell2mat(dates));
	elseif(strcmp(freq, 'W'))
		for i = 1 : length(dates)
			dates{i} = char(it.bancaditalia.oss.sdmx.util.WeekConverter.convert(dates{i}));
		end
		dates=(cell2mat(dates));
	else
		dates=(cell2mat(dates));
	end
end

function metadata = getMetaData(ts)
    metadata = containers.Map;
 
    %handle errors
    if ts.isErrorFlag
        metadata('ERROR_FLAG') = true;
        metadata('ERROR_MESSAGE') = ts.getErrorMessage;
        warning('The time series %s is not valid due to errors in the request: %s.', ts.getName, ts.getErrorMessage);
    end
    
    % get all dimensions
    tsdims = cell(ts.getDimensionsMap().keySet().toArray());    
    for i=1:length(tsdims)
        metadata(tsdims{i}) = char(ts.getDimension(tsdims{i}));
    end
    
    % get all ts level attributes
    tsattrs = cell(ts.getAttributesMap().keySet().toArray());    
    for i=1:length(tsattrs)
        metadata(tsattrs{i}) = char(ts.getAttribute(tsattrs{i}));
    end
    
    % get all ts level attributes 
    obsattrs = cell(ts.getObsLevelAttributesNamesArray);
    for i=1:length(obsattrs)
        attrval = ts.getObsLevelAttributesArray(obsattrs{i});
        if size(attrval) ~= ts.getObservationsArray().size()
            warning(['Attribute: ', obsattrs{i}, 'is malformed. Skipping.']);
        else
            metadata(obsattrs{i}) = cell(attrval);
        end
    end 
end
