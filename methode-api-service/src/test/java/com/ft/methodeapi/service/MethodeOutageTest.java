package com.ft.methodeapi.service;

import io.dropwizard.testing.junit.DropwizardAppRule;
import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

import org.junit.ClassRule;
import org.junit.runner.RunWith;

import com.ft.methodeapi.MethodeApiConfiguration;
import com.ft.methodeapi.MethodeApiApplication;

@RunWith(Cucumber.class)
@CucumberOptions(tags="@MethodeOutage", monochrome=true, format = { "pretty", "html:target/cucumber-html-report", "json:target/cucumber-json-report/publishing.json" })
public class MethodeOutageTest {
    @ClassRule
    public static DropwizardAppRule<MethodeApiConfiguration> appRule = new DropwizardAppRule<>(MethodeApiApplication.class, "methode-api-wrong-nsport.yaml");

}
