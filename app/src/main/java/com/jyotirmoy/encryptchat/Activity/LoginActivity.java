package com.jyotirmoy.encryptchat.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.jyotirmoy.encryptchat.R;

public class LoginActivity extends AppCompatActivity {

    EditText login_email, login_password;
    TextView signin_btn, txt_signup, forget_pass;

    FirebaseAuth auth;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_login);

        pd = new ProgressDialog(this);

        auth = FirebaseAuth.getInstance();

        txt_signup = findViewById(R.id.txt_signup);
        login_email = findViewById(R.id.login_email);
        login_password = findViewById(R.id.login_password);
        signin_btn = findViewById(R.id.signin_btn);
        forget_pass = findViewById(R.id.forget_pass);


        signin_btn.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                pd.setMessage("Please Wait..");
                pd.setCancelable(false);
                pd.show();

                //......Network Available check..........//
                if (!isConnected(this)) {
                    pd.dismiss();
                    ShowNetworkErrorDialog();
                } else {

                    String email = login_email.getText().toString().trim();
                    String password = login_password.getText().toString().trim();

                    if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                        pd.dismiss();
                        Toast.makeText(LoginActivity.this, "Please Enter Valid data", Toast.LENGTH_SHORT).show();
                    } else if (!email.matches(emailPattern)) {
                        pd.dismiss();
                        login_email.setError("invalid Email");
                    } else if (password.length() < 6) {
                        pd.dismiss();
                        login_password.setError("Password should be minimum 6 characters");
                    } else {
                        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {
                                    checkEmailVerified();

                                } else {
                                    pd.dismiss();
                                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();

                                }
                            }
                        });
                    }


                }

            }
        });


        txt_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
                finish();
            }
        });


        forget_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPasswordResetDialog();
            }
        });


    }

    private void ShowNetworkErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please Check your Internet Connection")
                .setCancelable(false)
                .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }

        });
        builder.create().show();

    }

    //................Network Available Check............... //
    private boolean isConnected(View.OnClickListener onClickListener) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ((wifiConn != null && wifiConn.isConnected()) || (mobileConn != null && mobileConn.isConnected())) {
            return true;
        } else {
            return false;
        }
    }

    private void checkEmailVerified() {

        if (auth.getCurrentUser().isEmailVerified()) {
            pd.dismiss();
         Toast.makeText(LoginActivity.this, "Logged In Successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();

        } else {
            pd.dismiss();
            Toast.makeText(LoginActivity.this, "Please Verify your Email", Toast.LENGTH_SHORT).show();
            login_password.setText("");

        }
    }


    private void showPasswordResetDialog() {
        //AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");
        builder.setMessage("Please Enter Your Email for Receive Password reset link....");
        builder.setCancelable(false);
        //set layout linear
        LinearLayout linearLayout = new LinearLayout(this);

        //view to set dialog
        EditText resetMail = new EditText(this);
        resetMail.setHint("Email");
        resetMail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        resetMail.setMinEms(16);

        linearLayout.addView(resetMail);
        linearLayout.setPadding(10, 10, 10, 10);
        builder.setView(linearLayout);

        //buttons yes
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String mail = resetMail.getText().toString();
                if (TextUtils.isEmpty(mail)) {
                    Toast.makeText(LoginActivity.this, "Not Enter any Email", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();

                } else {
                    beginRecovery(mail);
                }


            }
        });

        //buttons Cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    //.........Reset Password method .............//
    private void beginRecovery(String mail) {
        pd.setMessage("Sending Email..");
        pd.setCancelable(false);
        pd.show();
        auth.sendPasswordResetEmail(mail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                pd.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Reset Email Sent", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Failed....", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                //show proper error message
                Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onBackPressed() {
        finishAffinity();
        finish();
        System.exit(0);

    }
}