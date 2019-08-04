package com.bucket.bunti.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;

import com.bucket.bunti.Helpers.SharedPreferencesProject;
import com.bucket.bunti.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Map;

public class MenuActivity extends AppCompatActivity {

    private TextView btnLogout;
    //FIREBASE
    private FirebaseAuth oAuth;
    private FirebaseUser userCurrent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        btnLogout = findViewById(R.id.btnLogout);

        oAuth = FirebaseAuth.getInstance();
        userCurrent = oAuth.getCurrentUser();

        sendTokenIdToServer();

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                SharedPreferencesProject.deleteData(getApplicationContext(),"id_document");
                Intent login = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(login);
                finish();
            }
        });

        Button btnlocalization = (Button) findViewById(R.id.buttonSearch);
        btnlocalization.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentlocalization = new Intent(view.getContext(), LocalizationActivity.class);
                startActivityForResult(intentlocalization,0);
            }
        });

        Button btnUserFound = (Button) findViewById(R.id.buttonCall);
        btnUserFound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentUserFound = new Intent(view.getContext(),UserFound.class);
                startActivityForResult(intentUserFound,0);
            }
        });
    }

    private void sendTokenIdToServer() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if(!task.isSuccessful()) {
                            Log.w("MenuActivity", "getInstanceId failed", task.getException());
                            return;
                        }

                        String token = task.getResult().getToken();

                        FirebaseAuth oAuth = FirebaseAuth.getInstance();
                        FirebaseUser user = oAuth.getCurrentUser();
                        FirebaseFirestore dataBase = FirebaseFirestore.getInstance();

                        Map<String, Object> instanceId = new HashMap<>();
                        instanceId.put("app_token", token);

                        dataBase.collection("usuarios")
                                .document(user.getUid())
                                .update(instanceId)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("INSTANCE_ID", "onNewToken executed");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("INSTANCE_ID", "Algo falló con el token");
                                        Log.e("INSTANCE_ID", e.getMessage());
                                    }
                                });
                    }
                });
    }
}
