package com.shariful.hello;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.shariful.hello.adapters.AdapterChat;
import com.shariful.hello.models.ModelChat;
import com.shariful.hello.models.ModelUser;
import com.shariful.hello.notifications.APIService;
import com.shariful.hello.notifications.Client;
import com.shariful.hello.notifications.Data;
import com.shariful.hello.notifications.Sender;
import com.shariful.hello.notifications.Token;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileIv;
    TextView nameTv,user_statusTv;
    EditText messageEt;
    ImageButton sendBtn;


    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference userDbRef;

    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;

    List<ModelChat> chatList;
    AdapterChat adapterChat;


    String hisUid;
    String myUid;
    String hisImage;

    APIService apiService;
    boolean notify= false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbarID);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        recyclerView = findViewById(R.id.chat_recyclerViewID);
        profileIv = findViewById(R.id.profileIvID);
        nameTv = findViewById(R.id.nametv);
        user_statusTv = findViewById(R.id.userStatusTVID);
        messageEt = findViewById(R.id.messageEtID);
        sendBtn = findViewById(R.id.sendBtnID);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        //recycler view properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        // create api service
      //  apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);



        Intent intent = getIntent();
        hisUid = intent.getStringExtra("hisUid");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase= FirebaseDatabase.getInstance();
        userDbRef = firebaseDatabase.getReference("User");


        //search user to get that user's info
        Query userQuery = userDbRef.orderByChild("uid").equalTo(hisUid);
        //get user picture and name
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //check until required info is received
                for (DataSnapshot ds: dataSnapshot.getChildren())
                {
                    //get data
                    String name = ""+ ds.child("name").getValue();
                    hisImage = ""+ ds.child("image").getValue();
                    String typingStatus = ""+ ds.child("typingTo").getValue();

                    //check typing status
                    if (typingStatus.equals(myUid))
                    {
                        user_statusTv.setText("typing...");
                    }
                    else
                    {

                        //get online status
                        String onlineStatus = ""+ ds.child("onlineStatus").getValue();

                        if (onlineStatus.equals("online"))
                        {
                            user_statusTv.setText("online");
                        }
                        else
                        {
                            //convert timestamp to proper time date
                            // time date
                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(onlineStatus));
                            String dateTime= DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();

                            user_statusTv.setText("Last Seen: "+dateTime);
                        }

                    }


                    //set data
                    nameTv.setText(name);


                    try{
                        Picasso.get().load(hisImage).placeholder(R.drawable.ic_default_img).into(profileIv);

                    }
                    catch (Exception e)
                    {

                        Picasso.get().load(R.drawable.ic_default_img).into(profileIv);
                    }


                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                notify = true;

                String message = messageEt.getText().toString().trim();

                if (TextUtils.isEmpty(message))
                {

                    Toast.makeText(ChatActivity.this, "Cannot send empty message !!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                 sendMessage(message);

                }


            }
        });

             //check message editText change listener
        messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().trim().length()==0){

                    checkTypingStatus("noOne");
                }
                else {
                    checkTypingStatus(hisUid);//uid of receiver
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



              readMessages();
              seenMessage();


    }

    private void seenMessage() {

           userRefForSeen = FirebaseDatabase.getInstance().getReference("chats");
           seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                   for (DataSnapshot ds: dataSnapshot.getChildren())
                   {

                       ModelChat chat = ds.getValue(ModelChat.class);
                       if (chat.getReceiver().equals(myUid)&&chat.getSender().equals(hisUid))
                       {
                           HashMap<String,Object> hashSeenHashMap= new HashMap<>();
                           hashSeenHashMap.put("isSeen",true);
                           ds.getRef().updateChildren(hashSeenHashMap);


                       }



                   }


               }

               @Override
               public void onCancelled(@NonNull DatabaseError databaseError) {

               }




           });

    }

    private void readMessages()
    {
        chatList = new ArrayList<>();
        DatabaseReference dbRef= FirebaseDatabase.getInstance().getReference("chats");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
             chatList.clear();
             for (DataSnapshot ds: dataSnapshot.getChildren())
                {
                 ModelChat chat = ds.getValue(ModelChat.class);
                 if (chat.getReceiver().equals(myUid)&&chat.getSender().equals(hisUid)||chat.getReceiver().equals(hisUid)&&chat.getSender().equals(myUid))
                    {
                        chatList.add(chat);
                    }

                   adapterChat = new AdapterChat(ChatActivity.this,chatList,hisImage);
                   adapterChat.notifyDataSetChanged();

                 recyclerView.setAdapter(adapterChat);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {



            }
        });
    }

    private void sendMessage(final String message)
       {
          DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

          String timestamp = String.valueOf(System.currentTimeMillis());


           HashMap<String, Object>  hashMap = new HashMap<>();
           hashMap.put("sender",myUid);
           hashMap.put("receiver",hisUid);
           hashMap.put("message",message);
           hashMap.put("timestamp",timestamp);
           hashMap.put("isSeen",false);
           databaseReference.child("chats").push().setValue(hashMap);
           //reset editText after sending message
           messageEt.setText("");

           String msg= message;
          final DatabaseReference database =FirebaseDatabase.getInstance().getReference("User").child(myUid);
             database.addValueEventListener(new ValueEventListener() {
                 @Override
                 public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                     ModelUser user = dataSnapshot.getValue(ModelUser.class);
                     if (notify)
                     {
                         senNotification(hisUid,user.getName(),message);

                     }

                 }

                 @Override
                 public void onCancelled(@NonNull DatabaseError databaseError) {

                 }
             });


       }

    private void senNotification(final String hisUid, final String name, final String message)
        {
            DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
            Query query =allTokens.orderByKey().equalTo(hisUid);

            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot ds: dataSnapshot.getChildren())
                    {
                        Token token = ds.getValue(Token.class);
                        Data data = new Data(myUid,name+":"+message,"New Message",hisUid,R.drawable.ic_default_img);
                        Sender sender =new Sender(data,token.getToken());
                      //  apiService

                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });



        }

    private  void checkUserState() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser!=null)
        {
            //Stay here
            // profileTv.setText(firebaseUser.getEmail());
            myUid = firebaseUser.getUid();//currently signed in user id



        }
        else
        {
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private  void  checkOnlineStatus(String status) {

          DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("User").child(myUid);
          HashMap<String, Object> hashMap =new HashMap<>();
          hashMap.put("onlineStatus",status);
          //update online status of current user
          dbRef.updateChildren(hashMap);
    }

    private  void  checkTypingStatus(String typing) {

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("User").child(myUid);
        HashMap<String, Object> hashMap =new HashMap<>();
        hashMap.put("typingTo",typing);
        //update online status of current user
        dbRef.updateChildren(hashMap);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.searchID).setVisible(false);
        menu.findItem(R.id.addPostID).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onPause() {
        super.onPause();

       // get time stamp
        String timestamp = String.valueOf(System.currentTimeMillis());
        //set online status with last seen
        checkOnlineStatus(timestamp);
        checkTypingStatus("noOne");

        userRefForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {

        checkOnlineStatus("online");

        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id==R.id.logoutID)
        {
            firebaseAuth.signOut();
            checkUserState();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        checkUserState();
        checkOnlineStatus("online");
        super.onStart();
    }
}
