package com.lzmouse.myguc.Notebook;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.lzmouse.myguc.AudioBroadCastReciver;
import com.lzmouse.myguc.Helper;
import com.lzmouse.myguc.R;

import java.io.File;
import java.io.IOException;


public class AudioRecordService extends IntentService {
    private final int ID = 136; // Sasa and salah suggested it in CS tutorial.

    private static final String EXTRA_PATH = "com.lzmouse.gucintranet.Notebook.extra.PATH";
    private static final String EXTRA_NAME = "com.lzmouse.gucintranet.Notebook.extra.NAME";

    private NotificationCompat.Builder builder;
    private NotificationManager manager;
    private String path,name;

    public  enum State {RUNNING,STOP,CANCEL}
    public static State state;
    public AudioRecordService() {
        super("AudioRecordService");
    }

    public static void startRecordingService(Context context, String path,String name) {
        Intent intent = new Intent(context, AudioRecordService.class);
        intent.putExtra(EXTRA_PATH, path);
        intent.putExtra(EXTRA_NAME, name);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            path = intent.getStringExtra(EXTRA_PATH);
            name = intent.getStringExtra(EXTRA_NAME);
            state = State.RUNNING;
            setUpNotification();

        }
    }



    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final MediaRecorder mRecorder = new MediaRecorder();
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mRecorder.setOutputFile(path);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                try {
                    mRecorder.prepare();
                } catch (IOException e) {
                    Log.e("Recording", "prepare() failed");
                }
                mRecorder.start();
               while (true)
               {
                   if(state == State.STOP) {
                       mRecorder.stop();
                       mRecorder.release();
                       stopRecording();
                       break;
                   }
                   try {
                       Thread.sleep(1000);
                   } catch (InterruptedException e) {
                       e.printStackTrace();
                   }
               }
            }
        }).start();

    }

    private void stopRecording()
    {
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,
                Helper.getFileIntent(getApplicationContext(),new File(path)),PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentTitle(name)
                .setContentText("Record saved")
                .setOngoing(false)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .mActions.clear();
        manager.notify(ID,builder.build());
        stopSelf();
    }
    private void setUpNotification()
    {
        manager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            builder = new NotificationCompat.Builder(this, NotificationChannel.DEFAULT_CHANNEL_ID);
        else
            builder = new NotificationCompat.Builder(this);
        Intent intent =  new Intent(getApplicationContext(), AudioBroadCastReciver.class);
        intent.setAction(AudioBroadCastReciver.ACTION_STOP);
        PendingIntent stopIntent =
                PendingIntent.getBroadcast(getApplicationContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

//        intent.setAction(AudioBroadCastReciver.ACTION_CANCEL);
//        PendingIntent cancelIntent =
//                PendingIntent.getBroadcast(getApplicationContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentTitle("Recording...")
                .setContentText("Recording " + name )
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_stat_name)
                .addAction(new NotificationCompat.Action(R.drawable.ic_stop,"Stop",stopIntent));

       manager.notify(ID,builder.build());

    }
}
