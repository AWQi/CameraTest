package com.example.dell.cameratest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.VideoView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public  static  final  int TAKE_PHOTO  = 1;
    private ImageView picture;
    private Button shootingbtn;
    private Uri imageUri;
    private Button video;
    private VideoView player = null;
    public  static  final int  VIDEO_REQUEST_CODE = 2;
    private Button mediaRecorderBtn = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        shootingbtn = findViewById(R.id.shootingbtn);
        picture = findViewById(R.id.picture);
        video = findViewById(R.id.video);
        player = findViewById(R.id.player);


//        mediaRecorderBtn = findViewById(R.id.mediaRecorderBtn);
//        mediaRecorderBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent  =  new Intent(MainActivity.this,MediaRecoderActivity.class);
//                startActivity(intent);
//            }
//        });
        /**
         *
         *      拍照
         */
        shootingbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File outputImage = new File(getExternalCacheDir(),"output_image.jpg");

                try {
                    if (outputImage.exists())outputImage.delete();
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT>=24){
                    imageUri = FileProvider.getUriForFile(MainActivity.this,"shoot",outputImage);
                }else {
                    imageUri = Uri.fromFile(outputImage);
                }

                Intent intent =  new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                startActivityForResult(intent,TAKE_PHOTO);
            }
        });
        /**
         *       录像
         */
        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,30);//时长
//                intent.putExtra(MediaStore.EXTRA_MEDIA_ARTIST,1);
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,1);//画质
                startActivityForResult(intent,VIDEO_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: ");

        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case  TAKE_PHOTO:
                Log.d(TAG, "requestCode: "+requestCode);
                Log.d(TAG, "RESULT_OK: "+RESULT_OK);
                if (resultCode==RESULT_OK){
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        Log.d(TAG, "bitmap: "+bitmap);

                        picture.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
                case  VIDEO_REQUEST_CODE:
                    if (resultCode == RESULT_OK){
                        Uri uri = data.getData();
                        Log.d(TAG, "onActivityResult: "+uri.toString());
                        player.setVideoURI(uri);
                        player.start();
                        // 完成后继续播放来循环
                        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                player.start();
                            }
                        });

                    }
                    break;

            default: break;
        }
    }
}
