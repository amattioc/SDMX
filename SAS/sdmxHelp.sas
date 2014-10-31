%macro sdmxHelp;

data _null_;
declare javaobj jClient ( 'it.bankitalia.reri.sia.sdmx.helper.SDMXHelper' );
jClient.delete();
run;

%mend;