package com.mycorp;

import java.io.Closeable;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mycorp.support.Ticket;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Realm;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.uri.Uri;

/**
 * 
 * @author Andres.Vicente
 *
 */
public class Zendesk implements Closeable {
	/**
	 * 
	 */
    private static final String JSON = "application/json; charset=UTF-8";
    
    /**
     * 
     */
    private static final Pattern RESTRICTED_PATTERN = Pattern.compile("%2B", Pattern.LITERAL);
    
    /**
     * 
     */
    private final boolean closeClient;
    
    /**
     * 
     */
    private final AsyncHttpClient client;
    
    /**
     * 
     */
    private final Realm realm;
    
    /**
     * 
     */
    private final String url;
    
    /**
     * 
     */
    private final String oauthToken;
    
    /**
     * 
     */
    private final Logger logger;
    
    /**
     * 
     */
    private boolean closed = false;

	/**
	 * 
	 * @param client
	 * @param url
	 * @param username
	 * @param password
	 */
    public Zendesk(AsyncHttpClient client, String url, String username, String password) {
        this.logger = LoggerFactory.getLogger(Zendesk.class);
        this.closeClient = client == null;
        this.oauthToken = null;
        this.client = client == null ? new AsyncHttpClient() : client;
        this.url = url.endsWith("/") ? url + "api/v2" : url + "/api/v2";
        if (username != null) {
            this.realm = new Realm.RealmBuilder()
                    .setScheme(Realm.AuthScheme.BASIC)
                    .setPrincipal(username)
                    .setPassword(password)
                    .setUsePreemptiveAuth(true)
                    .build();
        } else {
            if (password != null) {
                throw new IllegalStateException("Cannot specify token or password without specifying username");
            }
            this.realm = null;
        }
    }

    /**
     * 
     * @param ticket
     * @return
     */
    public Ticket createTicket(Ticket ticket) {
        return complete(submit(req("POST", cnst("/tickets.json"),
                        JSON, MapperUtils.json(Collections.singletonMap("ticket", ticket))),
                handle(Ticket.class, "ticket")));
    }

    
    /**
     * 
     * @param method
     * @param template
     * @param contentType
     * @param body
     * @return
     */
    private Request req(String method, Uri template, String contentType, byte[] body) {
        RequestBuilder builder = new RequestBuilder(method);
        if (realm != null) {
            builder.setRealm(realm);
        } else {
            builder.addHeader("Authorization", "Bearer " + oauthToken);
        }
        builder.setUrl(RESTRICTED_PATTERN.matcher(template.toString()).replaceAll("+")); //replace out %2B with + due to API restriction
        builder.addHeader("Content-type", contentType);
        builder.setBody(body);
        return builder.build();
    }

    /**
     * 
     * @param template
     * @return
     */
    private Uri cnst(String template) {
        return Uri.create(url + template);
    }


    /**
     * 
     * @param request
     * @param handler
     * @return
     */
    private <T> ListenableFuture<T> submit(Request request, ZendeskAsyncCompletionHandler<T> handler) {
        if (logger.isDebugEnabled()) {
            if (request.getStringData() != null) {
                logger.debug("Request {} {}\n{}", request.getMethod(), request.getUrl(), request.getStringData());
            } else if (request.getByteData() != null) {
                logger.debug("Request {} {} {} {} bytes", request.getMethod(), request.getUrl(),
                        request.getHeaders().getFirstValue("Content-type"), request.getByteData().length);
            } else {
                logger.debug("Request {} {}", request.getMethod(), request.getUrl());
            }
        }
        return client.executeRequest(request, handler);
    }


    /**
     * 
     * @param clazz
     * @param name
     * @param typeParams
     * @return
     */
    protected <T> ZendeskAsyncCompletionHandler<T> handle(final Class<T> clazz, final String name, final Class... typeParams) {
        return new BasicAsyncCompletionHandler<T>(clazz, name, typeParams);
    }


    /**
     * Closeable interface methods
     * @return
     */
    public boolean isClosed() {
        return closed || client.isClosed();
    }
    
    /**
     * Closeable interface methods
     */
    public void close() {
        if (closeClient && !client.isClosed()) {
            client.close();
        }
        closed = true;
    }

    /**
     * Static helper methods
     * 
     * @param future
     * @return
     */
    private static <T> T complete(ListenableFuture<T> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            throw new ZendeskException(e.getMessage(), e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof ZendeskException) {
                throw (ZendeskException) e.getCause();
            }
            throw new ZendeskException(e.getMessage(), e);
        }
    }

    
}