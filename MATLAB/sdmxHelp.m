function sdmxHelp()
	% Open a GUI for SDMX metadata browsing
	%
	% Usage: sdmxHelp()
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
    
    initClasspath;
    
    %try java code
    try
        it.bancaditalia.oss.sdmx.helper.SDMXHelper.start();
    catch mexp
        error(['SDMX sdmxHelp()() error:\n' mexp.message]);
    end 
	
end

