package com.ft.methodeapi.atc;

import com.google.common.base.Preconditions;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Locates Methode instances and keeps track of their status.
 *
 * @author Simon.Gibbs
 */
public class AirTrafficController {

    private static Pattern IP_FINDER_PATTERN = Pattern.compile("Name:.*?([0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+)$",Pattern.DOTALL + Pattern.MULTILINE);

    private final Map<DataCentre, String> hostnames;
    private final DataCentre iAmIn;

    public AirTrafficController(AtcConfiguration atcConfig) {
        this.hostnames = atcConfig.getHostnames();
        this.iAmIn = atcConfig.getiAm();
    }

    public Map<DataCentre, String> reportIps() {
        Map<DataCentre, String> results = new HashMap<>(hostnames.size());
        for(DataCentre dataCentre : hostnames.keySet()) {
            String serviceIp = lookup(hostnames.get(dataCentre));
            results.put(dataCentre,serviceIp);
        }
        return results;
    }


    public static String parseIpFromNsLookupOutput(String output, String hostname) {
        Matcher matcher = IP_FINDER_PATTERN.matcher(output);
        if(matcher.find()) {
            return matcher.group(1);
        }

        throw new AirTrafficControllerException("Cannot parse an IP for hostname " + hostname + " from output: " + output);
    }


    public static String lookup(String hostname) {

        CommandLine cmdLine = new CommandLine("nslookup");

        cmdLine.addArgument(hostname);

        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream(120);

        ExecuteWatchdog watchdog = new ExecuteWatchdog(3000); // 3s timeout
        Executor executor = new DefaultExecutor();
        executor.setExitValue(0);
        executor.setWatchdog(watchdog);
        executor.setStreamHandler(new PumpStreamHandler(outputBuffer));

        try {
            executor.execute(cmdLine);
            String rawResult = new String(outputBuffer.toByteArray(), Charset.defaultCharset());

            return parseIpFromNsLookupOutput(rawResult, hostname);

        } catch (IOException e) {
            throw new AirTrafficControllerException("Failure while looking up system location for cmdLine " + cmdLine, e);
        }
    }

    public DataCentre whois(String activeIp, Map<DataCentre, String> methodeIps) {
    	if (activeIp == null) {
    		return null;
    	}
        for(Map.Entry<DataCentre,String> pair : methodeIps.entrySet()) {

            if(pair.getKey() == DataCentre.ACTIVE) {
                continue;
            }

            if(activeIp.equals(pair.getValue())) {
                return pair.getKey();
            }
        }
        return null;
    }

    /**
     * Find out if you are in a certain place.
     * @param dataCentre
     * @return true if this JVM is physically within the named DataCentre; otherwise false
     */
    public boolean amIIn(DataCentre dataCentre) {
        Preconditions.checkNotNull(dataCentre);
        if(dataCentre.equals(iAmIn)) {
            return true;
        }
        return false;
    }

    public WhereIsMethodeResponse fullReport() {
        Map<DataCentre,String> methodeIps = reportIps();

        String activeIp = methodeIps.get(DataCentre.ACTIVE);

        DataCentre activeDc = whois(activeIp, methodeIps);

        return new WhereIsMethodeResponse(amIIn(activeDc), activeDc, methodeIps);
    }
}
