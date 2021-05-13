package com.jyotirmoy.encryptchat.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jyotirmoy.encryptchat.ModelClass.Users;
import com.jyotirmoy.encryptchat.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {

    ImageView backBtn;
    TextView saveBtn;
    FirebaseDatabase database;
    FirebaseAuth auth;
    FirebaseStorage storage;
    CircleImageView accountImage;
    EditText settingName, settingStatus;
    ProgressBar p;
    FloatingActionButton cameraBtn;
    Uri setImageURI;
    String email;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        pd = new ProgressDialog(this);
        pd.setMessage("Updating...");
        pd.setCancelable(false);

        getSupportActionBar().hide();

        p = findViewById(R.id.progressbar);

        backBtn = findViewById(R.id.backBtn);
        saveBtn = findViewById(R.id.saveBtn);
        settingName = findViewById(R.id.settingName);
        settingStatus = findViewById(R.id.settingStatus);
        accountImage = findViewById(R.id.accountImage);
        cameraBtn = findViewById(R.id.cameraButton);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingActivity.this, HomeActivity.class));
                finish();
            }
        });


        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        DatabaseReference reference = database.getReference().child("user").child(auth.getUid());
        StorageReference storageReference = storage.getReference().child("upload").child(auth.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                email = snapshot.child("email").getValue().toString();
                String name = snapshot.child("name").getValue().toString();
                String status = snapshot.child("status").getValue().toString();
                String image = snapshot.child("imageUri").getValue().toString();


                settingName.setText(name);
                settingStatus.setText(status);
                Picasso.get().load(image).into(accountImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        p.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });


            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);
            }
        });


        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pd.show();

                String name = settingName.getText().toString();
                String status = settingStatus.getText().toString();

                if (setImageURI != null) {
                    storageReference.putFile(setImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String finalImageUri = uri.toString();
                                    Users users = new Users(auth.getUid(), name, email, finalImageUri, status);
                                    reference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                pd.dismiss();
                                                Toast.makeText(SettingActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(SettingActivity.this, HomeActivity.class));
                                            } else {
                                                pd.dismiss();
                                                Toast.makeText(SettingActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    });
                } else {
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String finalImageUri = uri.toString();
                            Users users = new Users(auth.getUid(), name, email, finalImageUri, status);
                            reference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        pd.dismiss();
                                        Toast.makeText(SettingActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(SettingActivity.this, HomeActivity.class));
                                    } else {
                                        pd.dismiss();
                                        Toast.makeText(SettingActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {
            if (data != null) {
                setImageURI = data.getData();
                accountImage.setImageURI(setImageURI);
            }
        }
    }
}