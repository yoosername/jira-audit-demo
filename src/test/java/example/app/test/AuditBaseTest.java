package example.app.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.testkit.client.Backdoor;
import com.atlassian.jira.testkit.client.model.FeatureFlag;
import com.atlassian.jira.testkit.client.util.TestKitLocalEnvironmentData;
import com.atlassian.jira.testkit.client.util.TimeBombLicence;
import com.atlassian.jira.webtest.webdriver.selenium.PageContainsCondition;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.webdriver.utils.element.ElementIsVisible;
import com.atlassian.webdriver.utils.element.ElementNotVisible;

public abstract class AuditBaseTest {

	private Logger logger = Logger.getLogger(AuditBaseTest.class);
	
	protected JiraTestedProduct jira;
	protected Backdoor backdoor;

	protected static final String AUDIT_TEST_USER = "audituser";
	protected static final String  AUDIT_TEST_PASSWORD = "auditpassword";
	protected static final String  AUDIT_TEST_DISPLAYNAME = "Audit Testing User";
	protected static final String  AUDIT_TEST_EMAIL = "audituser@localhost";
	protected static final String AUDIT_TEST_RELATIVE_LOG_FILE = "../../target/jira/home/log/custom-audit.log";
	protected AuditLogTester auditLog;

	@Before
	public void setup() throws FileNotFoundException {

		// Get handle to JIRA_HOME
		String jiraHomeDir = ClassLoader.getSystemClassLoader().getResource(".").getPath();
		// Get a Log Tester for required the audit log
		auditLog = new AuditLogTester(new File(jiraHomeDir,AUDIT_TEST_RELATIVE_LOG_FILE));

		// TestedProductFactory creates Page Object for JIRA that we use to control the behaviour in tests
		jira = TestedProductFactory.create(JiraTestedProduct.class);
		
		logger.debug("jira is : " +  jira);

		// Backdoor uses the Rest API to drive a JIRA Application instance
		backdoor = new Backdoor(new TestKitLocalEnvironmentData());
		logger.debug("backdoor is : " +  jira);

		// Start blank instance
		backdoor.restoreBlankInstance(TimeBombLicence.LICENCE_FOR_TESTING);
		// Restore standard Test Data
		//backdoor.restoreDataFromResource(AUDIT_TEST_BACKUP_XML, TimeBombLicence.LICENCE_FOR_TESTING);

		// Disable some unwanted plugins from local test container
		backdoor.plugins().disablePlugin("com.atlassian.support.stp");
		backdoor.plugins().disablePlugin("com.atlassian.feedback.jira-feedback-plugin");
		backdoor.plugins().disablePlugin("com.atlassian.analytics.analytics-client");

		// Disable Websudo requirement ( remove the extra step when accessing admin pages )
		backdoor.websudo().disable();

		// Add our test user if doesnt exist yet
		if(!backdoor.usersAndGroups().userExists(AUDIT_TEST_USER)){
			backdoor.usersAndGroups().addUser(
					AUDIT_TEST_USER,
					AUDIT_TEST_PASSWORD,
					AUDIT_TEST_DISPLAYNAME,
					AUDIT_TEST_EMAIL
					);
		}

		// Remove the Welcome Steps for a new user
		jira.backdoor().darkFeatures().disableForSite(FeatureFlag.featureFlag("jira.onboarding.feature"));

		// Now that weve logged in and gone home reset logging ready for tests
		auditLog.emptyFileAndResetLog();		
		// Start tailing the log;
		auditLog.startTailing();

	}

	@After
	public void teardown(){

		// Stop tailing the log;
		auditLog.stopTailing();

		// Delete cookies and logout
		logout();

		// Flush tested events
		auditLog.emptyFileAndResetLog();

	}
	
	/** 
	 * Utilities
	 * **/
	protected void login(){
		// Login to JIRA and Go Home
		gotoUrl("/login.jsp");
		waitSetFormElement(By.id("login-form-username"),AUDIT_TEST_USER);
		waitSetFormElement(By.id("login-form-password"),AUDIT_TEST_PASSWORD);
		waitAndClick(By.id("login-form-submit"));
	}

	protected void logout(){
		
		// Logout of JIRA via logout & confirm page
		gotoUrl("/secure/Logout!default.jspa");
		
		if(!pageSourceContains("You have already been logged out")){
			waitAndClick(By.id("confirm-logout-submit"));
		}
		
		jira.getTester().getDriver().manage().deleteAllCookies();
	}
	

	protected void assertTextNotPresent(String toFind) {
		assertFalse(pageSourceContains(toFind));
	}


	protected void assertTextPresent(String toFind) {
		assertTrue(pageSourceContains(toFind));
	}


	protected void click(By identifier) {
		jira.getTester().getDriver().findElement(identifier).click();
	}


	protected void waitForElement(By identifier) {
		new WebDriverWait(jira.getTester().getDriver(), 60).until(new ElementIsVisible(identifier, null));
	}
	
	protected void waitForElementToDisappear(By identifier) {
		new WebDriverWait(jira.getTester().getDriver(), 60).until(new ElementNotVisible(identifier, null));
	}


	protected void waitAndClick(By identifier) {
		waitForElement(identifier);
		click(identifier);
	}


	protected void reindex() {
		jira.getTester().gotoUrl(jira.getProductInstance().getBaseUrl() + "/secure/admin/IndexAdmin.jspa");
		waitAndClick(By.id("indexing-submit"));
		waitAndClick(By.id("acknowledge_submit"));
	}


	protected void gotoUrl(String url) {
		jira.getTester().gotoUrl(jira.getProductInstance().getBaseUrl() + url);
		waitForElement(By.id("content"));
	}


	protected void gotoUrlBodyWait(String url) {
		gotoUrlBodyWait(url, "body");
	}

	protected void gotoUrlBodyWait(String url, String containsCondition) {
		jira.getTester().gotoUrl(jira.getProductInstance().getBaseUrl() + url);
		new WebDriverWait(jira.getTester().getDriver(), 60).until(new PageContainsCondition(containsCondition));
	}


	private boolean pageSourceContains(String toFind) {
		return jira.getTester().getDriver().getPageSource().contains(toFind);
	}


	protected void setFormElement(By identifier, String text) {
		jira.getTester().getDriver().findElement(identifier).clear();
		jira.getTester().getDriver().findElement(identifier).sendKeys(text);
	}


	protected void waitSetFormElement(By identifier, String text) {
		waitForElement(identifier);
		setFormElement(identifier, text);

	}

}