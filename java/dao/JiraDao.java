package adapters.jira.dao;

import java.util.Date;

import org.json.JSONObject;

public interface JiraDao {

	public JSONObject getIssue(String key);
	
	public JSONObject getExpandedIssue(String key);
	
	public JSONObject search(String jql, int startAt, int maxResults);

	public JSONObject search(String jql, Date updatedOrCreatedSince);

	public JSONObject search(String jql, Date updatedOrCreatedSince, int startAt, int maxResults);
	
	
}
