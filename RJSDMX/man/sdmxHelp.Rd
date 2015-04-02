% Copyright 2010,2014 Bank Of Italy
%
% Licensed under the EUPL, Version 1.1 or as soon they
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

\name{sdmxHelp}
\alias{sdmxHelp}
\title{Open a helper graphical application}
\description{
  This function opens a small sdmx metadata browser that can be helpful when building queries.
}
\arguments{
  \item{internalJVM}{true (default) if the GUI has to live in the R JVM. Set this to false in MAC, to avoid issue #41}
}
\usage{
  sdmxHelp(internalJVM = T)
}
\examples{
# not run
# opens the helper in the R JVM
# sdmxHelp() 
# opens the helper in an external JVM
# sdmxHelp(F) 

}
\author{Attilio Mattiocco}
