package example.app.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;


@WebTest(value = { Category.WEBDRIVER_TEST, Category.DASHBOARDS, Category.PROJECTS, Category.PLUGINS })
public class LoginLogoutTest extends AuditBaseTest {

	
	/**
	 * Checks that logging in to JIRA triggers an audit event
	 */
	@Test
	public void testLoginCausesAuditEvent() {

		// Assert that Audit log is empty at the start of this test
		assertTrue(auditLog.getLastLogEntry().equals(""));

		// Login
		login();

		// Assert that Login audit entry was trapped
		assertTrue(auditLog.getLogEntry(0).contains("Login"));

		// and was created by the admin user
		assertTrue(auditLog.getLogEntry(0).contains(AuditBaseTest.AUDIT_TEST_USER));

	}

	/**
	 * Checks that logging out of JIRA triggers an audit event
	 */

	@Test
	public void testLogoutCausesAuditEvent() {

		// Assert that Audit log is empty at the start of this test
		assertTrue(auditLog.getLastLogEntry().equals(""));

		// Normal login doesnt seem to cause event via functest so use testkit as means of login event
		login();
		logout();


		// Assert that Login audit entry was trapped
		assertTrue(auditLog.getLastLogEntry().contains("Logout"));

		// and was created by the admin user
		assertTrue(auditLog.getLastLogEntry().contains(AuditBaseTest.AUDIT_TEST_USER));

	}


}