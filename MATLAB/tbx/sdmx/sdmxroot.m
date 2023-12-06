function root = sdmxroot()

arguments (Output)
    root (1,1) string
end

root = fileparts(fileparts(mfilename('fullpath')));

