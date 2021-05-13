package com.jyotirmoy.encryptchat.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.jyotirmoy.encryptchat.R;
import com.jyotirmoy.encryptchat.Adapter.UserAdapter;
import com.jyotirmoy.encryptchat.ModelClass.Users;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    FirebaseAuth auth;
    ProgressDialog pd;
    FirebaseDatabase database;
    FirebaseStorage storage;
    DatabaseReference reference;

    RecyclerView mainUserRecyclerView;
    UserAdapter adapter;
    ArrayList<Users> usersArrayList;
    String authValue;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        isNetworkConnected();

        progressBar=findViewById(R.id.loadUser);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        authValue = auth.getUid();

        pd = new ProgressDialog(this);
        usersArrayList = new ArrayList<>();

        DatabaseReference reference = database.getReference().child("user");


        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users users = dataSnapshot.getValue(Users.class);
                    usersArrayList.add(users);
                }
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        mainUserRecyclerView = findViewById(R.id.mainUserRecyclerview);
        mainUserRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(HomeActivity.this, usersArrayList);
        mainUserRecyclerView.setAdapter(adapter);


        if (auth.getCurrentUser() == null || auth.getCurrentUser().isEmailVerified() == false) {
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
        }


    }

    private void isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ((wifiConn != null && wifiConn.isConnected()) || (mobileConn != null && mobileConn.isConnected())) {

        } else {
            //Toast.makeText(HomeActivity.this, "Network Not Available", Toast.LENGTH_SHORT).show();
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        reference = FirebaseDatabase.getInstance().getReference().child("user");

        switch (item.getItemId()) {

            case R.id.settings:
                startActivity(new Intent(HomeActivity.this, SettingActivity.class));
                break;

            case R.id.logout:
                auth.signOut();
                Toast.makeText(HomeActivity.this, "Sign Out Successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                finish();
                break;
            case R.id.deleteAccount:

                pd = new ProgressDialog(this);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Delete Account");
                builder.setMessage("Are You Want to Delete Account Permanently");
                builder.setCancelable(false);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pd.setMessage("Deleting..");
                        pd.setCancelable(false);
                        pd.show();



                        reference.child(auth.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    //pd.dismiss();

                                    storage.getReference().child("upload").child(auth.getUid()).delete();
                                    deleteUser();

                                }
                            }
                        });


                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });
                builder.create().show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void deleteUser() {
        auth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {

            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    Toast.makeText(HomeActivity.this, "Account Deleted Successfully", Toast.LENGTH_LONG).show();
                    auth.signOut();
                    startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                    pd.dismiss();
                    // finishAffinity();
                } else {
                    pd.dismiss();
                    Toast.makeText(HomeActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}