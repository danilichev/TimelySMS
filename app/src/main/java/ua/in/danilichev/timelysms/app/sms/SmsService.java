package ua.in.danilichev.timelysms.app.sms;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

public class SmsService extends Service {
    public SmsService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Uri smsUri = intent.getData();
            SmsSender sms = new SmsSender(this, smsUri);
            sms.send();
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
