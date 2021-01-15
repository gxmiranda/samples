package adapters.jira.rest;

public class ConnectionDetails {
	String url;
	String user;
	String password;
	
	public ConnectionDetails(String url, String user, String password) {
		this.url = url;
		this.user = user;
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}
	

	@Override
	public String toString() {
		return "URL:"+ url +"\tUser:"+ user +"\tPassword:"+ passwordToAsterisk();
	}

	private String passwordToAsterisk() {
		String s = "";
		if (password == null) return s;
		for (int i = 0; i < password.length() ; i++) s += "*"; 
		return s;
	}
}
