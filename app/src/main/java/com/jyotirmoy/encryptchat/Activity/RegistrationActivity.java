package com.jyotirmoy.encryptchat.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jyotirmoy.encryptchat.R;
import com.jyotirmoy.encryptchat.ModelClass.Users;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegistrationActivity extends AppCompatActivity {
    CircleImageView profile_image;
    EditText reg_name, reg_email, reg_pass, reg_cpass;
    TextView signup_btn, txt_signin;
    FirebaseAuth auth;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    Uri imageUri;
    FirebaseDatabase database;
    FirebaseStorage storage;
    String imageURI;
    ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        pd = new ProgressDialog(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();


        txt_signin = findViewById(R.id.txt_signin);
        profile_image = findViewById(R.id.profile_image);
        reg_name = findViewById(R.id.reg_name);
        reg_email = findViewById(R.id.reg_email);
        reg_pass = findViewById(R.id.reg_pass);
        reg_cpass = findViewById(R.id.reg_cpass);
        signup_btn = findViewById(R.id.signup_btn);


        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.setMessage("Please Wait..");
                pd.setCancelable(false);
                pd.show();
                String name = reg_name.getText().toString();
                String email = reg_email.getText().toString().trim();
                String pass = reg_pass.getText().toString().trim();
                String cpass = reg_cpass.getText().toString().trim();
                String status="Hey There I'm Using EncryptChat";

                if (TextUtils.isEmpty(name)) {
                    pd.dismiss();
                    reg_name.setError("please Enter Name");
                    Toast.makeText(RegistrationActivity.this, "Please Enter Name", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(email)) {
                    pd.dismiss();
                    reg_email.setError("please Enter Email");
                    Toast.makeText(RegistrationActivity.this, "Please Enter Email", Toast.LENGTH_SHORT).show();
                }else if (!email.matches(emailPattern)) {
                    pd.dismiss();
                    reg_email.setError("please enter valid Email");
                    Toast.makeText(RegistrationActivity.this, "Please Enter Valid Email", Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(pass)){
                    pd.dismiss();
                    reg_pass.setError("Please Enter Password");
                    Toast.makeText(RegistrationActivity.this, "Please Enter Password", Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(cpass)){
                    pd.dismiss();
                    reg_pass.setError("Please Enter Confirm Password");
                    Toast.makeText(RegistrationActivity.this, "Please Enter confirm Password", Toast.LENGTH_SHORT).show();
                }
                else if (!pass.equals(cpass)) {
                    pd.dismiss();
                    Toast.makeText(RegistrationActivity.this, "Password does not match", Toast.LENGTH_SHORT).show();

                } else if (pass.length() < 6) {
                    pd.dismiss();
                    reg_pass.setError("Password should be minimum 6 characters");
                    Toast.makeText(RegistrationActivity.this, "Password should be minimum 6 characters", Toast.LENGTH_SHORT).show();
                } else {
                    auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                DatabaseReference reference = database.getReference().child("user").child(auth.getUid());
                                StorageReference storageReference = storage.getReference().child("upload").child(auth.getUid());
                                if (imageUri != null) {
                                    storageReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        imageURI = uri.toString();
                                                        Users users = new Users(auth.getUid(), name, email, imageURI,status);
                                                        reference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {

                                                                    sendEmailVerification();

                                                                } else {
                                                                    pd.dismiss();
                                                                    Toast.makeText(RegistrationActivity.this, "Error in create account", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }else{
                                    String status="Hey There I'm Using EncryptChat";
                                    imageURI = "https://firebasestorage.googleapis.com/v0/b/encryptchat-3f463.appspot.com/o/profile.png?alt=media&token=cc384d00-ccc8-4dae-937a-8a49e56021ea";
                                    Users users = new Users(auth.getUid(), name, email, imageURI,status);
                                    reference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                sendEmailVerification();
                                                //pd.dismiss();
                                                //startActivity(new Intent(RegistrationActivity.this, HomeActivity.class));
                                               // finish();
                                            } else {
                                                Toast.makeText(RegistrationActivity.this, "Error in create account", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            } else {
                                pd.dismiss();
                                Toast.makeText(RegistrationActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                reg_name.setText("");
                                reg_email.setText("");
                                reg_pass.setText("");
                                reg_cpass.setText("");
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            //show proper error message
                            Toast.makeText(RegistrationActivity.this, "Failed!!" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }


            }
        });


        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);
            }
        });


        txt_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void sendEmailVerification() {
    auth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
        @Override
        public void onComplete(@NonNull Task<Void> task) {
            if(task.isSuccessful()){
                pd.dismiss();
                Toast.makeText(RegistrationActivity.this, "Please Check Email for Verification", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegistrationActivity.this, HomeActivity.class));
                finish();


            }

        }
    });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {
            if (data != null) {
                imageUri = data.getData();
                profile_image.setImageURI(imageUri);
            }
        }
    }
    @Override
    public void onBackPressed() {
        finishAffinity();
        finish();
        System.exit(0);

    }
}