package com.example.kkp.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kkp.Api;
import com.example.kkp.R;
import com.example.kkp.adapter.RequestHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class Izin extends AppCompatActivity implements View .OnClickListener{

    Button MulaiIzin, AkhirIzin, btnIzin;
    EditText txtAlasan;
    TextView txtMulaiIzin, txtAkhirIzin;



    Calendar myCalendar;
    String nim;

    public static final String TAG_NIM = "nim";
    private static final String TAG_MESSAGE = "message";
    private String url = Api.URL + "izin.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_izin);
        nim = getIntent().getStringExtra(TAG_NIM);

        txtMulaiIzin = (TextView) findViewById(R.id.txtMulai_izin);
        txtAkhirIzin = (TextView) findViewById(R.id.txtAkhir_izin);
        txtAlasan = (EditText) findViewById(R.id.txtAlasan);

        MulaiIzin = (Button) findViewById(R.id.btnMulai_izin);
        AkhirIzin = (Button) findViewById(R.id.btnAkhir_izin);
        btnIzin = (Button) findViewById(R.id.btn_AjukanCuti);

        btnIzin.setOnClickListener(this);

        myCalendar = Calendar.getInstance();

        MulaiIzin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(Izin.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, month);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        String formatTanggal = "yyyy-MM-dd";
                        SimpleDateFormat sdf = new SimpleDateFormat(formatTanggal);
                        txtMulaiIzin.setText(sdf.format(myCalendar.getTime()));
                    }
                },
                        myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        AkhirIzin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(Izin.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, month);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        String formatTanggal = "yyyy-MM-dd";
                        SimpleDateFormat sdf = new SimpleDateFormat(formatTanggal);
                        txtAkhirIzin.setText(sdf.format(myCalendar.getTime()));
                    }
                },
                        myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == btnIzin){
            addIzin();
        }
    }

    private void addIzin(){

        final String awal_izin = txtMulaiIzin.getText().toString().trim();
        final String akhir_izin = txtAkhirIzin.getText().toString().trim();
        final String alasan = txtAlasan.getText().toString().trim();
        final String nim_asisten = getIntent().getStringExtra(TAG_NIM);

        class AddIzin extends AsyncTask<Void,Void,String> {

            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(Izin.this,"Menambahkan...","Tunggu...",false,false);
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


                //Toast.makeText(Izin.this,s,Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Izin.this, MainActivity.class);
                intent.putExtra(TAG_NIM, nim);
                startActivity(intent);
            }

            @Override
            protected String doInBackground(Void... v) {
                HashMap<String,String> params = new HashMap<>();
                params.put("awal_izin",awal_izin);
                params.put("akhir_izin",akhir_izin);
                params.put("keterangan",alasan);
                params.put("nim", nim_asisten);


                RequestHandler rh = new RequestHandler();
                String res = rh.sendPostRequest(url, params);
                return res;
            }
        }

        AddIzin ae = new AddIzin();
        ae.execute();
    }
}
