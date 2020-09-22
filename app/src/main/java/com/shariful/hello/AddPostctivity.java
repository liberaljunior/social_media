package com.shariful.hello;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.SuccessContinuation;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class AddPostctivity extends AppCompatActivity {

  ActionBar actionBar;
  FirebaseAuth firebaseAuth;

  EditText tittleEt,descriptionEt;
  Button uploadBtn;
  ImageView imageView;

  //user info
  String name,email,uid,dp;


  private  static  final  int CAMERA_REQUEST_CODE=100;
  private  static  final  int STORAGE_REQUEST_CODE=200;

  private  static  final  int IMAGE_PICK_CAMERA_CODE=300;
  private  static  final  int IMAGE_PICK_GALLERY_CODE=400;


  String cameraPermission[] ;
  String storagePermission[];

  Uri image_rui=null;

  ProgressDialog pd;

  DatabaseReference userDbRef;

  FirebaseUser user;

  String editTittle,editDescription,editImage;




  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add_postctivity);

    actionBar = getSupportActionBar();
    actionBar.setTitle(" Add New Post");
    //enable back button in actionbar
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setDisplayHomeAsUpEnabled(true);

    tittleEt= findViewById(R.id.pTittleEtID);
    descriptionEt = findViewById(R.id.pDescriptionEtID);
    uploadBtn = findViewById(R.id.pUploadBtnID);
    imageView = findViewById(R.id.pImageIvID);

    firebaseAuth = FirebaseAuth.getInstance();

    user=firebaseAuth.getCurrentUser();


    //get data from intent
    Intent intent= getIntent();
    final String isUpdateKey = ""+intent.getStringExtra("key");
    final String editPostId = ""+intent.getStringExtra("postId");


    if (isUpdateKey.equals("editPost")) {

      actionBar.setTitle("Update Post");
      uploadBtn.setText("Update");
      loadPostData(editPostId);
    }

    else{
      //
      actionBar.setTitle("Add New Post");
      uploadBtn.setText("Upload");

    }



    //actionBar.setTitle(email);


    pd = new ProgressDialog(this);

    //get some user info to include in post
    userDbRef = FirebaseDatabase.getInstance().getReference("User");


    Query query=userDbRef.orderByChild("email").equalTo(user.getEmail());
    query.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

        //check untill required data
        for (DataSnapshot ds:dataSnapshot.getChildren())
        {
          //get data
          name =""+ds.child("name").getValue();
          email =""+ds.child("email").getValue();
          dp =""+ds.child("image").getValue();  //profile pic



          try{
            //if image is recieved then set
            // Picasso.get().load(image).into(avaterIv);
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




    checkUserState();

    //init permission
    cameraPermission = new String[] {Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
    storagePermission = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};


    imageView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        showImagePicDialog();
      }
    });


    uploadBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        String tittle = tittleEt.getText().toString().trim();
        String description= descriptionEt.getText().toString().trim();


        if (TextUtils.isEmpty(tittle)){
          Toast.makeText(AddPostctivity.this, "Write tittle", Toast.LENGTH_SHORT).show();
          return;
        }
        if (TextUtils.isEmpty(description)){
          Toast.makeText(AddPostctivity.this, "Write Description !", Toast.LENGTH_SHORT).show();
          return;
        }

        if (isUpdateKey.equals("editPost")){

          beginUpdate(tittle,description,editPostId);
        }

        else
        {
          //with image
          uploadData(tittle,description);
        }




      }
    });


  }

  private void beginUpdate(String tittle, String description, String editPostId) {
    pd.setMessage("Updating Post...");
    pd.show();

    if (!editImage.equals("noImage")){
      //post was with image
      updateWasWithImage(tittle,description,editPostId);

    }
    else if(imageView.getDrawable()!=null){
      //post was not with image. but now update with image
      updateWithNowImage(tittle,description,editPostId);
    }
    else {
      //post was without image and now update without image
      updateWithoutImage(tittle,description,editPostId);

    }




  }

  private void updateWithoutImage(String tittle, String description, String editPostId) {

    //post without image
    HashMap<String, Object> hashMap = new HashMap<>();
    //put post info
    hashMap.put("uid",uid);
    hashMap.put("uName",name);
    hashMap.put("uEmail",email);
    hashMap.put("uDp",dp);
    hashMap.put("pTittle",tittle);
    hashMap.put("pDescription",description);
    hashMap.put("pImage","noImage");

    //path to store data
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
    //put data in this reference
    reference.child(editPostId).updateChildren(hashMap)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
              @Override
              public void onSuccess(Void aVoid) {
                pd.dismiss();
                Toast.makeText(AddPostctivity.this, "Post Updated !", Toast.LENGTH_SHORT).show();
                tittleEt.setText("");
                descriptionEt.setText("");
                imageView.setImageURI(null);
                image_rui=null;

              }
            }).addOnFailureListener(new OnFailureListener() {
      @Override
      public void onFailure(@NonNull Exception e) {
        pd.dismiss();

        Toast.makeText(AddPostctivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
      }
    });


  }

  private void updateWithNowImage(final String tittle, final String description, final String editPostId) {
    final String timeStamp = String.valueOf(System.currentTimeMillis());
    String filePathAndName = "Posts/" + "post_" + timeStamp;

    //get image from imageview
    Bitmap bitmap =((BitmapDrawable)imageView.getDrawable()).getBitmap();
    ByteArrayOutputStream baos= new ByteArrayOutputStream();
    //image compress
    bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
    byte[] data = baos.toByteArray();

    StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
    ref.putBytes(data)
            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
              @Override
              public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //image uploaded now get its url
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                String downloadUri =uriTask.getResult().toString();

                if (uriTask.isSuccessful()){
                  //url is received ..upload it to firebase database
                  HashMap<String, Object> hashMap = new HashMap<>();
                  //put post info
                  hashMap.put("uid",uid);
                  hashMap.put("uName",name);//............
                  hashMap.put("uEmail",email);
                  hashMap.put("uDp",dp);//.............
                  hashMap.put("pId",timeStamp);
                  hashMap.put("pTittle",tittle);
                  hashMap.put("pDescription",description);
                  hashMap.put("pImage",downloadUri);

                  //path to store data
                  DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
                  //put data in this reference
                  reference.child(editPostId).updateChildren(hashMap)
                          .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                              pd.dismiss();
                              Toast.makeText(AddPostctivity.this, "Post Updated !", Toast.LENGTH_SHORT).show();
                              tittleEt.setText("");
                              descriptionEt.setText("");
                              imageView.setImageURI(null);
                              image_rui=null;

                            }
                          }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                      pd.dismiss();

                      Toast.makeText(AddPostctivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                  });

                }



              }
            }).addOnFailureListener(new OnFailureListener() {
      @Override
      public void onFailure(@NonNull Exception e) {
        //image not uploaded
        pd.dismiss();
        Toast.makeText(AddPostctivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

      }
    });





  }

  private void updateWasWithImage(final String tittle, final String description, final String editPostId) {



    StorageReference mPictureRef= FirebaseStorage.getInstance().getReferenceFromUrl(editImage);

    mPictureRef.delete()
            .addOnSuccessListener(new OnSuccessListener<Void>() {
              @Override
              public void onSuccess(Void aVoid) {
                //previous  image deleted , now upload new image
                final String timeStamp = String.valueOf(System.currentTimeMillis());
                String filePathAndName = "Posts/" + "post_" + timeStamp;


                //get image from imageview
                Bitmap bitmap =((BitmapDrawable)imageView.getDrawable()).getBitmap();
                ByteArrayOutputStream baos= new ByteArrayOutputStream();
                //image compress
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                byte[] data = baos.toByteArray();

                StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
                ref.putBytes(data)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                          @Override
                          public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image uploaded now get its url

                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            String downloadUri =uriTask.getResult().toString();

                            if (uriTask.isSuccessful()){
                              //url is received ..upload it to firebase database
                              HashMap<String, Object> hashMap = new HashMap<>();
                              //put post info
                              hashMap.put("uid",uid);
                              hashMap.put("uName",name);//............
                              hashMap.put("uEmail",email);
                              hashMap.put("uDp",dp);//.............
                              hashMap.put("pId",timeStamp);
                              hashMap.put("pTittle",tittle);
                              hashMap.put("pDescription",description);
                              hashMap.put("pImage",downloadUri);

                              //path to store data
                              DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
                              //put data in this reference
                              reference.child(editPostId).updateChildren(hashMap)
                                      .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                          pd.dismiss();
                                          Toast.makeText(AddPostctivity.this, "Post Updated !", Toast.LENGTH_SHORT).show();
                                          tittleEt.setText("");
                                          descriptionEt.setText("");
                                          imageView.setImageURI(null);
                                          image_rui=null;

                                        }
                                      }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                  pd.dismiss();

                                  Toast.makeText(AddPostctivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                              });

                            }



                          }
                        }).addOnFailureListener(new OnFailureListener() {
                  @Override
                  public void onFailure(@NonNull Exception e) {
                    //image not uploaded
                    pd.dismiss();
                    Toast.makeText(AddPostctivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                  }
                });



              }
            }).addOnFailureListener(new OnFailureListener() {
      @Override
      public void onFailure(@NonNull Exception e) {
        Toast.makeText(AddPostctivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

      }
    });


  }




  private void loadPostData(String editPostId) {
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

    Query query = reference.orderByChild("pId").equalTo(editPostId);
    query.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        for (DataSnapshot ds: dataSnapshot.getChildren()){

          editTittle = ""+ds.child("pTittle").getValue();
          editDescription = ""+ds.child("pDescription").getValue();
          editImage = ""+ds.child("pImage").getValue();

          tittleEt.setText(editTittle);
          descriptionEt.setText(editDescription);


          if (!editImage.equals("noImage")){

            try{
              Picasso.get().load(editImage).into(imageView);

            }catch (Exception e){



            }

          }
        }

      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {

      }
    });


  }

  private void uploadData(final String tittle, final String description) {
    //write uploading code here
    pd.setMessage("Publishing post...");
    pd.show();

    final String timeStamp = String.valueOf(System.currentTimeMillis());
    String filePathAndName = "Posts/" + "post_" + timeStamp;

    //post with image
    if (imageView.getDrawable()!=null){

      //get image from imageview
      Bitmap bitmap =((BitmapDrawable)imageView.getDrawable()).getBitmap();
      ByteArrayOutputStream baos= new ByteArrayOutputStream();
      //image compress
      bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
      byte[] data = baos.toByteArray();

      //post with image
      StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
      ref.putBytes(data)
              .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                  //image is uploaded now get its uri
                  Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();

                  while (!uriTask.isSuccessful());

                  //  Uri downloadUri =uriTask.getResult();
                  String downloadUri =uriTask.getResult().toString();


                  if (uriTask.isSuccessful()){
                    //url is received ..upload it to firebase database
                    HashMap<String, Object> hashMap = new HashMap<>();
                    //put post info
                    hashMap.put("uid",uid);
                    hashMap.put("uName",name);//............
                    hashMap.put("uEmail",email);
                    hashMap.put("uDp",dp);//.............
                    hashMap.put("pId",timeStamp);
                    hashMap.put("pTittle",tittle);
                    hashMap.put("pDescription",description);
                    hashMap.put("pImage",downloadUri);
                    hashMap.put("pLikes","0");
                    hashMap.put("pTime",timeStamp);

                    //path to store data
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
                    //put data in this reference
                    reference.child(timeStamp).setValue(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                              @Override
                              public void onSuccess(Void aVoid) {
                                pd.dismiss();
                                Toast.makeText(AddPostctivity.this, "Post published !", Toast.LENGTH_SHORT).show();
                                tittleEt.setText("");
                                descriptionEt.setText("");
                                imageView.setImageURI(null);
                                image_rui=null;

                              }
                            }).addOnFailureListener(new OnFailureListener() {
                      @Override
                      public void onFailure(@NonNull Exception e) {
                        pd.dismiss();

                        Toast.makeText(AddPostctivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                      }
                    });

                  }


                }
              }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
          pd.dismiss();
          Toast.makeText(AddPostctivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
      });


    }

    else {

      //post without image
      HashMap<String, Object> hashMap = new HashMap<>();
      //put post info
      hashMap.put("uid",uid);
      hashMap.put("uName",name);
      hashMap.put("uEmail",email);
      hashMap.put("uDp",dp);
      hashMap.put("pId",timeStamp);
      hashMap.put("pTittle",tittle);
      hashMap.put("pDescription",description);
      hashMap.put("pImage","noImage");
      hashMap.put("pTime",timeStamp);

      //path to store data
      DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
      //put data in this reference
      reference.child(timeStamp).setValue(hashMap)
              .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                  pd.dismiss();
                  Toast.makeText(AddPostctivity.this, "Post Updated !", Toast.LENGTH_SHORT).show();
                  tittleEt.setText("");
                  descriptionEt.setText("");
                  imageView.setImageURI(null);
                  image_rui=null;

                }
              }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
          pd.dismiss();

          Toast.makeText(AddPostctivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
      });


    }
  }

  private void showImagePicDialog() {


    String option[]= {"Camera","Gallery"};
    //create aletdialouge
    AlertDialog.Builder builder= new AlertDialog.Builder(this);
    //set tittle
    builder.setTitle("Choose Image from");
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

  private boolean checkCameraPermission() {
    boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)
            ==(PackageManager.PERMISSION_GRANTED);

    boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
            ==(PackageManager.PERMISSION_GRANTED);

    return result && result1;

  }

  private  void requestCameraPermission() {

    ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_REQUEST_CODE);

  }

  private boolean checkStoragePermission() {
    boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
            ==(PackageManager.PERMISSION_GRANTED);

    return result;

  }

  private  void requestStoragePermission() {

    ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);

  }


  private void pickFromCamera() {
    ContentValues values= new ContentValues();
    values.put(MediaStore.Images.Media.TITLE,"Temp Pick");
    values.put(MediaStore.Images.Media.DESCRIPTION,"Temp Descr");
    image_rui = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_rui);
    startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);
  }

  private void pickFromGallery() {
    Intent galleryIntent = new Intent(Intent.ACTION_PICK);
    galleryIntent.setType("image/*");
    startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_CODE);
  }



  //handle permission
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    switch (requestCode)
    {
      case CAMERA_REQUEST_CODE:{
        if (grantResults.length>0)
        {
          boolean cameraAccepted= grantResults[0]==PackageManager.PERMISSION_GRANTED;
          boolean storageAccepted= grantResults[1]==PackageManager.PERMISSION_GRANTED;
          if (cameraAccepted && storageAccepted)
          {
            pickFromCamera();
          }
          else
          {
            Toast.makeText(this, "Please enable Camera & Storage permission", Toast.LENGTH_SHORT).show();
          }
        }
      }
      break;
      case  STORAGE_REQUEST_CODE:{
        if (grantResults.length>0)
        {
          boolean storageAccepted= grantResults[0]==PackageManager.PERMISSION_GRANTED;
          if (storageAccepted)
          {
            pickFromGallery();
          }
          else
          {
            Toast.makeText(this, "Please enable Storage permission", Toast.LENGTH_SHORT).show();
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
        image_rui=data.getData();
        //set image to imageView
        imageView.setImageURI(image_rui);

        // uploadProfileCoverPhoto(image_rui);
      }
      if (requestCode==IMAGE_PICK_CAMERA_CODE)
      {
        //set image to imageView
        imageView.setImageURI(image_rui);
        // uploadProfileCoverPhoto(image_rui);
      }

    }

    super.onActivityResult(requestCode, resultCode, data);
  }


  @Override
  protected void onStart() {
    super.onStart();
    checkUserState();
  }

  @Override
  protected void onResume() {
    super.onResume();
    checkUserState();
  }

  @Override
  public boolean onSupportNavigateUp() {
    onBackPressed();// go to previous activity

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
      email = firebaseUser.getEmail();
      uid = firebaseUser.getUid();


    }
    else
    {
      Intent intent = new Intent(this,MainActivity.class);
      startActivity(intent);
      finish();
    }
  }



}
