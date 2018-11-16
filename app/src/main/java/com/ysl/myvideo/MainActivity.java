package com.ysl.myvideo;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.SurfaceTexture;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "tag";
    @BindView(R.id.textureView)
    TextureView textureView;

    private String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/DCIM/Camera/VID_20180325_212926.mp4";
    private OrientationEventListener orientationEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initPermission();
        initMediaPlayer();
        initTextureView();
        textureView.setSurfaceTextureListener(surfaceTextureListener);

        orientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                Toast.makeText(MainActivity.this, "Orientation changed to " + orientation, Toast.LENGTH_SHORT).show();
                int screenOrientation = getResources().getConfiguration().orientation;
                if (((orientation >= 0) && (orientation < 45)) || (orientation > 315)) {//设置竖屏
                    if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT && orientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
                        Log.d(TAG, "设置竖屏");
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                } else if (orientation > 225 && orientation < 315) { //设置横屏
                    Log.d(TAG, "设置横屏");
                    if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    }
                } else if (orientation > 45 && orientation < 135) {// 设置反向横屏
                    Log.d(TAG, "反向横屏");
                    if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                    }
                } else if (orientation > 135 && orientation < 225) {
                    Log.d(TAG, "反向竖屏");
                    if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                    }
                }
            }
        };
        boolean autoRotateOn = (android.provider.Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1);
        //检查系统是否开启自动旋转
        if (autoRotateOn) {
            orientationEventListener.enable();
        }
    }

    private void initTextureView() {
        textureView.setAlpha(0.8f);
        textureView.setScaleX(1);
        textureView.setScaleY(0.8f);
    }

    private void initPermission() {
        ActivityCompat.requestPermissions(this, new String[]{permission.READ_EXTERNAL_STORAGE}, 0);
    }


    @OnClick({R.id.play, R.id.pause, R.id.goon, R.id.restart, R.id.horizontal, R.id.vertial})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.play:
                Toast.makeText(MainActivity.this,"开始播放", Toast.LENGTH_SHORT).show();
                if (chechPermission()) {
                    play();
                }else {
                    Toast.makeText(MainActivity.this,"请打开权限！", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.pause:
                pause();
                break;
            case R.id.goon:
                goon();
                break;
            case R.id.restart:
                restart();
                break;
            case R.id.horizontal:
                horizontal();
                break;
            case R.id.vertial:
                vertial();
                break;
        }
    }

    private void vertial() {
        textureView.setRotation(0);
    }

    private void horizontal() {
        textureView.setRotation(90);
    }

    private void play() {
        if (mMediaPlayer == null){
            return;
        }
        if (mMediaPlayer.isPlaying()){
            return;
        }
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(this, Uri.parse(path));
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void restart() {
        if (mMediaPlayer == null){
            return;
        }
        mMediaPlayer.stop();
        play();
    }

    private void goon() {
        if (mMediaPlayer == null){
            return;
        }
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }

    private void pause() {
        if (mMediaPlayer == null){
            return;
        }
        mMediaPlayer.pause();
    }

    private boolean chechPermission() {
        return PermissionChecker.checkSelfPermission(this, permission.READ_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED;
    }

    private void initMediaPlayer() {
        AudioManager mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int mVolumn = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {

                }
            });
            mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Toast.makeText(MainActivity.this,"播放完成！继续从头播放", Toast.LENGTH_SHORT).show();
                    restart();
                }
            });
            mMediaPlayer.setOnErrorListener(new OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    return false;
                }
            });
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnSeekCompleteListener(new OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mp) {

                }
            });
            mMediaPlayer.setVolume(mVolumn, mVolumn);

        } else {
            mMediaPlayer.reset();
        }
    }


    private MediaPlayer mMediaPlayer;
    private SurfaceTexture surfaceTexture;
    private SurfaceTextureListener surfaceTextureListener = new SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            if (mMediaPlayer == null){
                return;
            }
            mMediaPlayer.setSurface(new Surface(surface));
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            if (mMediaPlayer != null){
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        orientationEventListener.disable();
    }
}
