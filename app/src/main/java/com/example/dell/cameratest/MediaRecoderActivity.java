package com.example.dell.cameratest;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class MediaRecoderActivity extends AppCompatActivity implements SurfaceHolder.Callback{
    public  static  final int  VIDEO_REQUEST_CODE = 2;
    private static final String TAG = "MediaRecoderActivity";
    private SurfaceView surfaceView;
    private Button btnPlay;
    private Button btnStartStop;
    private boolean isStart = false;//是否正在录像
    private boolean isPlay = false;//是否正在播放
    private MediaRecorder recorder;
    private SurfaceHolder surfaceHolder;
    private ImageView imageView;
    private Camera camera;
    private MediaPlayer mediaPlayer;
    private String path;
    private TextView textView;
    private int text = 0;

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            text++;
            textView.setText(text);
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_mediarecoder);

        surfaceView = findViewById(R.id.surfaceView);
        btnPlay = findViewById(R.id.btnPlay);
        btnStartStop = findViewById(R.id.btnStartStop);
        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.text);
//        if (ContextCompat.checkSelfPermission(MediaRecoderActivity.this,Manifest.permission.CAMERA)!=
//                PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(MediaRecoderActivity.this,new String[]{Manifest.permission_group.CAMERA},VIDEO_REQUEST_CODE);
//        }

        btnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPlay) {
                    if (mediaPlayer != null) {
                        isPlay = false;
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                }
                if (!isStart) {
                    handler.postDelayed(runnable, 1000);
                    imageView.setVisibility(View.GONE);//GONE 不会占空间，INVISIBILITY还会占空间
                    if (recorder == null) {
                        recorder = new MediaRecorder();
                    }

                    camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                    if (camera != null) {
                        camera.setDisplayOrientation(90);
                        camera.unlock();
                        recorder.setCamera(camera);
                    }
                    try {
                        //  放在 Format前
                        recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                        // Format
                        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
                        recorder.setVideoSize(640, 480);
                        recorder.setVideoFrameRate(30);
                        recorder.setVideoEncodingBitRate(3 * 1024 * 1024);
                        recorder.setOrientationHint(90);
                        recorder.setMaxDuration(30 * 1000);
                        recorder.setPreviewDisplay(surfaceHolder.getSurface());
                        path = getSDPath();
                        if (path != null) {
                            File dir = new File(path + "/recordtest");
                            if (!dir.exists()) {
                                dir.mkdir();
                            }
                            path = dir + "/" + getDate() + ".mp4";
                            recorder.setOutputFile(path);
                            recorder.prepare();
                        }

                        recorder.start();
                        ;
                        isStart = true;
                        btnStartStop.setText("Stop");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        handler.removeCallbacks(runnable);
                        recorder.stop();
                        recorder.reset();
                        recorder.release();
                        recorder = null;
                        btnStartStop.setText("Start");
                        if (camera!=null){
                            camera.release();
                            camera = null;
                        }
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
                isStart = false;
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isPlay = true;
                imageView.setVisibility(View.GONE);
                if (mediaPlayer==null){
                    mediaPlayer = new MediaPlayer();
                }
                mediaPlayer.reset();
                Uri uri = Uri.parse(path);
                try {
                    mediaPlayer.setDataSource(MediaRecoderActivity.this,uri);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });


        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }
    public  void  recorder(){

    }
    /**
     *
     *   获取系统时间
     * @return
     */
    public  static  String getDate(){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int minute = calendar.get(Calendar.MINUTE);
        int hour = calendar.get(Calendar.HOUR);
        int second  = calendar.get(Calendar.SECOND);
        String  date =  ""+year+(month+1)+day+hour+minute+second;
        Log.d(TAG, "getDate: "+date);
        return date;
    }

    /**
     *   获取SD path
     * @param
     */
    public  String getSDPath(){
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (sdCardExist){
            sdDir = Environment.getExternalStorageDirectory();
            Log.d(TAG, "getSDPath: "+sdDir);
            return sdDir.toString();
        }
        return null;
    }
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
                this.surfaceHolder = surfaceHolder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            this.surfaceHolder = surfaceHolder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            surfaceView = null;
            surfaceHolder = null;
            handler.removeCallbacks(runnable);
            if (recorder!=null){
                recorder.release();
                recorder  = null;
                Log.d(TAG, "surfaceDestroyed release mRecorder");
            }
            if (camera!=null){
                camera.release();
                camera = null;
            }
            if (mediaPlayer!=null){
                mediaPlayer.release();
                mediaPlayer = null;
            }
    }
}
