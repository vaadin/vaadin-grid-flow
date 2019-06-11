/*
 * Copyright 2000-2018 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component;

import java.net.URL;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.testbench.Parameters;
import com.vaadin.testbench.TestBench;
import com.vaadin.testbench.parallel.Browser;

/**
 * Temp class for disabling the w3c communication mode on remote chrome.
 */
public class AbstractNoW3c extends AbstractComponentIT {

    @Override
    public void setup() throws Exception {
        if (Browser.CHROME == this.getRunLocallyBrowser() && (
                getRunOnHub(getClass()) != null
                        || Parameters.getHubHostname() != null)) {

            ChromeOptions options = new ChromeOptions();
            options.addArguments(
                    new String[] { "--headless", "--disable-gpu" });
            options.setExperimentalOption("w3c", false);

            options.merge(getDesiredCapabilities());
            setDesiredCapabilities(getDesiredCapabilities());

            WebDriver driver = TestBench.createDriver(
                    new RemoteWebDriver(new URL(getHubURL()), options));
            setDriver(driver);
        } else {
            super.setup();
        }
    }
}
