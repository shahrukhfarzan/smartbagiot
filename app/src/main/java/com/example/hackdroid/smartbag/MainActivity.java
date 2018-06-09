package com.example.hackdroid.smartbag;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAssignedNumbers;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;
import java.util.UUID;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class MainActivity extends AppCompatActivity {
    private Button addItemBtn , bagBtn ,createRem ,btBagcon;
    private EditText selectTime;
    private ImageButton blutoothConnect;
    private int ghanta,min;
    private String notFound;
    private BluetoothAdapter myBluetooth = null;
    BluetoothAdapter myBluttoth=null;
    private ArrayList<Rfid> rfidList;
    String address = null , name=null;
    ListView devicelist;
    BluetoothDevice bluetoothDevice;
    BluetoothSPP bluetooth;
    // list of database
    BluetoothSocket bluetoothSocket;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference ,databaseReferenceBag;
    private Set<BluetoothDevice> pairedDevices;
    public static int SPLASH_SCREEN_TIME_OUT=1000;
    private  Toast toast;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    /*
    * int intArray[];    //declaring array
intArray = new int[20];
    * */
   public  int Arr[];
    public int Bag[];
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
            firebaseDatabase=FirebaseDatabase.getInstance();
            databaseReference =firebaseDatabase.getReference().child("logs");
            databaseReferenceBag=firebaseDatabase.getReference().child("Bag");
        createRem=(Button)findViewById(R.id.reminderCreation);
        addItemBtn=(Button)findViewById(R.id.addItemBtn);
        bagBtn = (Button)findViewById(R.id.goToBag);
        blutoothConnect=(ImageButton)findViewById(R.id.blutoothConnect);
        devicelist=(ListView)findViewById(R.id.piredList);
        selectTime=(EditText) findViewById(R.id.selectTime);


        bluetooth=new BluetoothSPP(this);
        btBagcon=(Button)findViewById(R.id.btBagcon);
        if (!bluetooth.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext(), "Bluetooth is not available", Toast.LENGTH_SHORT).show();
            finish();
        }

        bluetooth.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                btBagcon.setText("Connected to " + name);
            }

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            public void onDeviceDisconnected() {
                btBagcon.setText("Connection lost");
                NotificationManager notificationManager= (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                Intent notificationIntent=new Intent(MainActivity.this,MainActivity.class);
                PendingIntent pendingIntentnot=PendingIntent.getActivity(MainActivity.this,(int)System.currentTimeMillis(),notificationIntent,0);
                Notification notification;
                notification = new Notification.Builder(MainActivity.this)
                        .setContentTitle("Smart Bag")
                        .setContentText("Yor bag is Disconnected from the App please be in range")
                        .setSmallIcon(R.drawable.ic_add_shopping_cart_black_24dp)
                        .setContentIntent(pendingIntentnot)
                        .setAutoCancel(true)
                        .build();
                int dealay=100000;

                notificationManager.notify((int)System.currentTimeMillis()+dealay,notification);

            }

            public void onDeviceConnectionFailed() {
               btBagcon.setText("Unable to connect");
            }
        });

        //
       btBagcon.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if(bluetooth.getServiceState() == BluetoothState.STATE_CONNECTED){
                   bluetooth.disconnect();
               }
               else{
                   Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                   startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
               }
           }
       });
        blutoothConnect.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Turning On Blutooth",Toast.LENGTH_LONG).show();
                myBluetooth=BluetoothAdapter.getDefaultAdapter();
                if(myBluetooth==null)
                {
                    Toast.makeText(getApplicationContext(),"Your Device doesnot support Bluetooth",Toast.LENGTH_LONG).show();
                    finish();
                }
                else{
                    if(myBluetooth.isEnabled()){
                        pairedDevicesList();
                    }
                    else{
                        Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(turnBTon,1);
                      //  registerReceiver(mReceiver,turnBTon);
                    }

                }
            }
        });

        //

        selectTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar=Calendar.getInstance();
                final int hour=calendar.get(Calendar.HOUR_OF_DAY);
                int minute=calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog;
        timePickerDialog= new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                ghanta=hourOfDay;
                min=minute;
                selectTime.setText(""+hourOfDay+":"+minute);
            }
        },hour,minute,true);
        timePickerDialog.setTitle("Select Time");
        timePickerDialog.show();
            }
        });
        addItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // Intent gotoAddItem=new Intent(getApplicationContext(;
                Intent gotoAdd=new Intent(getApplicationContext(),AddItem.class);
                startActivity(gotoAdd);
            }
        });
        bagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoBag=new Intent(getApplicationContext() , BagActivity.class);

                startActivity(gotoBag);

            }
        });

        createRem.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                String data=selectTime.getText().toString();
                if(TextUtils.isEmpty(data))
                {
                    toast.makeText(getApplicationContext(), "Please select Time First",Toast.LENGTH_LONG).show();

                }
                else{
                    int n=Bag.length;
                    int m=Arr.length;
                    boolean check;

                    //Toast.makeText(getApplicationContext(),""+check,Toast.LENGTH_SHORT).show();

                    for(int i=0;i<n;i++)
                    {

                        check=isContain(Arr,Bag[i]);
                        // Toast.makeText(getApplicationContext(),""+check,Toast.LENGTH_SHORT).show();
                        if(check==false)
                        {
                            notFound="Item with value "+Bag[i]+"is not Present";
                        }
                    }

                    // int hour=Integer.parseInt(data);
                    //check whether the bag data and scan data is same or not
                    // fetch the card scan data and genearate the list of array

                    int hour=ghanta ;
                    int chotaGhnata=min;
                  //  Toast.makeText(getApplicationContext(), ""+hour+""+chotaGhnata , Toast.LENGTH_LONG).show();
                    Intent alarmIntent=new Intent(MainActivity.this, Alarm.class);
                    PendingIntent pendingIntent=PendingIntent.getBroadcast(MainActivity.this,0,alarmIntent,0);

                    AlarmManager alarmManager= (AlarmManager)getSystemService(ALARM_SERVICE);
                        //creating notification manager
                    NotificationManager notificationManager= (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                    Intent notificationIntent=new Intent(MainActivity.this,MainActivity.class);
                    PendingIntent pendingIntentnot=PendingIntent.getActivity(MainActivity.this,(int)System.currentTimeMillis(),notificationIntent,0);
                    Notification notification;
                    notification = new Notification.Builder(MainActivity.this)
                            .setContentTitle("Smart Bag")
                            .setContentText(""+notFound)
                            .setSmallIcon(R.drawable.ic_add_shopping_cart_black_24dp)
                            .setContentIntent(pendingIntentnot)
                            .setAutoCancel(true)
                            .build();
                    int dealay=100000;

                    notificationManager.notify((int)System.currentTimeMillis()+dealay,notification);

                    alarmManager.set(alarmManager.RTC, System.currentTimeMillis()+5*1000
                            ,pendingIntent);
                    //toast.makeText(getApplicationContext() , ""+hour,Toast.LENGTH_LONG).show();
                    //Toast.makeText(getApplicationContext(),""+System.currentTimeMillis(),Toast.LENGTH_LONG).show();
                }
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        if (!bluetooth.isBluetoothEnabled()) {
            bluetooth.enable();
        } else {
            if (!bluetooth.isServiceAvailable()) {
                bluetooth.setupService();
                bluetooth.startService(BluetoothState.DEVICE_OTHER);
            }
        }
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            if(dataSnapshot.exists())
            {   int i=0;
                Arr= new int[4];
                for(DataSnapshot post:dataSnapshot.getChildren())
                {
                    String Key=post.getKey();
                    //String Val=post.getValue();
                    String Val= (String) post.getValue();
                    try{
                        Arr[i]=Integer.parseInt(Val);
                    }catch (Exception e){
                       Toast.makeText(getApplicationContext(), ""+e,Toast.LENGTH_LONG).show();
                    }


                   // Toast.makeText(getApplicationContext(),""+Val,Toast.LENGTH_LONG).show();
                    i++;

                }


            }
            else{

            }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        databaseReferenceBag.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    int P=0;
                    Bag=new int[4];
                    for (DataSnapshot bag:dataSnapshot.getChildren())
                    {
                        try{
                                String BagValue= (String) bag.child("Value").getValue();
                                Bag[P]=Integer.parseInt(BagValue);
                           // Toast.makeText(getApplicationContext(),""+BagValue,Toast.LENGTH_SHORT).show();
                        }catch (Exception e)
                        {
                            Toast.makeText(getApplicationContext() , ""+e,Toast.LENGTH_SHORT).show();
                        }
                        P++;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetooth.stopService();
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bluetooth.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bluetooth.setupService();
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void pairedDevicesList() {
        pairedDevices=myBluetooth.getBondedDevices();
        ArrayList arrayList=new ArrayList();
        if(pairedDevices.size()>0)
        {
            //arrayList.add()
            for(BluetoothDevice bt :pairedDevices)
            {
                address=bt.getAddress().toString();
                name=bt.getName().toString();
       arrayList.add(bt.getName()+"\n"+ bt.getAddress());
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }
        try{

            //myBluttoth=BluetoothAdapter.getDefaultAdapter();
         bluetoothDevice=myBluetooth.getRemoteDevice(address);
           }catch (Exception e){}
      try{
          bluetoothSocket=bluetoothDevice.createInsecureRfcommSocketToServiceRecord(myUUID);
          bluetoothSocket.connect();
      }catch (Exception e)
      {

      }
    final ArrayAdapter arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,arrayList);
        devicelist.setAdapter(arrayAdapter);


    }
    public static boolean isContain(int[] arr, int item ) {

        for(int i=0;i<arr.length;i++)
        {
            if(arr[i]==item)
            {
                return  true;
            }

        }
        return  false;
    }


}
