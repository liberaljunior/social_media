package com.shariful.hello;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
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
import com.shariful.hello.adapters.AdapterPosts;
import com.shariful.hello.models.ModelPost;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ThereProfileActivity extends AppCompatActivity {

    RecyclerView postRecyclerView;

    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;

    FirebaseAuth firebaseAuth;


    TextView nameTv,emailTv,phoneTv;
    ImageView avaterIv,coverIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profile);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //init views

        coverIv = findViewById(R.id.coverIvID);
        avaterIv= findViewById(R.id.avaterIvID);
        nameTv = findViewById(R.id.nameTvID);
        emailTv = findViewById(R.id.emailTvID);
        phoneTv= findViewById(R.id.phoneTvID);

        postRecyclerView = findViewById(R.id.recyclerView_postID);

        firebaseAuth = FirebaseAuth.getInstance();


        //get uid of clicked user to retrive his post
        Intent intent =getIntent();
        uid = intent.getStringExtra("uid");

          //
        Query query=FirebaseDatabase.getInstance().getReference("User").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //check untill required data
                for (DataSnapshot ds:dataSnapshot.getChildren())
                {
                    //get data
                    String name =""+ds.child("name").getValue();
                    String email =""+ds.child("email").getValue();
                    String phone =""+ds.child("phone").getValue();
                    String image =""+ds.child("image").getValue();
                    String cover =""+ds.child("cover").getValue();


                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);
                    try{
                        //if image is recieved then set
                        Picasso.get().load(image).into(avaterIv);
                    }
                    catch (Exception e)
                    {
                        //if image is recieved then set
                        Picasso.get().load(R.drawable.ic_default_face).into(avaterIv);
                    }

                    try{
                        //if image is recieved then set
                        Picasso.get().load(cover).into(coverIv);
                    }
                    catch (Exception e)
                    {
                        //if image is recieved then set
                        // Picasso.get().load(R.drawable.ic_default_face).into(avaterIv);
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //
        postList = new ArrayList<>();
        checkUserState();
        loadHisPosts();


    }

    private void loadHisPosts() {

        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //show newest post first
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerview
        postRecyclerView.setLayoutManager(layoutManager);

        //init post list
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");

        //query to load post
        Query query = ref.orderByChild("uid").equalTo(uid);
        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    //add to list
                    postList.add(myPosts);

                    //adapter
                    adapterPosts = new AdapterPosts(ThereProfileActivity.this,postList);
                    //set this adapter to recyclerview
                    postRecyclerView.setAdapter(adapterPosts);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(ThereProfileActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }
    private void searchHisPost(final String searchQuery){

        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //show newest post first
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerview
        postRecyclerView.setLayoutManager(layoutManager);

        //init post list
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");

        //query to load post
        Query query = ref.orderByChild("uid").equalTo(uid);
        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    if (myPosts.getpTittle().toLowerCase().contains(searchQuery.toLowerCase())){
                        //add to list
                        postList.add(myPosts);
                    }

                    //adapter
                    adapterPosts = new AdapterPosts(ThereProfileActivity.this,postList);
                    //set this adapter to recyclerview
                    postRecyclerView.setAdapter(adapterPosts);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(ThereProfileActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private  void checkUserState() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser!=null)
        {
            //Stay here
            // profileTv.setText(firebaseUser.getEmail());

        }
        else
        {
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
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


        MenuItem item = menu.findItem(R.id.searchID);

        SearchView searchView =(SearchView) MenuItemCompat.getActionView(item);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //called when click search button
                if (!TextUtils.isEmpty(query)){
                    searchHisPost(query);

                }else {
                     loadHisPosts();

                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //called when user type any character in searchbar
                if (!TextUtils.isEmpty(newText)){
                    searchHisPost(newText);

                }else {
                        loadHisPosts();

                }

                return false;
            }
        });


        return super.onCreateOptionsMenu(menu);
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



}
