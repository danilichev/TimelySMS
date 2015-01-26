package ua.in.danilichev.timelysms.app.sms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.Calendar;

public class SmsAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(
                context, SmsService.class).setData(intent.getData()));
    }

    public void setAlarm(Context context, Calendar time, Uri messageUri) {
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, SmsAlarmReceiver.class);
        intent.setData(messageUri);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingIntent);
    }

    public void cancelAlarm(Context context, Uri messageUri) {
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, SmsAlarmReceiver.class);
        intent.setData(messageUri);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.cancel(pendingIntent);
    }
}
