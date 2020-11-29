package com.mosin.weatheronboard;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.mosin.weatheronboard.interfaces.IOpenWeatherMap;
import com.mosin.weatheronboard.model.WeatherRequest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FragmentWeather extends Fragment {
    private IOpenWeatherMap openWeather;
    private final String WEATHER_URL = "https://api.openweathermap.org/";
    private final String API_KEY = "762ee61f52313fbd10a4eb54ae4d4de2";
    private final String MERRIC = "metric";
    private TextView showTempView, showWindSpeed, showPressure, showHumidity, cityName, dateNow, tempLikeView, someText;
    private ImageView icoWeather, icoWind, icoPress, icoHum;
    SharedPreferences sharedPreferences;
    private String cityChoice, icoView, temperatureValue, dateText, windSpeedStr, pressureText, humidityStr, tempLike;
    private float tempValFloat;
    public static float lat, lng;
    ConstraintLayout frgMain;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_weather, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        findView(view);
        initLastCity();
        initRetorfit();
        requestRetrofit(cityChoice, MERRIC, API_KEY);
    }

    public void findView(View view) {
        showTempView = view.findViewById(R.id.showTempViewFragmentShowCityInfo);
        showWindSpeed = view.findViewById(R.id.windSpeedView);
        showPressure = view.findViewById(R.id.pressureView);
        showHumidity = view.findViewById(R.id.humidityView);
        icoWeather = view.findViewById(R.id.weatherIcoView);
        cityName = view.findViewById(R.id.cityNameView);
        dateNow = view.findViewById(R.id.date_view);
        tempLikeView = view.findViewById(R.id.tempLike);
        someText = view.findViewById(R.id.someText);
        icoHum = view.findViewById(R.id.humidity_ico);
        icoWind = view.findViewById(R.id.wind_ico);
        icoPress = view.findViewById(R.id.pressure_ico);

    }
//TODO
//    public void setSomeText(){
//        if (tempValFloat > -5  && tempValFloat < 0){
//            someText.setText("Сагодня в " + cityChoice + " зябко");
//        }
//        else if (tempValFloat > -10 && tempValFloat <= - 6){
//            someText.setText("Сагодня в " + cityChoice + " Алеша");
//        }
//        else if (tempValFloat > -20 && tempValFloat <= - 11){
//            someText.setText("Сагодня в " + cityChoice + " Рома");
//        }
//    }

    public void initLastCity() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        cityChoice = sharedPreferences.getString("cityName", cityChoice);
        if (cityChoice != null) {
            cityName.setText(cityChoice.toUpperCase());
        }
    }

    private void initRetorfit() {
        Retrofit retrofit;
        retrofit = new Retrofit.Builder()
                .baseUrl(WEATHER_URL) //Базовая часть адреса
                .addConverterFactory(GsonConverterFactory.create()) //Конвертер, необходимый для преобразования JSON'а в объекты
                .build();
        openWeather = retrofit.create(IOpenWeatherMap.class); //Создаем объект, при помощи которого будем выполнять запросы
    }

    private void requestRetrofit(String city, String metric, String keyApi) {
        openWeather.loadWeather(city, metric, keyApi)
                .enqueue(new Callback<WeatherRequest>() {
                    @Override
                    public void onResponse(Call<WeatherRequest> call, Response<WeatherRequest> response) {
                        if (response.body() != null) {
                            tempValFloat = response.body().getMain().getTemp();
                            temperatureValue = String.format(Locale.getDefault(), "%.0f°", response.body().getMain().getTemp());
                            windSpeedStr = String.format(Locale.getDefault(), "%.0f", response.body().getWind().getSpeed());
                            pressureText = String.format(Locale.getDefault(), "%.0f", response.body().getMain().getPressure());
                            humidityStr = String.format(Locale.getDefault(), "%d", response.body().getMain().getHumidity());
                            tempLike = String.format(Locale.getDefault(), "%.0f°", response.body().getMain().getFeels_like());
                            lat = response.body().getCoord().getLat();
                            lng = response.body().getCoord().getLon();
                            icoView = response.body().getWeather()[0].getIcon();
                            tempLikeView.setText(String.format("%s %s", getResources().getString(R.string.temp_like), tempLike));
                            showTempView.setText(temperatureValue);
                            showWindSpeed.setText(String.format("%s м/с", windSpeedStr));
                            showPressure.setText(String.format("%s мм рт.ст.", pressureText));
                            showHumidity.setText(String.format("%s%%", humidityStr));
                            setIcoViewImage();

                        } else if (response.code() == 404) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle(R.string.exclamation)
                                    .setMessage(R.string.msg_to_er_url)
                                    .setIcon(R.mipmap.ic_launcher_round)
                                    .setPositiveButton(R.string.ok_button, null);
                            AlertDialog alert = builder.create();
                            alert.show();
                        }

                    }

                    @Override
                    public void onFailure(Call<WeatherRequest> call, Throwable t) {
                        cityName.setText("");
                        icoHum.setVisibility(View.GONE);
                        icoPress.setVisibility(View.GONE);
                        icoWind.setVisibility(View.GONE);
                        dateNow.setVisibility(View.GONE);

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle(R.string.exclamation)
                                .setMessage(R.string.msg_to_er_internet)
                                .setIcon(R.mipmap.ic_launcher_round)
                                .setPositiveButton(R.string.ok_button, null);
                        AlertDialog alert = builder.create();
                        alert.show();

                    }
                });
    }

    //TODO вынести в текст ресурсы
    private void setIcoViewImage() {

        if (icoView.equals("01d")) {
            icoWeather.setImageResource(R.drawable.clear_sky_d);
            someText.setText("Ясно");
        } else if (icoView.equals("01n")) {
            icoWeather.setImageResource(R.drawable.clear_sky_n);
            someText.setText("Ясно");
        } else if (icoView.equals("02d") || icoView.equals("03d") || icoView.equals("04d")) {
            icoWeather.setImageResource(R.drawable.few_clouds_d);
            someText.setText("Облачно");
        } else if (icoView.equals("02n") || icoView.equals("03n") || icoView.equals("04n")) {
            icoWeather.setImageResource(R.drawable.few_clouds_n);
            someText.setText("Облачно");
        } else if (icoView.equals("09d") || icoView.equals("10d")) {
            icoWeather.setImageResource(R.drawable.rain_d);
            someText.setText("Дождь");
        } else if (icoView.equals("09n") || icoView.equals("10n")) {
            icoWeather.setImageResource(R.drawable.rain_n);
            someText.setText("Дождь");
        } else if (icoView.equals("13d")) {
            icoWeather.setImageResource(R.drawable.snow);
            someText.setText("Снег");
        } else if (icoView.equals("13n")) {
            icoWeather.setImageResource(R.drawable.snow);
            someText.setText("Снег");
        } else if (icoView.equals("50n")) {
            icoWeather.setImageResource(R.drawable.mist);
            someText.setText("Туман");
        } else if (icoView.equals("50d")) {
            icoWeather.setImageResource(R.drawable.mist);
            someText.setText("Туман");
        }
    }
}
