package com.mycorp;

import com.ning.http.client.AsyncHttpClient;

/***
 * 
 * @author Andres.Vicente
 *
 */
public class Builder {
	
	/**
	 * 
	 */
    private AsyncHttpClient client = null;
    
    /**
     * 
     */
    private final String url;
    
    /**
     * 
     */
    private String username = null;
    
    /**
     * 
     */
    private String password = null;
    
    /**
     * 
     */
    private String token = null;

    /**
     * 
     * @param url
     */
    public Builder(String url) {
        this.url = url;
    }

    /**
     * 
     * @param username
     * @return
     */
    public Builder setUsername(String username) {
        this.username = username;
        return this;
    }

    /**
     * 
     * @param password
     * @return
     */
    public Builder setPassword(String password) {
        this.password = password;
        if (password != null) {
            this.token = null;
        }
        return this;
    }

    /**
     * 
     * @param token
     * @return
     */
    public Builder setToken(String token) {
        this.token = token;
        if (token != null) {
            this.password = null;
        }
        return this;
    }

    /**
     * 
     * @return
     */
    public Zendesk build() {
        if (token != null) {
            return new Zendesk(client, url, username + "/token", token);
        }
        return new Zendesk(client, url, username, password);
    }
}
