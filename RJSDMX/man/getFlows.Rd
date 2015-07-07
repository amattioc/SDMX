\name{getFlows}
\alias{getFlows}
\title{get provider flow list}
\usage{
getFlows(provider, pattern = "")
}
\arguments{
  \item{pattern}{the pattern to match against the dataflow
  id or description. If a pattern is not provided, all
  dataflows are returned.}

  \item{provider}{the name of the provider}
}
\description{
Extract the list of DataFlows of a provider. This function
is used to query the list of dataflows of the provider. A
matching pattern can be provided, if needed.
}
\details{
getFlows(provider, pattern)
}
\examples{
\dontrun{
## get all flows from ECB
flows = getFlows('ECB')
## get all flows that contain the 'EXR
flows = getFlows('ECB','*EXR*')
}
}
\author{
Attilio Mattiocco
\email{Attilio.Mattiocco@bancaditalia.it}, Diana Nicoletti
}
\keyword{rJava}

