package com.ft.methodeapi.service.atc;

import com.ft.methodeapi.atc.AirTrafficController;
import com.ft.methodeapi.atc.AtcConfiguration;
import com.ft.methodeapi.atc.DataCentre;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * AirTrafficControllerTest
 *
 * @author Simon.Gibbs
 */
public class AirTrafficControllerTest {

    /**
     * Output of the DOS command "nslookup"
     */
    private static String WINDOWS_NS_LOOKUP_OUTPUT = "Server:  UnKnown\n" +
            "Address:  10.112.31.21\n" +
            "\n" +
            "Non-authoritative answer:\n" +
            "Name:    ftcms01-uviw-uk-p.osb.ft.com\n" +
            "Address:  10.118.101.117\n" +
            "Aliases:  ftcms-p.osb.ft.com\n" +
            "\n";

    private static String WINDOWS_NS_LOOKUP_OUTPUT_EXAMPLE = "Server:  UnKnown\n" +
            "Address:  10.112.31.21\n" +
            "\n" +
            "Non-authoritative answer:\n" +
            "Name:    example.com\n" +
            "Addresses:  2606:2800:220:6d:26bf:1447:1097:aa7\n" +
            "          93.184.216.119\n";

    /**
     * Output of the Linux command "host"
     */
    private static String LINUX_NS_LOOKUP_OUTPUT = "Server:         10.112.31.14\n" +
            "Address:        10.112.31.14#53\n" +
            "\n" +
            "Non-authoritative answer:\n" +
            "ftcms-p.osb.ft.com      canonical name = ftcms01-uviw-uk-p.osb.ft.com.\n" +
            "Name:   ftcms01-uviw-uk-p.osb.ft.com\n" +
            "Address: 10.118.101.117";


    private static String LINUX_NS_LOOKUP_OUTPUT_EXAMPLE ="Server:         10.112.31.14\n" +
            "Address:        10.112.31.14#53\n" +
            "\n" +
            "Non-authoritative answer:\n" +
            "Name:   example.com\n" +
            "Address: 93.184.216.119\n";


    @Test
    public void shouldReturnIPAddressFromLinuxOutput() {
        String ip = AirTrafficController.parseIpFromNsLookupOutput(LINUX_NS_LOOKUP_OUTPUT);
        assertThat(ip,is("10.118.101.117"));
    }

    @Test
    public void shouldReturnIPAddressFromWindowsOutput() {
        String ip = AirTrafficController.parseIpFromNsLookupOutput(WINDOWS_NS_LOOKUP_OUTPUT);
        assertThat(ip,is("10.118.101.117"));
    }

    @Test
    public void shouldReturnIpAddressForExampleDotComOnLinux() {
        String ip = AirTrafficController.parseIpFromNsLookupOutput(LINUX_NS_LOOKUP_OUTPUT_EXAMPLE);
        assertThat(ip,is("93.184.216.119"));
    }

    @Test
    public void shouldReturnIpAddressForExampleDotComOnWindows() {
        String ip = AirTrafficController.parseIpFromNsLookupOutput(WINDOWS_NS_LOOKUP_OUTPUT_EXAMPLE);
        assertThat(ip,is("93.184.216.119"));
    }

    @Test
    public void shouldDoRealLookupOfExampleDotComAccurately() {
        String ip = AirTrafficController.lookup("example.com");
        assertThat(ip,is("93.184.216.119"));
    }

    @Test
    public void shouldReturnRealDCNameForIp() {
        Map<DataCentre,String> reportedIps = new LinkedHashMap<>(3);
        reportedIps.put(DataCentre.ACTIVE,"192.168.0.1");
        reportedIps.put(DataCentre.PARK_ROYAL,"192.168.0.2");
        reportedIps.put(DataCentre.WATFORD,"192.168.0.1");

        AirTrafficController sit = new AirTrafficController(mock(AtcConfiguration.class));

        DataCentre result = sit.whois("192.168.0.1",reportedIps);

        assertThat(result,is(DataCentre.WATFORD));
    }

}
