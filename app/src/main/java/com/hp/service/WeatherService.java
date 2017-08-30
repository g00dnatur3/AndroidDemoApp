package com.hp.service;

import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.hp.http.JsonHttpResponseHandler;
import com.hp.app.Application;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import cz.msebera.android.httpclient.Header;

/**
 * Created by developer on 11/3/15.
 */
@Singleton
public class WeatherService {

    private final String TAG = getClass().getCanonicalName();

    private final String url = Application.getProperty("weather.url");

    private final AsyncHttpClient httpClient;

    public WeatherService(AsyncHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void getForecast(String city, String state, ForecastResponseHandler handler) {
        String q = "select item.forecast, item.condition from weather.forecast where woeid in (" +
                   "select woeid from geo.places(1) where text='" + city + ", " + state + "')";
        RequestParams params = new RequestParams();
        params.put("q", q);
        params.put("format", "json");
        Log.i(TAG, "GET " + url + " ? " + params.toString());
        httpClient.get(url, params, handler);
    }

    public static class ForecastResponseHandler extends JsonHttpResponseHandler {

        private final String TAG = getClass().getCanonicalName();

        public void onSuccess(int statusCode, Header[] headers, JsonNode response) {
            Log.i(TAG, "onSuccess --> " + getRequestURI());
            Log.i(TAG, "response --> " + response);
            ArrayNode data = (ArrayNode) response.get("query").get("results").get("channel");
            List<Map<String, String>> forecastData = new ArrayList<Map<String, String>>();
            for (int i=0; i<data.size(); i++) {
                Map<String, String> forecast = new HashMap<String, String>();
                forecast.putAll(toMap(data.get(i).get("item").get("forecast")));
                if (i==0) {
                    forecast.putAll(toMap(data.get(i).get("item").get("condition")));
                }
                forecastData.add(forecast);
            }
            onForecastData(forecastData);
        }

        private Map<String, String> toMap(JsonNode node) {
            return mapper.convertValue(node, Map.class);
        }

        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JsonNode errorResponse) {
            Log.i(TAG, "onFailure --> " + getRequestURI());
            if (throwable != null) {
                throwable.printStackTrace();
            }
            if (errorResponse != null) {
                Log.i(TAG, "errorResponse --> " + errorResponse);
            }
        }

        public void onForecastData(List<Map<String, String>> forecastData) {
            Log.w(TAG, "onForecastData(ArrayNode) was not overriden, but callback was received");
        };

    }
}
