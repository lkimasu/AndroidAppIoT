package com.example.lwlst.cap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity
{

    private final int REQUEST_BLUETOOTH_ENABLE = 100;
    private TextView mConnectionStatus;
    ConnectedTask mConnectedTask = null;
    static BluetoothAdapter mBluetoothAdapter;
    private String mConnectedDeviceName = null;
    static boolean isConnectionError = false;
    private static final String TAG = "BluetoothClient";
    Button btnled, btnled1, btnled2, btnled3,btnled4,btnled5,btnled6,btnled7;

    private static final int RESULT_SPEECH =1;
    private Intent i;
    private  ImageButton btn;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn =findViewById(R.id.imageView1);
        btnled = (Button) findViewById(R.id.btnled);
        btnled1 = (Button) findViewById(R.id.btnled1);
        btnled2 = (Button) findViewById(R.id.btnled2);
        btnled3 = (Button) findViewById(R.id.btnled3);
        btnled4 = (Button) findViewById(R.id.btnled4);
        btnled5 = (Button) findViewById(R.id.btnled5);
        btnled6 = (Button) findViewById(R.id.btnled6);
        btnled7 = (Button) findViewById(R.id.btnled7);

        btn.setOnClickListener(new View.OnClickListener(){ //마이크 버튼 클릭시 이벤트

            @Override
            public void onClick(View view) {

                i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getPackageName());
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");
                i.putExtra(RecognizerIntent.EXTRA_PROMPT,"말해주세요");

                try{
                    startActivityForResult(i,RESULT_SPEECH);

                }catch (ActivityNotFoundException e){
                    e.getStackTrace();
                }
            }
        });
        //버튼 클릭시 이벤트
        btnled.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                    sendMessage("1");
            }
        });

        btnled1.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                sendMessage("2");
            }
        });

        btnled2.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                sendMessage("3");
            }
        });

        btnled3.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                sendMessage("4");
            }
        });

        btnled4.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                sendMessage("5");
            }
        });

        btnled5.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                sendMessage("6");
            }
        });

        btnled6.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                sendMessage("7");
            }
        });

        btnled7.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                sendMessage("8");
            }
        });

        //어떤 블루투스에 연결 되었는지 확인
        mConnectionStatus = (TextView)findViewById(R.id.connection_status_textview);


        Log.d( TAG, "Initalizing Bluetooth adapter...");
        //1.블루투스 사용 가능한지 검사합니다.
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            showErrorDialog("This device is not implement Bluetooth.");
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_BLUETOOTH_ENABLE);
        }
        else {
            Log.d(TAG, "Initialisation successful.");

            //2. 페어링 되어 있는 블루투스 장치들의 목록을 보여줍니다.
            //3. 목록에서 블루투스 장치를 선택하면 선택한 디바이스를 인자로 하여
            //   doConnect 함수가 호출됩니다.
            showPairedDevicesListDialog();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if ( mConnectedTask != null ) {
            mConnectedTask.cancel(true);
        }
    }

    //외부 블루투스와 연결,백그라운드로
    private class ConnectTask extends AsyncTask<Void, Void, Boolean> {

        private BluetoothSocket mBluetoothSocket = null;
        private BluetoothDevice mBluetoothDevice = null;

        ConnectTask(BluetoothDevice bluetoothDevice) {
            mBluetoothDevice = bluetoothDevice;
            mConnectedDeviceName = bluetoothDevice.getName();

            //SPP
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                Log.d( TAG, "create socket for "+mConnectedDeviceName);

            } catch (IOException e) {
                Log.e( TAG, "socket create failed " + e.getMessage());
            }

            mConnectionStatus.setText("connecting...");
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            // Always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mBluetoothSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mBluetoothSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " +
                            " socket during connection failure", e2);
                }
                return false;
            }
            return true;
        }

        //소켓이 생성되면 연결, 소켓이 없으면 오류 출력
        @Override
        protected void onPostExecute(Boolean isSucess) {

            if ( isSucess ) {
                connected(mBluetoothSocket);
            }
            else{
                isConnectionError = true;
                Log.d( TAG,  "Unable to connect device");
                showErrorDialog("Unable to connect device");
            }
        }
    }

    //소켓과 연결
    public void connected( BluetoothSocket socket ) {
        mConnectedTask = new ConnectedTask(socket);
        mConnectedTask.execute();
    }

    private class ConnectedTask extends AsyncTask<Void, String, Boolean> {

        private InputStream mInputStream = null;
        private OutputStream mOutputStream = null;
        private BluetoothSocket mBluetoothSocket = null;

        ConnectedTask(BluetoothSocket socket){

            mBluetoothSocket = socket;
            try {
                mInputStream = mBluetoothSocket.getInputStream();
                mOutputStream = mBluetoothSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "socket not created", e );
            }

            Log.d( TAG, "connected to "+mConnectedDeviceName);
            mConnectionStatus.setText( "connected to "+mConnectedDeviceName); //연결된 블루투스 입력
        }

        //이 부분은 뭐지?
        @Override
        protected Boolean doInBackground(Void... params) {

            byte [] readBuffer = new byte[1024];
            int readBufferPosition = 0;

            // Keep listening to the InputStream while connected
            while (true) {

                if ( isCancelled() ) return false;

                try {

                    int bytesAvailable = mInputStream.available();

                    if(bytesAvailable > 0) {

                        byte[] packetBytes = new byte[bytesAvailable];
                        // Read from the InputStream
                        mInputStream.read(packetBytes);

                        for(int i=0;i<bytesAvailable;i++) {

                            byte b = packetBytes[i];
                            if(b == '\n')
                            {
                                byte[] encodedBytes = new byte[readBufferPosition];
                                System.arraycopy(readBuffer, 0, encodedBytes, 0,
                                        encodedBytes.length);
                                String recvMessage = new String(encodedBytes, "UTF-8");

                                readBufferPosition = 0;

                                Log.d(TAG, "recv message: " + recvMessage);
                                publishProgress(recvMessage);
                            }
                            else
                            {
                                readBuffer[readBufferPosition++] = b;
                            }
                        }
                    }
                } catch (IOException e) {

                    Log.e(TAG, "disconnected", e);
                    return false;
                }
            }
        }

        @Override
        protected void onProgressUpdate(String... recvMessage) {

        }

        //연결이 끊어 지게 되면 오류를 출력
        @Override
        protected void onPostExecute(Boolean isSucess) {
            super.onPostExecute(isSucess);

            if ( !isSucess ) {
                closeSocket();
                Log.d(TAG, "Device connection was lost");
                isConnectionError = true;
                showErrorDialog("Device connection was lost");
            }
        }

        //앱을 종료 할시 or 블루투스를 종료할시에 소켓 종료
        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);

            closeSocket();
        }

        void closeSocket(){

            try {

                mBluetoothSocket.close();
                Log.d(TAG, "close socket()");

            } catch (IOException e2) {

                Log.e(TAG, "unable to close() " +
                        " socket during connection failure", e2);
            }
        }

        void write(String msg){

            try {
                mOutputStream.write(msg.getBytes());
                mOutputStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "Exception during send", e );
            }

        }
    }

     //페어링 된 블루투스 목록 가져욤
    public void showPairedDevicesListDialog()
    {
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        final BluetoothDevice[] pairedDevices = devices.toArray(new BluetoothDevice[0]);

        if ( pairedDevices.length == 0 ){
            showQuitDialog( "No devices have been paired.\n"
                    +"You must pair it with another device.");
            return;
        }

        String[] items;
        items = new String[pairedDevices.length];
        for (int i=0;i<pairedDevices.length;i++) {
            items[i] = pairedDevices[i].getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select device");
        builder.setCancelable(false);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                // Attempt to connect to the device
                ConnectTask task = new ConnectTask(pairedDevices[which]);
                task.execute();
            }
        });
        builder.create().show();
    }

    //목록 가져오기 오류
    public void showErrorDialog(String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quit");
        builder.setCancelable(false);
        builder.setMessage(message);
        builder.setPositiveButton("OK",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if ( isConnectionError  ) {
                    isConnectionError = false;
                    finish();
                }
            }
        });
        builder.create().show();
    }

     //목록 가져오기에서 종료
    public void showQuitDialog(String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quit");
        builder.setCancelable(false);
        builder.setMessage(message);
        builder.setPositiveButton("OK",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.create().show();
    }

    void sendMessage(String message){

        if ( mConnectedTask != null ) {
            mConnectedTask.write(message);
        }

        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        //음성인식
        if(resultCode == RESULT_OK && (requestCode == RESULT_SPEECH)){
            ArrayList<String> ssResult = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String result_sst = ssResult.get(0);
            String cmdNum ="";
            if("말보루".equals(ssResult.get(0))){
                cmdNum ="1";
            }else if("에쎄".equals(ssResult.get(0))){
                cmdNum ="2";
            }
            else if("한라산".equals(ssResult.get(0))){
                cmdNum ="3";
            }
            else if("레종".equals(ssResult.get(0))){
                cmdNum ="4";
            }
            else if("장미".equals(ssResult.get(0))){
                cmdNum ="5";
            }
            else if("타임".equals(ssResult.get(0))){
                cmdNum ="6";
            }
            else if("던힐".equals(ssResult.get(0))){
                cmdNum ="7";
            }
            else if("더원".equals(ssResult.get(0))){
                cmdNum ="8";
            }
            else {
                Log.d(TAG, "알수 없는 명령어 입니다.");
            }
            Toast.makeText(MainActivity.this,result_sst,Toast.LENGTH_SHORT).show();
            sendMessage(cmdNum);
        }

        if(requestCode == REQUEST_BLUETOOTH_ENABLE){
            if (resultCode == RESULT_OK){
                //BlueTooth is now Enabled
                showPairedDevicesListDialog();
            }
            if(resultCode == RESULT_CANCELED){
                showQuitDialog( "You need to enable bluetooth");
            }
        }
    }


}
