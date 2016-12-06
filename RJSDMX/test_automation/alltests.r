#!/usr/bin/env Rscript
library(RUnit)
library(RJSDMX)

print("Building test suite")

print('Disabilita messaggi di errore su checkException')
a=getOption('RUnit')
a$silent=T
options(RUnit=a)

testsuite.all <- defineTestSuite("all",
                                 dirs = 'test_automation',
                                 testFileRegexp="^test.+\\.r",
                                 testFuncRegexp="^test.+",
                                 rngKind = "Marsaglia-Multicarry",
                                 rngNormalKind="Kinderman-Ramage")
                                   
print("Running test suite")
testResult <- runTestSuite(testsuite.all)
printTextProtocol(testResult)
print(testResult)
#quit("no", testResult$all$nErr + testResult$all$nFail)
