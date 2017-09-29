
% Simple script for UNIT TEST purposes
%
% #############################################################################################
% Copyright 2010,2014 Bank Of Italy
%
% Licensed under the EUPL, Version 1.1 or as soon as they
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

%% Test 1: getProviders
providers = getProviders;
assert(length(providers) > 1, 'Error getProviders');

%% Test 2: getFlows
flows = getFlows('ECB');
assert(length(flows) > 1, 'Error flow number');
assert(strcmp(flows('EXR'), 'Exchange Rates'), 'Error flow names');

%% Test 3: getDimensions
dims = getDimensions('ECB', 'EXR');
assert(length(dims) == 5), 'Error dimension number';
assert(strcmp(dims{1}, 'FREQ'), 'Error dimension names');

%% Test 4: getTimeSeries
tts = getTimeSeries('ECB', 'EXR.M.USD|GBP.EUR.SP00.A');
assert(length(tts) == 2, 'Error getTimeseries');


