package com.lzmouse.myguc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lzmouse.myguc.Notebook.AudioRecordService;

public class AudioBroadCastReciver extends BroadcastReceiver {

    public static String ACTION_STOP  = "action_stop";
    public static String ACTION_CANCEL = "action_cancel";
    @Override
    public void onReceive(Context context, Intent intent) {
       if(intent.getAction().equals(ACTION_STOP))
           AudioRecordService.state = AudioRecordService.State.STOP;
       else
           AudioRecordService.state = AudioRecordService.State.CANCEL;

    }
}
