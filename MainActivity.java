package com.babagroup.link;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.Toast;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.app.PendingIntent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends Activity
{
    private static final int OVERLAY_PERMISSION_CODE = 1000;

    public static final int NOTIFICATION_ID = 17;
    private static final String CHANNEL_ID = "stop_app_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        izin();
    }

    private void izin()
    {
        // Memeriksa apakah izin sudah diberikan
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this))
        {
            // Jika izin belum diberikan, buka pengaturan untuk meminta izin
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_CODE);
        }
        else
        {
            // Jika izin sudah diberikan, lanjutkan dengan memanggil layanan PopupService
            mainFunction();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_CODE)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this))
            {
                // Izin diberikan, lanjutkan dengan memanggil layanan PopupService
                Toast.makeText(this, "TERIMA KASIH", Toast.LENGTH_LONG).show();
                mainFunction();
            }
            else
            {
                // Jika pengguna menolak izin, beri tahu pengguna dan mungkin tampilkan pesan tambahan
                Toast.makeText(this, "Izin overlay ditolak. Aplikasi tidak dapat menampilkan pop-up.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void mainFunction()
    {
        showNotification();
        
        // Mendapatkan ClipboardManager
        final ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        // Menambahkan listener untuk ClipboardManager
        clipboardManager.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {

                @Override
                public void onPrimaryClipChanged()
                {
                    // Ambil teks yang disalin dari clipboard
                    String copiedText = clipboardManager.getPrimaryClip().getItemAt(0).getText().toString();

                    Intent serviceIntent = new Intent(MainActivity.this, PopupService.class);
                    serviceIntent.putExtra("TEKS_CLIPBOARD", copiedText);
                    startService(serviceIntent);
                }
            });
    }

    public void stopApp(View v)
    {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
        
        finishAffinity();
    }

    public void showNotification()
    {
        createNotificationChannel();

        Intent stopAppIntent = new Intent(this, StopAppReceiver.class);
        PendingIntent stopAppPendingIntent = PendingIntent.getBroadcast(this, 0, stopAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            //.setSmallIcon(android.R.drawable.ic_notification_overlay)
            .setSmallIcon(R.drawable.i)
            .setContentTitle("Running")
            //.setContentText("Tap untuk membuka aplikasi")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "STOP", stopAppPendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
