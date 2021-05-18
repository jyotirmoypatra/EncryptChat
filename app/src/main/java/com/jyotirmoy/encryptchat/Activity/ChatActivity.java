package com.jyotirmoy.encryptchat.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.jyotirmoy.encryptchat.Adapter.MessagesAdapter;
import com.jyotirmoy.encryptchat.ModelClass.Messages;

import com.jyotirmoy.encryptchat.R;
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {


    String ReceiverImage, ReceiverUid, ReceiverName, SenderUID;
    CircleImageView profileImage;
    TextView receiverName, onlineStatus;
    LinearLayout receiverInfo;
    ImageView backBtn, video, audio;

    CardView sendBtn, clip;
    EditText editMessage;
    FirebaseAuth auth;
    FirebaseDatabase database;


    String senderRoom, receiverRoom;

    RecyclerView messageAdapter;

    ArrayList<Messages> messagesArrayList;
    MessagesAdapter adapter;


    public byte encryptionKey[] = {9, 115, 51, 86, 105, 4, -31, -23, -68, 88, 17, 20, 3, -105, 119, -53};
    public Cipher cipher, decipher;
    public SecretKeySpec secretKeySpec;
    public String stringMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        getSupportActionBar().hide();
        database = FirebaseDatabase.getInstance();

        backBtn = findViewById(R.id.back_btn);
        video = findViewById(R.id.videoCall);
        audio = findViewById(R.id.audioCall);
        onlineStatus = findViewById(R.id.onlineStatus);

        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChatActivity.this, "Video Call Features Not Available", Toast.LENGTH_SHORT).show();
            }
        });

        audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChatActivity.this, "Audio Call Features Not Available", Toast.LENGTH_SHORT).show();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(ChatActivity.this, HomeActivity.class));
            }
        });

        ReceiverName = getIntent().getStringExtra("name");
        ReceiverImage = getIntent().getStringExtra("ReceiverImage");
        ReceiverUid = getIntent().getStringExtra("uid");

        profileImage = findViewById(R.id.Receiver_Profile_image);
        receiverName = findViewById(R.id.Receiver_name);
        receiverInfo = findViewById(R.id.receiverInfo);


        //Show Online offline status//
        database.getReference().child("presence").child(ReceiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = snapshot.getValue(String.class);
                    if (!status.isEmpty()) {
                        if(status.equals("Offline")){
                            onlineStatus.setVisibility(View.GONE);
                        }
                        else {
                            onlineStatus.setText(status);
                            onlineStatus.setVisibility(View.VISIBLE);
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        messageAdapter = findViewById(R.id.messageAdapter);
        messagesArrayList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messageAdapter.setLayoutManager(linearLayoutManager);
        adapter = new MessagesAdapter(ChatActivity.this, messagesArrayList);
        messageAdapter.setAdapter(adapter);

        sendBtn = findViewById(R.id.sendBtn);
        editMessage = findViewById(R.id.editMessage);

        Picasso.get().load(ReceiverImage).into(profileImage);
        receiverName.setText("" + ReceiverName);

        auth = FirebaseAuth.getInstance();
        SenderUID = auth.getUid();

        senderRoom = SenderUID + ReceiverUid;
        receiverRoom = ReceiverUid + SenderUID;


        //DatabaseReference chatReference = database.getReference().child("chats").child(senderRoom).child("messages");

        try {
            DatabaseReference chatReference = database.getReference().child("chats").child(senderRoom).child("messages");


            try {
                cipher = Cipher.getInstance("AES");
                decipher = Cipher.getInstance("AES");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            }

            secretKeySpec = new SecretKeySpec(encryptionKey, "AES");

            chatReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    messagesArrayList.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Messages messages = dataSnapshot.getValue(Messages.class);


                        messagesArrayList.add(messages);
                        messageAdapter.smoothScrollToPosition(messageAdapter.getAdapter().getItemCount());
                    }
                    adapter.notifyDataSetChanged();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageTxt = editMessage.getText().toString();
                if (messageTxt.isEmpty()) {
                    Toast.makeText(ChatActivity.this, "Please Enter Message", Toast.LENGTH_SHORT).show();

                } else {
                    String message = AESEncryptionMethod(editMessage.getText().toString());
                    editMessage.setText("");
                    Date date = new Date();
                    Messages messages = new Messages(message, SenderUID, date.getTime());

                    database = FirebaseDatabase.getInstance();
                    HashMap<String, Object> lastMsgObj = new HashMap<>();
                    lastMsgObj.put("lastMsg", messages.getMessage());
                    lastMsgObj.put("lastMsgTime", date.getTime());
                    database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                    database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

                    database.getReference().child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .push()
                            .setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            database.getReference().child("chats")
                                    .child(receiverRoom)
                                    .child("messages")
                                    .push()
                                    .setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                }
                            });
                        }
                    });
                }
            }
        });


        //............Typing... message status.......//
        final Handler handler = new Handler();
        editMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                database.getReference().child("presence").child(SenderUID).setValue("Typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping, 800);

            }

            Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("presence").child(SenderUID).setValue("Online");
                }
            };
        });


// .............  Receiver Profile details activity...................//
        receiverInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, ReceiverProfile.class);
                intent.putExtra("name", ReceiverName);
                intent.putExtra("ReceiverImage", ReceiverImage);
                intent.putExtra("uid", ReceiverUid);
                ChatActivity.this.startActivity(intent);
            }
        });

        clip = findViewById(R.id.clip);
        clip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChatActivity.this, "attach file", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private String AESEncryptionMethod(String string) {
        byte[] stringByte = string.getBytes();
        byte[] encryptedByte = new byte[stringByte.length];

        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            encryptedByte = cipher.doFinal(stringByte);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        String returnString = null;

        try {
            returnString = new String(encryptedByte, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return returnString;
    }


    @Override
    protected void onResume() {
        super.onResume();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        String currentId= FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Offline");
    }
}