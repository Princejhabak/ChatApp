package com.example.android.chatapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    private String TAG = "SignUp";

    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    private EditText editTextName, editTextEmail, editTextPassword, editTextConfirmPassword;
    private TextView textViewSignIn;
    private Button buttonRegister;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("chatApp") ;
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        progressDialog = new ProgressDialog(this);

        buttonRegister = (Button) findViewById(R.id.button_register);
        editTextName = (EditText) findViewById(R.id.et_register_name);
        editTextEmail = (EditText) findViewById(R.id.et_register_email);
        editTextPassword = (EditText) findViewById(R.id.et_register_password);
        editTextConfirmPassword = (EditText) findViewById(R.id.et_register_confirm_password);
        textViewSignIn = (TextView) findViewById(R.id.tvSignIn);

        hideKeyBoard();

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        textViewSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Sign in page
                finish();
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));

            }
        });


    }


    private void registerUser() {

        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String cPassword = editTextConfirmPassword.getText().toString().trim();


        if (TextUtils.isEmpty(name)) {
            // Password field is empty
            Toast.makeText(SignupActivity.this, "Please enter name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            // Email field is empty
            Toast.makeText(SignupActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            // Password field is empty
            Toast.makeText(SignupActivity.this, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(cPassword)) {
            // Password field is empty
            Toast.makeText(SignupActivity.this, "Please confirm your password", Toast.LENGTH_SHORT).show();
            return;
        }

        /*if(password != cPassword){
            Toast.makeText(SignupActivity.this,"Passwors dosen't match" ,Toast.LENGTH_SHORT).show();
            return;
        }*/

        progressDialog.setMessage("Registering User...");
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            // User is succesfully registerd and logged in
                            Toast.makeText(SignupActivity.this, "Regisration succesfull", Toast.LENGTH_SHORT).show();
                            saveUserInfo();
                            finish();
                            startActivity(new Intent(SignupActivity.this, MainActivity.class));
                        } else {

                            try {
                                throw task.getException();
                            }
                            // if user enters wrong email.
                            catch (FirebaseAuthWeakPasswordException weakPassword) {
                                Log.d(TAG, "onComplete: weak_password");
                                Toast.makeText(SignupActivity.this, "Weak password", Toast.LENGTH_LONG).show();
                            }
                            // if user enters wrong password.
                            catch (FirebaseAuthInvalidCredentialsException malformedEmail) {
                                Log.d(TAG, "onComplete: malformed_email");
                                Toast.makeText(SignupActivity.this, "Invalid email", Toast.LENGTH_LONG).show();
                            } catch (FirebaseAuthUserCollisionException existEmail) {
                                Log.d(TAG, "onComplete: exist_email");
                                Toast.makeText(SignupActivity.this, "Email already registerd", Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Log.d(TAG, "onComplete: " + e.getMessage());
                                Toast.makeText(SignupActivity.this, "Registration failed", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });

    }

    private void hideKeyBoard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            view.clearFocus();
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    private void saveUserInfo() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();


        UserInformation userInformation = new UserInformation(name, email, password);

        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        databaseReference.child("Users").child(firebaseUser.getUid()).setValue(userInformation);

    }
}
