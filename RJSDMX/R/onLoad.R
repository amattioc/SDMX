# Copyright 2010,2014 Bank Of Italy
#
# Licensed under the EUPL, Version 1.1 or - as soon they
# will be approved by the European Commission - subsequent
# versions of the EUPL (the "Licence");
# You may not use this work except in compliance with the
# Licence.
# You may obtain a copy of the Licence at:
#
#
# http://ec.europa.eu/idabc/eupl
#
# Unless required by applicable law or agreed to in
# writing, software distributed under the Licence is
# distributed on an "AS IS" basis,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
# express or implied.
# See the Licence for the specific language governing
# permissions and limitations under the Licence.
#
.onLoad <- function(libname, pkgname) {
	.jpackage(pkgname, lib.loc = libname)
	#jv <- .jcall("java/lang/System", "S", "getProperty", "java.runtime.version") 
	#if(substr(jv, 1L, 1L) == "1") {
	#	jvn <- as.numeric(paste0(strsplit(jv, "[.]")[[1L]][1:2], collapse = "."))
	#	if(jvn < 1.7) stop(paste("Java 7 is needed for this package but rJava is linked to Java ", jvn))
	#}
	
}
