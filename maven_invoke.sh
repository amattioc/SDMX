#!/bin/bash
echo
echo '   Maven invocation help:'
echo
printf 'mvn -f JAVA/pom.xml clean deploy \\\n        -Dlocal.filerepo=file://<stata-repo>/ \\\n        -Dcompile-stata=true -Dsdmx.external.build=true -Dmaven.test.skip=true \\\n        -Dhttps.proxyHost=<proxyhost> -Dhttps.proxyPort=<proxyport>\n'
echo
echo The proxy settings are needed for github-release-plugin which does not respect maven proxy settings.
echo
