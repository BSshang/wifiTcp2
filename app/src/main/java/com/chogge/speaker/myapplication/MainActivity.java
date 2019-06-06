package com.chogge.speaker.myapplication;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.ToastUtils;
import com.chogge.speaker.myapplication.socket.ClientLastly;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static com.blankj.utilcode.util.CrashUtils.init;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    /*接收发送定义的常量*/
    private EditText put_info;
    private Button  bt_client,bt_put;
    private TextView tvMessage;
    private String mIp = "";
    private int mPort = 0;
    private SendThread sendthread;

    private StringBuffer receiveData = new StringBuffer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initview();
        bt_client.setText("连接");
        bt_put.setEnabled(false);
    }

    private void initview() {

        put_info = findViewById(R.id.put_info);
        bt_client = findViewById(R.id.bt_client);
        tvMessage= findViewById(R.id.tv_message);
        bt_put = findViewById(R.id.bt_put);
        bt_put.setOnClickListener(this);
        bt_client.setOnClickListener(this);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_put:
                putInfo();
                break;
            case R.id.bt_client:
                clinet();
                break;
        }
    }
    public  void putInfo(){
                 new Thread(new Runnable() {
                     @Override
                     public void run() {
                         if (put_info.getText().toString().length() > 0) {
                             sendthread.send(put_info.getText().toString());
                         } else {
                             Toast.makeText(MainActivity.this, "请输入发送内容！", Toast.LENGTH_SHORT).show();
                         }
                     }
                 }).start();

        put_info.setText("");
    }

    public void clinet(){
        mIp =  "192.168.1.1";
        mPort = 6060;

        if(mIp.length()<= 0 ){
            Toast.makeText(MainActivity.this, "请输入ip地址和端口号！", Toast.LENGTH_SHORT).show();
        }else{

            sendthread = new SendThread(mIp, mPort, mHandler);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    sendthread.run();
                    if(sendthread.isConnected()){
                        mHandler.sendEmptyMessage(0x04);
                    }

                }
            }).start();
        }

    }


    Handler mHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            switch(msg.what){
                case 0x00:
                    Log.i("mr_收到的数据： ", msg.obj.toString());
                    receiveData.append("接收到：" + msg.obj.toString());
                    //收到数据
                    tvMessage.setText(receiveData);
                    receiveData.append("\r\n");
                    break;
                case 0x01:
                    break;
                case 0x02:
                    receiveData.append("连接中断：" );
                    tvMessage.setText(receiveData);
                    receiveData.append("\r\n");
                    break;
                case 0x03:
                    receiveData.append("连接建立：" );
                    tvMessage.setText(receiveData);
                    receiveData.append("\r\n");
                    bt_put.setEnabled(true);
                    break;
                case 0x04:
                    bt_client.setText("断开");
                    receiveData.append("连接成功");
                    tvMessage.setText(receiveData);
                    receiveData.append("\r\n");
                    break;
                case 0x05:
                    bt_client.setText("连接");
                    receiveData.append("连接失败");
                    tvMessage.setText(receiveData);
                    receiveData.append("\r\n");
                    bt_put.setEnabled(false);
                    break;
                case 0x06:
                    receiveData.append("连接开始");
                    tvMessage.setText(receiveData);
                    receiveData.append("\r\n");
                    bt_put.setEnabled(false);
                    break;
                case 0x07:
                    receiveData.append("创建连接失败");
                    tvMessage.setText(receiveData);
                    receiveData.append("\r\n");
                    bt_put.setEnabled(false);
                    break;
                case 0x08:
                    receiveData.append(msg.obj);
                    tvMessage.setText(receiveData);
                    receiveData.append("\r\n");
                    break;
            }
        }
    };


    @Override
    protected void onDestroy()
    {
        sendthread.close();
        super.onDestroy();
    }

}
