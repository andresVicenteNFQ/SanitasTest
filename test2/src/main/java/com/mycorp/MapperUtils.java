package com.mycorp;


import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ning.http.client.Response;

/**
 * 
 * @author Andres.Vicente
 *
 */
public class MapperUtils {
	
    /**
     * 
     */
    private static ObjectMapper mapper;

    
    /**
     * 
     * @param object
     * @return
     */
    public static byte[] json(Object object) {
    	
    	createMapper();
        try {
            return mapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new ZendeskException(e.getMessage(), e);
        }
    }
    
    /**
     * 
     * @param clazz
     * @param typeParams
     * @return
     */
    public static JavaType constructParametricType(Class clazz, Class... typeParams) {
    	return mapper.getTypeFactory().constructParametricType(clazz, typeParams);
    }
    
    /**
     * 
     * @param response
     * @param name
     * @param type
     * @return
     */
    public static Object convertValue(Response response, String name, JavaType type) {
    	try {
			return mapper.convertValue(mapper.readTree(response.getResponseBodyAsStream()).get(name), type);
		} catch (IllegalArgumentException | IOException e) {
			throw new ZendeskException(response.toString());
		}
    }
    
    /**
     * 
     * @param response
     * @param name
     * @param type
     * @return
     */
    public static Object convertValue(Response response, String name, Class type) {
    	try {
			return mapper.convertValue(mapper.readTree(response.getResponseBodyAsStream()).get(name), type);
		} catch (IllegalArgumentException | IOException e) {
			throw new ZendeskException(response.toString());
		}
    }
    
    
    
    /**
     * 
     * @return
     */
    private static ObjectMapper createMapper() {
    	
    	if (mapper == null) {
	        ObjectMapper mapper = new ObjectMapper();
	        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
	        mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
	        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	        
    	}
    	
    	return mapper;
    }

}
