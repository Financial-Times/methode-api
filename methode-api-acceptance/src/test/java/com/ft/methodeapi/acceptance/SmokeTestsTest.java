/**
 * AccessTests
 *
 * @author Simon.Gibbs
 */
package com.ft.methodeapi.acceptance;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        monochrome=true,
        tags = "@smoke",
        format = { "pretty", "html:target/cucumber-html-report", "json:target/cucumber-json-report/access.json" }
)
public class SmokeTestsTest {
}
