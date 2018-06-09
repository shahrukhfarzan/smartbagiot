package com.example.hackdroid.smartbag;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class AddItem extends AppCompatActivity {
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private static DatabaseReference databaseReferenceBag;
    private RecyclerView recycleViewItemList;
    private  boolean mProcessState=false;
    Button bag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        setTitle("Add item Into Your Bag");
        //connecting with Firebase
        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference().child("Rfid");
        databaseReferenceBag =firebaseDatabase.getReference().child("Bag");
        recycleViewItemList=(RecyclerView)findViewById(R.id.recycleViewItemList);
        recycleViewItemList.setHasFixedSize(true);
        recycleViewItemList.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Item,ItemViewHolder> firebaseRecyclerAdapter= new FirebaseRecyclerAdapter<Item, ItemViewHolder>(
                Item.class,
                R.layout.single_row,
                ItemViewHolder.class,
                databaseReference
        ) {
            @Override
            protected void populateViewHolder(ItemViewHolder viewHolder, final Item model, int position) {
               final String ProductKey=getRef(position).toString();
               final String val=model.getValue();
                viewHolder.setName(model.getName());
                viewHolder.setValue(model.getValue());
                viewHolder.setImage(getApplication(), model.getImage());
               viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       Toast.makeText(getApplicationContext(),ProductKey,Toast.LENGTH_LONG).show();
                   }
               });
               //viewHolder.
                viewHolder.addToBag.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mProcessState=true;

                       // Toast.makeText(getApplicationContext(), ""+ProductKey,Toast.LENGTH_LONG).show();
                        databaseReferenceBag.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(mProcessState == true)
                                        {
                                            databaseReferenceBag.child(""+model.getName()).child("Value").setValue(model.getValue());
                                            databaseReferenceBag.child(""+model.getName()).child("Image").setValue(model.getImage());
                                            databaseReferenceBag.child(""+model.getName()).child("Name").setValue(model.getName());
                                            mProcessState =false;
                                        }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }
        };
        recycleViewItemList.setAdapter(firebaseRecyclerAdapter);
    }
    public static class ItemViewHolder extends RecyclerView.ViewHolder{
        View mView;
        Button addToBag;

        public ItemViewHolder(View itemView) {

            super(itemView);
            mView=itemView;
            addToBag=(Button)mView.findViewById(R.id.addToBag);

        }
        public void setName(String name){
            TextView PostName=(TextView)mView.findViewById(R.id.itemName);
            PostName.setText(name);
        }
        public void setValue(String Value)
        {
            TextView PostValue=(TextView)mView.findViewById(R.id.itemValue);
            PostValue.setText(Value);
        }
        public void setImage(Context ctx, String image)
        {
            ImageView imageUrl=(ImageView)mView.findViewById(R.id.itemImage);
          Picasso.with(ctx).load(image).into(imageUrl);
        }
    }
}
