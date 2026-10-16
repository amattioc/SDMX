classdef testSetupEnv < matlab.unittest.TestCase

    properties
        JarPath (1,1) string
    end

    methods (TestClassSetup)
        function addSourceFolderToPath(testCase)
            matlabRoot = fileparts(fileparts(mfilename("fullpath")));
            testCase.JarPath = fullfile(matlabRoot, "files", "lib", "SDMX.jar");
        end
    end

    methods (Test)
        function testMakesBundledJarAvailableToJava(testCase)
            sdmx.setupEnv();

            javaPath = string([javaclasspath("-static"); ...
                javaclasspath("-dynamic")]);
            testCase.verifyTrue(any(javaPath == testCase.JarPath));
        end

        function testDoesNotAddDuplicateJar(testCase)
            sdmx.setupEnv();
            sdmx.setupEnv();

            javaPath = string([javaclasspath("-static"); ...
                javaclasspath("-dynamic")]);
            testCase.verifyEqual(sum(javaPath == testCase.JarPath), 1);
        end
    end
end
