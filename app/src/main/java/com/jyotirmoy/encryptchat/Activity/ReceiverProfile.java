package com.jyotirmoy.encryptchat.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jyotirmoy.encryptchat.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class ReceiverProfile extends AppCompatActivity {
    String ReceiverName, ReceiverImage, ReceiverUid;
    ImageView receiverImage;
    FirebaseAuth auth;
    TextView receiverName, receiverEmail,receiverStatus;

    FirebaseDatabase database;
    ProgressBar p;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver_profile);
        getSupportActionBar().hide();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        ReceiverName = getIntent().getStringExtra("name");
        ReceiverImage = getIntent().getStringExtra("ReceiverImage");
        ReceiverUid = getIntent().getStringExtra("uid");


        p = findViewById(R.id.loadingbar);




        receiverName = findViewById(R.id.receiverName);
        receiverEmail = findViewById(R.id.receiverEmail);
        receiverImage = findViewById(R.id.receiverImage);
        receiverStatus=findViewById(R.id.receiverStatus);

        receiverName.setText(ReceiverName);
        Picasso.get().load(ReceiverImage).into(receiverImage, new Callback() {
            @Override
            public void onSuccess() {
                p.setVisibility(View.GONE);
            }

            @Override
            public void onError(Exception e) {

            }
        });


        DatabaseReference reference = database.getReference().child("user").child(ReceiverUid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String Email = snapshot.child("email").getValue().toString();
                String status = snapshot.child("status").getValue().toString();

                receiverEmail.setText(Email);
                receiverStatus.setText(status);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




    }
}