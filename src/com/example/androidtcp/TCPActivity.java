package com.example.androidtcp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class TCPActivity extends Activity {
	private TextView txtReceiveInfo;
	private EditText edtRemoteIP,edtRemotePort,edtSendInfo,edtLocalPort,edtSeverSendInfo;
	private Button btnConnect,btnSend,btnListen,btnServerSend;
	private ImageView imageView;
	private boolean isConnected=false,isListened=false;
	private Socket socketClient=null,socket=null;
	private String receiveInfoClient,receiveInfoServer;
	private boolean mutex=false;
	static boolean offline = false;
	private boolean dataProceState = false;
	private boolean dataStartState = false;
	private FileOutputStream fos = null;
	private byte[] buffer = new byte[800];
	private int len = 0;
	private String jpgNameArray[] = {"testJpg0.jpg", "testJpg1.jpg"};
	private int jpgNameRotate = 0;
	static BufferedReader bufferedReaderServer=null;
	static BufferedInputStream bufferedReaderClient = null;
	static PrintWriter printWriterClient = null,printWriterServer=null;
	private HandleBroadcastReceiver hbr;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tcp);
		btnConnect=(Button)findViewById(R.id.btnConnect);
		btnSend=(Button)findViewById(R.id.btnSend);
		txtReceiveInfo=(TextView)findViewById(R.id.txtReceiveInfo);
		edtRemoteIP=(EditText)findViewById(R.id.edtRemoteIP);
		edtRemotePort=(EditText)findViewById(R.id.edtRemotePort);
		edtSendInfo=(EditText)findViewById(R.id.edtSendInfo);
		imageView=(ImageView)findViewById(R.id.imageView);
		
		try {
			fileDelete("testJpg0.jpg");
			fileDelete("testJpg1.jpg");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//连接按钮单击事件
	public void ConnectButtonClick(View source)
	{
		if(isConnected)
		{
			isConnected=false;
			if(socketClient!=null)
			{
				try 
				{
					socketClient.close();
					socketClient=null;	
					printWriterClient.close();
					printWriterClient = null;
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
			new tcpClientThread().interrupt();
			btnConnect.setText("开始连接");
			edtRemoteIP.setEnabled(true);
			edtRemotePort.setEnabled(true);	
			
			unregisterReceiver(hbr);	
			Intent intent  = new Intent(this, LongRunningService.class);
			stopService(intent);
		}
		else
		{
			isConnected=true;
			btnConnect.setText("停止连接");
			edtRemoteIP.setEnabled(false);
			edtRemotePort.setEnabled(false);
			new tcpClientThread().start();
		}
	}
	//发送信息按钮单击事件
	public void SendButtonClick(View source)
	{
		if ( isConnected && socketClient!=null) 
		{
			String sendInfo =edtSendInfo.getText().toString();
			try 
			{				    	
		    	printWriterClient.print(sendInfo);
		    	printWriterClient.flush();
				receiveInfoClient = "发送信息"+"\""+sendInfo+"\""+"\n";
				Message msg = new Message();
				msg.what=0x123;
				handler.sendMessage(msg);
			}
			catch (Exception e) 
			{
				receiveInfoClient = e.getMessage() + "\n";
				Message msg = new Message();
				msg.what=0x123;
				handler.sendMessage(msg);
			}
		}
	}
	//TCP客户端线程
	private class tcpClientThread extends Thread 
	{
		@Override
		public void run()
		{
			try 
			{				
				socketClient = new Socket(edtRemoteIP.getText().toString(), Integer.parseInt(edtRemotePort.getText().toString()));	
				bufferedReaderClient = new BufferedInputStream(socketClient.getInputStream());
				printWriterClient = new PrintWriter(socketClient.getOutputStream(), true);
				receiveInfoClient = "连接服务器成功!\n";
				offline = false;
				
				Intent alarmIntent = new Intent(MyApplication.getContext(), LongRunningService.class);
				startService(alarmIntent);
				
				hbr = new HandleBroadcastReceiver();
				IntentFilter intentFilter  = new IntentFilter();
				intentFilter.addAction("com.example.androidtcp.receiver");
				registerReceiver(hbr, intentFilter);
				
				Message msg = new Message();
				msg.what=0x123;
				handler.sendMessage(msg);
			}
			catch (Exception e) 
			{
				receiveInfoClient = e.getMessage() + "\n";
				Message msg = new Message();
				msg.what=0x123;
				handler.sendMessage(msg);
			}			
			while (isConnected)
			{
				Log.d("Test", "The current thread ID is : " + Thread.currentThread().getId());
				try
				{
					if((mutex == false))
					{
						Log.d("Test", "Enter the mutex == false");
						if((len = bufferedReaderClient.read(buffer))>0)
						{	
							Log.d("Test", "Enter the read data");
							mutex = true;
							Message msg = new Message();
							msg.what=0x123;
							handler.sendMessage(msg);
						}
						else if(offline == false)
						{
							Log.d("Test", "Enter the offline == false");
							try {
								socket.sendUrgentData(0xFF);
								Log.d("Test", "End of the UrgenData send");
							} catch (Exception e) {
								  Log.d("Test", "Enter the Exception 1");
								  receiveInfoClient = e.getMessage() + "\n";
								  offline = true;
								  Message msg = new Message();
								  msg.what=0x123;
								  handler.sendMessage(msg);
							}
						}
					}
				} catch (Exception e) {
					  Log.d("Test", "Enter the Exception 2");
					  receiveInfoClient = e.getMessage() + "\n";
					  Message msg = new Message();
					  msg.what=0x123;
					  handler.sendMessage(msg);
				}  
			}
			{
				  Log.d("Test", "The child thread ID End.");
				  offline = true;
				  Message msg = new Message();
				  msg.what=0x123;
				  handler.sendMessage(msg);
			  }		
		}
	};

	Handler handler = new Handler()
	{		
		@Override
		public void handleMessage(Message msg)										
		{				
			Log.d("Test", "Enter the handleMessage().");
			if(msg.what==0x123)
			{	if(receiveInfoClient != null)
					txtReceiveInfo.setText("Current State: "+ receiveInfoClient + "\n");	// 刷新
				receiveInfoClient = null;
				if((offline == true) && (isConnected == true))
				{
					handleOffline(isConnected);
					reConnect(isConnected);
				}
				if(mutex == true)
				{	
					Log.d("Test", "The current thread ID is : " + Thread.currentThread().getId());
					if(dataProceState == true)
					{
						BitmapFactory.Options option = new BitmapFactory.Options();
				        option.inSampleSize = 1;
						Bitmap bm = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().toString() + "/" + jpgNameArray[jpgNameRotate], option);
				        imageView.setImageBitmap(bm);
				        if(jpgNameRotate == 0) {
				        	jpgNameRotate = 1;
				        }
				        else {
				        	jpgNameRotate = 0;
				        }
				        try {
							fileDelete(jpgNameArray[jpgNameRotate]);
						} catch (IOException e) {
							e.printStackTrace();
							return;
						}
				    	txtReceiveInfo.setText("Current State: "+ "实时画面" + "\n");
				    	dataProceState = false;	
				    	mutex = false;
						printWriterClient.print("GP");
				    	printWriterClient.flush();
					}					 
					else if(dataStartState == false) {
							int tempi = 0;
							for(int i = 0; i < (len - 1); i++) {    //must check the situation that 0xff 0xd9 in the same buffer 
								if((dataStartState == false)&&(buffer[i] == -1)&&(buffer[i + 1] == -40)) {
										dataStartState = true;
										tempi = i;
										try {
											fos = getfos(jpgNameArray[jpgNameRotate]);
										} catch (IOException e) {
											e.printStackTrace();
											return;
										}
								}
								if((dataStartState == true)&&(buffer[i] == -1)&&(buffer[i + 1] == -39)) {
									try {
										fos.write(buffer, tempi, (i + 2 - tempi));
										fos.close();
									} catch (IOException e) {
										e.printStackTrace();
										return;
									}
									dataStartState = false;
									dataProceState = true;
									break;
								}
								else if((i == (len - 2))&&(dataStartState == true)) {
									try {
										fos.write(buffer, tempi, (len - tempi));
									} catch (IOException e) {
										e.printStackTrace();
										return;
									}
								}									
							}
						}
						else {
							for(int i = 0; i < (len - 1); i++) {
								if((buffer[i] == -1)&&(buffer[i + 1] == -39)) {
									try {
										fos.write(buffer, 0, (i + 2));
										fos.close();
									} catch (IOException e) {
										e.printStackTrace();
										return;
									}
									dataStartState = false;
									dataProceState = true;
									break;
								}
								else if(i == (len - 2)) {
									try {
										fos.write(buffer, 0, len);
									} catch (IOException e) {
										e.printStackTrace();
										return;
									}
								}
							}
						}
					}
					mutex = false;
				}				
			if(msg.what==0x456)
			{
				txtReceiveInfo.append("TCPServer: "+receiveInfoServer);	
			}
		}									
	};
	
	@Override
	protected void onDestroy() {  
        //注销广播  
        unregisterReceiver(hbr);  
        super.onDestroy();  
    }  
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tc, menu);
		return true;
	}
	
	public FileOutputStream getfos(String fileName) throws IOException {
		File file = new File(Environment.getExternalStorageDirectory(),fileName); 
        FileOutputStream fos = new FileOutputStream(file, true);
        return fos;
	}
	
	public void fileDelete(String fileName) throws IOException{  
        
        File file = new File(Environment.getExternalStorageDirectory(),fileName); 
        if(file.exists() == true)
        {
        	file.delete();
        }
    }

	public void handleOffline(boolean isConnected) {
		if(isConnected)
		{
			isConnected=false;
			if(socketClient!=null)
			{
				try 
				{
					socketClient.close();
					socketClient=null;	
					printWriterClient.close();
					printWriterClient = null;
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
					
				}
			}
			new tcpClientThread().interrupt();
			btnConnect.setText("开始连接");
			edtRemoteIP.setEnabled(true);
			edtRemotePort.setEnabled(true);
			
			unregisterReceiver(hbr);
			
			Intent intent  = new Intent(this, LongRunningService.class);
			stopService(intent);
		}
	}
	public void reConnect(boolean isConnected){
		isConnected=true;
		btnConnect.setText("停止连接");
		edtRemoteIP.setEnabled(false);
		edtRemotePort.setEnabled(false);
		new tcpClientThread().start();		
	}
	
	//得到本地IP地址（WIFI）
/*
	private String GetLocalIP()
	{  
        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);    
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();    
        int ipAddress = wifiInfo.getIpAddress(); 
        if(ipAddress==0)return null;  
        return ((ipAddress & 0xff)+"."+(ipAddress>>8 & 0xff)+"."  
                +(ipAddress>>16 & 0xff)+"."+(ipAddress>>24 & 0xff));  
    } 
*/	
	
	class HandleBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("TestForS&R", "Enter the error handler.");
			printWriterClient.print("GP");
	    	printWriterClient.flush();
			/*
			offline = true;
			Message msg = new Message();
			msg.what=0x123;
			handler.sendMessage(msg);
			*/
		}
		
	}
}