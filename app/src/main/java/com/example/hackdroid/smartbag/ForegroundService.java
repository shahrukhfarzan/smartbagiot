package com.example.hackdroid.smartbag;



//Import needed files

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.constraint.solver.widgets.Helper;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.example.hackdroid.smartbag.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

//Service class
public class ForegroundService extends Service {
    int FOREGROUND_ID = 1997;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter BA;
    private BluetoothSocket BS;
    private StringBuilder sb = new StringBuilder();
    private Toast toast;
    private String mac;
    private Handler h;
    private Helper helper;


    //When the service is created
    //Doesn't run again unless service is totally killed
    public void onCreate() {
        //Set service as foreground service
        startForeground(FOREGROUND_ID, buildForegroundNotification());
        toast = Toast.makeText(getApplicationContext(), "Connecting to bag...", Toast.LENGTH_SHORT);
        toast.show();
        helper=new Helper();

        //Get bluetooth adapter and set it to BA
        BA = BluetoothAdapter.getDefaultAdapter();

        //Create a handler to listen for data
        //New threads send data to this handler
        //If it receives data, show it in a toast
        h = new Handler() {
            public void handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                toast = Toast.makeText(getApplicationContext(), bundle.getString("str"), Toast.LENGTH_SHORT);
                toast.show();
            }


        };
    }
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Toast.makeText(getApplicationContext(),"hello farzan ",Toast.LENGTH_LONG).show();
    }


    //Called every time an activity runs startService
    public int onStartCommand(Intent intent, int flags, int startId) {
        //When an activity starts the service, it sends an action
        //Check if action = btConnect
        if (intent.getAction().equals("btConnect")) {
            //Make sure there's not already an open socket
            if (BS == null) {
                //Creates a bundle to get data
                Bundle extras = intent.getExtras();
                //Extracts data from bundle, in this case the mac address
                mac = extras.getString("mac");

                //Creates another handler to listen for data
                final Handler mHandler = new Handler();

                //A runnable called when can't connect
                final Runnable hUnsuccessful = new Runnable() {
                    public void run() {
                        toast = Toast.makeText(getApplicationContext(),
                                "Unsuccessful", Toast.LENGTH_SHORT);
                        toast.show();
                        toast = Toast.makeText(getApplicationContext(), "Service stopped", Toast.LENGTH_SHORT);
                        toast.show();
                        //Kill the service
                        stopForeground(true);
                        stopSelf();
                    }
                };

                //A runnable called when successfully connected
                final Runnable hSuccessful = new Runnable() {
                    public void run() {
                        //Run method using socket BS
                        //Method listens for data coming across socket
                        ConnectedThread(BS);
                        toast = Toast.makeText(getApplicationContext(),
                                "Connected", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                };
                //Creates a new thread since BS.connect
                //will block the main thread otherwise
                Thread thread = new Thread() {
                    public void run() {

                        //Creates BluetoothDevice from the mac address
                        //mac address was passed to service in onStartCommand
                        BluetoothDevice device = BA.getRemoteDevice(mac);
                        // Get a BluetoothSocket to connect with the given BluetoothDevice
                        try {
                            // MY_UUID is the app's UUID string, also used by the server code
                            BS = device.createRfcommSocketToServiceRecord(MY_UUID);
                        }
                        //If it can't create a socket
                        catch (IOException e) {
                            //Send unsuccessful to the handler
                            mHandler.post(hUnsuccessful);
                            return;
                        }

                        // Cancel discovery because it will slow down the connection
                        BA.cancelDiscovery();
                        //Try to connect
                        try {
                            BS.connect();
                        }
                        // Unable to connect, send a toast and close the socket
                        catch (IOException connectException) {
                            //Try to close socket
                            try {
                                if (BS != null) {
                                    BS.close();
                                    BS = null;
                                }
                                //Send unsuccessful to the handler
                                mHandler.post(hUnsuccessful);
                                return;
                            }
                            //Can't close socket, do nothing
                            catch (IOException closeException) {
                            }
                        }
                        //If it made it to here, it was successful
                        //Send successful to the handler
                        mHandler.post(hSuccessful);
                    }
                };
                //Start the thread
                thread.start();
            }
            //if BS != null
            //Currently connected to a socket
            else {
                toast = Toast.makeText(getApplicationContext(), "You must disconnect first", Toast.LENGTH_SHORT);
                toast.show();
            }
        }

        //When an activity starts the service, it sends an action
        //Check if action = stopForeground
        if (intent.getAction().equals("stopForeground")) {
            toast = Toast.makeText(getApplicationContext(), "Service stopped", Toast.LENGTH_SHORT);
            toast.show();
            //Try to close socket if one is open
            try {
                if (BS != null) {
                    BS.close();
                    BS = null;
                    toast = Toast.makeText(getApplicationContext(),
                            "Bluetooth device disconnected", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
            //If it can't close it, do nothing
            catch (IOException e) {
            }
            //Kill the service
            stopForeground(true);
            stopSelf();
        }
        //Service should stay alive, unless we kill it
        return START_STICKY;
    }

    //Method to create foreground notification
    private Notification buildForegroundNotification() {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this);

        notification.setOngoing(true);
        notification.setContentTitle("Bluetooth Service")
                .setContentText("Service to listen for bluetooth data")
                .setSmallIcon(R.drawable.notification)
                .setPriority(Notification.PRIORITY_MIN);
        return (notification.build());
    }

    //Method to create a notification
    private void notification(String title, String text) {
        int mId = 0;
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notification)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                        .setPriority(NotificationCompat.PRIORITY_MAX);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(mId, mBuilder.build());
    }

    //Looping method to listen for data over bluetooth socket
    private void ConnectedThread(BluetoothSocket socket) {
        final InputStream mmInStream;
        InputStream tmpIn = null;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
        }

        mmInStream = tmpIn;

        //Create new thread as it will otherwise block main thread
        Thread thread = new Thread() {
            public void run() {
                byte[] buffer = new byte[256];  //buffer store for the stream
                int bytes; //bytes returned from read()

                //Keep listening to the InputStream until an exception occurs
                while (true) {
                    try {
                        Message msg = new Message();
                        Bundle bundle = new Bundle();
                        // Read from the InputStream
                        bytes = mmInStream.read(buffer);        // Get number of bytes and message in "buffer"
                        String strIncom = new String(buffer, 0, bytes);
                        sb.append(strIncom);
                        int endOfLineIndex = sb.indexOf("\r\n");
                        if (endOfLineIndex > 0) {
                            String sbprint = sb.substring(0, endOfLineIndex);
                            sb.delete(0, sb.length());
                            bundle.putString("str", sbprint);
                            msg.setData(bundle);
                            h.sendMessage(msg);
                            notification("Sensor Data:", sbprint);
                            Toast.makeText(ForegroundService.this, ""+sbprint, Toast.LENGTH_SHORT).show();
                            sendData(sbprint);
                            String id=sbprint.replaceAll("\\s+","");
                            /*Cursor c=helper.getItem(id);

                            if (c.moveToFirst()) {
                                do {
                                    helper.setValue(id);
                                }
                                while (c.moveToNext());
                            }
                            */

                        }
                    }
                    //If InputStream gets interrupted
                    catch (IOException e) {
                        Message msg = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putString("str", "Lost connection");
                        msg.setData(bundle);
                        h.sendMessage(msg);
                        notification("Oops", "Connection to the bag has been lost.Your Bag is out of the range");
                        Intent stopIntent = new Intent(getApplicationContext(), ForegroundService.class);
                        stopIntent.setAction("stopForeground");
                        startService(stopIntent);
                        break;
                    }
                }
            }
        };
        //Start the thread
        thread.start();
    }

    private void sendData(String sbprint) {
        Intent i = new Intent("1");
        i.putExtra("id", sbprint);
        sendBroadcast(i);
    }

    //Required
    public IBinder onBind(Intent intent) {
        return null;
    }
}