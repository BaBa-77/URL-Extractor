package com.babagroup.link;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.NotificationManager;

public class StopAppReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        // Memanggil metode untuk menghentikan aplikasi
        stopApp(context);
    }

    private void stopApp(Context context)
    {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(MainActivity.NOTIFICATION_ID);
        
        // Memanggil metode untuk menghentikan layanan, jika diperlukan
        Intent serviceIntent = new Intent(context, PopupService.class);
        context.stopService(serviceIntent);

        // Menghentikan proses aplikasi secara langsung
        android.os.Process.killProcess(android.os.Process.myPid());
        
        abortBroadcast();
    }
}
