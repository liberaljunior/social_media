package com.shariful.hello;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.jar.Attributes;

public class PostDetailActivity extends AppCompatActivity {

    String hisUid, myUid,myEmail,myName,myDp,postId,pLikes,hisDp,hisName,pImage;

    ImageView uPictureIv, pImageIv;
    TextView uNameTv, pTimeTv, pTittleTv, pDescriptionTv,pLikesTv,pCommentsTv;
    ImageButton moreBtn;
    Button likeBtn,shareButton;
    LinearLayout profileLayout;

    EditText commentEt;
    ImageButton sendBtn;
    ImageView cAvaterIv;

    ProgressDialog pd;

    boolean mProceessComment = false ;
    boolean mProceessLike = false ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        ActionBar actionBar =getSupportActionBar();
        actionBar.setTitle("Post Details");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //get intent data
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");


        uPictureIv = findViewById(R.id.uPictureIvId);
        pImageIv = findViewById(R.id.postImageIvID);
        uNameTv =findViewById(R.id.uNameTvID_post);
        pTimeTv = findViewById(R.id.pTimeTvID_post);
        pTittleTv =findViewById(R.id.pTittleTvID_post);
        pDescriptionTv = findViewById(R.id.descripotionTv_postID);
        pLikesTv = findViewById(R.id.pLikesTvID);
        pCommentsTv = findViewById(R.id.pCommentsTvID);


        moreBtn =findViewById(R.id.moreBtnID);
        likeBtn = findViewById(R.id.likeBtnId);
        shareButton =findViewById(R.id.shareBtnId);
        profileLayout = findViewById(R.id.profileLayoutID);
        commentEt =findViewById(R.id.commentEtId);
        sendBtn = findViewById(R.id.SendBtnID_c);

        cAvaterIv =findViewById(R.id.cAvaterIvId);


        loadPostInfo();
        checkUserStatus();
        loadUserInfo();
        setLikes();

        //set subtittle of actionbar
        actionBar.setSubtitle("Signed in As: "+myEmail);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            postComment();
            }
        });



        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost();
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOption();
            }
        });


    }

    private void showMoreOption() {


        @SuppressLint({"NewApi", "LocalSuppress"})
        final PopupMenu popupMenu = new PopupMenu(this,moreBtn, Gravity.END);
        //show delete option for only currently signed user
        if (hisUid.equals(myUid)){
            popupMenu.getMenu().add(Menu.NONE,0,0,"Delete");
            popupMenu.getMenu().add(Menu.NONE,1,0,"Edit Post");
        }

        popupMenu.getMenu().add(Menu.NONE,2,0,"View Details");




        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                int id= item.getItemId();
                if (id==0){
                    //delete is clicked
                    beginDelete();
                }
                else if (id==1){
                    //edit is clicked
                    Intent intent = new Intent(PostDetailActivity.this, AddPostctivity.class);
                    intent.putExtra("key","editPost");
                    intent.putExtra("editPostId",postId);
                    startActivity(intent);

                }
                else if (id==2){

                    //start post details activity
                    Intent intent = new Intent(PostDetailActivity.this, PostDetailActivity.class);
                    intent.putExtra("postId",postId);
                    startActivity(intent);
                }

                return false;
            }
        });
        popupMenu.show();

    }

    private void beginDelete() {

        if (pImage.equals("noImage")){
            //post without image
            deleteWithoutImage();

        }
        else{
            //post with image
            deleteWithImage();

        }

    }

    private void deleteWithImage() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting...");
        progressDialog.show();

        //steps:
        //1. delete image using url
        //2. delete image from database
        StorageReference picRef= FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //image deleted now delete from database
                        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
                        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds: dataSnapshot.getChildren()){

                                    ds.getRef().removeValue(); // remove value from firebase where pId matched

                                }
                                Toast.makeText(PostDetailActivity.this, "Post Deleted !", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });



                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                progressDialog.dismiss();
                Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void deleteWithoutImage() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting...");
        progressDialog.show();

        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    ds.getRef().removeValue(); // remove value from firebase where pId matched

                }
                progressDialog.dismiss();
                Toast.makeText(PostDetailActivity.this, "Post Deleted !", Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {


            }
        });

    }


    private void setLikes() {
        //when the details of the post is loaded, also check if current user if it like or not

        final DatabaseReference likesRef= FirebaseDatabase.getInstance().getReference().child("Likes");

        likesRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NewApi")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(postId).hasChild(myUid)) {
                   likeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_liked_blue,0,0,0);
                   likeBtn.setText("Liked");


                }
                else{
                    likeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_black,0,0,0);
                    likeBtn.setText("Like");

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void likePost() {


        mProceessLike = true;

       final DatabaseReference likesRef= FirebaseDatabase.getInstance().getReference().child("Likes");
        final DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("Posts");


        likesRef.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (mProceessLike){

                    if (dataSnapshot.child(postId).hasChild(myUid)){
                        //already liked..now remove like
                        postRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)-1));
                        likesRef.child(postId).child(myUid).removeValue();
                        mProceessLike=false;




                    }
                    else{
                        //not like, like it
                        postRef.child(postId).child("pLikes").setValue(""+(pLikes+1));
                        likesRef.child(postId).child(myUid).setValue("Liked");
                        mProceessLike=false;

                    }

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void postComment() {
     pd = new ProgressDialog(this);
     pd.setMessage("Adding Message...");

     //get data from comment edittex
        String comment = commentEt.getText().toString();
        if (TextUtils.isEmpty(comment)){
            Toast.makeText(this, "Please write something...", Toast.LENGTH_SHORT).show();
            return;
        }

        String timeStamp = String.valueOf(System.currentTimeMillis());

          DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");

        HashMap<String,Object>  hashMap = new HashMap<>();
        hashMap.put("cId",timeStamp);
        hashMap.put("comment",comment);
        hashMap.put("timeStamp",timeStamp);
        hashMap.put("uid",myUid);
        hashMap.put("uEmail",myEmail);
        hashMap.put("uDp",myDp);
        hashMap.put("uName",myName);

        //put this value
        ref.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                     //added
                        pd.dismiss();
                        Toast.makeText(PostDetailActivity.this, "Comment Added !", Toast.LENGTH_SHORT).show();
                        commentEt.setText("");
                        updateCommentCount();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });




    }




    private void updateCommentCount() {
           //whenever user add comment increase comment numbers like as like count
        mProceessComment = true;
      final DatabaseReference ref =  FirebaseDatabase.getInstance().getReference("Posts").child(postId);
       ref.addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
             if (mProceessComment){

                 String comment =""+ dataSnapshot.child("pComments").getValue();
                 int newCommentVal = Integer.parseInt(comment)+1;
                 ref.child("pComments").setValue(""+newCommentVal);
                 mProceessComment = false ;



             }

           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });




    }

    private void loadUserInfo() {
            //get current user info
        Query myRef = FirebaseDatabase.getInstance().getReference("User");
        myRef.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                   myName =""+ds.child("name").getValue();
                   myDp =""+ds.child("image").getValue();

                   try{
                       Picasso.get().load(myDp).placeholder(R.drawable.ic_default_img).into(cAvaterIv);

                   }catch (Exception e){
                          Picasso.get().load(R.drawable.ic_default_img).into(cAvaterIv);

                   }


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void loadPostInfo() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

        Query query =ref.orderByChild("pId").equalTo(postId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //keep checking until get required post
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    String pTittle = ""+ds.child("pTittle").getValue();
                    String pDescription = ""+ds.child("pDescription").getValue();
                    pLikes = ""+ds.child("pLikes").getValue();
                    String pTimeStamp = ""+ds.child("pTime").getValue();
                    pImage = ""+ds.child("pImage").getValue();
                    hisDp = ""+ds.child("uDp").getValue();
                    hisUid = ""+ds.child("uid").getValue();
                    String uEmail = ""+ds.child("uEmail").getValue();
                    hisName = ""+ds.child("uName").getValue();
                    String pCommentsCount  = ""+ds.child("pComments").getValue();



                    //convert time stamp to loacl time
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime = (String) DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

                    //set data
                    pTittleTv.setText(pTittle);
                    pDescriptionTv.setText(pDescription);
                    pLikesTv.setText(pLikes + "Likes");
                    pTimeTv.setText(pTime);
                    uNameTv.setText(hisName);
                    pCommentsTv.setText(pCommentsCount+" Comments");
                    

                    if (pImage.equals("noImage")){

                        //hide imageView
                        pImageIv.setVisibility(View.GONE);
                    }
                    else {

                       pImageIv.setVisibility(View.VISIBLE);

                        try{
                            Picasso.get().load(pImage).into(pImageIv);
                        }
                        catch (Exception e){


                        }

                    }

                    //set user image in comment part
                    try{
                        Picasso.get().load(hisDp).placeholder(R.drawable.ic_default_img).into(uPictureIv);
                    }
                    catch (Exception e){
                        Picasso.get().load(R.drawable.ic_default_img).into(uPictureIv);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {


            }
        });

    }

    private  void  checkUserStatus(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user!=null){
            //user is signed in
            myEmail =user.getEmail();
            myUid = user.getUid();

        }
        else{
            //is is not signed in , go to main activity
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);

        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.addPostID).setVisible(false);
        menu.findItem(R.id.searchID).setVisible(false);

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id==R.id.logoutID)
        {
            FirebaseAuth.getInstance().signOut();
            checkUserStatus();
        }

        return super.onOptionsItemSelected(item);

    }






}
