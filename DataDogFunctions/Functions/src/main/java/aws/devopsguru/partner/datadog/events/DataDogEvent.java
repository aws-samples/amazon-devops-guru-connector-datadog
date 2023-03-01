package aws.devopsguru.partner.datadog.events;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import aws.devopsguru.partner.datadog.Constants;

public class DataDogEvent implements RequestHandler<Map<String, Object>, String> {

	public String handleRequest(Map<String, Object> input, Context context) {
		Constants.getLogger().info("Inside the DataDog Event handleRequest");
		// Take the object and make a string representation of it
		String result = "SUCCESS";
		ObjectMapper objectMapper = new ObjectMapper();
		String inputData = null;
		try {
			inputData = objectMapper.writeValueAsString(input);
		} catch (JsonProcessingException e) {
			result = "FAILED";
			Constants.getLogger().error(
					"DataDogEvent, handleRequest input map conversion to String inputData error: {}", e.getMessage());
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
