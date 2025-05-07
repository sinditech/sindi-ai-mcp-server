package za.co.sindi.ai.mcp.tools;

///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.quarkus:quarkus-bom:3.19.2@pom
//DEPS io.quarkiverse.mcp:quarkus-mcp-server-stdio:1.0.0.Beta4
//DEPS com.squareup.okhttp3:okhttp:3.14.9
//DEPS com.fasterxml.jackson.core:jackson-databind:2.18.3
//FILES application.properties

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import za.co.sindi.ai.mcp.server.spi.Tool;
import za.co.sindi.ai.mcp.server.spi.ToolArgument;

public class MCPRealWeather {

  private static final String DEFAULT_FORECAST_API_URL = "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&current_weather=true";
  private static final String DEFAULT_COORDINATES_API_URL = "https://geocoding-api.open-meteo.com/v1/search?name=%s&format=json";

  private final HttpClient httpClient = HttpClient.newBuilder().build();

  private String forecastApiUrl;
  private String coordinatesApiUrl;

  public MCPRealWeather() {
    this(DEFAULT_FORECAST_API_URL, DEFAULT_COORDINATES_API_URL);
  }

  //For testing purposes
  public MCPRealWeather(String forecastApiUrl, String coordinatesApiUrl) {
    this.forecastApiUrl = forecastApiUrl;
    this.coordinatesApiUrl = coordinatesApiUrl;
  }

  @Tool(description = "A tool to get the current weather a given city and country code")
  public String getWeather(
	@ToolArgument(description = "The city to get the weather for") String city, 
	@ToolArgument(description = "The country code to get the weather for") String countryCode) {

    return getCoordinates(city, countryCode)
        .flatMap(this::fetchWeather)
        .orElse(String.format("Sorry, I couldn't find the weather for %s, %s", city, countryCode));
  }

  private Optional<String> executeHTTPRequest(String url) {
    HttpRequest request = HttpRequest.newBuilder(URI.create(url)).build();
    try {
		HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
		int code = response.statusCode() / 100;
		if (code >= 2 && code <= 3) return Optional.ofNullable(response.body());
		
		System.err.println("Request failed with status: " + response.statusCode());
		return Optional.empty();
	} catch (IOException | InterruptedException e) {
		// TODO Auto-generated catch block
		System.err.println("Error executing HTTP request: " + e.getMessage());
	    return Optional.empty();
	}
  }

  private Optional<double[]> extractCoordinates(String jsonResponse, String countryCode) {
	  JsonReader reader = Json.createReader(new StringReader(jsonResponse));
	  JsonObject jsonNode = reader.readObject();
	  JsonArray resultsArray = jsonNode.getJsonArray("results");
	  
	  if (Objects.isNull(resultsArray) || resultsArray.isEmpty()) {
        return Optional.empty();
      }
	  
	  return StreamSupport.stream(resultsArray.spliterator(), false)
          .filter(node -> matchesCountry(node, countryCode))
          .findFirst()
          .map(node -> new double[] {
              node.asJsonObject().getJsonNumber("latitude").doubleValue(),
              node.asJsonObject().getJsonNumber("longitude").doubleValue()
      });
  }

  private Optional<double[]> getCoordinates(String city, String countryCode) {
    String url = String.format(coordinatesApiUrl, city);
    
    return executeHTTPRequest(url)
        .flatMap(response -> extractCoordinates(response, countryCode));
  }

  private String getWeatherDescription(int code) {
    return switch (code) {
      case 0 -> "Clear sky";
      case 1, 2, 3 -> "Partly cloudy";
      case 45, 48 -> "Foggy";
      case 51, 53, 55 -> "Drizzle";
      case 61, 63, 65 -> "Rainy";
      case 71, 73, 75 -> "Snowy";
      case 95, 96, 99 -> "Stormy";
      default -> "Unknown conditions";
    };
  }

  private Optional<String> parseWeatherData(String jsonResponse) {
	  JsonReader reader = Json.createReader(new StringReader(jsonResponse));
	  JsonObject jsonNode = reader.readObject();
	  JsonObject currentWeather = jsonNode.getJsonObject("current_weather");
	  
	  if (currentWeather != null) {
        double temperature = currentWeather.getJsonNumber("temperature").doubleValue();
        double windspeed = currentWeather.getJsonNumber("windspeed").doubleValue();
        int weatherCode = currentWeather.getJsonNumber("weathercode").intValue();
        String weatherDesc = getWeatherDescription(weatherCode);

        String result = String.format(
            Locale.US,
            "Current temperature: %.1f°C, Windspeed: %.1f km/h, Condition: %s",
            temperature, windspeed, weatherDesc);
        
        return Optional.of(result);
      }
      
	  return Optional.empty();
  }

  private Optional<String> fetchWeather(double[] coords) {
    String url = String.format(forecastApiUrl, coords[0], coords[1]);
    
    return executeHTTPRequest(url)
        .flatMap(this::parseWeatherData);
  }
  
  private boolean matchesCountry(JsonValue node, String countryCode) {
    if (node == null || countryCode == null) {
      return false;
    }
    
    String countryLower = countryCode.toLowerCase();
    
    // Check country_code field which contains actual codes like "de"
    String countryCodeNode = node.asJsonObject().getString("country_code");
    if (countryCodeNode != null && countryLower.equals(countryCodeNode.toLowerCase())) {
      return true;
    }
    
    // Fallback to country name check, but this usually contains full names like "Germany"
    String countryNode = node.asJsonObject().getString("country");
    if (countryNode != null && countryNode.toLowerCase().startsWith(countryLower)) {
      return true;
    }
    
    return false;
  }
  
//  public static void main(String[] args) {
//	System.out.println(new MCPRealWeather().getWeather("Johannesburg", "ZA"));
//  }
}
