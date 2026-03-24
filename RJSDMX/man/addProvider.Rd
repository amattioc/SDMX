\name{addProvider}
\alias{addProvider}
\title{add new provider}
\usage{
addProvider(name, endpoint, needsCredentials = FALSE, needsURLEncoding = FALSE,
  supportsCompression = TRUE, description = "", sdmxVersion = "V2", 
  supportsAvailability = F)
}
\arguments{
  \item{name}{the name of the provider}

  \item{endpoint}{the URL where the provider resides}

  \item{needsCredentials}{it can be one of "TRUE", "FALSE", "BASIC", "BEARER", "NONE". TRUE means "BASIC" and FALSE means "NONE" for backward compatility}

  \item{needsURLEncoding}{set this to TRUE if the provider
  does not handle character '+' in URLs}

  \item{supportsCompression}{set this to TRUE if the
  provider is able to handle compression}

  \item{description}{a brief text description of the
  provider}

  \item{supportsAvailability}{set this to TRUE if the
  provider is able to handle availability queries}

  \item{sdmxVersion}{what sdmx version this provider supports}
}
\description{
Configure a new data provider (only SDMX 2.1 REST providers
are supported). This function can be used to configure a
new (SDMX 2.1 compliant, REST based) data provider.
}
\examples{
\dontrun{
addProvider('test', 'http://sdw-wsrest.ecb.europa.eu/service', FALSE)
getProviders()
}
}

