package example.app.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;

@WebTest(value = { Category.WEBDRIVER_TEST, Category.DASHBOARDS, Category.PROJECTS, Category.PLUGINS })
public class UserMentionedInCommentTest extends AuditBaseTest {


	/**
	 * Checks that mentioning a user in a comment triggers an audit event
	 */
	@Test
	public void testUserMentionedInCommentCausesAuditEvent() {

		// Assert that Audit log is empty at the start of this test
		assertTrue(auditLog.getLastLogEntry().equals(""));

		// Login
		login();

		// Create a new Issue
		IssueCreateResponse response = jira.backdoor().issues().createIssue("HSP", "Test Issue");
		backdoor.issues().commentIssue(response.key(), "test comment with message for [~admin]");

		auditLog.logAllEntries();

		// Check the log contains a User Mention
		assertTrue(auditLog.getLastLogEntry().contains("User Mention"));

		// Check the log contains the from and to users
		assertTrue(auditLog.getLastLogEntry().contains("from:admin(admin),to:[admin(admin)]"));

		// Check the log contains the message itself
		assertTrue(auditLog.getLastLogEntry().contains("msg:test comment with message for [~admin]"));



	}

	private void typeUsernameAndSelectFromPicker(String location, String user) {
		// Type username in the comment
		typeWithFullKeyEvents(location, user);
		// It should associated dropdown to pick a real user now
		// Hit enter to select it
		waitAndClick(By.className("jira-mention-suggestion"));
		//hitEnter();		
	}

	private void hitEnter() {
		jira.getTester().getDriver().getKeyboard().releaseKey(Keys.RETURN);
	}

	public void typeWithFullKeyEvents(String locator, String string)
	{
		char[] chars = string.toCharArray();
		jira.getTester().getDriver().findElement(By.id(locator)).click();
		for (int i = 0; i < chars.length; i++)
		{
			//jira.getTester().getDriver().getKeyboard().pressKey(Character.toString(chars[i]));
			jira.getTester().getDriver().getKeyboard().releaseKey(Character.toString(chars[i]));
		}
	}

	// TODO: 
	// Issue Exports
	// Quick Browse
	// Quick search
	// Issue search event
	// Issue View event
	// Issue Event ( create / delete / modify )
	// Project Event ( create / delete / modify )

}
