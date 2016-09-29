package fantaq.fanta.alarm;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Vibrator;

import fantaq.fanta.App;

public class AlarmService extends Service {
    private final static int VIBRATE_DELAY_TIME = 2000;
    private final static int DURATION_OF_VIBRATION = 1000;
    private final static int VOLUME_INCREASE_DELAY = 600;
    private final static float VOLUME_INCREASE_STEP = 0.01f;
    private final static float MAX_VOLUME = 1.0f;
    private MediaPlayer mPlayer;
    private Vibrator mVibrator;
    private float mVolumeLevel = 0;

    private Handler mHandler = new Handler();
    private Runnable mVibrationRunnable = new Runnable() {
        @Override
        public void run() {
            mVibrator.vibrate(DURATION_OF_VIBRATION);
            mHandler.postDelayed(mVibrationRunnable,
                    DURATION_OF_VIBRATION + VIBRATE_DELAY_TIME);
        }
    };

    private Runnable mVolumeRunnable = new Runnable() {
        @Override
        public void run() {
            if (mPlayer != null && mVolumeLevel < MAX_VOLUME) {
                mVolumeLevel += VOLUME_INCREASE_STEP;
                mPlayer.setVolume(mVolumeLevel, mVolumeLevel);
                mHandler.postDelayed(mVolumeRunnable, VOLUME_INCREASE_DELAY);
            }
        }
    };

    private MediaPlayer.OnErrorListener mErrorListener = (mp, what, extra) -> {
        mp.stop();
        mp.release();
        mHandler.removeCallbacksAndMessages(null);
        AlarmService.this.stopSelf();
        return true;
    };

    @Override
    public void onCreate() {
        HandlerThread ht = new HandlerThread("alarm_service");
        ht.start();
        mHandler = new Handler(ht.getLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startPlayer();
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.setComponent(new ComponentName(this, AlarmActivity.class));
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(i);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (mPlayer.isPlaying()) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
        mHandler.removeCallbacksAndMessages(null);
    }

    private void startPlayer() {
        mPlayer = new MediaPlayer();
        mPlayer.setOnErrorListener(mErrorListener);

        try {
            if (App.getState().settings().vibrate()) {
                mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                mHandler.post(mVibrationRunnable);
            }
            String ringtone = App.getState().settings().ringtone();
            if (ringtone.startsWith("content://media/external/audio/media/") &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString();
            }
            mPlayer.setDataSource(this, Uri.parse(ringtone));
            mPlayer.setLooping(true);
            mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mPlayer.setVolume(mVolumeLevel, mVolumeLevel);
            mPlayer.prepare();
            mPlayer.start();

            if (App.getState().settings().ramping()) {
                mHandler.postDelayed(mVolumeRunnable, VOLUME_INCREASE_DELAY);
            }
        } catch (Exception e) {
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
            }
            stopSelf();
        }
    }
}
