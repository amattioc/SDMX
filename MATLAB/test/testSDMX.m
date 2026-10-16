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
            %% Test 1: getProviders/add provider
            providers = getProviders;
            n = length(providers);
            tc.assertTrue(n > 1, 'Error getProviders');

            addProvider('test', 'https://a.b.c.d', false, false, false, '', 'V2', false);
            providers = getProviders;
            tc.assertTrue(n+1 == length(providers), 'Error addProvider');
        end

        function tGetFlows(tc)

            %% Test 2: getFlows
            flows = getFlows('ECB');
            tc.assertTrue(length(flows) > 1, 'Error flow number');
            tc.assertTrue(strcmp(flows('ECB,EXR,1.0'), 'Exchange Rates'), 'Error flow names');

        end

        function tGetDimensions(tc)
            %% Test 3: getDimensions
            dims = getDimensions('ECB', 'EXR');
            tc.assertTrue(length(dims) == 5), 'Error dimension number';
            tc.assertTrue(strcmp(dims{1}, 'FREQ'), 'Error dimension names');
        end

        function tGetTimeSeries(tc)
            %% Test 4: getTimeSeries
            tts = getTimeSeries('ECB', 'EXR.M.USD+GBP.EUR.SP00.A');
            tc.assertTrue(length(tts) == 2, 'Error getTimeseries');
        end

        function tGetTimeSeriesTable(tc)
            %% Test 5: getTimeSeriesTable
            ttst = getTimeSeriesTable('ECB', 'EXR.M.USD+GBP.EUR.SP00.A');
            dims = size(ttst);
            tc.assertTrue(dims(1)>1, 'Error getTimeseriesTable');
            tc.assertTrue(dims(2)>1, 'Error getTimeseriesTable');
        end

    end

end