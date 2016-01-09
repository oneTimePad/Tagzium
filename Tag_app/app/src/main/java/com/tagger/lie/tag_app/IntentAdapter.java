package com.tagger.lie.tag_app;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class IntentAdapter extends BaseAdapter {

    ArrayList<String> namesGrid;
    ArrayList<Drawable> imagesGrid;
    //ArrayList<Intent> intentsGrid;
    Context context;

    private static LayoutInflater inflater = null;


    public IntentAdapter(UserEventsActivity activity,ArrayList<String> names,ArrayList<Drawable> images){
        namesGrid=names;
        imagesGrid=images;
        //intentsGrid=intents;
        context=activity;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount(){
       return namesGrid.size();
    }

    @Override
    public  Object getItem(int position){
        return position;
    }

    @Override
    public  long getItemId(int position){
        return position;
    }

    public class Holder{

        TextView tv;
        ImageView img;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){

        Holder holder = new Holder();
        View rowView;
            rowView = inflater.inflate(R.layout.get_image_grid,null);
            holder.tv = (TextView)rowView.findViewById(R.id.textView1);
            holder.img = (ImageView)rowView.findViewById(R.id.imageView1);
        holder.tv.setText(namesGrid.get(position));
        holder.img.setImageDrawable(imagesGrid.get(position));
        holder.tv.getLayoutParams().height=150;
        holder.tv.getLayoutParams().width=150;
        holder.img.getLayoutParams().height=150;
        holder.img.getLayoutParams().width=150;

        //holder.img.setFocusable(true);


        return rowView;
    }

}
