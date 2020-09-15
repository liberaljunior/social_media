package com.shariful.hello;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.shariful.hello.adapters.AdapterPosts;
import com.shariful.hello.models.ModelPost;
import com.squareup.picasso.Picasso;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    //firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    //views from xml
    TextView nameTv,emailTv,phoneTv;
    ImageView avaterIv,coverIv;

    FloatingActionButton fab;
    ProgressDialog pd;

    private  static  final  int CAMERA_REQUEST_CODE=100;
    private  static  final  int STORAGE_REQUEST_CODE=200;
    private  static  final  int IMAGE_PICK_GALLERY_CODE=300;
    private  static  final  int IMAGE_PICK_CAMERA_CODE=400;

    String cameraPermission[] ;
    String storagePermission[];

    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;


    RecyclerView postRecyclerView;


    Uri image_uri;
    String profileOrCoverPhoto;
    StorageReference storageReference;
    String storagePath= "User_Profile_Cover/Imgs/";



    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)  {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_profile, container, false);

        //init firebase
        firebaseAuth=FirebaseAuth.getInstance();
        user=firebaseAuth.getCurrentUser();
        firebaseDatabase =FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("User");
        storageReference =getInstance().getReference();

        //init permission
        cameraPermission = new String[] {Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};


        //init views

        coverIv = view.findViewById(R.id.coverIvID);
        avaterIv= view.findViewById(R.id.avaterIvID);
        nameTv =view.findViewById(R.id.nameTvID);
        emailTv = view.findViewById(R.id.emailTvID);
        phoneTv=view.findViewById(R.id.phoneTvID);

        fab= view.findViewById(R.id.fabID);
        postRecyclerView = view.findViewById(R.id.recyclerView_postID);

        //init progress dialogue
        pd= new ProgressDialog(getActivity());



        Query query=databaseReference.orderByChild("email").equalTo(user.getEmail());
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


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialogue();

            }
        });


        postList = new ArrayList<>();
        checkUserState();
        loadMyPost();

        return view;
    }

    private void loadMyPost() {

        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
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
                    adapterPosts = new AdapterPosts(getActivity(),postList);
                    //set this adapter to recyclerview
                    postRecyclerView.setAdapter(adapterPosts);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void searchMyPost(final String searchQuery) {

        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
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
                    adapterPosts = new AdapterPosts(getActivity(),postList);
                    //set this adapter to recyclerview
                    postRecyclerView.setAdapter(adapterPosts);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);

        return result;

    }

    private  void requestStoragePermission() {

       requestPermissions(storagePermission,STORAGE_REQUEST_CODE);

    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA)
                ==(PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);

        return result && result1;

    }

    private  void requestCameraPermission() {

        requestPermissions(cameraPermission,CAMERA_REQUEST_CODE);

    }


    private void showEditProfileDialogue() {
        //option to show in dialog
        String option[]= {"Edit Profile Picture","Edit Cover Photo","Edit Name","Edit Phone"};

        //create aletdialouge
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
        //set tittle
        builder.setTitle("Choose Action");
        //set iteems to dialogue
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which==0) {
                   //edit profile click
                    pd.setMessage("Updating Profile Picture");
                    profileOrCoverPhoto = "image";
                    showImPicDialog();

                }
                else if(which==1)
                {
                    //edit cover click
                    pd.setMessage("Updating cover photo");
                    profileOrCoverPhoto = "cover";
                    showImPicDialog();   //38 min

                }
                else if (which==2)
                {
                    //edit name click
                    pd.setMessage("Updating Name");
                    //calling method and pass key name
                    showNamePhoneUdateDialog("name");
                    

                }
                else if (which==3)
                {
                    //edit phone click
                    pd.setMessage("Updating Phone");
                    showNamePhoneUdateDialog("phone");

                }

            }
        });
        //create and show dialogue
        builder.create().show();

    }

    private void showNamePhoneUdateDialog(final String key) {
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Upadte "+ key); //e.g update name or update phone
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(linearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        final EditText editText = new EditText(getActivity());
        editText.setHint("Enter "+key);
        linearLayout.addView(editText);
        builder.setView(linearLayout);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final String value =editText.getText().toString().trim();
                if (!TextUtils.isEmpty(value))
                {
                    pd.show();
                    HashMap<String, Object> result =new HashMap<>();
                    result.put(key,value);
                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Updated ...", Toast.LENGTH_SHORT).show();


                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
                     // if user edit his name also change it from his post
                    if (key.equals("name")){
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        Query query =ref.orderByChild("uid").equalTo(uid);

                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds: dataSnapshot.getChildren()){
                                    String child = ds.getKey();
                                    dataSnapshot.getRef().child(child).child("uName").setValue(value);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        //update name in current user's comment on post
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                for (DataSnapshot ds: dataSnapshot.getChildren()){
                                    String child =ds.getKey();
                                    if (dataSnapshot.child(child).hasChild("Comments")){
                                        String child1= ""+dataSnapshot.child(child).getKey();
                                        Query child2 =FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                        child2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                for (DataSnapshot ds: dataSnapshot.getChildren()){
                                                      String child = ds.getKey();
                                                      dataSnapshot.getRef().child(child).child("uName").setValue(value);


                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }


                }
                else
                {
                    Toast.makeText(getActivity(), "Please Enter "+key, Toast.LENGTH_SHORT).show();
                }

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pd.dismiss();

            }
        });
        builder.create().show();


    }


    private void showImPicDialog() {

        String option[]= {"Camera","Gallery"};
        //create aletdialouge
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
        //set tittle
        builder.setTitle("Pick Image from");
        //set iteems to dialogue
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which==0)
                {
                    //camera click
                    if (!checkCameraPermission())
                    {
                        requestCameraPermission();
                    }
                    else
                    {
                        pickFromCamera();
                    }


                }
                else if(which==1)
                {
                    //gallery click
                    if (!checkStoragePermission())
                    {
                        requestStoragePermission();
                    }
                    else
                    {
                        pickFromGallery();
                    }


                }


            }
        });
        //create and show dialogue
        builder.create().show();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode)
        {
            case CAMERA_REQUEST_CODE:{
               if (grantResults.length>0)
               {
                   boolean cameraAccepted= grantResults[0]==PackageManager.PERMISSION_GRANTED;
                   boolean writeStorageAccepted= grantResults[1]==PackageManager.PERMISSION_GRANTED;
                   if (cameraAccepted && writeStorageAccepted)
                   {
                       pickFromCamera();
                   }
                   else
                   {
                       Toast.makeText(getActivity(), "Please enable Camera & Storage permission", Toast.LENGTH_SHORT).show();
                   }
               }
            }
            break;
            case  STORAGE_REQUEST_CODE:{
                if (grantResults.length>0)
                {
                    boolean writeStorageAccepted= grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted)
                    {
                        pickFromGallery();
                    }
                    else
                    {
                        Toast.makeText(getActivity(), "Please enable Storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode==RESULT_OK)
        {
            if (requestCode==IMAGE_PICK_GALLERY_CODE)
            {
                image_uri=data.getData();
                uploadProfileCoverPhoto(image_uri);
            }
            if (requestCode==IMAGE_PICK_CAMERA_CODE)
            {
                uploadProfileCoverPhoto(image_uri);
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(Uri uri) {
        pd.show();

         String filePathAndName = storagePath+""+profileOrCoverPhoto+"_"+user.getUid();

         StorageReference storageReference2nd= storageReference.child(filePathAndName);
         storageReference2nd.putFile(uri)
                 .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                     @Override
                     public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                         Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();

                         while (!uriTask.isSuccessful());

                         final Uri downloadUri =uriTask.getResult();

                         //check if image is uploaded or not and uri recieved
                         if (uriTask.isSuccessful())
                         {
                             //image uploaded
                             //add update uri in user database
                             HashMap<String, Object> results= new HashMap<>();
                             results.put(profileOrCoverPhoto,downloadUri.toString());
                             databaseReference.child(user.getUid()).updateChildren(results)
                                     .addOnSuccessListener(new OnSuccessListener<Void>() {
                                         @Override
                                         public void onSuccess(Void aVoid) {

                                             pd.dismiss();
                                             Toast.makeText(getActivity(), "Image Updated ...", Toast.LENGTH_SHORT).show();

                                         }
                                     }).addOnFailureListener(new OnFailureListener() {
                                 @Override
                                 public void onFailure(@NonNull Exception e) {

                                     pd.dismiss();
                                     Toast.makeText(getActivity(), "Error updating Image..!", Toast.LENGTH_SHORT).show();

                                 }
                             });

                             // if user edit his name also change it from his post
                             if (profileOrCoverPhoto.equals("image")){
                                 DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                 Query query =ref.orderByChild("uid").equalTo(uid);

                                 query.addValueEventListener(new ValueEventListener() {
                                     @Override
                                     public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                         for (DataSnapshot ds: dataSnapshot.getChildren()){
                                             String child = ds.getKey();
                                             dataSnapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                         }
                                     }

                                     @Override
                                     public void onCancelled(@NonNull DatabaseError databaseError) {

                                     }
                                 });

                                 //update user image in current user in post comment
                                 ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                     @Override
                                     public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                         for (DataSnapshot ds: dataSnapshot.getChildren()){
                                             String child =ds.getKey();
                                             if (dataSnapshot.child(child).hasChild("Comments")){
                                                 String child1= ""+dataSnapshot.child(child).getKey();
                                                 Query child2 =FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                                 child2.addValueEventListener(new ValueEventListener() {
                                                     @Override
                                                     public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                         for (DataSnapshot ds: dataSnapshot.getChildren()){
                                                             String child = ds.getKey();
                                                             dataSnapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());


                                                         }
                                                     }

                                                     @Override
                                                     public void onCancelled(@NonNull DatabaseError databaseError) {

                                                     }
                                                 });
                                             }
                                         }
                                     }

                                     @Override
                                     public void onCancelled(@NonNull DatabaseError databaseError) {

                                     }
                                 });


                             }


                         }
                         else
                         {
                             //error message
                             pd.dismiss();
                             Toast.makeText(getActivity(), "Some error occured !!", Toast.LENGTH_SHORT).show();
                         }


                     }
                 }).addOnFailureListener(new OnFailureListener() {
             @Override
             public void onFailure(@NonNull Exception e) {
                 pd.dismiss();
                 Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

             }
         });


    }




    private void pickFromCamera()
    {
        ContentValues values= new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);



    }

    private void pickFromGallery()
    {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_CODE);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_main,menu);

        MenuItem item = menu.findItem(R.id.searchID);

        SearchView searchView =(SearchView) MenuItemCompat.getActionView(item);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //called when click search button
                if (!TextUtils.isEmpty(query)){
                    searchMyPost(query);

                }else {
                       loadMyPost();

                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //called when user type any character in searchbar
                if (!TextUtils.isEmpty(newText)){
                    searchMyPost(newText);

                }else {

                        loadMyPost();
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



    private  void checkUserState() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser!=null)
        {
            //Stay here
            // profileTv.setText(firebaseUser.getEmail());
              uid = user.getUid();

        }
        else
        {
            Intent intent = new Intent(getActivity(),MainActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
    }




}




