package example.app;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openqa.selenium.By;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;


@WebTest(value = { Category.WEBDRIVER_TEST, Category.DASHBOARDS, Category.PROJECTS, Category.PLUGINS })
public class AllEventsListenerTests extends AuditBaseTest {

	@Test
	public void testLoginCausesAuditEvent() {

		// Assert that Audit log is empty at the start of this test
		assertTrue(auditLog.getLastLogEntry().equals(""));

		// Normal login doesnt seem to cause event via functest so use testkit as means of login event
		backdoor.analytics().acknowledgePolicy();

		// Assert that Login audit entry was trapped
		assertTrue(auditLog.getLastLogEntry().contains("Login"));

		// and was created by the admin user
		assertTrue(auditLog.getLastLogEntry().contains("admin"));

	}

	@Test
	public void testDashboardViewCausesAuditEvent() {

		// Assert that Audit log is empty at the start of this test
		assertTrue(auditLog.getLastLogEntry().equals(""));

		login();

		// Goto Dashboard
		gotoUrl("/secure/Dashboard.jspa");
		waitForElement(By.id("dashboard"));
		assertTextPresent("System Dashboard");

		// Assert that Dashboard View audit entry was trapped
		assertTrue(auditLog.getLastLogEntry().contains("Dashboard View"));

		// and was created by the audituser test user
		assertTrue(auditLog.getLastLogEntry().contains("audituser"));

		// and captured the dashboard url
		assertTrue(auditLog.getLastLogEntry().contains("/jira/secure/Dashboard.jspa"));

		// and captured the dashboard url
		//assertTrue(containsValidDate(auditLog.getLastLogEntry()));

	}


}