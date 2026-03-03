package com.yourname.notifalarm;
import android.content.*;
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // NotificationListenerService re-binds automatically after reboot
    }
}
