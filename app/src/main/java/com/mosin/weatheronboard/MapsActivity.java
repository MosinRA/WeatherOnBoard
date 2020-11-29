package com.mosin.weatheronboard;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

public class MapsActivity extends Fragment implements OnMapReadyCallback {
    private static final int PERMISSION_REQUEST_CODE = 10;
    private GoogleMap mMap;
    private Marker currentMarker;
    SharedPreferences sharedPreferences;
    private String cityChoice;
    FragmentWeather fragmentWeather = new FragmentWeather();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        requestPemissions();
    }


    // Запрашиваем Permission’ы
    private void requestPemissions() {
        // Проверим, есть ли Permission’ы, и если их нет, запрашиваем их у
        // пользователя
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Запрашиваем координаты
            requestLocation();
        } else {
            // Permission’ов нет, запрашиваем их у пользователя
            requestLocationPermissions();
        }
    }

    /**
     * Этот метод позволяет управлять картой, когда она станет доступна.
     * Обратный вызов этого метода срабатывает, когда карта готова.
     * Здесь можно размещать метки на карте, добавлять слушатели,
     * перемещать область видимости и т. д.
     * Для примера добавлена метка возле Сиднея.
     * Если сервисы Google не инсталлированы, то для работы приложения
     * пользователь должен их установить.
     * Только когда сервисы установлены, картами можно пользоваться.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(fragmentWeather.lat, fragmentWeather.lng);
        currentMarker = mMap.addMarker(new MarkerOptions().position(sydney).title("Текущая позиция"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }


    // Запрашиваем координаты
    private void requestLocation() {
        // Если Permission’а всё- таки нет, просто выходим: приложение не имеет
        // смысла
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        // Получаем менеджер геолокаций
        LocationManager locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);

        // Получаем наиболее подходящий провайдер геолокации по критериям.
        // Но определить, какой провайдер использовать, можно и самостоятельно.
        // В основном используются LocationManager.GPS_PROVIDER или
        // LocationManager.NETWORK_PROVIDER, но можно использовать и
        // LocationManager.PASSIVE_PROVIDER - для получения координат в
        // пассивном режиме
        String provider = locationManager.getBestProvider(criteria, true);
        if (provider != null) {
            // Будем получать геоположение через каждые 10 секунд или каждые
            // 10 метров
            locationManager.requestLocationUpdates(provider, 1000, 10, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {


                    mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                        @Override
                        public boolean onMyLocationButtonClick() {
                            double latToNavBtn = location.getLatitude();
                            double lngToNavBtn = location.getLongitude();
                            LatLng currentPositionToBtnNav = new LatLng(latToNavBtn, lngToNavBtn);
                            getAddress(currentPositionToBtnNav);
                            return false;
                        }
                    });

                    float lat = fragmentWeather.lat;   // Широта
                    float lng = fragmentWeather.lng;    // Долгота
                    LatLng currentPosition = new LatLng(lat, lng);
                    currentMarker.setPosition(currentPosition);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, (float) 12));
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            });
        }
    }

    // Запрашиваем Permission’ы для геолокации
    private void requestLocationPermissions() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CALL_PHONE)) {
            // Запрашиваем эти два Permission’а у пользователя
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    PERMISSION_REQUEST_CODE);
        }
    }

    // Результат запроса Permission’а у пользователя:
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {   // Запрошенный нами
            // Permission
            if (grantResults.length == 2 &&
                    (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                // Все препоны пройдены и пермиссия дана
                // Запросим координаты
                requestLocation();
            }
        }
    }

    private void getAddress(final LatLng location) {
        final Geocoder geocoder = new Geocoder(getContext());
        Handler handler = new Handler();
        // Поскольку Geocoder работает по интернету, создаём отдельный поток
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<Address> addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            cityChoice = addresses.get(0).getLocality().toUpperCase();
                            System.out.println(cityChoice);
                            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("cityName", cityChoice);
                            editor.apply();
                            requireActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragmentContainer, new FragmentWeather())
                                    .replace(R.id.fragmentContainerMap, new MapsActivity())
                                    .commit();
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}

