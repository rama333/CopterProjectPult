package com.labs.robots.copterprojectpult;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static com.labs.robots.copterprojectpult.R.id.buttonF;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "TAG";
    private static final int REQUEST_ENABLE_BT = 1;
    private StringBuilder sb = new StringBuilder();
    final int RECIEVE_MESSAGE = 1;
    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket = null;
    private ConnectedThread mConnectedThread;
    Handler h;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    TextView textView;
    int i = 0;
    private static String MacAdress = "98:D3:32:70:D7:5A";
    Text text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Button buttonF = (Button) findViewById(R.id.buttonF);
        Button buttonR = (Button) findViewById(R.id.buttonR);
        Button buttonL = (Button) findViewById(R.id.buttonL);
        Button buttonB = (Button) findViewById(R.id.buttonB);

        buttonF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mConnectedThread.write("2");
            }
        });

        buttonR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mConnectedThread.write("1");
            }
        });

        buttonL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mConnectedThread.write("0");
            }
        });

        buttonB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mConnectedThread.write("3");
            }
        });
        

        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                int i = 0;
                switch (msg.what) {
                    case RECIEVE_MESSAGE:                                                   // если приняли сообщение в Handler
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIncom = new String(readBuf, 0, msg.arg1);
                        sb.append(strIncom);                                                // формируем строку
                        int endOfLineIndex = sb.indexOf("\r\n");                            // определяем символы конца строки
                        if (endOfLineIndex > 0) {                                            // если встречаем конец строки,
                            String sbprint = sb.substring(0, endOfLineIndex);               // то извлекаем строку
                            sb.delete(0, sb.length());                                      // и очищаем sb
                            textView.setText("response Arduino: " + i +" "+ sbprint)
                        }
                        Log.d(TAG, "...string:"+ sb.toString() +  "byte:" + msg.arg1 + "...");
                        break;
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // получаем локальный Bluetooth адаптер
        checkBTState();
    }


    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "...onResume - reconnect...");

        BluetoothDevice device = btAdapter.getRemoteDevice(MacAdress);

        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        btAdapter.cancelDiscovery();

        Log.d(TAG, "...Connect...");
        try {
            btSocket.connect();
            Log.d(TAG, "...Conection succes...");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        Log.d(TAG, "...Создание Socket...");

        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        try     {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    private void checkBTState() {
        if(btAdapter==null) {
            errorExit("Fatal Error", "Bluetooth not received");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth on...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    private void errorExit(String title, String message){
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(String message) {
            byte[] msgBuffer = message.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                Log.d(TAG, "...Error sending data: " + e.getMessage() + "...");
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}
