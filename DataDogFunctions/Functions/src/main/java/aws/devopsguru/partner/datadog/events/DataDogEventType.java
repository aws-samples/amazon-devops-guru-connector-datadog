package aws.devopsguru.partner.datadog.events;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.datadog.api.client.ApiClient;
import com.datadog.api.client.ApiException;
import com.datadog.api.client.v1.api.EventsApi;
import com.datadog.api.client.v1.model.EventAlertType;
import com.datadog.api.client.v1.model.EventCreateRequest;
import com.datadog.api.client.v1.model.EventCreateResponse;
import com.datadog.api.client.v1.model.EventPriority;
import com.fasterxml.jackson.databind.JsonNode;

import aws.devopsguru.partner.datadog.Constants;

public class DataDogEventType {

	private static ApiClient defaultApiClient;

	public static void createEvent(JsonNode input, Context context) {

		ApiClient defaultClient = getDefaultEventApiClient(context);
		EventsApi apiInstance = new EventsApi(defaultClient);
		Map<String, String> textDetail = null;
		EventCreateRequest eventCreateRequestBody = null;
		if (input.get("detail").has("anomalies")) {
			textDetail = new HashMap<>();
			textDetail.put("eventType", input.get("detail").get("messageType").toString());
			textDetail.put("insightName", input.get("detail-type").toString());
			textDetail.put("insightDescription", input.get("detail").get("insightDescription").toString());
			textDetail.put("insightId", input.get("detail").get("insightId").toString());
			textDetail.put("insightSeverity", input.get("detail").get("insightSeverity").toString());
			textDetail.put("insightType", input.get("detail").get("insightType").toString());
			textDetail.put("startTime",
					unixTimeStampToISOConverter(input.get("detail").get("startTime").asText().replace("\"", "")));
			textDetail.put("endTime",
					unixTimeStampToISOConverter(input.get("detail").get("startTime").asText().replace("\"", "")));
			textDetail.put("awsAccountId", input.get("detail").get("accountId").toString());
			textDetail.put("insightLink", input.get("detail").get("insightUrl").toString());
			textDetail.put("Anomalies",
					"[ metricName: "
							+ input.get("detail").get("anomalies").get(0).get("sourceDetails").get(0)
									.get("dataIdentifiers").get("name").toString()
							+ ", Dimensions: " + input.get("detail").get("anomalies").get(0).get("sourceDetails").get(0)
									.get("dataIdentifiers").get("dimensions").get(0).get("value").toString()
							+ "]");
		} else if (input.get("detail").has("recommendations")) {
			textDetail = new HashMap<>();
			textDetail.put("eventType", input.get("detail").get("messageType").toString());
			textDetail.put("insightId", input.get("detail").get("insightId").toString());
			textDetail.put("awsAccountId", input.get("detail").get("accountId").toString());
			textDetail.put("Anomalies",
					"[ metricName: "
							+ input.get("detail").get("recommendations").get(0).get("relatedAnomalies").get(0)
									.get("sourceDetails").get("cloudWatchMetrics").get(0).get("metricName").toString()
							+ ", Dimensions: "
							+ input.get("detail").get("recommendations").get(0).get("relatedAnomalies").get(0)
									.get("sourceDetails").get("cloudWatchMetrics").get(0).get("namespace").toString()
							+ "]");
		} else if (input.get("detail").get("messageType").toString().replace("\"", "")
				.equalsIgnoreCase("SEVERITY_UPGRADED")) {
			textDetail = new HashMap<>();
			textDetail.put("eventType", input.get("detail").get("messageType").toString());
			textDetail.put("insightId", input.get("detail").get("insightId").toString());
			textDetail.put("awsAccountId", input.get("detail").get("accountId").toString());
		}

		String linkUrl = null;
		if (input.get("detail").has("anomalies")) {
			input.get("detail").get("insightUrl").toString();
		}
		if (!input.get("detail").get("messageType").toString().replace("\"", "")
				.equalsIgnoreCase("SEVERITY_UPGRADED")) {
			eventCreateRequestBody = new EventCreateRequest()
					/*
					 * Title attribute of DataDog is mapped with detail-type attribute of DevOps
					 * Guru Insight
					 */
					.title(input.path("detail-type").asText())
					/*
					 * Text attribute of DataDog is mapped with detail attribute of DevOps Guru
					 * Insight
					 */
					.text(textDetail.toString())
					/*
					 * Tags attribute of DataDog is mapped with messageType attribute of DevOps Guru
					 * Insight
					 */
					.tags(Collections.singletonList(input.get("detail").get("messageType").toString()))
					/*
					 * Aggregation Key attribute of DataDog is mapped with accountId attribute of
					 * DevOps Guru Insight
					 */
					.aggregationKey(input.get("detail").get("insightId").toString()).alertType(EventAlertType.ERROR)
					/*
					 * Device Name attribute of DataDog is mapped with Account attribute of DevOps
					 * Guru Insight and Date Happened attribute of DataDog is taking current system
					 * time to log the event
					 */
					.dateHappened(System.currentTimeMillis() / 1000L).deviceName(input.path("account").asText())
					/*
					 * Host attribute of DataDog is mapped with accountId attribute of DevOps Guru
					 * Insight
					 */
					.host(linkUrl)
					/*
					 * Related Event Id attribute of DataDog is mapped with insightId attribute of
					 * DevOps Guru Insight
					 */
					.relatedEventId(input.get("detail").get("insightId").asLong()).priority(EventPriority.NORMAL)
					/*
					 * Source Type Name attribute of DataDog is mapped with source attribute of
					 * DevOps Guru Insight
					 */
					.sourceTypeName(input.get("source").toString());
		} else {
			eventCreateRequestBody = new EventCreateRequest()
					/*
					 * Title attribute of DataDog is mapped with detail-type attribute of DevOps
					 * Guru Insight
					 */
					.title(input.path("detail-type").asText())
					/*
					 * Text attribute of DataDog is mapped with detail attribute of DevOps Guru
					 * Insight
					 */
					.text(textDetail.toString())
					/*
					 * Tags attribute of DataDog is mapped with messageType attribute of DevOps Guru
					 * Insight
					 */
					.tags(Collections.singletonList(input.get("detail").get("messageType").toString()))
					/*
					 * Aggregation Key attribute of DataDog is mapped with accountId attribute of
					 * DevOps Guru Insight
					 */
					.aggregationKey(input.get("detail").get("insightId").toString()).alertType(EventAlertType.ERROR)
					/*
					 * Device Name attribute of DataDog is mapped with Account attribute of DevOps
					 * Guru Insight and Date Happened attribute of DataDog is taking current system
					 * time to log the event
					 */
					.dateHappened(System.currentTimeMillis() / 1000L).deviceName(input.path("account").asText())
					/*
					 * Related Event Id attribute of DataDog is mapped with insightId attribute of
					 * DevOps Guru Insight
					 */
					.relatedEventId(input.get("detail").get("insightId").asLong()).priority(EventPriority.NORMAL)
					/*
					 * Source Type Name attribute of DataDog is mapped with source attribute of
					 * DevOps Guru Insight
					 */
					.sourceTypeName(input.get("source").toString());
		}
		try {
			EventCreateResponse eventCreateResponse = apiInstance.createEvent(eventCreateRequestBody);
			Constants.getLogger().info("Response from the create event API call:  {}" + eventCreateResponse.toString());
		} catch (ApiException e) {
			Constants.getLogger().error("Exception when calling EventsApi#createEvent");
			Constants.getLogger().error("Status code: " + e.getCode());
			Constants.getLogger().error("Reason: " + e.getResponseBody());
			Constants.getLogger().error("Response headers: " + e.getResponseHeaders());
		}
	}

	public static String unixTimeStampToISOConverter(String unixTimeStampInput) {
		long unixTimeStamp = Long.parseLong(unixTimeStampInput);
		Instant instant = Instant.ofEpochSecond(unixTimeStamp);
		LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
		DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
		return localDateTime.format(isoFormatter);
	}

	public static ApiClient getDefaultEventApiClient(Context context) {
		String apiKey;
		String appKey;
		String ddSite;

		if (defaultApiClient != null) {
			return defaultApiClient;
		}

		JSONObject serviceNowCredentials = getSecret(System.getenv("SECRET_NAME"), context);
		try {
			apiKey = serviceNowCredentials.getString("DD_API_KEY");
			appKey = serviceNowCredentials.getString("DD_APP_KEY");
			ddSite = serviceNowCredentials.getString("DD_SITE");
		} catch (JSONException e) {
			context.getLogger().log("ERROR JsonProcessingException : " + e.getMessage());
			throw new RuntimeException(e);
		}

		if (ddSite == null || apiKey == null || appKey == null) {
			throw new RuntimeException("ERROR! Could not find environment variables for DataDog!");
		}
		defaultApiClient = new ApiClient();

		// Configure the Datadog site to send API calls to
		String site = ddSite;
		if (site != null) {
			HashMap<String, String> serverVariables = new HashMap<String, String>();
			serverVariables.put("site", site);
			defaultApiClient.setServerVariables(serverVariables);
		}
		// Configure API key authorization
		HashMap<String, String> secrets = new HashMap<String, String>();
		String apiKeyAuth = apiKey;
		if (apiKeyAuth != null) {
			secrets.put("apiKeyAuth", apiKeyAuth);
		}
		String appKeyAuth = appKey;
		if (appKeyAuth != null) {
			secrets.put("appKeyAuth", appKeyAuth);
		}
		defaultApiClient.configureApiKeys(secrets);

		return defaultApiClient;
	}

	public static JSONObject getSecret(String secretName, Context context) {

		AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard().build();

		String secret;
		GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(secretName);
		GetSecretValueResult getSecretValueResult = null;

		try {
			getSecretValueResult = client.getSecretValue(getSecretValueRequest);
		} catch (Exception e) {
			context.getLogger().log("ERROR Exception : " + e.getMessage());
			throw e;
		}
		if (getSecretValueResult.getSecretString() != null) {
			secret = getSecretValueResult.getSecretString();
			try {
				return new JSONObject(secret);
			} catch (JSONException e) {
				context.getLogger().log("ERROR JSONException : " + e.getMessage());
				throw new RuntimeException(e);
			}
		}
		context.getLogger().log("Error while fetching secret values, refer to above logs");
		throw new RuntimeException("Error while fetching secret values, refer to above logs");
	}

}