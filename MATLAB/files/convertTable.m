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

function tsTable = convertTable(ds, isTimeTable) 

    %check arguments
    if nargin < 1
        error(sprintf(['Usage: convert(list)\n' ...
                    'Arguments\n' ...
                    'list: a it.bancaditalia.oss.sdmx.api.PortableDataSet of SDMX TimeSeries']));
    elseif nargin < 2
        isTimeTable = false;
    end
     
    %check class
    if (~ isa(ds,'it.bancaditalia.oss.sdmx.api.PortableDataSet'))
        error('SDMX convert(list) error: input list must be of class it.bancaditalia.oss.sdmx.api.PortableDataSet.');
    end
    
    %handle errors
    if ds.isErrorFlag
        warning('The time series %s is not valid due to errors in the request.', ds.getErrorObjects);
    end

    values = cell(ds.getObservations);
    if(isTimeTable)
        TIME_PERIOD = datetime(cell(ds.getGregorianTimeStamps));
    else
        TIME_PERIOD = cell(ds.getTimeStamps);
    end
    dimNames = cell(ds.getMetadataNames);
    
    nMeta = length(dimNames);
    metadata = cell(1, nMeta);
    for i = 1:nMeta
        metadata{i} = cell(ds.getMetadata(dimNames(i)));
    end

    if(isTimeTable)
        tsTable = timetable(TIME_PERIOD, values, metadata{:});
        tsTable.Properties.VariableNames = ['OBS_VALUE'; dimNames];
    else
        tsTable = table(TIME_PERIOD, values, metadata{:});
        tsTable.Properties.VariableNames = ['TIME_PERIOD'; 'OBS_VALUE'; dimNames];
    end

   
end