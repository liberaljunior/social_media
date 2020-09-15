package com.shariful.hello.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shariful.hello.ChatActivity;
import com.shariful.hello.R;
import com.shariful.hello.ThereProfileActivity;
import com.shariful.hello.models.ModelUser;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.Myholder> {

     Context context;
     List<ModelUser> userList;


    public AdapterUsers(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public Myholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //inflate layout(row_user.xml)
         View view = LayoutInflater.from(context).inflate(R.layout.row_users,viewGroup,false);
         return new Myholder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull Myholder holder, int position) {
        //get data

        final String hisUID =userList.get(position).getUid();
        String userImage =userList.get(position).getImage();
        String userName =userList.get(position).getName();
        final String userEmail=userList.get(position).getEmail();

        //set data
       holder.mNameTv.setText(userName);
       holder.mEmailTv.setText(userEmail);

       try{
           Picasso.get().load(userImage).placeholder(R.drawable.ic_default_img).into(holder.mAvaterIv);


       }
       catch (Exception e)
       {

       }

       //handle item click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Profile","Chat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (which==0){
                            //profile clicked
                            Intent intent = new Intent(context, ThereProfileActivity.class);
                            intent.putExtra("uid",hisUID);
                            context.startActivity(intent);

                        }
                        if (which==1){
                            //chat click
                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra("hisUid",hisUID);
                            context.startActivity(intent);

                        }

                    }
                });

            }
        });




    }

    @Override
    public int getItemCount() {

        return userList.size();
    }

    class Myholder extends RecyclerView.ViewHolder{

        ImageView mAvaterIv;
        TextView mNameTv,mEmailTv;




        public Myholder(@NonNull View itemView) {
            super(itemView);

            mAvaterIv = itemView.findViewById(R.id.avaterIv);
            mNameTv = itemView.findViewById(R.id.user_nameTV);
            mEmailTv = itemView.findViewById(R.id.user_emailTv);


        }
    }



}
