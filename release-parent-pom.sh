mvn -f parent-pom.xml clean gpg:sign org.sonatype.plugins:nexus-staging-maven-plugin:1.6.8:deploy -DnexusUrl=https://oss.sonatype.org/ -DserverId=ossrh -DautoReleaseAfterClose=true
