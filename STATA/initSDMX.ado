program initSDMX
	version 17
	
	capture findfile SDMX.jar
	if _rc != 0 {
		error "Cannot find SDMX.jar in ado-path."
        exit
	}
	
	java, shared(BItools): /cp `r(fn)'
	
	display "SDMX Connectors have been initialized."
end
