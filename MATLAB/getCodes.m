function codes = getCodes(provider, flow, dimension)
% Get the list of data flows for this provider
%
% Usage: getCodes(provider, flow, dimension)
%
% Arguments
%
% provider: the name of the SDMX data provider
% flow: the name of the flow
% dimension: the name of the dimension
%
% #############################################################################################
% Copyright 2019,2019 Bank Of Italy
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

initClasspath;

if nargin < 3
    error(sprintf(['\nUsage: getCodes(provider, flow, dimension)\n\n' ...
        'Arguments\n\n' ...
        'provider: the name of the SDMX data provider\n' ...
        'flow: the name of the flow \n' ...
        'dimension: the name of the dimension \n' ...
        ]));
end

%get codes
try
    result = it.bancaditalia.oss.sdmx.client.SdmxClientHandler.getCodes(provider, flow, dimension);
catch mexp
    error(['SDMX getCodes() error:\n' mexp.message]);
end

%verify returned class type
if (~ isa(result,'java.util.Map'))
    error('SDMX getCodes() returned class error.')
end

%create Map
ids         = cell(result.keySet.toArray);
description = cell(result.values.toArray);
codes       = containers.Map(ids, description);

end
