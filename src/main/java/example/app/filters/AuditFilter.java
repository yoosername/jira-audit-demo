package example.app.filters;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import example.app.service.AuditService;
import example.app.service.AuditableEvent;

public class AuditFilter implements Filter {

	private static final Logger log = LoggerFactory.getLogger(AuditFilter.class);
	private AuditService auditService;
	private SearchService searchService;

	@Autowired
	public AuditFilter(AuditService auditService, @ComponentImport SearchService searchService){
		this.auditService = auditService;
	}

	@Override
	public void destroy() {

	}

	private ApplicationUser getCurrentUser(){
		JiraAuthenticationContext context = ComponentAccessor.getJiraAuthenticationContext();
		return context.getLoggedInUser();
	}

	private static Map<String, String[]> getQueryParameters(HttpServletRequest request) {
		Map<String, String[]> queryParameters = new HashMap<>();
		String queryString = request.getQueryString();

		if (StringUtils.isEmpty(queryString)) {
			return queryParameters;
		}

		String[] parameters = queryString.split("&");

		for (String parameter : parameters) {
			String[] keyValuePair = parameter.split("=");
			String[] values = queryParameters.get(keyValuePair[0]);
			values = ArrayUtils.add(values, keyValuePair.length == 1 ? "" : keyValuePair[1]); //length is one if no value is available.
			queryParameters.put(keyValuePair[0], values);
		}
		return queryParameters;
	}

	private int getNumberOfIssuesForJQLQuery(ApplicationUser user, String jql){

		int count = 0;
		final SearchService.ParseResult parseResult = searchService.parseQuery(user, jql);
		if (parseResult.isValid())
		{
			try
			{
				final SearchResults results = searchService.search(user, parseResult.getQuery(), PagerFilter.getUnlimitedFilter());
				count += results.getTotal();
			}
			catch (SearchException e)
			{
				log.error("Error running search", e);
			}
		}
		else
		{
			log.warn("Error parsing jqlQuery: " + parseResult.getErrors());
		}

		return count;
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		try{
			HttpServletRequest request = (HttpServletRequest) req;
			String uri = request.getRequestURI();
			String queryString = request.getQueryString();
			Map<String, String[]> params = getQueryParameters(request);


			// Example: /jira/browse
			// Is actually redirected to: /jira/secure/ProjectIssueNavigatorAction:issueViewWithSidebar.jspa
			boolean isIssueBrowseUrl = uri.contains("ProjectIssueNavigatorAction");

			if( isIssueBrowseUrl ){

				String issueKey = params.get("issueKey")[0].toString();
				log.debug(uri + "?" + queryString + " - " + params.get("issueKey")[0].toString());

				auditService.handleEvent(
						new AuditableEvent()
						.withUserId(getCurrentUser().getUsername())
						.withUrl(uri)
						.withType("Browse Issue")
						.withTypeDescription(issueKey)
						.isContentAffectedAction(false)
						.withIsAnonymousAction(false)
						.withIsAdminOnly(false)
						.withIsDestructiveAction(false)
						.at(new Date())
						);
			}

			// Example: /jira/issues/?jql=text%20~%20"search%20text"
			//log.debug(uri + "?" + queryString + " - " + params.get("issueKey")[0].toString());
			boolean isIssueSearchUrl = uri.contains("issues");
			if( isIssueSearchUrl ){

				String jql = params.get("jql")[0].toString();
				int issueCount = getNumberOfIssuesForJQLQuery(getCurrentUser(), jql);

				auditService.handleEvent(
						new AuditableEvent()
						.withUserId(getCurrentUser().getUsername())
						.withUrl(uri)
						.withType("JQL Search")
						.withTypeDescription(jql + " (" + issueCount + ")" )
						.isContentAffectedAction(false)
						.withIsAnonymousAction(false)
						.withIsAdminOnly(false)
						.withIsDestructiveAction(false)
						.at(new Date())
						);
			}


		}
		catch(Exception e){
			log.debug(e.getMessage());
		}
		finally{
			chain.doFilter(req, res);
		}

	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub

	}

}
