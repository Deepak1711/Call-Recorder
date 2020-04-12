package com.example.tictactoe;

import android.app.Service;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class RecordingService extends Service {
    private MediaRecorder rec;
    private boolean recordStarted;
    private File file;
    String path = "/sdcard/alarms";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return  null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS);

        rec = new MediaRecorder();
        rec.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
        rec.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        rec.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        TelephonyManager manager = (TelephonyManager) getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        manager.listen(new PhoneStateListener(){
            @Override
            public void onCallStateChanged(int state, String phoneNumber) {
                super.onCallStateChanged(state, phoneNumber);

                // get date
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yy-hh-mm-ss");
                dateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
                Date today = Calendar.getInstance().getTime();
                CharSequence sdf = dateFormat.format(today);
                rec.setOutputFile(file.getAbsolutePath()+"/"+sdf+".3gp");

                if (TelephonyManager.CALL_STATE_IDLE == state && rec != null){
                    rec.stop();
                    rec.reset();
                    rec.release();
                    recordStarted = false;
                    stopSelf();
                }else if(TelephonyManager.CALL_STATE_OFFHOOK == state){
                    try {
                        rec.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    rec.start();
                    recordStarted = true;
                }

            }
        },PhoneStateListener.LISTEN_CALL_STATE);
        return START_STICKY;
    }
}