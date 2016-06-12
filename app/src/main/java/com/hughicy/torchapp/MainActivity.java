package com.hughicy.torchapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    private boolean status = false;
    private Camera camera = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_update) {
            final Handler newhandler = new Handler(){
                @Override
                public void handleMessage(Message msg){
                    Bundle data = msg.getData();
                    final String url = data.getString("url");
                    final String ver = data.getString("newVername");
                    final String changelog = data.getString("changelog");
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(getString(R.string.new_version)+" "+ver)
                            .setMessage(changelog)
                            .setPositiveButton(getString(R.string.update_now), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent();
                                    intent.setAction("android.intent.action.VIEW");
                                    Uri content_url = Uri.parse(url);
                                    intent.setData(content_url);
                                    startActivity(intent);
                                }
                            })
                            .show();
                }
            };
            final Handler nonewhandler = new Handler(){
                @Override
                public void handleMessage(Message msg){
                    Toast.makeText(MainActivity.this,getString(R.string.no_new),Toast.LENGTH_LONG).show();
                }
            };
            Runnable update = new Runnable() {
                @Override
                public void run() {
                    UpdateManager updateManager = new UpdateManager(getApplicationContext());
                    if(updateManager.checkUpdate()){
                        Message msg = new Message();
                        Bundle data = new Bundle();
                        data.putString("url",updateManager.url);
                        data.putString("changelog",updateManager.changelog);
                        data.putString("newVername",updateManager.newVername);
                        msg.setData(data);
                        newhandler.sendMessage(msg);
                    }else{
                        Message msg = new Message();
                        nonewhandler.sendMessage(msg);
                    }
                }
            };
            new Thread(update).start();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int KeyCode,KeyEvent event){
        if(KeyCode == KeyEvent.KEYCODE_BACK){
            try{
                camera.release();
            }catch(Exception e){

            }
            System.exit(0);
        }
        return true;
    }

    public void start() {
        final ImageView torchimg = (ImageView)findViewById(R.id.torchimg);
        final TextView torchtxt = (TextView)findViewById(R.id.torchtxt);
        final SharedPreferences sp = getSharedPreferences("torch",MODE_PRIVATE);
        final SharedPreferences.Editor editor = sp.edit();
        
        if(status = getCamStatus()){
            torchimg.setBackgroundResource(R.drawable.bulb_yellow);
            torchtxt.setText("ON");
            editor.putBoolean("STATUS",true);
        }else{
            torchimg.setBackgroundResource(R.drawable.bulb_grey);
            torchtxt.setText("OFF");
            editor.putBoolean("STATUS",false);
        }
        editor.commit();
        torchimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status = sp.getBoolean("STATUS",false);
                if(status){
                    torchimg.setBackgroundResource(R.drawable.bulb_grey);
                    torchtxt.setText("OFF");
                    camera.stopPreview();
                    camera.release();
                    camera = null;
                    editor.putBoolean("STATUS", false);
                }else{
                    openCam();
                    if(camera != null) {
                        torchimg.setBackgroundResource(R.drawable.bulb_yellow);
                        torchtxt.setText("ON");
                        android.hardware.Camera.Parameters mParameters = camera.getParameters();
                        List<String> flashModes = mParameters.getSupportedFlashModes();
                        System.out.println(flashModes);
                        mParameters.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_TORCH);
                        camera.setParameters(mParameters);
                        camera.startPreview();
                        camera.autoFocus(new android.hardware.Camera.AutoFocusCallback() {
                            @Override
                            public void onAutoFocus(boolean success, android.hardware.Camera camera) {
                            }
                        });
                        editor.putBoolean("STATUS", true);
                    }
                }
                editor.commit();
            }
        });
    }

    public boolean getCamStatus(){
        try{
            Camera cam = android.hardware.Camera.open();
            System.out.println("status:off");
            cam.release();
            return false;
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("status:on");
            return true;
        }
    }

    public void openCam(){
        try{
            camera = android.hardware.Camera.open();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
