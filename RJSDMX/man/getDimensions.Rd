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

\name{getDimensions}
\alias{getDimensions}
\title{Extract the dimensions of a DataFlow}
\description{
  This function is used to retrieve the list of dimensions of the input dataflow
}
\usage{
  getDimensions(provider, dataflow)
}
\arguments{
  \item{provider}{the name of the provider}
  \item{dataflow}{the identifier of the dataflow}
}
\examples{
# not run
# dims = getDimensions('ECB','EXR')
}
\author{Attilio Mattiocco, Diana Nicoletti}
