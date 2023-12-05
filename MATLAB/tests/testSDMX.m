classdef testSDMX < matlab.unittest.TestCase
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

    methods (Test)

        function tGetProviders(tc)
            % Test 1: getProviders
            providers = sdmx.getProviders;
            tc.verifyNotEmpty(providers)
        end

        function tGetFlows(tc)
            % Test 2: getFlows
            flows = sdmx.getFlows('ECB');
            tc.verifyNotEmpty(flows)
            tc.verifyEqual(flows('ECB,EXR,1.0'), "Exchange Rates")
        end

        function tGetDimensions(tc)
            % Test 3: getDimensions
            dims = sdmx.getDimensions('ECB', 'EXR');
            tc.verifyLength(dims, 5);
            tc.verifyEqual(dims{1}, 'FREQ');
        end

        function tGetTimeSeries(tc)
            % Test 4: getTimeSeries
            tts = sdmx.getTimeSeries('ECB', 'EXR.M.USD|GBP.EUR.SP00.A');
            tc.verifyLength(tts, 2)
        end

        function tGetCodes(tc)
            map = sdmx.getCodes('ECB','ECB,EXR,1.0', 'FREQ');
            tc.verifyEqual(map('A'), "Annual")
        end

    end

end