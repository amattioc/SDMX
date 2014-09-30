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

function tsList = convert(list) 

    %check arguments
    if nargin ~= 1
        error([ 'Usage: convert(list)\n' ...
                    'Arguments\n' ...
                    'list: a java.util.List of SDMX TimeSeries']);
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
		ts = convertSeries(series);
		tsList{i+1} = ts;
	end %for i
	
end % function convert       

function ts = convertSeries(series)
	
    %check arguments
    if nargin ~= 1
        error([ 'Usage: convertSeries(series)\n' ...
                    'Arguments\n' ...
                    'series: a it.bankitalia.reri.sia.sdmx.api.PortableTimeSeries']);
    end
     
    %check class
    if (~ isa(series,'it.bankitalia.reri.sia.sdmx.api.PortableTimeSeries')) 
        error('SDMX convertSeries(series) error: input list must be of class it.bankitalia.reri.sia.sdmx.api.PortableTimeSeries.');
    end
    
    % get frequency
	freq = series.getFrequency();

	% get all attributes and put them to DataInfo.UserData field
	attrs = series.getAttributesArray();
	cArrayAttrs = cell(attrs);

	% get all dimensions and put them to UserData property
	dims = series.getDimensionsArray();
	cArrayDims = cell(dims);

	% now set time and values
	timeSlots = series.getTimeSlotsArray();
	cArrayTimeSlots = cell(timeSlots);
	arrayTimeSlots = convertDates(freq, cArrayTimeSlots);
	
	numOfTimes = length(cArrayTimeSlots);
	observations = series.getObservationsArray();
	cArrayObservations = cell(observations);
	numOfSamples = length(cArrayObservations);
	arrayObservations = cell2mat(cArrayObservations);

	% create name
	name = series.getName();

    if numOfSamples ~= 0
        if numOfSamples == numOfTimes 
            ts = timeseries(arrayObservations, arrayTimeSlots);
            ts.timeinfo.startdate = arrayTimeSlots(1,:);
        else
            error(['Time series:', char(name), '. Number of samples different from number of timeslots']);
        end   %if 
    else
        warning(['Time series:', char(name), '. No observations found']);
        ts = timeseries();
    end
    
    %add metadata
	ts.DataInfo.UserData = cArrayAttrs;
	set(ts, 'UserData', cArrayDims);
	ts.timeinfo.units='days';
	set(ts, 'Name', char(name));
end

function dates = convertDates(freq, dates)
	if(strcmp(freq, 'Q'))
		dates=regexprep(dates, 'Q1', '03');
		dates=regexprep(dates, 'Q2', '06');
		dates=regexprep(dates, 'Q3', '09');
		dates=regexprep(dates, 'Q4', '12');   
		dates=(cell2mat(dates));
	elseif(strcmp(freq, 'A'))
		dates=strcat(cell2mat(dates), '-12-31');
	else
		dates=(cell2mat(dates));
	end
end

