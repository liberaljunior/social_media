package com.shariful.hello;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.ActionProvider;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shariful.hello.adapters.AdapterPosts;
import com.shariful.hello.models.ModelPost;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

  FirebaseAuth firebaseAuth;

  RecyclerView recyclerView;
  List<ModelPost> postList ;
  AdapterPosts adapterPosts;

  ImageButton moreBtn1;


  public HomeFragment() {
    // Required empty public constructor
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view= inflater.inflate(R.layout.fragment_home, container, false);
    firebaseAuth= FirebaseAuth.getInstance();

    //recycler view and its properties
    recyclerView = view.findViewById(R.id.postRecyclerViewID);
    moreBtn1 = view.findViewById(R.id.moreBtnID);

    LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
    layoutManager.setStackFromEnd(true);
    layoutManager.setReverseLayout(true);

    //set layout to recucler view
    recyclerView.setLayoutManager(layoutManager);


    //init post list
    postList = new ArrayList<>();
    loadPost();

    return  view;
  }

  private void loadPost() {
    //path of all post
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
    //get all data from this ref
    ref.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        postList.clear();
        for (DataSnapshot ds: dataSnapshot.getChildren())
        {
          ModelPost modelPost = ds.getValue(ModelPost.class);
          postList.add(modelPost);

          //adapter
          adapterPosts = new AdapterPosts(getActivity(),postList);
          //set adapter
          recyclerView.setAdapter(adapterPosts);
        }

      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {

        Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
      }
    });

  }

  private  void  searchPost(final String searchQuery){

    //path of all post
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
    //get all data from this ref
    ref.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        postList.clear();
        for (DataSnapshot ds: dataSnapshot.getChildren())
        {
          ModelPost modelPost = ds.getValue(ModelPost.class);

          if (modelPost.getpTittle().toLowerCase().contains(searchQuery.toLowerCase())){

            postList.add(modelPost);

          }

          //adapter
          adapterPosts = new AdapterPosts(getActivity(),postList);
          //set adapter
          recyclerView.setAdapter(adapterPosts);
        }

      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {

        Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
      }
    });



  }



  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    setHasOptionsMenu(true);
    super.onCreate(savedInstanceState);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.menu_main,menu);
    //search menu
    MenuItem item = menu.findItem(R.id.searchID);
    SearchView searchView = (SearchView)MenuItemCompat.getActionView(item);

    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String s) {
        //called when user press search button
        if (!TextUtils.isEmpty(s)){

          searchPost(s);
        }
        else
        {
          loadPost();
        }


        return false;
      }

      @Override
      public boolean onQueryTextChange(String s) {
        //call when user write text
        if (!TextUtils.isEmpty(s)){

          searchPost(s);
        }
        else
        {
          loadPost();
        }

        return false;
      }
    });


    super.onCreateOptionsMenu(menu,inflater);
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    int id = item.getItemId();

    if (id==R.id.logoutID)
    {
      firebaseAuth.signOut();
      checkUserState();
    }
    if (id==R.id.addPostID)
    {
      startActivity(new Intent(getActivity(),AddPostctivity.class));
    }

    return super.onOptionsItemSelected(item);
  }

  private  void checkUserState()
  {
    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

    if (firebaseUser!=null)
    {
      //Stay here
      // profileTv.setText(firebaseUser.getEmail());
    }
    else
    {
      Intent intent = new Intent(getActivity(),MainActivity.class);
      startActivity(intent);
      getActivity().finish();
    }
  }


}
