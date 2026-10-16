function setupEnv()
%SETUPENV Configure the Java environment for the SDMX toolbox.
%
%   sdmx.setupEnv() adds the bundled SDMX Java archive to MATLAB's
%   dynamic Java class path when it is not already present.

% Get java dynamic path
djp = javaclasspath("-dynamic");

% Get location of JAR file
sdmxRoot = fileparts(fileparts(fileparts(mfilename('fullpath'))));

jarPath = fullfile(sdmxRoot, 'lib', 'SDMX.jar');

if ~isfile(jarPath)
    error('sdmx:setupEnv:missingJar', 'The JAR file is missing, please reinstall the toolbox');
end

if ~any(contains(djp, jarPath))
    % JAR not in path, add it
    javaaddpath(jarPath);

    % MATLAB Online instances using Java 8 can have an outdated Java CA store.
    % Its web client has a current CA store, so use it to bootstrap the Java
    % HTTPS client used by the bundled SDMX JAR. This retains normal certificate
    % and hostname validation and is needed only once per MATLAB session.
    isOnline = matlab.internal.environment.context.isMATLABOnline;
    isJava8 = startsWith(string(version("-java")), "Java 1.8");
    if isOnline && isJava8
        configureOnlineJavaTrust();
    end

end

end

function configureOnlineJavaTrust()
caFile = fullfile(tempdir, "sdmx-cacert.pem");
caBundleUrl = "https://curl.se/ca/cacert.pem";

try
    websave(caFile, caBundleUrl);

    certificateFactory = java.security.cert.CertificateFactory.getInstance("X.509");
    inputStream = java.io.FileInputStream(caFile);
    streamCleanup = onCleanup(@() inputStream.close());
    certificates = certificateFactory.generateCertificates(inputStream);
    clear streamCleanup

    keyStore = java.security.KeyStore.getInstance( ...
        java.security.KeyStore.getDefaultType());
    % Empty character vectors select KeyStore.load(InputStream, char[]) and
    % initialize a new in-memory keystore through MATLAB's Java bridge.
    keyStore.load('', '');

    certificateIterator = certificates.iterator();
    certificateNumber = 0;
    while certificateIterator.hasNext()
        certificateNumber = certificateNumber + 1;
        keyStore.setCertificateEntry( ...
            sprintf("ca-%03d", certificateNumber), certificateIterator.next());
    end

    trustManagerFactory = javax.net.ssl.TrustManagerFactory.getInstance( ...
        javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm());
    trustManagerFactory.init(keyStore);

    sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
    sslContext.init([], trustManagerFactory.getTrustManagers(), ...
        java.security.SecureRandom());
    javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory( ...
        sslContext.getSocketFactory());
catch exception
    error("sdmx:setupEnv:onlineTrustConfigurationFailed", ...
        ["Unable to configure Java HTTPS trust in MATLAB Online. " ...
         "The original error was: %s"], exception.message);
end
end
