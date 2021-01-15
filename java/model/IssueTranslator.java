package adapters.jira.model;

import org.json.JSONException;
import org.json.JSONObject;

public class IssueTranslator {

	Issue issue;

	public IssueTranslator(Issue issue) {
		this.issue = issue;
	}
	
	public JSONObject toJson() throws JSONException {
		JSONObject json = new JSONObject();
		if (issue.getKey() != null) json.put("key", issue.getKey());
		
		JSONObject fields = new JSONObject();
		if (issue.getProjectKey() != null) {
			JSONObject fieldsProject = new JSONObject();
			fieldsProject.put("key", issue.getProjectKey());
			fields.put("project", fieldsProject);
		}
		if (issue.getSummary() != null) fields.put("summary", issue.getSummary());
		if (issue.getDescription() != null) fields.put("description", issue.getDescription());
		if (issue.getType() != null) {
			JSONObject issueType = new JSONObject();
			issueType.put("name", issue.getType());
			fields.put("issuetype", issueType);
		}
		
		if (issue.getAssignee() != null) {
			JSONObject assignee = new JSONObject();
			assignee.put("name", issue.getAssignee());
			fields.put("assignee", assignee);
		}
		
		json.put("fields", fields);
		
		return json;
	}
	
}
