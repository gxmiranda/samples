package adapters.jira.dao;

import java.text.SimpleDateFormat;
import java.util.Date;

import adapters.jira.model.Issue;
import adapters.jira.model.IssueTranslator;
import adapters.jira.rest.ConnectionDetails;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import adapters.jira.ssl.SslUtil;

public class JiraDaoRest implements JiraDao {
	Logger log = Logger.getLogger(this.getClass());

	RestTemplate template;
	ConnectionDetails connection;
	
	
	
	public JiraDaoRest(ConnectionDetails connection) {
		log.info("Initializing JiraDaoRest ["+ connection +"].");
		this.connection = connection;
		template = new RestTemplate();
		SslUtil.disableSslCertificationCheck();
	}


	// Services

	@Override
	public JSONObject getIssue(String key) {
		ResponseEntity<String> forEntity = template.getForEntity(constructUriForIssue(), String.class, key);
		return toJson(forEntity);
	}
	
	@Override
	public JSONObject getExpandedIssue(String key) {
		ResponseEntity<String> forEntity = template.getForEntity(constructUriForExpandedIssue(), String.class, key);
		return toJson(forEntity);
	}

	@Override
	public JSONObject search(String jql, int startAt, int maxResults) {
		ResponseEntity<String> forEntity = template.getForEntity(constructUriForSearch(startAt, maxResults), String.class, jql);
		return toJson(forEntity);
	}
	@Override
	public JSONObject search(String jql, Date updatedOrCreatedSince) {
		ResponseEntity<String> forEntity = template.getForEntity(constructUriForSearch(), String.class, constructJqlForSearch(jql, updatedOrCreatedSince));
		return toJson(forEntity);
	}
	@Override
	public JSONObject search(String jql, Date updatedOrCreatedSince, int startAt, int maxResults) {
		ResponseEntity<String> forEntity = template.getForEntity(constructUriForSearch(startAt, maxResults), String.class, constructJqlForSearch(jql, updatedOrCreatedSince));
		return toJson(forEntity);
	}
	
	
	public String upsert(Issue issue) {
		IssueTranslator translator = new IssueTranslator(issue);
		String json = null;
		try {
			json = translator.toJson().toString();
		} catch (JSONException e) {
			log.error(e, e);
		}
		log.debug("Upserting issue: "+ json);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(json ,headers);

		if (isUpdate(issue)) {

			template.put(constructUriForUpdateIssue(issue.getKey()), entity);

			return issue.getKey();
		} else {

			String response = template.postForObject(constructUriForCreateIssue(), entity, String.class);

			try {

				JSONObject responseJson = toJson(response);

				return responseJson.getString("key");
			} catch (JSONException e) {
				log.error(e,e);
				return null;
			}
		}
	}
	
	// Helpers
	
	private boolean isUpdate(Issue issue) {
		return issue.getKey() != null && !issue.getKey().trim().isEmpty();
	}


	private String constructUriForCreateIssue() {
		return constructUriForUpdateIssue("");
	}
	private String constructUriForUpdateIssue(String key) {
		String uri = connection.getUrl() + "/issue/" + key;
		uri += constructCredentialsSuffix(uri);
		return uri;
	}


	private JSONObject toJson(ResponseEntity<String> forEntity) {
		try {
			JSONObject json = new JSONObject(forEntity.getBody());
			return json;
		} catch (JSONException e) {
			log.error(e, e);
			throw new RestClientException(e.getMessage(), e);
		}
	}
	private JSONObject toJson(String response) {
		try {
			JSONObject json = new JSONObject(response);
			return json;
		} catch (JSONException e) {
			log.error(e, e);
			throw new RestClientException(e.getMessage(), e);
		}
	}


	private String constructUriForIssue() {
		String uri = connection.getUrl() + "/issue/{id}";
		uri += constructCredentialsSuffix(uri);
		return uri;
	}
	
	private String constructUriForExpandedIssue() {
		String uri = connection.getUrl() + "/issue/{id}?expand=changelog";
		uri += constructCredentialsSuffix(uri);
		return uri;
	}

	private String constructCredentialsSuffix(String uri) {
		String suffix = "";
		if (uri.indexOf("?") > -1) suffix += "&";
		else suffix += "?";
		
		suffix += "os_username="+ connection.getUser() +"&os_password="+ connection.getPassword();
		
		return suffix;
	}


	private String constructUriForSearch(int startAt, int maxResults) {
		String uri = connection.getUrl() + "/search?jql={jql}&startAt="+ startAt +"&maxResults="+ maxResults;
		uri += constructCredentialsSuffix(uri);
		log.debug("uri for search: "+ uri);
		return uri;
	}
	private String constructJqlForSearch(String jql, Date updatedOrCreatedSince) {
		String dateFilter = constuctDateFilter(updatedOrCreatedSince);
		String orderByUpdatedAsc = " ORDER BY updated ASC";
		jql = jql + dateFilter + orderByUpdatedAsc;
		log.debug("JQL for search: "+ jql);
		return jql;
	}

	private String constructUriForSearch() {
		String uri = connection.getUrl() + "/search?jql={jql}&maxResults=1000";
		uri += constructCredentialsSuffix(uri);
		return uri;
	}



	private String constuctDateFilter(Date updatedOrCreatedSince) {
		// And (updated >= '2012-12-30 00:00' or created >= '2012-12-30 00:00')
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String dateFormatted = sdf.format(updatedOrCreatedSince);
		String filter = " And (updated >= '"+ dateFormatted + "' or created >= '"+ dateFormatted +"')";
		return filter;
	}





}
