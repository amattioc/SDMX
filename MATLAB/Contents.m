% Toolbox MatSDMX
% Version 1.4.2
% MATLAB interface to SDMX Web Services
%
% Files 
%
%	getProviders  - get the list of available data providers
%	getFlows - get the list of available data flows for the input provider
%	getDimensions - get the list of dimensions for the input dataflow
%	getDSDIdentifier - get the name of the DSD for the input dataflow
%	getTimeSeries - get the list of time series that match the input query 
%	and return a cell of timeseries
%	getTimeSeriesTable - get the list of time series that match the input query
%	and return a table
%	sdmxtable - converts the results of the getTimeSeries call from a cell
%	array of timeseries to a table
%	sdmxHelp - opena graphical metadata browser that helps building  data queries
%
% #############################################################################################
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
