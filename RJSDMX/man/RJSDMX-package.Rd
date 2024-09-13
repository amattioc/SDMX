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
\name{RJSDMX-package}
\title{Gets timeseries from SDMX data Provider}
\description{  This package provides functions to extract timeseries data and 
structural metadata from an SDMX Provider (e.g. ECB,OECD, EUROSTAT) via SDMX Web Service}
\details{\tabular{ll}{
Package: \tab RJSDMX\cr
Type: \tab Package\cr
}
The SDMX Connectors framework (of which RJSDMX is part) aims to offer data users the means for efficiently interacting with SDMX Web Service providers 
from within the most popular statistical tools.
The source code of the SDMX Connectors project can be found at:  

\url{https://github.com/amattioc/SDMX}

Information about the R Connector can be found in the dedicated wiki page: 

\url{https://github.com/amattioc/SDMX/wiki/RJSDMX:-Connector-for-R}

In particular, all information related to configuration (network, tracing, security) can be found at: 

\url{https://github.com/amattioc/SDMX/wiki/Configuration}

}
\alias{RJSDMX}
\docType{package}
\keyword{package}
\seealso{\bold{getProviders, getTimeSeries, sdmxHelp}}
\examples{
\dontrun{
 my_ts = getTimeSeries('ECB','EXR.M.USD.EUR.SP00.A')
}
}
\author{Attilio Mattiocco, Bank of Italy 
\email{attilio.mattiocco@bancaditalia.it}}
