#' Convert SDMX
#'
#' Convert objects returned by \code{getSDMX}
#'
#' Convert objects returned by \code{getSDMX} into other object types
#'
#' @param SDMXTS object returned from \code{getSDMX}
#' @param provider ?
#' @param timevar ?
#' @param numeric ?
#'
#' @author Bo Werth \email{Bo.WERTH@@oecd.org}
#' @keywords SDMX zoo
#' @seealso \code{\link{getSDMX}}, \code{\link{makeSDMXTS}}
#'
#' @export
#' @examples
#' df <- sdmxTS2DF()

sdmxTS2DF <- function(SDMXTS,provider,timevar="date",numeric=TRUE) {
    require(reshape2)
    id <- sapply(strsplit(names(SDMXTS[1]), "[.]"), "[[", 1)
    dimnames <- names(getDimensions(provider, id))
    SDMXTS <- do.call("merge.zoo", SDMXTS)
    SDMXTS.df <- as.data.frame(SDMXTS)
    ## add functions to convert date to R date format
    SDMXTS.df[[timevar]] <- rownames(SDMXTS.df)
    if (numeric==TRUE) SDMXTS.df[[timevar]] <- as.numeric(SDMXTS.df[[timevar]])
    SDMXTS.df.m <- suppressWarnings(melt(SDMXTS.df, id.vars = c(timevar)))
    X <- strsplit(as.character(SDMXTS.df.m$variable), "[.]")
    for (d in seq(along = dimnames)) {
        SDMXTS.df.m[dimnames[d]]  <- sapply(X, '[[', d+1)
    }
    SDMXTS.df.m <- SDMXTS.df.m[,!colnames(SDMXTS.df.m)=="variable"]
    return(SDMXTS.df.m)
}
