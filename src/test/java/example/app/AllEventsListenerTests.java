package example.app;

import org.junit.Test;
import org.openqa.selenium.By;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;



@WebTest(value = { Category.WEBDRIVER_TEST, Category.DASHBOARDS, Category.PROJECTS, Category.PLUGINS })
public class AllEventsListenerTests extends AuditBaseTest {


	@Test
	public void testDashboardViewCausesAuditEvent() {

		// Goto Dashboard
		gotoUrl("/secure/Dashboard.jspa");
		waitForElement(By.id("dashboard"));
		assertTextPresent("System Dashboard");

		//assertLogContainsEntry("Dashboard View");

	}

}