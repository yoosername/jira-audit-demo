package example.app;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
import com.atlassian.jira.testkit.client.Backdoor;
import com.atlassian.jira.testkit.client.model.FeatureFlag;
import com.atlassian.jira.testkit.client.util.TestKitLocalEnvironmentData;
import com.atlassian.jira.testkit.client.util.TimeBombLicence;
import com.atlassian.jira.webtest.webdriver.selenium.PageContainsCondition;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.webdriver.utils.element.ElementIsVisible;


public abstract class AuditBaseTest {

	protected JiraTestedProduct jira;
	protected Backdoor backdoor;

	protected static final String AUDIT_TEST_USER = "audituser";
	protected static final String  AUDIT_TEST_PASSWORD = "auditpassword";
	protected static final String  AUDIT_TEST_DISPLAYNAME = "Audit Testing User";
	protected static final String  AUDIT_TEST_EMAIL = "audituser@localhost";
	
	protected static final String  AUDIT_TEST_BACKUP_XML = "jira-with-one-project-one-issue.zip";


	@Before
	public void setup() {
		
		// TestedProductFactory creates Page Object for JIRA that we use to control the behaviour in tests
		jira = TestedProductFactory.create(JiraTestedProduct.class);
		
		// Backdoor uses the Rest API to drive a JIRA Application instance
		backdoor = new Backdoor(new TestKitLocalEnvironmentData());
		
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
		
		// Login to JIRA and Go Home
		JiraLoginPage loginPage = jira.gotoLoginPage();
		loginPage.loginAndGoToHome(AUDIT_TEST_USER, AUDIT_TEST_PASSWORD);
		
	}

	@After
	public void teardown(){
		// Delete cookies and logout.
		jira.getTester().getDriver().manage().deleteAllCookies();
		jira.logout();
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

	protected File getAuditLogFile(String partialName){
		Enumeration e = Logger.getRootLogger().getAllAppenders();
	    while ( e.hasMoreElements() ){
	      Appender app = (Appender)e.nextElement();
	      if ( app instanceof FileAppender && ((FileAppender) app).getFile().contains(partialName) ){
	        return new File(((FileAppender) app).getFile());
	      }
	    }
	    return null;
	}
	protected void emptyLog() {
		File auditlog = getAuditLogFile("custom-audit");
		try {
			// Empty existing log
			new PrintWriter(auditlog).close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void assertLogContainsEntry(String expectedLogText) {
		
		List<String> lines;
		String actualLastLogEntry = "";
		
		try {
			lines = Files.readAllLines(Paths.get(getAuditLogFile("custom-audit").getPath()), Charset.defaultCharset());
			actualLastLogEntry = lines.get(lines.size());
		} catch (IOException e) {
			actualLastLogEntry = "couldnt read audit file";
		}finally{
			assertTrue(actualLastLogEntry.contains(expectedLogText));
		}
		

	}

}