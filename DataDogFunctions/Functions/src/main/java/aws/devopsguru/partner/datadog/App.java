package aws.devopsguru.partner.datadog;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import aws.devopsguru.partner.datadog.events.DataDogEventType;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<Map<String, Object>, Object> {

    public Object handleRequest(Map<String, Object> input, Context context) {

Constants.getLogger().info("Inside the DataDog Event handleRequest");
		// Take the object and make a string representation of it
		String result = "SUCCESS";
		ObjectMapper objectMapper = new ObjectMapper();
		String inputData = null;
		try {
			inputData = objectMapper.writeValueAsString(input);
		} catch (JsonProcessingException e) {
			result = "FAILED";
			Constants.getLogger().error("DataDogEvent, handleRequest input map conversion to String inputData error: {}",
					e.getMessage());
		}
		// Make a json out of that string
		JsonNode jsonNode = null;
		try {
			jsonNode = objectMapper.readTree(inputData);
		} catch (JsonMappingException e) {
			result = "FAILED";
			Constants.getLogger().error("DataDogEvent, handleRequest inputData conversion to JsonNode  error: {}",
					e.getMessage());
		} catch (JsonProcessingException e) {
			result = "FAILED";
			Constants.getLogger().error("DataDogEvent, handleRequest inputData conversion error: {}", e.getMessage());
		}
		DataDogEventType.createEvent(jsonNode, context);
		return result;
	        
    }

}
