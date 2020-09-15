package com.shariful.hello.adapters;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.shariful.hello.AddPostctivity;
import com.shariful.hello.PostDetailActivity;
import com.shariful.hello.R;
import com.shariful.hello.ThereProfileActivity;
import com.shariful.hello.models.ModelPost;
import com.squareup.picasso.Picasso;


import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder>  {
    Context context;
    List<ModelPost> postList;

    String myUid;

    private DatabaseReference likeRef; //refrence of like
    private  DatabaseReference postRef; //reference of post

    boolean mProcessLike=false;


    String id1,id2,postId2,postImage;



    public AdapterPosts(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;

        myUid= FirebaseAuth.getInstance().getCurrentUser().getUid();
        likeRef= FirebaseDatabase.getInstance().getReference().child("Likes");
        postRef= FirebaseDatabase.getInstance().getReference().child("Posts");



    }


    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
         //inflate row_post.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts,parent,false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {
        //get data
        final String uid =postList.get(position).getUid();
        String uEmail =postList.get(position).getuEmail();
        String uName =postList.get(position).getuName();
        String uDp =postList.get(position).getuDp();//profilepic
        final String pId =postList.get(position).getpId();
        String pTittle =postList.get(position).getpTittle();
        String pDescription =postList.get(position).getpDescription();
        final String pImage =postList.get(position).getpImage();
        String pTimeStamp =postList.get(position).getpTime();

        String pLikes =postList.get(position).getpLikes();
        String pComments =postList.get(position).getpComments();



        //convert time stamp to loacl time
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = (String) DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

        //set data
        holder.uNameTv.setText(uName);
        holder.pTimeTv.setText(pTime);
        holder.pTittleTv.setText(pTittle);
        holder.pDescriptionTv.setText(pDescription);
        holder.pLikesTv.setText(pLikes+" Likes");
        holder.pCommentsTv.setText(pComments+" Comments");

        //set likes for each post
        setLikes(holder,pId);


        // set user dp
        try{
            Picasso.get().load(uDp).placeholder(R.drawable.ic_default_img).into(holder.uPictureIv);
        }
        catch (Exception e){

        }

        // if there is no image in post then hide imageView
       if (pImage.equals("noImage")){

            //hide imageView
          holder.pImageIv.setVisibility(View.GONE);
       }
       else {

           holder.pImageIv.setVisibility(View.VISIBLE);

            try{
                Picasso.get().load(pImage).into(holder.pImageIv);
            }
            catch (Exception e){


            }

       }




        //handle button click event
     holder.moreBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

              // showPopup(holder.moreBtn,uid,myUid,pId,pImage); //for alternative deleteing method
                showMoreOption(holder.moreBtn,uid,myUid,pId,pImage);

            }
        });

        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final int pLikes= Integer.parseInt(postList.get(position).getpLikes());
                mProcessLike = true;

                final String postId =postList.get(position).getpId();

                likeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (mProcessLike){

                             if (dataSnapshot.child(postId).hasChild(myUid)){
                               //already liked..now remove like
                                 postRef.child(postId).child("pLikes").setValue(""+(pLikes-1));
                                 likeRef.child(postId).child(myUid).removeValue();
                                 mProcessLike=false;


                             }
                             else{
                                   //not like, like it
                                 postRef.child(postId).child("pLikes").setValue(""+(pLikes+1));
                                 likeRef.child(postId).child(myUid).setValue("Liked");
                                 mProcessLike=false;

                             }

                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }
        });

        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //start post details activity
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId",pId);
                context.startActivity(intent);


            }
        });

        holder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //will implent latter
                Toast.makeText(context, "share...coming soon......", Toast.LENGTH_SHORT).show();
            }
        });

        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, ThereProfileActivity.class);
                intent.putExtra("uid",uid);
                context.startActivity(intent);

            }
        });



    }

    private void setLikes(final MyHolder holder, final String postKey) {
        likeRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NewApi")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(postKey).hasChild(myUid)) {
                    holder.likeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_liked_blue,0,0,0);
                    holder.likeBtn.setText("Liked");


                }
                else{
                    holder.likeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_black,0,0,0);
                    holder.likeBtn.setText("Like");

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    private void showMoreOption(ImageButton moreBtn, String uid, String myUid, final String pId, final String pImage) {

        @SuppressLint({"NewApi", "LocalSuppress"})
        final PopupMenu popupMenu = new PopupMenu(context,moreBtn, Gravity.END);
        //show delete option for only currently signed user
        if (uid.equals(myUid)){
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
                    beginDelete(pId,pImage);
                }
               else if (id==1){
                   //edit is clicked
                    Intent intent = new Intent(context, AddPostctivity.class);
                    intent.putExtra("key","editPost");
                    intent.putExtra("postId",pId);
                    context.startActivity(intent);

                }
               else if (id==2){

                    //start post details activity
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("postId",pId);
                    context.startActivity(intent);
                }

                return false;
            }
        });
        popupMenu.show();
    }


    private void beginDelete(String pId, String pImage) {

            if (pImage.equals("noImage")){
                //post without image
                deleteWithoutImage(pId);

            }
            else{
                //post with image
                deleteWithImage(pId,pImage);

            }


    }



    private void deleteWithImage(final String pId, String pImage) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
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
                        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds: dataSnapshot.getChildren()){

                                    ds.getRef().removeValue(); // remove value from firebase where pId matched

                                }
                                Toast.makeText(context, "Post Deleted !", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });


    }

    private void deleteWithoutImage(String pId) {

        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting...");
        progressDialog.show();

        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    ds.getRef().removeValue(); // remove value from firebase where pId matched

                }
                progressDialog.dismiss();
                Toast.makeText(context, "Post Deleted !", Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {


            }
        });

    }


    @Override
    public int getItemCount() {
        return postList.size();
    }




    class  MyHolder extends RecyclerView.ViewHolder{
    //views from row_post.xml
    ImageView uPictureIv,pImageIv;
    TextView uNameTv,pTimeTv,pTittleTv,pDescriptionTv,pLikesTv,pCommentsTv;
    ImageButton moreBtn;

    Button likeBtn,commentBtn,shareBtn;

    LinearLayout profileLayout;


    public MyHolder(@NonNull View itemView) {
        super(itemView);
        //init views
      uPictureIv=  itemView.findViewById(R.id.uPictureIvId);
      pImageIv = itemView.findViewById(R.id.postImageIvID);
      uNameTv = itemView.findViewById(R.id.uNameTvID_post);
      pTimeTv = itemView.findViewById(R.id.pTimeTvID_post);
      pTittleTv = itemView.findViewById(R.id.pTittleTvID_post);
      pDescriptionTv = itemView.findViewById(R.id.descripotionTv_postID); //..............
      pLikesTv = itemView.findViewById(R.id.pLikesTvID);
      pCommentsTv = itemView.findViewById(R.id.pCommentsTvID);
      moreBtn= itemView.findViewById(R.id.moreBtnID);
      likeBtn =itemView.findViewById(R.id.likeBtnId);
      commentBtn =itemView.findViewById(R.id.commentBtnID);
      shareBtn =itemView.findViewById(R.id.shareBtnId);

      profileLayout= itemView.findViewById(R.id.profileLayoutID);

    }


  }


}
