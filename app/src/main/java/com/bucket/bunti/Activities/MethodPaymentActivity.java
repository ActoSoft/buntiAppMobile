package com.bucket.bunti.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bucket.bunti.Helpers.SharedPreferencesProject;
import com.bucket.bunti.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.security.AccessController.getContext;

public class MethodPaymentActivity extends AppCompatActivity {

    //FIREBASE
    private FirebaseAuth oAuth;
    private FirebaseUser user;
    private FirebaseFirestore dataBase;
    private CollectionReference usuariosRef;
    private Date date = new Date();
    private String address, latitud,longitud;
    private Intent uiCheckPayment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_method_payment);
        Button btnCash = (Button) findViewById(R.id.buttonCash);
        Button btnCreditCard = (Button) findViewById(R.id.buttonCreditCard);
        Button btnPaypal = (Button) findViewById(R.id.buttonPayPal);
        uiCheckPayment = new Intent(this, CheckPayment.class);
        oAuth       = FirebaseAuth.getInstance();
        user = oAuth.getCurrentUser();
        dataBase    = FirebaseFirestore.getInstance();
        usuariosRef = dataBase.collection("usuarios");

        locationStart();

        btnCash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveService("efectivo");
            }
        });

        btnCreditCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveService("tarjeta de credito");
            }
        });

        btnPaypal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveService("paypal");
            }
        });
    }

    private void locationStart() {
        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Localizacion Local = new Localizacion();
        Local.setLocActivity(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
            return;
        }
        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (LocationListener) Local);
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) Local);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationStart();
                return;
            }
        }
    }

    public void setLocation(Location loc) {
        //Obtener la direccion de la calle a partir de la latitud y la longitud
        if (loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(
                        loc.getLatitude(), loc.getLongitude(), 1);
                if (!list.isEmpty()) {
                    Address DirCalle = list.get(0);
                    SharedPreferencesProject.insertData(getApplicationContext(),
                            "address",DirCalle.getAddressLine(0));
                    SharedPreferencesProject.insertData(getApplicationContext(),
                            "latitud",String.valueOf(loc.getLatitude()));
                    SharedPreferencesProject.insertData(getApplicationContext(),
                            "longitud",String.valueOf(loc.getLongitude()));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /* Aqui empieza la Clase Localizacion */
    public class Localizacion implements LocationListener {
        MethodPaymentActivity locActivity;

        public MethodPaymentActivity getLocActivity() {
            return locActivity;
        }

        public void setLocActivity(MethodPaymentActivity locActivity) {
            this.locActivity = locActivity;
        }

        @Override
        public void onLocationChanged(Location loc) {
            this.locActivity.setLocation(loc);
        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("debug", "LocationProvider.AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                    break;
            }
        }
    }

    private void saveService(final String metodo_pago){
        address  = SharedPreferencesProject.retriveData(getApplicationContext(),"address");
        latitud  = SharedPreferencesProject.retriveData(getApplicationContext(),"latitud");
        longitud = SharedPreferencesProject.retriveData(getApplicationContext(),"longitud");

        // Create a new user
        Map<String, Object> service = new HashMap<>();
        service.put("usuario", user.getDisplayName());
        service.put("pago",metodo_pago);
        service.put("direccion", address);
        service.put("latitud", latitud);
        service.put("longitud", longitud);
        service.put("fecha", getDate());
        service.put("hora", getHour());
        service.put("exactTime",getVarDate());

        // Add a new document with a generated ID
        //retrieve data
        String id_document = SharedPreferencesProject.retriveData(getApplicationContext(),"id_document");
        usuariosRef.document(id_document).collection("services").add(service)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        updateUI("check_payment",metodo_pago);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showMessage("Ocurrio un error, intenta mas tarde.");
                    }
                });
    }

    private void updateUI(String activity, String paymet_method){
        String dateNow =getDate()+" - "+getHourAmPm();
        switch (activity){
            case "check_payment":
                uiCheckPayment.putExtra("client",user.getDisplayName());
                uiCheckPayment.putExtra("date",dateNow);
                uiCheckPayment.putExtra("address",address);
                uiCheckPayment.putExtra("payment",paymet_method);
                startActivityForResult(uiCheckPayment,0);
                break;
        }
        finish();
    }

    private String getDate(){
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return dateFormat.format(date)+"";
    }

    private String getHour(){
        DateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
        return hourFormat.format(date)+"";
    }

    private String getHourAmPm(){
        DateFormat hourFormat = new SimpleDateFormat("hh:mm a");
        return hourFormat.format(date)+"";
    }

    private String getVarDate(){
        String exactTime = "";
        Calendar fecha = Calendar.getInstance();
        String year = String.valueOf(fecha.get(Calendar.YEAR));
        String month = String.valueOf(fecha.get(Calendar.MONTH) + 1);
        String day = String.valueOf(fecha.get(Calendar.DAY_OF_MONTH));
        String hour = String.valueOf(fecha.get(Calendar.HOUR_OF_DAY));
        String minute = String.valueOf(fecha.get(Calendar.MINUTE));
        String second = String.valueOf(fecha.get(Calendar.SECOND));

        exactTime = year+""+month+""+day+""+hour+""+minute+""+second;

        return exactTime;
    }

    private void showMessage(String message){
        // Method for toast messages
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
