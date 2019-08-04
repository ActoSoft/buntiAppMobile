package com.bucket.bunti.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bucket.bunti.Helpers.SharedPreferencesProject;
import com.bucket.bunti.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import static java.security.AccessController.getContext;

public class RegisterActivity extends AppCompatActivity {

    private EditText txtUser, txtEmail, txtPhone, txtPassword, txtPasswordC;
    private Button btnRegister, btnLogin;
    private CheckBox cbConditions;
    private Intent homeActivity, loginActivity;

    //FIREBASE
    private FirebaseAuth oAuth;
    private FirebaseFirestore dataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        txtUser      = findViewById(R.id.txtName);
        txtEmail     = findViewById(R.id.txtEmail);
        txtPhone     = findViewById(R.id.txtPhone);
        txtPassword  = findViewById(R.id.txtPassword);
        txtPasswordC = findViewById(R.id.txtPasswordConfirm);
        btnRegister  = findViewById(R.id.btnRegister);
        btnLogin     = findViewById(R.id.btnBackLogin);
        cbConditions = findViewById(R.id.cbAcept);
        oAuth        = FirebaseAuth.getInstance();
        dataBase     = FirebaseFirestore.getInstance();
        homeActivity  = new Intent(this,MenuActivity.class);
        loginActivity  = new Intent(this,LoginActivity.class);


        btnRegister.setEnabled(false);
        cbConditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cbConditions.isChecked()){
                    btnRegister.setEnabled(true);
                } else {
                    btnRegister.setEnabled(false);
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                disableComponents();

                final String user  = txtUser.getText().toString();
                final String email = txtEmail.getText().toString();
                final String phone = txtPhone.getText().toString();
                final String password = txtPassword.getText().toString();
                final String passwordC = txtPasswordC.getText().toString();

                if(user.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || passwordC.isEmpty()){
                    // Wrong - fields are empty
                    showMessage("Por favor completa todos los campos");
                    enableComponents();
                } else {
                    // fields ok
                    if(password.length() >= 8 && passwordC.length() >= 8){
                        if(password.equals(passwordC)){
                            // Create account
                            CreateUserAccount(user,email,phone,password);
                        } else {
                            // Wrong - passwords not same
                            showMessage("Las contraseñas no coinciden");
                            enableComponents();
                        }
                    } else {
                        showMessage("Las contraseñas deben tener al menos 8 digitos");
                        enableComponents();
                    }
                }
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUI("login");
            }
        });
    }

    private void CreateUserAccount(final String user,final String email,final String phone, final String password) {
        //create new account
        oAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            updateUserInfo(user, oAuth.getCurrentUser());
                            CreateLoginAccount(user, email, phone, oAuth.getUid());
                        } else {
                            enableComponents();
                            showMessage("Ocurrio un error, intenta más tarde");
                        }
                    }
                });
    }

    private void CreateLoginAccount(final String user, String email,String phone, String uid){
        // Create a new user
        Map<String, Object> usuario = new HashMap<>();
        usuario.put("nombre", user);
        usuario.put("email", email);
        usuario.put("telefono", phone);

        // Add a new document with a generated ID
        dataBase.collection("usuarios")
                .document(uid)
                .set(usuario)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void documentReference) {
                        //after create account, create login auth
                        showMessage("¡Registro exitoso!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Log.w("usuario", "Error adding document", e);
                        showMessage("Ocurrio un error, intenta mas tarde.");
                    }
                });
    }

    private void updateUserInfo(final String user, FirebaseUser currentUser){
        // Update username
        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                .setDisplayName(user)
                .build();

        currentUser.updateProfile(profileUpdate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // register finished
                        if(task.isSuccessful()){
                            showMessage("Cuenta creada...");
                            enableComponents();
                            updateUI("login");
                        }
                    }
                });
    }

    private void updateUI(String activity){
        // sent to new activity
        switch (activity){
            case "home":
                startActivity(homeActivity);
                break;

            case "login":
                startActivity(loginActivity);
                break;
        }
        finish();
    }

    private void showMessage(String message){
        // Method for toast messages
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void disableComponents(){
        txtUser.setEnabled(false);
        txtEmail.setEnabled(false);
        txtPhone.setEnabled(false);
        txtPassword.setEnabled(false);
        txtPasswordC.setEnabled(false);
        btnLogin.setEnabled(false);
        btnRegister.setEnabled(false);
        btnRegister.setText("Registrando...");
        btnRegister.setTextColor(getApplication().getResources().getColor(R.color.colorPrimaryDark));
    }

    private void enableComponents(){
        txtUser.setEnabled(true);
        txtEmail.setEnabled(true);
        txtPhone.setEnabled(true);
        txtPassword.setEnabled(true);
        txtPasswordC.setEnabled(true);
        btnLogin.setEnabled(true);
        btnRegister.setEnabled(true);
        btnRegister.setText(getResources().getString(R.string.registrarme));
        btnRegister.setTextColor(getApplication().getResources().getColor(R.color.colorText));
    }
}
