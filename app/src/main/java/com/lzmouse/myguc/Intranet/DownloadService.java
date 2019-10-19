package com.lzmouse.myguc.Intranet;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.ImageView;

import com.lzmouse.myguc.Helper;
import com.lzmouse.myguc.Login.NTLMAuthenticator;
import com.lzmouse.myguc.Login.Student;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadService extends IntentService {
    private static final String TAG = "DownloadService";

    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_NAME = "extra_name";

    public DownloadService() {
        super("DownloadService");
    }

    private static final int ID = 0;
    private static final String CHANNEL = "DOWNLOAD_CHANNEL";

    private String url;
    private String name;
    private long max;
    private NotificationCompat.Builder builder;
    private NotificationManager manager;
    private File downloadFile;
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            manager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
            url = intent.getStringExtra(EXTRA_URL);
            name = intent.getStringExtra(EXTRA_NAME);
            setupNotification();
            new DownloadTask().execute();
        }
    }

    public void initChannels() {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(CHANNEL,
                "Download Files",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setBypassDnd(true);
        channel.setImportance(NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Notify user when downloading a file from intranet");
        notificationManager.createNotificationChannel(channel);
    }
    private void setupNotification() {
        initChannels();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            builder = new NotificationCompat.Builder(this, CHANNEL);
        else
            builder = new NotificationCompat.Builder(this);

        builder.setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle(name)
                .setContentText("Downloading " + name)
                .setProgress(0, 0, true)
                .setOngoing(true);
        manager.notify(ID, builder.build());

    }
    private class DownloadTask extends AsyncTask<Void,Void,Void>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Log.d(TAG,url);
                final String path;

                OkHttpClient client;
                if(Student.getInstance()!= null)
                {
                    path = "http://student.guc.edu.eg" +url;
                    client = new OkHttpClient.Builder()
                            .authenticator(new NTLMAuthenticator(Student.getInstance().getUsername(), Student.getInstance().getPassword()))
                            .build();
                }
                else
                {
                    path = url;
                   client =  new OkHttpClient.Builder()
                            .build();
                }
                Response response = client.newCall(new Request.Builder().url(path).build()).execute();
                InputStream reader = response.body().byteStream();
                long len = response.body().contentLength();
                max = len;

                manager.notify(ID,builder.build());
                ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
                byte[] b = new byte[1024];
                int t = 0;
                long lastTime = System.currentTimeMillis();
                while (true) {
                    int read = reader.read(b);
                    if (read == -1)
                        break;
                    arrayOutputStream.write(b, 0, read);
                    if(System.currentTimeMillis() - lastTime >= 3000)
                    {
                        lastTime = System.currentTimeMillis();


                        manager.notify(ID,builder.setProgress((int)(len / 100),t / 100,false).build());

                    }
                    t += read;
                }
                Log.d(TAG, t + "");
                Log.d(TAG, len + "");
                File folder = new File(Environment.getExternalStorageDirectory(), "My Guc");
                folder.mkdirs();
                downloadFile = Helper.createNewFile(new File(folder.getAbsolutePath(), name),name);
                FileOutputStream fileOutputStream = new FileOutputStream(downloadFile);
                fileOutputStream.write(arrayOutputStream.toByteArray());
                fileOutputStream.close();
                arrayOutputStream.close();
                response.body().close();
                Log.d(TAG, "File Downloaded to " + downloadFile.getAbsolutePath());

            }catch (IOException e) {
                Log.e(TAG, "Downloading file...", e);
            }

            return null;
        }



        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(downloadFile != null) {
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0
                        , Helper.getFileIntent(getApplicationContext(), downloadFile.getAbsolutePath(), Helper.getMimeType(downloadFile.getName()))
                        , PendingIntent.FLAG_UPDATE_CURRENT);
                manager.notify(ID, builder.setProgress(0, 0, false)
                        .setContentTitle(name)
                        .setSmallIcon(android.R.drawable.stat_sys_download_done)
                        .setAutoCancel(true)
                        .setContentText("File Downloaded")
                        .setContentIntent(pendingIntent)
                        .setOngoing(false)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .build());
            }
            else
                manager.cancel(ID);
        }
    }

}
