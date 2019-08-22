package edu.ncsu.csc.itrust.selenium;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import junit.framework.TestCase;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.FileUtils;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import edu.ncsu.csc.itrust.exception.DBException;
import edu.ncsu.csc.itrust.model.old.beans.TransactionBean;
import edu.ncsu.csc.itrust.model.old.enums.TransactionType;
import edu.ncsu.csc.itrust.unit.datagenerators.TestDataGenerator;
import edu.ncsu.csc.itrust.unit.testutils.TestDAOFactory;

/**
 * There's nothing special about this class other than adding a few handy test
 * utility methods and variables. When extending this class, be sure to invoke
 * super.setUp() first.
 */
abstract public class iTrustSeleniumTest extends TestCase {
	/*
	 * The URL for iTrust, change as needed
	 */
	/** ADDRESS */
	public static final String ADDRESS = "http://localhost:8080/iTrust/";
	/** gen */
	protected TestDataGenerator gen = new TestDataGenerator();

	/** Default timeout for Selenium webdriver */
	public static final int DEFAULT_TIMEOUT = 2;

	/**
	 * Name of the value attribute of html tags. Used for getting the value from
	 * a form input with .getAttribute("value"). Was previously
	 * .getAttribute(VALUE) before being removed by s selenium.
	 */
	public static final String VALUE = "value";

	/*
	Copied from TomcatBaseTest
	 */
	// Embedded tomcat instance
	private static Tomcat tomcat = null;
	// Path of the base directory used by Tomcat
	private static final String tomcatBaseDir = System.getProperty("tomcat.base.directory", "tomcat");
	// Path of the directory to which web application files are extracted
	private static final String webAppsBaseDir = System.getProperty("webapps.base.directory", "webapps");
	// Set of names of the web applications already added to tomcat
	private static final HashSet<String> addedWebApps = new HashSet<>();

	/* Returns a newly created file handler with the specified logging level that logs to a file in the base tomcat directory. */
	private static Handler createFileHandler() throws IOException
	{
		// Ensure that the base directory exists
		File baseDirFile = new File(tomcatBaseDir);
		if(!baseDirFile.isDirectory() && !baseDirFile.mkdirs()) {
			System.err.println("Failed to make base directory for embedded tomcat.");
		}
		Handler fileHandler = new FileHandler(tomcatBaseDir + File.separator + "catalina.out", true);
		fileHandler.setFormatter(new SimpleFormatter());
		fileHandler.setLevel( Level.INFO);
		fileHandler.setEncoding("UTF-8");
		return fileHandler;
	}

	/* Removes any existing handlers for the specified logger and adds the specified handler. */
	private static void replaceRootLoggerHandlers() throws IOException {
		Logger rootLogger = LogManager.getLogManager().getLogger("");
		rootLogger.setUseParentHandlers(false);
		// Change the level of any existing handlers to OFF
		for(Handler h : rootLogger.getHandlers()) {
			h.setLevel(Level.OFF);
		}
		// Add a file handler for INFO level logging
		rootLogger.addHandler(createFileHandler());
	}

	/* If the directory for the web-app with the specified name does not exists creates it by unzipping the war for the
	 * web-app. */
	private static void unzipWar(String warFile, String shortName) throws IOException {
		File dir = new File(webAppsBaseDir, shortName);
		if(!dir.isDirectory()) {
			// Unzip war into the directory only if it does not already exists
			File webAppWar = new File(warFile);
			if(!webAppWar.isFile()) {
				throw new RuntimeException("Could not find war file for: " + warFile);
			}
			try( ZipFile zipFile = new ZipFile(webAppWar)) {
				Enumeration<? extends ZipEntry> entries = zipFile.entries();
				while(entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();
					File entryDestination = new File(dir, entry.getName());
					if(entry.isDirectory()) {
						if(!entryDestination.isDirectory() && !entryDestination.mkdirs()) {
							throw new RuntimeException("Failed to make directory for: " + entryDestination);
						}
						// Add velocity.properties file
						File velocityPropFile = new File(entryDestination, "velocity.properties");
						try {
							org.apache.commons.io.FileUtils.writeStringToFile(velocityPropFile,
									"runtime.log.logsystem.class=org.apache.velocity.runtime.log.NullLogChute", Charset.defaultCharset(), false);
						} catch(Exception e) {
							//
						}
					} else {
						if(!entryDestination.getParentFile().isDirectory() && !entryDestination.getParentFile().mkdirs()) {
							throw new RuntimeException("Failed to make directory for: " + entryDestination.getParentFile());
						}
						if(entry.getName().endsWith("log4j.properties")) {
							// Write different logging properties
							writeLog4JProperties(entryDestination);
						} else {
							InputStream in = zipFile.getInputStream(entry);
							OutputStream out = new FileOutputStream(entryDestination);
							IOUtils.copy(in, out);
							IOUtils.closeQuietly(in);
							out.close();
						}
					}
				}
			}
		}
	}

	/* Writes log4j properties to disable logging to the specified file. */
	private static void writeLog4JProperties(File file) throws IOException {
		String content =
				"log4j.rootLogger=OFF, stdout\n" +
						"log4j.appender.stdout=org.apache.log4j.ConsoleAppender\n" +
						"log4j.appender.stdout.layout=org.apache.log4j.SimpleLayout\n";
		org.apache.commons.io.FileUtils.writeStringToFile(file,
				content, Charset.defaultCharset(), false);
	}

	@Override
	protected void setUp() throws Exception {
		if(tomcat == null) {
			try {
				System.out.println("Setting up tomcat");
				// Setup the embedded server
				tomcat = new Tomcat();
				tomcat.setBaseDir(tomcatBaseDir);
				tomcat.getHost().setAppBase(tomcatBaseDir);
				String protocol = Http11NioProtocol.class.getName();
				Connector connector = new Connector(protocol);
				// Listen on localhost
				connector.setAttribute("address", InetAddress.getByName("localhost").getHostAddress());
				connector.setAttribute("scanClassPath", "false");
				connector.setAttribute("scanAllFiles", "false");
				connector.setAttribute("scanAllDirectories", "false");
				// Use a random free port
				connector.setPort(8080);
//				connector.setPort(Integer.valueOf(System.getProperty("tomcat.port")));
				tomcat.getService().addConnector(connector);
				tomcat.setConnector(connector);
				tomcat.setSilent(true);
				tomcat.getHost().setDeployOnStartup(true);
				tomcat.getHost().setAutoDeploy(true);
				// Reduce logging
				System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
				replaceRootLoggerHandlers();
				tomcat.enableNaming(); //Needed to process context.xml
				tomcat.init();
				tomcat.start();

				//Deploy the app
				String warFile = "target/iTrust-23.0.0.war";
				unzipWar(warFile, "iTrust");

				Context ctx = tomcat.addWebapp("/iTrust", new File(webAppsBaseDir, "iTrust").getCanonicalPath());

				// Add the shutdown hook to stop the embedded server
				Runnable shutdown = new Runnable() {
					@Override
					public void run() {
						try {
							try {
								// Stop and destroy the server
								if (tomcat.getServer() != null && tomcat.getServer().getState() != LifecycleState.DESTROYED) {
									if (tomcat.getServer().getState() != LifecycleState.STOPPED) {
										tomcat.stop();
									}
									tomcat.destroy();
									tomcat = null;
								}
							} finally {
								// Delete tomcat's temporary working directory
								FileUtils.deleteDirectory(new File(tomcatBaseDir));
							}
						} catch (Throwable t) {
							t.printStackTrace();
						}
					}
				};
				Runtime.getRuntime().addShutdownHook(new Thread(shutdown));
			}catch(Throwable t){
				t.printStackTrace();
				throw t;
			}
		}
		gen.clearAllTables();
	}

	/**
	 * Helper method for logging in to iTrust
	 * 
	 * Also creates an explicit WebDriverWait for optional use.
	 * 
	 * @param username
	 *            username
	 * @param password
	 *            password
	 * @return {@link WebConversation}
	 * @throws Exception
	 */
	public WebDriver login(String username, String password) throws Exception {
		// begin at the iTrust home page
		WebDriver wd = new Driver();

		// Implicitly wait at most 2 seconds for each element to load
		wd.manage().timeouts().implicitlyWait(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

		wd.get(ADDRESS);
		// log in using the given username and password
		WebElement user = wd.findElement(By.name("j_username"));
		WebElement pass = wd.findElement(By.name("j_password"));
		user.sendKeys(username);
		pass.sendKeys(password);
		pass.submit();

		if (wd.getTitle().equals("iTrust - Login")) {
			throw new IllegalArgumentException("Error logging in, user not in database?");
		}

		return wd;
	}

	/**
	 * assertLogged
	 * 
	 * @param code
	 *            code
	 * @param loggedInMID
	 *            loggedInMID
	 * @param secondaryMID
	 *            secondaryMID
	 * @param addedInfo
	 *            addedInfo
	 * @throws DBException
	 */
	public static void assertLogged(TransactionType code, long loggedInMID, long secondaryMID, String addedInfo)
			throws DBException, InterruptedException {

		// Selenium on jenkins sometimes has issues finding a log the first
		// time.
		// The proper solution would be to add explicit waits, but it is easier
		// to wait a second and try again.
		int i = 0;
		while (i < 3) {
			List<TransactionBean> transList = TestDAOFactory.getTestInstance().getTransactionDAO().getAllTransactions();
			for (TransactionBean t : transList) {
				if ((t.getTransactionType() == code) && (t.getLoggedInMID() == loggedInMID)
						&& (t.getSecondaryMID() == secondaryMID)) {
					assertTrue(t.getTransactionType() == code);
					if (!t.getAddedInfo().trim().contains(addedInfo.trim())) {
						fail("Additional Information is not logged correctly.");
					}
					return;
				}
			}

			i++;
			Thread.sleep(1000);
		}

		fail("Event not logged as specified.");
	}

	/**
	 * assertLogged
	 * 
	 * @param code
	 *            code
	 * @param loggedInMID
	 *            loggedInMID
	 * @param secondaryMID
	 *            secondaryMID not used
	 * @param addedInfo
	 *            addedInfo
	 * @throws DBException
	 */
	public static void assertLoggedNoSecondary(TransactionType code, long loggedInMID, long secondaryMID,
			String addedInfo) throws DBException {
		List<TransactionBean> transList = TestDAOFactory.getTestInstance().getTransactionDAO().getAllTransactions();
		for (TransactionBean t : transList) {
			if ((t.getTransactionType() == code) && (t.getLoggedInMID() == loggedInMID)) {
				assertTrue(t.getTransactionType() == code);
				if (!t.getAddedInfo().trim().contains(addedInfo.trim())) {
					fail("Additional Information is not logged correctly.");
				}
				return;
			}
		}
		fail("Event not logged as specified.");
	}

	/**
	 * assertNotLogged
	 * 
	 * @param code
	 *            code
	 * @param loggedInMID
	 *            loggedInMID
	 * @param secondaryMID
	 *            secondaryMID
	 * @param addedInfo
	 *            addedInfo
	 * @throws DBException
	 */
	public static void assertNotLogged(TransactionType code, long loggedInMID, long secondaryMID, String addedInfo)
			throws DBException {
		List<TransactionBean> transList = TestDAOFactory.getTestInstance().getTransactionDAO().getAllTransactions();
		for (TransactionBean t : transList) {
			if ((t.getTransactionType() == code) && (t.getLoggedInMID() == loggedInMID)
					&& (t.getSecondaryMID() == secondaryMID) && (t.getAddedInfo().trim().contains(addedInfo))) {
				fail("Event was logged, but should NOT have been logged");
				return;
			}
		}
	}

}
