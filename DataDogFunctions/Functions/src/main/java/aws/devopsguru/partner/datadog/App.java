package aws.devopsguru.partner.datadog;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
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

		// Take the object and make a string representation of it
		String result = "SUCCESS";
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			String inputData = objectMapper.writeValueAsString(input);
			// Make a json out of that string
			JsonNode jsonNode = null;
			jsonNode = objectMapper.readTree(inputData);
			Constants.getLogger().info("Inside the DataDog Event handleRequest",
					jsonNode.get("detail").get("insightId").toString());
			DataDogEventType.createEvent(jsonNode, context);
		} catch (JsonMappingException e) {
			result = "FAILED";
			Constants.getLogger().error("DataDogEvent, handleRequest inputData conversion to JsonNode  error: {}",
					e.getMessage());
		} catch (JsonProcessingException e) {
			result = "FAILED";
			Constants.getLogger().error("DataDogEvent, handleRequest inputData conversion error: {}", e.getMessage());
		}
		return result;
	}
}
