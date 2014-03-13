package com.ft.methodeapi.atc;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang.SystemUtils;

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


    public AirTrafficController(Map<DataCentre, String> hostnames) {
        this.hostnames = hostnames;
    }

    public Map<DataCentre, String> reportIps() {
        Map<DataCentre, String> results = new HashMap<>(hostnames.size());
        for(DataCentre dataCentre : hostnames.keySet()) {
            String serviceIp = lookup(hostnames.get(dataCentre));
            results.put(dataCentre,serviceIp);
        }
        return results;
    }


    public static String parseLookupOutput(String output) {
        Matcher matcher = IP_FINDER_PATTERN.matcher(output);
        if(matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }


    public static String lookup(String hostname) {

        CommandLine cmdLine;
        if(SystemUtils.IS_OS_WINDOWS) {
            cmdLine = new CommandLine("nslookup");
        } else {
            cmdLine = new CommandLine("host");
        }

        cmdLine.addArgument(hostname);

        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream(120);

        ExecuteWatchdog watchdog = new ExecuteWatchdog(3000);
        Executor executor = new DefaultExecutor();
        executor.setExitValue(0);
        executor.setWatchdog(watchdog);
        executor.setStreamHandler(new PumpStreamHandler(outputBuffer));

        try {
            executor.execute(cmdLine);
            String rawResult = new String(outputBuffer.toByteArray(), Charset.defaultCharset());

            return parseLookupOutput(rawResult);

        } catch (IOException e) {
            throw new AirTrafficControllerException("Failure while looking up system location", e);
        }
    }

    public DataCentre whois(String activeIp, Map<DataCentre, String> methodeIps) {
        for(DataCentre dc : methodeIps.keySet()) {

            if(dc == DataCentre.ACTIVE) {
                continue;
            }

            if(activeIp.equals(methodeIps.get(dc))) {
                return dc;
            }
        }
        return null;
    }
}
