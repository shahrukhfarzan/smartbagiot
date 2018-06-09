package com.example.hackdroid.smartbag;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class BagActivity extends AppCompatActivity {
private DatabaseReference databaseReference;
private FirebaseDatabase firebaseDatabase;
private RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bag);
        setTitle("Your Bag");
        firebaseDatabase =FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference().child("Bag");
        recyclerView= (RecyclerView)findViewById(R.id.recycleViewBagList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter  <Bag , BagViewHolder>  bagAdapter = new FirebaseRecyclerAdapter<Bag, BagViewHolder>(
           Bag.class,
           R.layout.single_layout_bag,
           BagViewHolder.class,
           databaseReference
        ) {
            @Override
            protected void populateViewHolder(BagViewHolder viewHolder, Bag model, int position) {
                viewHolder.setName(model.getName());
                viewHolder.setImage(getApplication() , model.getImage());
            }
        };
        recyclerView.setAdapter(bagAdapter);
    }
    public static class BagViewHolder extends  RecyclerView.ViewHolder{
            View view;
        public BagViewHolder(View itemView) {

            super(itemView);
            view=itemView;
        }
        public void setName(String bagName){
            TextView name=(TextView)view.findViewById(R.id.bagName);
            name.setText(bagName);
        }
        public  void setImage(Context ctx, String bagImage){
            /*
            *  ImageView imageUrl=(ImageView)mView.findViewById(R.id.itemImage);
          Picasso.with(ctx).load(image).into(imageUrl);
            * */
            ImageView imageView=(ImageView)view.findViewById(R.id.bagImage);
            Picasso.with(ctx).load(bagImage).into(imageView);


        }
    }
}
