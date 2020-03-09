package com.example.kkp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kkp.Api;
import com.example.kkp.R;
import com.example.kkp.adapter.RequestHandler;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    CardView absenDatang,absenPulang,Logout;
    TextView jarak,txt_waktu_datang,txt_waktu_pulang;
    SharedPreferences sharedpreferences;
    public static final String TAG_NAME = "name";
    public static final String TAG_NIM = "nim";
    private static final String TAG_MESSAGE = "message";
    String nim;
    int PERMISSION_ID = 44;
    FusedLocationProviderClient mFusedLocationClient;
    SwipeRefreshLayout swipe;
    private String url = Api.URL + "presensi.php";
    private String url_absen = Api.URL + "retrieve_presensi.php?nim=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nim = getIntent().getStringExtra(TAG_NIM);

        absenDatang = (CardView)findViewById(R.id.cardDatang);
        absenPulang = (CardView)findViewById(R.id.cardPulang);
        Logout = (CardView)findViewById(R.id.cardLogout);
        jarak = (TextView) findViewById(R.id.jarak);
        txt_waktu_datang = (TextView) findViewById(R.id.txt_absen_datang);
        txt_waktu_pulang = (TextView) findViewById(R.id.txt_absen_pulang);
        swipe = (SwipeRefreshLayout) findViewById(R.id.swipe);
        swipe.setColorSchemeResources(R.color.colorAccent,R.color.colorPrimary);

        sharedpreferences = getSharedPreferences(Login.my_shared_preferences, Context.MODE_PRIVATE);



        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        getLastLocation();
        getAbsen();


        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                new Handler().postDelayed(new Runnable() {
                    @Override public void run() {

                        // Berhenti berputar/refreshing
                        swipe.setRefreshing(false);
                        requestNewLocationData();
                        getAbsen();
                    }
                }, 2000);
            }
        });





        Logout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Login.session_status, false);
                editor.putString(TAG_NAME, null);
                editor.putString(TAG_NIM, null);
                editor.commit();

                Intent intent = new Intent(MainActivity.this, Login.class);
                finish();
                startActivity(intent);
            }
        });
    }

    private static Double toRad(Double value) {
        return value * Math.PI / 180;
    }


    public void Izin(View view) {
        Intent intent = new Intent(MainActivity.this, Izin.class);
        intent.putExtra(TAG_NIM, nim);
        startActivity(intent);
    }


    //ini function untuk mendaptkan lokasi
    @SuppressLint("MissingPermission")
    private int getLastLocation(){
        if (checkPermissions()) {
                if (isLocationEnabled()) {
                    mFusedLocationClient.getLastLocation().addOnCompleteListener(
                            new OnCompleteListener<Location>() {
                                @Override
                                public void onComplete(@NonNull Task<Location> task) {
                                    Location location = task.getResult();
                                    if (location == null) {
                                        requestNewLocationData();
                                    } else {
                                        final int R = 6371;
                                        Double lat1 = location.getLatitude();
                                        Double lon1 = location.getLongitude();
                                        Double lat2 = Double.parseDouble("-6.2340998");
                                        Double lon2 = Double.parseDouble("106.747489");
                                        Double latDistance = toRad(lat2 - lat1);
                                        Double lonDistance = toRad(lon2 - lon1);
                                        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                                                Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
                                                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
                                        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                                        Double distance = (R * c) * 1000;

                                        DecimalFormat dec = new DecimalFormat("#0");
                                        String singkat = dec.format(distance);
                                        String hasil = singkat;
                                        final int hasil2 = Integer.parseInt(hasil);
                                        jarak.setText(hasil + " Meter");

                                        absenDatang.setOnClickListener(new View.OnClickListener() {

                                            @Override
                                            public void onClick(View v) {
                                                // TODO Auto-generated method stub

                                                if (hasil2 <= 100) {
                                                    addAbsen();
                                                } else {
                                                    peringatan();
                                                }


                                            }
                                        });

                                    }
                                }
                            }
                    );

        }else{
                    Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
            }
        } else {
            requestPermissions();
        }
        return 0;
    }


    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            final int R = 6371;
            Double lat1 = mLastLocation.getLatitude();
            Double lon1 = mLastLocation.getLongitude();
            Double lat2 = Double.parseDouble("-6.2340998");
            Double lon2 = Double.parseDouble("106.747489");
            Double latDistance = toRad(lat2-lat1);
            Double lonDistance = toRad(lon2-lon1);
            Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
                            Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
            Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
            Double distance = (R * c)*1000;

            DecimalFormat dec = new DecimalFormat("#0");
            String singkat = dec.format(distance);
            String hasil = singkat;
            final int hasil2 = Integer.parseInt(hasil);
            jarak.setText(hasil + " Meter");

            absenDatang.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub

                    if(hasil2 <= 100){
                        addAbsen();
                    }
                    else {
                        peringatan();
                    }


                }
            });
        }
    };

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

//    public static boolean isMockSettingsON(Context context) {
//        // returns true if mock location enabled, false if not enabled.
//        if (Settings.Secure.getString(context.getContentResolver(),
//                Settings.Secure.ALLOW_MOCK_LOCATION).equals("0"))
//            return false;
//        else
//            return true;
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }

    }

    //akhir function lokasi


    private void addAbsen(){

        nim = getIntent().getStringExtra(TAG_NIM);


        class AddEmployee extends AsyncTask<Void,Void,String> {

            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(MainActivity.this,"Menambahkan...","Tunggu...",false,false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                try {
                    JSONObject jObj = new JSONObject(s);
                    Toast.makeText(getApplicationContext(), jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected String doInBackground(Void... v) {
                HashMap<String,String> params = new HashMap<>();
                params.put(TAG_NIM, nim);

                RequestHandler rh = new RequestHandler();
                String res = rh.sendPostRequest(url, params);
                return res;
            }
        }

        AddEmployee ae = new AddEmployee();
        ae.execute();
    }

    private void peringatan(){
        Toast.makeText(this, "Jarak Anda Lebih dari 100 M", Toast.LENGTH_LONG).show();
    }

    private void getAbsen(){
        class GetEmployee extends AsyncTask<Void,Void,String> {
            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(MainActivity.this,"Mengambil data ..","Tunggu...",false,false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                showAbsen(s);
            }

            @Override
            protected String doInBackground(Void... params) {
                RequestHandler rh = new RequestHandler();
                String nim2 = getIntent().getStringExtra(TAG_NIM);
                String s = rh.sendGetRequestParam(url_absen,nim2);
//                String s = rh.sendGetRequest(url_absen);
                return s;
            }
        }
        GetEmployee ge = new GetEmployee();
        ge.execute();
    }

    private void showAbsen(String json){
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray result = jsonObject.getJSONArray("result");
            JSONObject c = result.getJSONObject(0);
            String waktu_datang = c.getString("waktu_datang");
            String waktu_pulang = c.getString("waktu_pulang");

            if(waktu_datang == "null" && waktu_pulang == "null"){
                txt_waktu_datang.setText("Anda Belum Absen Datang");
                txt_waktu_pulang.setText("Anda Belum Absen Pulang");
            }
            else
                if(waktu_pulang == "null"){
                txt_waktu_datang.setText(waktu_datang);
                txt_waktu_pulang.setText("Anda Belum Absen Pulang");
            }
            else{
                txt_waktu_datang.setText(waktu_datang);
                txt_waktu_pulang.setText(waktu_pulang);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
