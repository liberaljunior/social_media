package com.shariful.hello;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.widget.SearchView;

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


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shariful.hello.adapters.AdapterUsers;
import com.shariful.hello.models.ModelUser;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends Fragment {

  RecyclerView recyclerView;
  AdapterUsers adapterUsers;
  List<ModelUser> userList;

  FirebaseAuth firebaseAuth;


  public UsersFragment() {
    // Required empty public constructor
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view =  inflater.inflate(R.layout.fragment_users, container, false);

    firebaseAuth=FirebaseAuth.getInstance();

    recyclerView = view.findViewById(R.id.user_recyclerViewID);
    recyclerView.setHasFixedSize(true);
    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


    //init userList
    userList = new ArrayList<>();
    //get All users
    getAllUsers();

    return view;
  }

  private void getAllUsers()
  {
    //get current user
    final FirebaseUser FUser = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference ref= FirebaseDatabase.getInstance().getReference("User");

    ref.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

        userList.clear();
        for (DataSnapshot ds: dataSnapshot.getChildren())
        {
          ModelUser modelUser =ds.getValue(ModelUser.class);
          if (!modelUser.getUid().equals(FUser.getUid()))
          {
            userList.add(modelUser);

          }
          //adapter
          adapterUsers = new AdapterUsers(getActivity(),userList);
          //set adapter
          recyclerView.setAdapter(adapterUsers);

        }

      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {

      }
    });

  }

  private void searhUsers(final String query)
  {

    final FirebaseUser FUser = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference ref= FirebaseDatabase.getInstance().getReference("User");

    ref.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

        userList.clear();
        for (DataSnapshot ds: dataSnapshot.getChildren())
        {
          ModelUser modelUser =ds.getValue(ModelUser.class);


          if (!modelUser.getUid().equals(FUser.getUid()))
          {
            if (modelUser.getName().toLowerCase().contains(query.toLowerCase())||modelUser.getEmail().toLowerCase().contains(query.toLowerCase()))
            {
              userList.add(modelUser);
            }


          }
          //adapter
          adapterUsers = new AdapterUsers(getActivity(),userList);
          adapterUsers.notifyDataSetChanged();
          //set adapter
          recyclerView.setAdapter(adapterUsers);

        }

      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {

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

    //hide add post icon from this fragment
    menu.findItem(R.id.addPostID).setVisible(false);



    MenuItem item = menu.findItem(R.id.searchID);
    SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
    //SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);


    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String s) {
        //called when user press search button
        if (!TextUtils.isEmpty(s.trim()))
        {
          searhUsers(s);
        }
        else
        {
          getAllUsers();
        }

        return false;
      }

      @Override
      public boolean onQueryTextChange(String s1) {
        //cal when user press any single latter
        //called when user press search button
        if (!TextUtils.isEmpty(s1.trim()))
        {
          searhUsers(s1);
        }
        else
        {
          getAllUsers();
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
