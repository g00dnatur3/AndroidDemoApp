package com.hp.app.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hp.app.Application;
import com.hp.app.R;
import com.hp.service.WeatherService;
import com.hp.view.fragment.LoggingFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
 * Created by developer on 11/2/15.
 */
public class WeatherFragment extends LoggingFragment {

    @Inject WeatherService weatherService;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Application) getActivity().getApplication()).component().inject(this);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_weather, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final TextView textViewMaxTemperature = (TextView) view.findViewById(R.id.max_temp);
        final TextView textViewMinTemperature = (TextView) view.findViewById(R.id.min_temp);
        final TextView textViewTemperature = (TextView) view.findViewById(R.id.current_temp);
        final TextView textViewWeatherType = (TextView) view.findViewById(R.id.weather_type);
        final TextView textViewWeekDay = (TextView) view.findViewById(R.id.week_day);
        final TextView textLocation = (TextView) view.findViewById(R.id.city_name);
        final ImageView imageViewWeatherType = (ImageView) view.findViewById(R.id.weather_img);

        // Need to add ability to get city & state from android's current location
        String city = "Palo Alto";
        String state = "CA";
        textLocation.setText(city + ", " + state);

        weatherService.getForecast("Palo Alto", "CA", new WeatherService.ForecastResponseHandler() {
            public void onForecastData(List<Map<String, String>> data) {

                /* FOR DEBUG PURPOSES
                for (Map<String, String> item : data) {
                    for (String key : item.keySet()) {
                        Log.w(TAG, key + " = " + item.get(key));
                    }
                    Log.w(TAG, "-----------------");
                }
                */

                String unit = "\u00B0";
                textViewTemperature.setText(data.get(0).get("temp") + unit);
                String condition = data.get(0).get("text"); //cloudy, sunny, etc.
                textViewWeatherType.setText(condition);

                imageViewWeatherType.setImageResource(getDrawableForDayCondition(condition, true));
                textViewMaxTemperature.setText(data.get(0).get("high") + unit);
                textViewMinTemperature.setText(data.get(0).get("low") + unit);
                textViewWeekDay.setText(new SimpleDateFormat("EEEE").format(new Date()));

            };
        });
    }

    private int getDrawableForDayCondition(String condition, boolean big) {
        switch(condition) {
            case "Mostly Cloudy":
                return big ? R.drawable.weather_cloudy_big : R.drawable.weather_cloudy;

            case "Partly Cloudy":
                return big ? R.drawable.weather_partly_cloudy_big : R.drawable.weather_partly_cloudy;

            case "Showers":
                return big ? R.drawable.weather_rainy_big : R.drawable.weather_rainy;

            default: //Sunny
                return big ? R.drawable.weather_sunny_big : R.drawable.weather_sunny;
        }
    }

}
