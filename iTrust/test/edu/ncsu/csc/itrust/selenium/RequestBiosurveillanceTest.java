package edu.ncsu.csc.itrust.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

public class RequestBiosurveillanceTest extends iTrustSeleniumTest {
    private WebDriver driver = null;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        gen.clearAllTables();
        gen.standardData();
    }

    public void testDetectEpidemicMalaria() throws Exception {
        //Login
        driver = login("9000000000", "pw");
        assertEquals("iTrust - HCP Home", driver.getTitle());

        //Click on "Transaction Log"
        driver.findElement(By.linkText("Request Biosurveillance")).click();

        //Type in 84.50 for icdCode
        driver.findElement(By.name("icdCode")).clear();
        driver.findElement(By.name("icdCode")).sendKeys("84.50");

        //Type in 27606 for zipCode
        driver.findElement(By.name("zipCode")).clear();
        driver.findElement(By.name("zipCode")).sendKeys("27606");

        //Set dates
        driver.findElement(By.name("date")).clear();
        driver.findElement(By.name("date")).sendKeys("11/09/2017");

        //Set threshold
        driver.findElement(By.name("threshold")).clear();
        driver.findElement(By.name("threshold")).sendKeys("4.9");

        //Click on "See Trends" button
        driver.findElement(By.name("getEpidemic")).click();

        //Find "Yes"
        assertFalse(driver.getPageSource().contains("<div>Yes</div>"));
    }

    public void testDetectEpidemicInfluenza() throws Exception {
        //Login
        driver = login("9000000000", "pw");
        assertEquals("iTrust - HCP Home", driver.getTitle());

        //Click on "Transaction Log"
        driver.findElement(By.linkText("Request Biosurveillance")).click();

        //Type in 487.00 for icdCode
        driver.findElement(By.name("icdCode")).clear();
        driver.findElement(By.name("icdCode")).sendKeys("487.00");

        //Type in 27606 for zipCode
        driver.findElement(By.name("zipCode")).clear();
        driver.findElement(By.name("zipCode")).sendKeys("27606");

        //Set dates
        driver.findElement(By.name("date")).clear();
        driver.findElement(By.name("date")).sendKeys("11/09/2017");

        //Clear the threshold
        driver.findElement(By.name("threshold")).clear();

        //Click on "See Trends" button
        driver.findElement(By.name("getEpidemic")).click();

        //Find "Yes"
        assertFalse(driver.getPageSource().contains("<div>Yes</div>"));
    }

    public void testDetectEpidemicInvalidICD() throws Exception {
        //Login
        driver = login("9000000000", "pw");
        assertEquals("iTrust - HCP Home", driver.getTitle());

        //Click on "Transaction Log"
        driver.findElement(By.linkText("Request Biosurveillance")).click();

        //Type in 487.00 for icdCode
        driver.findElement(By.name("icdCode")).clear();
        driver.findElement(By.name("icdCode")).sendKeys("70.9");

        //Type in 27606 for zipCode
        driver.findElement(By.name("zipCode")).clear();
        driver.findElement(By.name("zipCode")).sendKeys("27606");

        //Set dates
        driver.findElement(By.name("date")).clear();
        driver.findElement(By.name("date")).sendKeys("11/09/2017");

        //Clear the threshold
        driver.findElement(By.name("threshold")).clear();

        //Click on "See Trends" button
        driver.findElement(By.name("getEpidemic")).click();

        //Find "Yes"
        assertFalse(driver.getPageSource().contains("<div>invalid diagnosis code</div>"));
    }

    public void testDetectEpidemicInvalidZipCode() throws Exception {
        //Login
        driver = login("9000000000", "pw");
        assertEquals("iTrust - HCP Home", driver.getTitle());

        //Click on "Transaction Log"
        driver.findElement(By.linkText("Request Biosurveillance")).click();

        //Type in 487.00 for icdCode
        driver.findElement(By.name("icdCode")).clear();
        driver.findElement(By.name("icdCode")).sendKeys("487.00");

        //Type in 27606 for zipCode
        driver.findElement(By.name("zipCode")).clear();

        //Set dates
        driver.findElement(By.name("date")).clear();
        driver.findElement(By.name("date")).sendKeys("11/09/2017");

        //Clear the threshold
        driver.findElement(By.name("threshold")).clear();

        //Click on "See Trends" button
        driver.findElement(By.name("getEpidemic")).click();

        //Find "Yes"
        assertFalse(driver.getPageSource().contains("<div>invalid zip code</div>"));
    }

    public void testDetectEpidemicInvalidThreshold() throws Exception {
        //Login
        driver = login("9000000000", "pw");
        assertEquals("iTrust - HCP Home", driver.getTitle());

        //Click on "Transaction Log"
        driver.findElement(By.linkText("Request Biosurveillance")).click();

        //Type in 487.00 for icdCode
        driver.findElement(By.name("icdCode")).clear();
        driver.findElement(By.name("icdCode")).sendKeys("84.5");

        //Type in 27606 for zipCode
        driver.findElement(By.name("zipCode")).clear();
        driver.findElement(By.name("zipCode")).sendKeys("27606");

        //Set dates
        driver.findElement(By.name("date")).clear();
        driver.findElement(By.name("date")).sendKeys("11/09/2017");

        //Clear the threshold
        driver.findElement(By.name("threshold")).clear();

        //Click on "See Trends" button
        driver.findElement(By.name("getEpidemic")).click();

        //Find "Yes"
        assertFalse(driver.getPageSource().contains("<div>invalid threshold</div>"));
    }

    public void testSeeTrendsTest() throws Exception {
        //Login
        driver = login("9000000000", "pw");
        assertEquals("iTrust - HCP Home", driver.getTitle());

        //Click on "Transaction Log"
        driver.findElement(By.linkText("Request Biosurveillance")).click();

        //Type in 84.50 for icdCode
        driver.findElement(By.name("icdCode")).clear();
        driver.findElement(By.name("icdCode")).sendKeys("84.50");

        //Type in 11111 for zipCode
        driver.findElement(By.name("zipCode")).clear();
        driver.findElement(By.name("zipCode")).sendKeys("11111");

        //Set dates
        driver.findElement(By.name("date")).clear();
        driver.findElement(By.name("date")).sendKeys("09/06/2011");

        //Click on "See Trends" button
        driver.findElement(By.name("seeTrend")).click();

        //Find charts
        assertFalse(driver.getPageSource().contains("Invalid diagnosis code. Please try again!"));
        assertFalse(driver.getPageSource().contains("Invalid zip code. Please try again!"));
        assertFalse(driver.getPageSource().contains("Invalid date. Please try again!"));
        assertFalse(driver.findElement(By.id("diagchart")) == null);
    }

    public void testSeeTrendsInvalidICD() throws Exception {
        //Login
        driver = login("9000000000", "pw");
        assertEquals("iTrust - HCP Home", driver.getTitle());

        //Click on "Transaction Log"
        driver.findElement(By.linkText("Request Biosurveillance")).click();

        //Type in 84.50 for icdCode
        driver.findElement(By.name("icdCode")).clear();
        driver.findElement(By.name("icdCode")).sendKeys("hello i am icd code open sesame");

        //Type in 11111 for zipCode
        driver.findElement(By.name("zipCode")).clear();
        driver.findElement(By.name("zipCode")).sendKeys("11111");

        //Set dates
        driver.findElement(By.name("date")).clear();
        driver.findElement(By.name("date")).sendKeys("09/06/2011");

        //Click on "See Trends" button
        driver.findElement(By.name("seeTrend")).click();

        //Find charts
        assertTrue(driver.getPageSource().contains("Invalid diagnosis code. Please try again!"));
        assertFalse(driver.getPageSource().contains("Invalid zip code. Please try again!"));
        assertFalse(driver.getPageSource().contains("Invalid date. Please try again!"));
        assertTrue(driver.findElements(By.id("diagchart")).size() == 0);
    }

    public void testSeeTrendsInvalidZipcode() throws Exception {
        //Login
        driver = login("9000000000", "pw");
        assertEquals("iTrust - HCP Home", driver.getTitle());

        //Click on "Transaction Log"
        driver.findElement(By.linkText("Request Biosurveillance")).click();

        //Type in 84.50 for icdCode
        driver.findElement(By.name("icdCode")).clear();
        driver.findElement(By.name("icdCode")).sendKeys("84.50");

        //Type in 11111 for zipCode
        driver.findElement(By.name("zipCode")).clear();
        driver.findElement(By.name("zipCode")).sendKeys("im zippy the zipcode can i be your friend");

        //Set dates
        driver.findElement(By.name("date")).clear();
        driver.findElement(By.name("date")).sendKeys("09/06/2011");

        //Click on "See Trends" button
        driver.findElement(By.name("seeTrend")).click();

        //Find charts
        assertFalse(driver.getPageSource().contains("Invalid diagnosis code. Please try again!"));
        assertTrue(driver.getPageSource().contains("Invalid zip code. Please try again!"));
        assertFalse(driver.getPageSource().contains("Invalid date. Please try again!"));
        assertTrue(driver.findElements(By.id("diagchart")).size() == 0);
    }

    public void testSeeTrendsInvalidDate() throws Exception {
        //Login
        driver = login("9000000000", "pw");
        assertEquals("iTrust - HCP Home", driver.getTitle());

        //Click on "Transaction Log"
        driver.findElement(By.linkText("Request Biosurveillance")).click();

        //Type in 84.50 for icdCode
        driver.findElement(By.name("icdCode")).clear();
        driver.findElement(By.name("icdCode")).sendKeys("84.50");

        //Type in 11111 for zipCode
        driver.findElement(By.name("zipCode")).clear();
        driver.findElement(By.name("zipCode")).sendKeys("11111");

        //Set dates
        driver.findElement(By.name("date")).clear();
        driver.findElement(By.name("date")).sendKeys("Christmas");

        //Click on "See Trends" button
        driver.findElement(By.name("seeTrend")).click();

        //Find charts
        assertFalse(driver.getPageSource().contains("Invalid diagnosis code. Please try again!"));
        assertFalse(driver.getPageSource().contains("Invalid zip code. Please try again!"));
        assertTrue(driver.getPageSource().contains("Invalid date. Please try again!"));
        assertTrue(driver.findElements(By.id("diagchart")).size() == 0);
    }
}
