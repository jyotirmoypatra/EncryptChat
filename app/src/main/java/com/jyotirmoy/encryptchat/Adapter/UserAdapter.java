package com.jyotirmoy.encryptchat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jyotirmoy.encryptchat.Activity.ChatActivity;
import com.jyotirmoy.encryptchat.Activity.HomeActivity;
import com.jyotirmoy.encryptchat.R;
import com.jyotirmoy.encryptchat.ModelClass.Users;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.Viewholder> {
    Context homeActivity;
    ArrayList<Users> usersArrayList;

    public byte encryptionKey[] = {9, 115, 51, 86, 105, 4, -31, -23, -68, 88, 17, 20, 3, -105, 119, -53};
    public Cipher cipher, decipher;
    public SecretKeySpec secretKeySpec;

    FirebaseAuth auth;
    FirebaseUser user;


    public UserAdapter(HomeActivity homeActivity, ArrayList<Users> usersArrayList) {
        this.homeActivity = homeActivity;
        this.usersArrayList = usersArrayList;

    }


    @NonNull
    @Override

    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        View view = LayoutInflater.from(homeActivity).inflate(R.layout.item_user_raw, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {


        Users users = usersArrayList.get(position);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user != null) {
            String currentUserId = user.getUid();
            if (currentUserId.equals(users.getUid())) {
                holder.itemView.setVisibility(View.GONE);
                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
//                ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
//                params.height = 0;
//                params.width = 0;
//                holder.itemView.setLayoutParams(params);
            }
        }
//        if(currentUserId.equals(users.getUid()))
//        {
//            holder.itemView.setVisibility(View.GONE);
//        }

        String senderId = FirebaseAuth.getInstance().getUid();
        String senderRoom = senderId + users.getUid();

        try {
            cipher = Cipher.getInstance("AES");
            decipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        secretKeySpec = new SecretKeySpec(encryptionKey, "AES");
        FirebaseDatabase.getInstance().getReference()
                .child("chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {
                            String lastMsg = snapshot.child("lastMsg").getValue(String.class);
                             long time = snapshot.child("lastMsgTime").getValue(Long.class);

                             SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                             holder.msgTime.setText(dateFormat.format(new Date(time)));
                            try {
                                holder.user_status.setText(AESDecryptionMethod(lastMsg));

                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }

                        } else {

                            holder.user_status.setText(users.status);
                            holder.msgTime.setText("");


                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        holder.user_name.setText(users.name);
        // holder.user_status.setText(users.status);
        Picasso.get().load(users.imageUri).into(holder.user_profile);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(homeActivity, ChatActivity.class);
                intent.putExtra("name", users.getName());
                intent.putExtra("ReceiverImage", users.getImageUri());
                intent.putExtra("uid", users.getUid());
                homeActivity.startActivity(intent);
            }
        });
    }

    private String AESDecryptionMethod(String string) throws UnsupportedEncodingException {

        byte[] EncryptedByte = string.getBytes("ISO-8859-1");
        String decryptedString = string;

        byte[] decryption;
        try {
            decipher.init(cipher.DECRYPT_MODE, secretKeySpec);
            decryption = decipher.doFinal(EncryptedByte);
            decryptedString = new String(decryption);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return decryptedString;
    }


    @Override
    public int getItemCount() {
        return usersArrayList.size();
    }

    class Viewholder extends RecyclerView.ViewHolder {
        CircleImageView user_profile;
        TextView user_name;
        TextView user_status;
        TextView msgTime;

        public Viewholder(@NonNull View itemView) {
            super(itemView);

            user_profile = itemView.findViewById(R.id.user_image);
            user_name = itemView.findViewById(R.id.user_name);
            user_status = itemView.findViewById(R.id.user_status);
            msgTime = itemView.findViewById(R.id.msgTime);

        }
    }
}
