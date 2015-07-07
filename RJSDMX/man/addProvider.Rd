\name{addProvider}
\alias{addProvider}
\title{add new provider}
\usage{
addProvider(name, endpoint, needsCredentials = F, needsURLEncoding = F,
  supportsCompression = T, description = "")
}
\arguments{
  \item{name}{the name of the provider}

  \item{endpoint}{the URL where the provider resides}

  \item{needsCredentials}{set this to TRUE if the user
  needs to authenticate to query the provider}

  \item{needsURLEncoding}{set this to TRUE if the provider
  does not handle character '+' in URLs}

  \item{supportsCompression}{set this to TRUE if the
  provider is able to handle compression}

  \item{description}{a brief text description of the
  provider}
}
\description{
Configure a new data provider (only SDMX 2.1 REST providers
are supported). This function can be used to configure a
new (SDMX 2.1 compliant, REST based) data provider.
}
\details{
addProvider(name, agency, endpoint, needsCredentials)
}
\examples{
\dontrun{
addProvider('pname', 'pendpoint', F)
}
}

