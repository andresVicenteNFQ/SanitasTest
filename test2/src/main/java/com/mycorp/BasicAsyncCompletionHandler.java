package com.mycorp;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JavaType;
import com.ning.http.client.Response;

/**
 * 
 * @author Andres.Vicente
 *
 * @param <T>
 */
public class BasicAsyncCompletionHandler<T> extends ZendeskAsyncCompletionHandler<T> {
	
	/**
	 * 
	 */
    private final Class<T> clazz;
    
    /**
     * 
     */
    private final String name;
    
    /**
     * 
     */
    private final Class[] typeParams;
    
    /**
     * 
     */
    private final Logger logger;

    /**
     * 
     * @param clazz
     * @param name
     * @param typeParams
     */
    public BasicAsyncCompletionHandler(Class clazz, String name, Class... typeParams) {
        this.clazz = clazz;
        this.name = name;
        this.typeParams = typeParams;        
        this.logger = LoggerFactory.getLogger(BasicAsyncCompletionHandler.class);
    }

    /**
     * 
     */
    @Override
    public T onCompleted(Response response) throws Exception {
        logResponse(response);
        if (isStatus2xx(response)) {
            if (typeParams.length > 0) {
                JavaType type = MapperUtils.constructParametricType(clazz, typeParams);
                return (T) MapperUtils.convertValue(response,name, type);
            }
            return (T) MapperUtils.convertValue(response,name, clazz);
        } else if (isRateLimitResponse(response)) {
            throw new ZendeskException(response.toString());
        }
        if (response.getStatusCode() == 404) {
            return null;
        }
        throw new ZendeskException(response.toString());
    }
    
    /**
     * 
     * @param response
     * @throws IOException
     */
    private void logResponse(Response response) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Response HTTP/{} {}\n{}", response.getStatusCode(), response.getStatusText(),
                    response.getResponseBody());
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Response headers {}", response.getHeaders());
        }
    }
    
    /**
     * 
     * @param response
     * @return
     */
    private boolean isStatus2xx(Response response) {
        return response.getStatusCode() / 100 == 2;
    }
    
    /**
     * 
     * @param response
     * @return
     */
    private boolean isRateLimitResponse(Response response) {
        return response.getStatusCode() == 429;
    }
}