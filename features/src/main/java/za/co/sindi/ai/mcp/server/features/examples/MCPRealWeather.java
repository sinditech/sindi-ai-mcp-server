package za.co.sindi.ai.mcp.server.features.examples;

///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.quarkus:quarkus-bom:3.19.2@pom
//DEPS io.quarkiverse.mcp:quarkus-mcp-server-stdio:1.0.0.Beta4
//DEPS com.squareup.okhttp3:okhttp:3.14.9
//DEPS com.fasterxml.jackson.core:jackson-databind:2.18.3
//FILES application.properties

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

	private static final String GEOCODING_API = "https://geocoding-api.open-meteo.com/v1/search";
    private static final String WEATHER_API = "https://api.open-meteo.com/v1/forecast";
    private static final String AIR_QUALITY_API = "https://air-quality-api.open-meteo.com/v1/air-quality";
    private static final String IP_LOCATION_API = "http://ip-api.com/json/";

	private final HttpClient httpClient = HttpClient.newBuilder()
													.connectTimeout(Duration.ofSeconds(10)).build();

	@Tool(description = "A tool to get the current weather from a given city and country code")
	public String getWeather(
			@ToolArgument(description = "The city to get the weather for") String city,
			@ToolArgument(description = "The country code of the given city to get the weather for") String countryCode) {

		return getCoordinates(city, countryCode)
				.map(this::getCurrentWeather)
				.flatMap(this::getWeatherData)
				.orElse(String.format("Sorry, I couldn't find the weather for %s, %s", city, countryCode));
	}
	
	@Tool(description = "A tool to get the current local weather")
	public String getCurrentLocalWeather() {

		return getCurrentLocation()
				.map(this::getCurrentWeather)
				.flatMap(this::getWeatherData)
				.orElse(String.format("Sorry, I couldn't find the local weather."));
	}
	
	@Tool(description = "A tool to get the current air quality from a given city and country code")
	public String getAirQuality(
			@ToolArgument(description = "The city to get the air quality for") String city,
			@ToolArgument(description = "The country code of the given city to get the air quality for") String countryCode) {

		return getCoordinates(city, countryCode)
				.map(this::getCurrentAirQuality)
				.flatMap(this::getAirQualityData)
				.orElse(String.format("Sorry, I couldn't find the air quality for %s, %s", city, countryCode));
	}
	
	@Tool(description = "A tool to get the current local air quality")
	public String getCurrentLocalAirQuality() {

		return getCurrentLocation()
				.map(this::getCurrentAirQuality)
				.flatMap(this::getAirQualityData)
				.orElse(String.format("Sorry, I couldn't find the local air quality."));
	}

	private Optional<String> executeHTTPRequest(String url) {
		HttpRequest request = HttpRequest.newBuilder(URI.create(url))
										 .header("User-Agent", "OpenMeteoWeatherApp/1.0")
										 .timeout(Duration.ofSeconds(10)).build();
		try {
			HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
			int code = response.statusCode() / 100;
			if (code >= 2 && code <= 3)
				return Optional.ofNullable(response.body());

			System.err.println("Request failed with status: " + response.statusCode());
			return Optional.empty();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			System.err.println("Error executing HTTP request: " + e.getMessage());
			return Optional.empty();
		}
	}
	
	private Optional<Location> getCoordinates(String city, String countryCode) {
		String url = String.format(GEOCODING_API + "?name=%s&format=json", city);
		
		String jsonResponse = executeHTTPRequest(url).get();
		JsonReader reader = Json.createReader(new StringReader(jsonResponse));
		JsonObject jsonNode = reader.readObject();
		JsonArray resultsArray = jsonNode.getJsonArray("results");
  
		if (Objects.isNull(resultsArray) || resultsArray.isEmpty()) {
			return Optional.empty();
		}
  
		return StreamSupport.stream(resultsArray.spliterator(), false)
							.filter(node -> matchesCountry(node, countryCode))
							.findFirst()
							.map(node ->  {
								Location location = new Location();
								location.latitude = node.asJsonObject().getJsonNumber("latitude").doubleValue();
								location.longitude = node.asJsonObject().getJsonNumber("longitude").doubleValue();
								location.city = city;
								location.regionName = node.asJsonObject().getString("admin1");
								return location;
							});
	    
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

	/**
     * Get current location using IP-based geolocation
     * In a real application, you might want to use more accurate methods
     * like GPS or allow user input for specific locations
     */
    private Optional<Location> getCurrentLocation() {
    	
    	String jsonResponse = executeHTTPRequest(IP_LOCATION_API).get();
        JsonReader reader = Json.createReader(new StringReader(jsonResponse));
  	  	JsonObject jsonNode = reader.readObject();
        
        if ("success".equals(jsonNode.getString("status"))) {
        	Location location = new Location();
        	location.latitude = jsonNode.getJsonNumber("lat").doubleValue();
        	location.longitude = jsonNode.getJsonNumber("lon").doubleValue();
        	location.city = jsonNode.getString("city");
        	location.country = jsonNode.getString("country");
        	location.timezone = jsonNode.getString("timezone");
        	location.region = jsonNode.getString("region");
        	location.regionName = jsonNode.getString("regionName");
        	
        	return Optional.ofNullable(location);
        }
        
        System.out.println("Failed to get location: " + jsonNode.getString("message"));
        return Optional.empty();
    }
    
    /**
     * Get current weather data from Open Meteo API
     */
    private WeatherData getCurrentWeather(Location location) {
        String url = WEATHER_API + 
            "?latitude=" + location.latitude +
            "&longitude=" + location.longitude +
            "&current=temperature_2m,relative_humidity_2m,apparent_temperature," +
            "is_day,precipitation,rain,showers,snowfall,weather_code," +
            "cloud_cover,pressure_msl,surface_pressure,wind_speed_10m," +
            "wind_direction_10m,wind_gusts_10m" +
            "&hourly=temperature_2m,relative_humidity_2m,dew_point_2m," +
            "apparent_temperature,precipitation_probability,precipitation," +
            "rain,showers,snowfall,weather_code,pressure_msl,surface_pressure," +
            "cloud_cover,visibility,wind_speed_10m,wind_direction_10m,wind_gusts_10m," +
            "uv_index,is_day" +
            "&daily=weather_code,temperature_2m_max,temperature_2m_min," +
            "apparent_temperature_max,apparent_temperature_min,sunrise,sunset," +
            "daylight_duration,sunshine_duration,uv_index_max,uv_index_clear_sky_max," +
            "precipitation_sum,rain_sum,showers_sum,snowfall_sum,precipitation_hours," +
            "precipitation_probability_max,wind_speed_10m_max,wind_gusts_10m_max," +
            "wind_direction_10m_dominant" +
            "&timezone=auto" +
            "&forecast_days=1";
        
        String jsonResponse = executeHTTPRequest(url).get();
        JsonReader reader = Json.createReader(new StringReader(jsonResponse));
  	  	JsonObject jsonNode = reader.readObject();
        
        WeatherData weather = new WeatherData();
        weather.location = location;
        weather.timestamp = LocalDateTime.now();
        
        // Current weather
        JsonObject current = jsonNode.getJsonObject("current");
        if (current != null) {
            weather.temperature = current.getJsonNumber("temperature_2m").doubleValue();
            weather.humidity = current.getJsonNumber("relative_humidity_2m").intValue();
            weather.apparentTemperature = current.getJsonNumber("apparent_temperature").doubleValue();
            weather.precipitation = current.getJsonNumber("precipitation").doubleValue();
            weather.rain = current.getJsonNumber("rain").doubleValue();
            weather.weatherCode = current.getJsonNumber("weather_code").intValue();
            weather.cloudCover = current.getJsonNumber("cloud_cover").intValue();
            weather.pressure = current.getJsonNumber("pressure_msl").doubleValue();
            weather.windSpeed = current.getJsonNumber("wind_speed_10m").doubleValue();
            weather.windDirection = current.getJsonNumber("wind_direction_10m").intValue();
            weather.windGusts = current.getJsonNumber("wind_gusts_10m").doubleValue();
            weather.isDay = current.getJsonNumber("is_day").intValue() == 1;
        }
        
        // Today's forecast
        JsonObject daily = jsonNode.getJsonObject("daily");
        if (daily != null) {
        	JsonArray tempMax = daily.getJsonArray("temperature_2m_max");
        	JsonArray tempMin = daily.getJsonArray("temperature_2m_min");
        	JsonArray sunrise = daily.getJsonArray("sunrise");
        	JsonArray sunset = daily.getJsonArray("sunset");
            
            if (tempMax != null && tempMax.size() > 0) {
                weather.tempMax = tempMax.getJsonNumber(0).doubleValue();
            }
            if (tempMin != null && tempMin.size() > 0) {
                weather.tempMin = tempMin.getJsonNumber(0).doubleValue();
            }
            if (sunrise != null && sunrise.size() > 0) {
                weather.sunrise = sunrise.getJsonString(0).getString();
            }
            if (sunset != null && sunset.size() > 0) {
                weather.sunset = sunset.getJsonString(0).getString();
            }
        }
        
        weather.weatherDescription = getWeatherDescription(weather.weatherCode);
        
        return weather;
    }
    
    /**
     * Get current air quality data from Open Meteo API
     */
    private AirQualityData getCurrentAirQuality(Location location) {
        String url = AIR_QUALITY_API +
            "?latitude=" + location.latitude +
            "&longitude=" + location.longitude +
            "&current=european_aqi,us_aqi,pm10,pm2_5,carbon_monoxide,nitrogen_dioxide," +
            "sulphur_dioxide,ozone,aerosol_optical_depth,dust,uv_index,uv_index_clear_sky," +
            "ammonia,alder_pollen,birch_pollen,grass_pollen,mugwort_pollen,olive_pollen," +
            "ragweed_pollen" +
            "&timezone=auto";
        
        String jsonResponse = executeHTTPRequest(url).get();
        JsonReader reader = Json.createReader(new StringReader(jsonResponse));
  	  	JsonObject jsonNode = reader.readObject();
        
        AirQualityData airQuality = new AirQualityData();
        airQuality.location = location;
        airQuality.timestamp = LocalDateTime.now();
        
        JsonObject current = jsonNode.getJsonObject("current");
        if (current != null) {
            airQuality.europeanAqi = current.getJsonNumber("european_aqi").doubleValue();
            airQuality.usAqi = current.getJsonNumber("us_aqi").doubleValue();
            airQuality.pm10 = current.getJsonNumber("pm10").doubleValue();
            airQuality.pm25 = current.getJsonNumber("pm2_5").doubleValue();
            airQuality.carbonMonoxide = current.getJsonNumber("carbon_monoxide").doubleValue();
            airQuality.nitrogenDioxide = current.getJsonNumber("nitrogen_dioxide").doubleValue();
            airQuality.sulphurDioxide = current.getJsonNumber("sulphur_dioxide").doubleValue();
            airQuality.ozone = current.getJsonNumber("ozone").doubleValue();
            airQuality.uvIndex = current.getJsonNumber("uv_index").doubleValue();
            airQuality.dust = current.getJsonNumber("dust").doubleValue();
            airQuality.ammonia = current.isNull("ammonia") ? null : current.getJsonNumber("ammonia").doubleValue();
        }
        
        return airQuality;
    }
	
	/**
     * Convert weather code to human-readable description
     */
    private String getWeatherDescription(int code) {
        switch (code) {
            case 0: return "Clear sky";
            case 1: return "Mainly clear";
            case 2: return "Partly cloudy";
            case 3: return "Overcast";
            case 45: case 48: return "Fog";
            case 51: return "Light drizzle";
            case 53: return "Moderate drizzle";
            case 55: return "Dense drizzle";
            case 56: case 57: return "Freezing drizzle";
            case 61: return "Slight rain";
            case 63: return "Moderate rain";
            case 65: return "Heavy rain";
            case 66: case 67: return "Freezing rain";
            case 71: return "Slight snowfall";
            case 73: return "Moderate snowfall";
            case 75: return "Heavy snowfall";
            case 77: return "Snow grains";
            case 80: return "Slight rain showers";
            case 81: return "Moderate rain showers";
            case 82: return "Violent rain showers";
            case 85: return "Slight snow showers";
            case 86: return "Heavy snow showers";
            case 95: return "Thunderstorm";
            case 96: case 99: return "Thunderstorm with hail";
            default: return "Unknown (" + code + ")";
        }
    }
    
    /**
     * Get European AQI description
     */
    private String getEuropeanAqiDescription(int aqi) {
        if (aqi <= 20) return "Good";
        else if (aqi <= 40) return "Fair";
        else if (aqi <= 60) return "Moderate";
        else if (aqi <= 80) return "Poor";
        else if (aqi <= 100) return "Very Poor";
        else return "Extremely Poor";
    }
    
    /**
     * Get US AQI description
     */
    private String getUsAqiDescription(int aqi) {
        if (aqi <= 50) return "Good";
        else if (aqi <= 100) return "Moderate";
        else if (aqi <= 150) return "Unhealthy for Sensitive Groups";
        else if (aqi <= 200) return "Unhealthy";
        else if (aqi <= 300) return "Very Unhealthy";
        else return "Hazardous";
    }
	
	/**
     * Get UV Index description
     */
    private String getUvIndexDescription(double uvIndex) {
        if (uvIndex < 3) return "Low";
        else if (uvIndex < 6) return "Moderate";
        else if (uvIndex < 8) return "High";
        else if (uvIndex < 11) return "Very High";
        else return "Extreme";
    }
    
    /**
     * Display weather data in a formatted way
     */
    private Optional<String> getWeatherData(WeatherData weather) {
    	if (weather == null) return Optional.empty();
    	
    	StringBuilder sb = new StringBuilder();
        sb.append("🌡️  === CURRENT WEATHER ===");
        sb.append("\n🌍 Location: " + weather.location.city + ", " + weather.location.country); 
        sb.append("\n📅 Updated: " + weather.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        sb.append("\n🌡️ Temperature: " + weather.temperature + "°C (feels like " + weather.apparentTemperature + "°C)");
        sb.append("\n📊 High: " + weather.tempMax + "°C | Low: " + weather.tempMin + "°C");
        sb.append("\n🌤️ Condition: " + weather.weatherDescription);
        sb.append("\n💧 Humidity: " + weather.humidity + "%");
        sb.append("\n🌧️ Precipitation: " + weather.precipitation + " mm");
        if (weather.rain > 0) {
            sb.append("\n☔ Rain: " + weather.rain + " mm");
        }
        sb.append("\n☁️ Cloud Cover: " + weather.cloudCover + "%");
        sb.append("\n🌬️ Wind: " + weather.windSpeed + " km/h from " + weather.windDirection + "°");
        if (weather.windGusts > 0) {
            sb.append("\n💨 Wind Gusts: " + weather.windGusts + " km/h");
        }
        sb.append("\n🔽 Pressure: " + weather.pressure + " hPa");
        sb.append("\n🌅 Sunrise: " + weather.sunrise);
        sb.append("\n🌇 Sunset: " + weather.sunset);
        sb.append("\n☀️ Day/Night: " + (weather.isDay ? "Day" : "Night"));
        sb.append("\n");
        
        return Optional.ofNullable(sb.toString());
    }
    
    /**
     * Display air quality data in a formatted way
     */
    private Optional<String> getAirQualityData(AirQualityData airQuality) {
    	if (airQuality == null) return Optional.empty();
    	
    	StringBuilder sb = new StringBuilder();
        sb.append("🏭 === AIR QUALITY ===");
        sb.append("\n🌍 Location: " + airQuality.location.city + ", " + airQuality.location.country);
        sb.append("\n📅 Updated: " + airQuality.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        if (airQuality.europeanAqi > 0) {
            sb.append("\n🇪🇺 European AQI: " + (int)airQuality.europeanAqi + " (" + getEuropeanAqiDescription((int)airQuality.europeanAqi) + ")");
        }
        if (airQuality.usAqi > 0) {
            sb.append("\n🇺🇸 US AQI: " + (int)airQuality.usAqi + " (" + getUsAqiDescription((int)airQuality.usAqi) + ")");
        }
        
        sb.append("\n\n🔬 Pollutant Levels:");
        if (airQuality.pm25 > 0) {
            sb.append("\n PM2.5: " + String.format("%.1f", airQuality.pm25) + " μg/m³");
        }
        if (airQuality.pm10 > 0) {
            sb.append("\n PM10: " + String.format("%.1f", airQuality.pm10) + " μg/m³");
        }
        if (airQuality.ozone > 0) {
            sb.append("\n Ozone (O₃): " + String.format("%.1f", airQuality.ozone) + " μg/m³");
        }
        if (airQuality.nitrogenDioxide > 0) {
            sb.append("\n Nitrogen Dioxide (NO₂): " + String.format("%.1f", airQuality.nitrogenDioxide) + " μg/m³");
        }
        if (airQuality.sulphurDioxide > 0) {
            sb.append("\n Sulphur Dioxide (SO₂): " + String.format("%.1f", airQuality.sulphurDioxide) + " μg/m³");
        }
        if (airQuality.carbonMonoxide > 0) {
            sb.append("\n Carbon Monoxide (CO): " + String.format("%.1f", airQuality.carbonMonoxide) + " μg/m³");
        }
        if (airQuality.ammonia != null) {
            sb.append("\n Ammonia (NH₃): " + String.format("%.1f", airQuality.ammonia) + " μg/m³");
        }
        
        if (airQuality.uvIndex > 0) {
            sb.append("\n☀️ UV Index: " + String.format("%.1f", airQuality.uvIndex) + " (" + getUvIndexDescription(airQuality.uvIndex) + ")");
        }
        
        sb.append("");
        return Optional.ofNullable(sb.toString());
    }
	
 // Data classes
    static final class Location implements Serializable {
        double latitude;
        double longitude;
        String city;
        String country;
        String timezone;
        String region;
        String regionName;
    }
    
    static final class WeatherData implements Serializable {
        Location location;
        LocalDateTime timestamp;
        double temperature;
        double apparentTemperature;
        double tempMax;
        double tempMin;
        int humidity;
        double precipitation;
        double rain;
        int weatherCode;
        String weatherDescription;
        int cloudCover;
        double pressure;
        double windSpeed;
        int windDirection;
        double windGusts;
        boolean isDay;
        String sunrise;
        String sunset;
    }
    
    static final class AirQualityData implements Serializable {
        Location location;
        LocalDateTime timestamp;
        double europeanAqi;
        double usAqi;
        double pm10;
        double pm25;
        double carbonMonoxide;
        double nitrogenDioxide;
        double sulphurDioxide;
        double ozone;
        double uvIndex;
        double dust;
        Double ammonia;
    }

//	public static void main(String[] args) {
//		MCPRealWeather weather = new MCPRealWeather();
//		System.out.println(weather.getCurrentLocalWeather()); //weather.getWeather("Johannesburg", "ZA")
//		System.out.println(weather.getCurrentLocalAirQuality()); //weather.getAirQuality("Johannesburg", "ZA")
//	}
}
