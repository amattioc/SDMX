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

\name{addProvider}
\alias{addProvider}
\title{Configure a new data provider (only SDMX 2.1 REST providers are supported)}
\description{
  This function can be used to configure a new (SDMX 2.1 compliant, REST based) data provider.
}
\usage{
  addProvider(name, endpoint, needsCredentials, needsURLEncoding, supportsCompression, description)
}
\arguments{
  \item{name}{the name of the provider}
  \item{endpoint}{the URL where the provider resides}
  \item{needsCredentials}{set this to TRUE if the user needs to authenticate to query the provider }
  \item{needsURLEncoding}{set this to TRUE if the provider does not handle character '+' in URLs }
  \item{supportsCompression}{set this to TRUE if the provider is able to handle compression }
  \item{description}{a brief text description of the provider }
}
\examples{
#not run
#addProvider('pname', 'pendpoint', F)
}
\author{Attilio Mattiocco}
