package com.shariful.hello;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.shariful.hello.notifications.Token;

public class DashboardActivity extends AppCompatActivity {

  FirebaseAuth firebaseAuth;

  boolean doubleBackToExitPressedOnce = false;
  ActionBar actionBar;

  String mUID;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_dashboard);

    actionBar = getSupportActionBar();
    actionBar.setTitle("Profile");

    firebaseAuth=FirebaseAuth.getInstance();

    //bottom navigationview
    BottomNavigationView navigationView =findViewById(R.id.navigation_bottomID);

    navigationView.setOnNavigationItemSelectedListener(selectedListener);



    // updateToken(FirebaseInstanceId.getInstance().getToken());

    defaultFragment();

  }

  @Override
  protected void onResume() {
    checkUserState();
    super.onResume();
  }

  public  void  updateToken(String token){
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
    Token mToken = new Token(token);
    ref.child(mUID).setValue(mToken);


  }

  public void defaultFragment(){

    actionBar.setTitle("Home");
    HomeFragment fragment1 = new HomeFragment();
    FragmentTransaction ft1 =getSupportFragmentManager().beginTransaction();
    ft1.replace(R.id.content,fragment1,"");
    ft1.commit();

  }

  private BottomNavigationView.OnNavigationItemSelectedListener selectedListener=
          new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

              switch (menuItem.getItemId())
              {
                case R.id.nav_homeID:
                  //home fragment transaction
                  actionBar.setTitle("Home");
                  HomeFragment fragment1 = new HomeFragment();
                  FragmentTransaction ft1 =getSupportFragmentManager().beginTransaction();
                  ft1.replace(R.id.content,fragment1,"");
                  ft1.commit();

                  return true;
                case R.id.nav_profileID:
                  //profile fragment transaction
                  actionBar.setTitle("Profile");
                  ProfileFragment fragment2 = new ProfileFragment();
                  FragmentTransaction ft2 =getSupportFragmentManager().beginTransaction();
                  ft2.replace(R.id.content,fragment2,"");
                  ft2.commit();
                  return true;

                case R.id.nav_userID:
                  //home fragment transaction
                  actionBar.setTitle("User");
                  UsersFragment fragment3 = new UsersFragment();
                  FragmentTransaction ft3 =getSupportFragmentManager().beginTransaction();
                  ft3.replace(R.id.content,fragment3,"");
                  ft3.commit();
                  return true;

                case R.id.nav_chatID:
                  //home fragment transaction
                  actionBar.setTitle("Chat");
                  ChatListFragment fragment4 = new ChatListFragment();
                  FragmentTransaction ft4 =getSupportFragmentManager().beginTransaction();
                  ft4.replace(R.id.content,fragment4,"");
                  ft4.commit();
                  return true;
              }

              return false;
            }
          };




  private  void checkUserState()
  {
    FirebaseUser user = firebaseAuth.getCurrentUser();

    if (user!=null)
    {
      //Stay here
      // profileTv.setText(firebaseUser.getEmail());
      mUID= user.getUid();
      SharedPreferences sp = getSharedPreferences("SP_USER" ,MODE_PRIVATE);
      SharedPreferences.Editor editor = sp.edit();
      editor.putString("CURRENT_USERID",mUID);
      editor.apply();



    }
    else
    {
      startActivity(new Intent(DashboardActivity.this,MainActivity.class));
    }
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    finish();
  }

  @Override
  protected void onStart() {
    checkUserState();
    super.onStart();
  }



}
