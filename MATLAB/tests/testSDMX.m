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

            tts = sdmx.getTimeSeries('EUROSTAT_COMP', 'AID_MARE/A.CY.');
            tc.verifyLength(tts, 3)
            tb = sdmxtable(tts, true);
            tc.verifyClass(tb, 'table')

            tts = sdmx.getTimeSeries('ISTAT','101_1039.A.001004.ALL.NUMAGRIAUTH');
            tc.verifyLength(tts, 1)
            tb = sdmxtable(tts);
            tc.verifyClass(tb, 'table')

            tts = sdmx.getTimeSeries('ECB','IEAF.Q.SK.N.V.D92.Z.S2.A1.S.2.X.N.Z');
            tc.verifyLength(tts, 1)
            tb = sdmxtable(tts, true);
            tc.verifyClass(tb, 'table')

            tts = sdmx.getTimeSeries('ECB', 'BKN/H.AT.A020.....');
            tc.verifyLength(tts, 1)
            tb = sdmxtable(tts, true);
            tc.verifyClass(tb, 'table')

        end

        function tGetTimeSeriesTable(tc)
            % Test 5: getTimeSeriesTable
            tb = sdmx.getTimeSeriesTable('ECB', 'EXR.M.USD|GBP.EUR.SP00.A');
            tc.verifyClass(tb, 'table')

            tb = sdmx.getTimeSeriesTable('ECB', 'EXR.M.USD.EUR.SP00.A');
            tc.verifyClass(tb, 'table')

            startTime = datetime(1995,1,1, 'Format','yyyy-MM-dd');
            endTime = datetime(2005,1,1, 'Format','yyyy-MM-dd');

            tb = sdmx.getTimeSeriesTable('ECB', 'EXR.M.USD.EUR.SP00.A', string(startTime), string(endTime));
            tb.TIME_PERIOD = datetime(tb.TIME_PERIOD, 'InputFormat','uuuu-MM', 'Format','uuuu-MM');

            tc.verifyLessThan(tb.TIME_PERIOD, endTime);
            tc.verifyGreaterThanOrEqual(tb.TIME_PERIOD, startTime)
        end

        function tGetTimeSeriesRevisions(tc)
            % Test 6: getTimeSeriesRevisions

            startTime = datetime(1995,1,1, 'Format','yyyy-MM-dd');
            endTime = datetime(2005,1,1, 'Format','yyyy-MM-dd');

            tb = sdmx.getTimeSeriesRevisions('ECB', 'EXR.M.USD.EUR.SP00.A', string(startTime), string(endTime));
            tb.TIME_PERIOD = datetime(tb.TIME_PERIOD, 'InputFormat','uuuu-MM', 'Format','uuuu-MM');

            tc.verifyLessThan(tb.TIME_PERIOD, endTime);
            tc.verifyGreaterThanOrEqual(tb.TIME_PERIOD, startTime)

            startTime = datetime(2002,1,1, 'Format','yyyy-MM-dd');
            tb = sdmx.getTimeSeriesRevisions('ECB', 'EXR.M.USD.EUR.SP00.A', string(startTime), string(endTime), '', true);
            tb.TIME_PERIOD = datetime(tb.TIME_PERIOD, 'InputFormat','uuuu-MM', 'Format','uuuu-MM');
            tc.verifyClass(tb, 'table');
            tc.verifyLessThan(tb.TIME_PERIOD, endTime);
            tc.verifyGreaterThanOrEqual(tb.TIME_PERIOD, startTime)

            tb = sdmx.getTimeSeriesRevisions('ECB', 'EXR.M.USD.EUR.SP00.A', string(startTime), string(endTime), '2003-01-01', true);
            tb.TIME_PERIOD = datetime(tb.TIME_PERIOD, 'InputFormat','uuuu-MM', 'Format','uuuu-MM');
            tc.verifyClass(tb, 'table');
            tc.verifyLessThan(tb.TIME_PERIOD, endTime);
            tc.verifyGreaterThanOrEqual(tb.TIME_PERIOD, startTime)
        end


        function tAddProvider(tc)
            % Test 7: addProvider

            sdmx.addProvider('ECB_TEST', 'http://sdw-wsrest.ecb.europa.eu/service', false, false, false, 'Sample ECB provider');
            providers = sdmx.getProviders;
            tc.verifyTrue(any(contains(providers, 'ECB_TEST')))
        end

        function tGetCodes(tc)
            % Test 8: getCodes

            map = sdmx.getCodes('ECB','ECB,EXR,1.0', 'FREQ');
            tc.verifyEqual(map('A'), "Annual")
        end

        function tGetDSDIdentifier(tc)
            % Test 9: getDSDIdentifier

            id = sdmx.getDSDIdentifier('ECB','ECB,EXR,1.0');
            tc.verifyEqual(id, "ECB/ECB_EXR1/1.0")
        end

        function tGetSDMXTable(tc)
            % getSDMXTable
            
            tts = sdmx.getTimeSeries('ECB', 'EXR.M.USD|GBP.EUR.SP00.A');
            tb = sdmxtable(tts, true);
            tc.verifyClass(tb, 'table')
        end
    end

end