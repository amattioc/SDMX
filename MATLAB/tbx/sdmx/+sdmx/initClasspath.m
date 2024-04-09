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

function initClasspath() 
    jarLoaded = exist('it.bancaditalia.oss.sdmx.helper.SDMXHelper', 'class');
    if jarLoaded ~= 8
        mFilesLoaded = exist('sdmxroot.m', 'file');
        if mFilesLoaded == 2
            javaaddpath(dir(fullfile(sdmxroot, '/lib/SDMX*.jar')));
        else
            error('Error: the m-files of the MatSDMX toolbox cannot be found in the MATLAB path');
        end
    end
end