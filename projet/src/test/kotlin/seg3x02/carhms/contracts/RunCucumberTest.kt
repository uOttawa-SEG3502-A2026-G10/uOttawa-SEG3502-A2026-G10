package seg3x02.carhms.contracts

import org.junit.platform.suite.api.ConfigurationParameter
import org.junit.platform.suite.api.IncludeEngines
import org.junit.platform.suite.api.SelectClasspathResource
import org.junit.platform.suite.api.Suite

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("seg3x02.carhms.contracts")
@ConfigurationParameter(key = "cucumber.glue", value = "seg3x02.carhms.contracts")
@ConfigurationParameter(key = "cucumber.plugin", value = "pretty,html:build/reports/cucumber/contracts.html,json:build/reports/cucumber/contracts.json")
@ConfigurationParameter(key = "cucumber.publish.quiet", value = "true")
class RunCucumberTest
