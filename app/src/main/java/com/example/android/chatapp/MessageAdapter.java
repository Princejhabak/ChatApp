package com.example.android.chatapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import static android.view.Gravity.END;

public class MessageAdapter extends ArrayAdapter<ChatMessages> {


    public MessageAdapter(Context context, int resource, List<ChatMessages> objects) {
        super(context, resource,objects);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.chat_item, parent, false);
        }

        LinearLayout linearLayoutTextOne ,linearLayoutImageOne ;

        linearLayoutTextOne = (LinearLayout)convertView.findViewById(R.id.linear_layout_one);
        linearLayoutImageOne = (LinearLayout)convertView.findViewById(R.id.linear_layout_image_one);



        ImageView photoImageView1 = (ImageView) convertView.findViewById(R.id.photoImageView);
        TextView messageTextView1 = (TextView) convertView.findViewById(R.id.chat_text_one);


        TextView messageTextTime1 = (TextView) convertView.findViewById(R.id.chat_time_one);
        TextView messageImageTime1 = (TextView) convertView.findViewById(R.id.image_time_one);


        TextView currentDate = (TextView) convertView.findViewById(R.id.current_day);

        ChatMessages message = getItem(position);

        boolean isPhoto = message.getPhotoUrl() != null;
        if (isPhoto) {


            currentDate.setVisibility(View.GONE);

            photoImageView1.setVisibility(View.VISIBLE);

            messageImageTime1.setVisibility(View.VISIBLE);

            linearLayoutImageOne.setVisibility(View.VISIBLE);

            linearLayoutTextOne.setVisibility(View.GONE);


            Glide.with(photoImageView1.getContext())
                    .load(message.getPhotoUrl())
                    .into(photoImageView1);
            messageImageTime1.setText(DateFormat.format("HH:mm",message.getMessageTime()));
        }

        else {

            messageTextView1.setVisibility(View.VISIBLE);
            messageTextTime1.setVisibility(View.VISIBLE);

            currentDate.setVisibility(View.GONE);

            linearLayoutImageOne.setVisibility(View.GONE);
            linearLayoutTextOne.setVisibility(View.VISIBLE);

          /*  FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();
            UserInformation userInformation = new UserInformation(user.getDisplayName(),user.getEmail());

            if(!(auth.getCurrentUser().getEmail().toString().equals(userInformation.getEmail().toString()))){

                GradientDrawable bgShape = (GradientDrawable) linearLayoutTextOne.getBackground();
                bgShape.setColor(Color.CYAN);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.END;

                linearLayoutTextOne.setLayoutParams(params);
            }
            else{

            }*/

            messageTextView1.setText(message.getMessageText());
            messageTextTime1.setText(DateFormat.format("HH:mm",message.getMessageTime()));
        }



        return convertView;
    }

}
