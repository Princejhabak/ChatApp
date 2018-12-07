package com.example.android.chatapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {

    private String TAG = "SignIn";

    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    private EditText editTextEmail, editTextPassword;
    private Button buttonSignup, buttonSignin;

    private KeyListener originalKeyListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);

        editTextEmail = (EditText) findViewById(R.id.et_email);
        editTextPassword = (EditText) findViewById(R.id.et_password);
        buttonSignin = (Button) findViewById(R.id.button_login);
        buttonSignup = (Button) findViewById(R.id.button_signup);

        checkInternetConnection();
        hideKeyBoard();

        buttonSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userLogin();
            }
        });

        buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });


        /*FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        } else {
            // User is signed out
            Log.d(TAG, "onAuthStateChanged:signed_out");
        }*/

    }


    private void userLogin() {

        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            // Email field is empty
            Toast.makeText(LoginActivity.this, "Please enter email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            // Password field is empty
            Toast.makeText(LoginActivity.this, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Logging in...");
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            finish();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        } else {
                            try {
                                throw task.getException();
                            }
                            // if user enters wrong email.
                            catch (FirebaseAuthInvalidUserException invalidEmail) {
                                Log.d(TAG, "onComplete: invalid_email");
                                Toast.makeText(LoginActivity.this, "Invalid email", Toast.LENGTH_LONG).show();
                            }
                            // if user enters wrong password.
                            catch (FirebaseAuthInvalidCredentialsException wrongPassword) {
                                Log.d(TAG, "onComplete: wrong_password");
                                Toast.makeText(LoginActivity.this, "Incorrect Password", Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Log.d(TAG, "onComplete: " + e.getMessage());
                                Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });

    }

    private void checkInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();
        if (nInfo != null && nInfo.isConnected()) {

        } else {
            AlertDialog.Builder a_builder = new AlertDialog.Builder(LoginActivity.this);
            a_builder.setMessage("Please enable internet connection !!!")
                    .setCancelable(false)
                    .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {


                            Intent in = new Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
                            startActivity(in);

                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            AlertDialog alert = a_builder.create();
            alert.setTitle("No Internet Connection");
            alert.show();
        }

    }

    private void hideKeyBoard() {

        // On clicking search button keyboard appears
        // Save its key listener which makes it editable.
        originalKeyListener = editTextEmail.getKeyListener();
        // Restore key listener - this will make the field editable again.
        editTextEmail.setKeyListener(originalKeyListener);

        // Focus the field.
        editTextEmail.requestFocus();

        // Show soft keyboard for the user to enter the value.
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editTextEmail, InputMethodManager.SHOW_IMPLICIT);
    }


}
