package edu.ncsu.csc.itrust.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

public class FilteredTransactionLogTest extends iTrustSeleniumTest {
    private WebDriver driver = null;

    protected void setUp() throws Exception {
        super.setUp();
        gen.clearAllTables();
        gen.standardData();
    }

    public void viewTransLogTest() throws Exception {

    }

    public void sumTransLogTest() throws Exception {
        //Login
        driver = login("9000000001", "pw");
        assertEquals("iTrust - Patient Home", driver.getTitle());

        //Click on "Transaction Log"
        driver.findElement(By.linkText("Transaction Log")).click();

        //Select "Doctor" for first role
        new Select(driver.findElement(By.name("userRole"))).selectByVisibleText("Doctor");

        //Select "Patient" for second role
        new Select(driver.findElement(By.name("secondaryRole"))).selectByVisibleText("Patient");

        //Set dates
        driver.findElement(By.name("startDate")).sendKeys("06/25/2007");
        driver.findElement(By.name("endDate")).sendKeys("06/26/2007");

        //select "1900" for transaction type
        new Select(driver.findElement(By.name("transactionType"))).selectByVisibleText("1900");

        //Click on "Summary button"
        driver.findElement(By.linkText("Sum Filtered Record")).click();

        //Find charts
        assertFalse(driver.findElement(By.name("chart1")) == null);
        assertFalse(driver.findElement(By.name("chart2")) == null);
        assertFalse(driver.findElement(By.name("chart3")) == null);
        assertFalse(driver.findElement(By.name("chart4")) == null);
    }
}
