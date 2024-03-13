package com.babagroup.link;

import android.app.Service;
import android.os.IBinder;
import android.widget.Toast;
import android.os.Handler;
import android.content.Intent;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.content.Context;
import android.view.View;
import android.content.DialogInterface;
import android.widget.LinearLayout;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import android.widget.Button;
import android.graphics.Color;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.net.Uri;
import java.util.ArrayList;
import android.graphics.Typeface;
import android.view.View.OnClickListener;
import android.view.MotionEvent;

public class PopupService extends Service
{
    private Handler handler;

    @Override
    public void onCreate()
    {
        super.onCreate();
        handler = new Handler(getMainLooper());
    }

    private void showToast(final String message)
    {
        handler.post(new Runnable() {
                @Override
                public void run()
                {
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                }
            });
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (intent != null)
        {
            String str_teks = intent.getStringExtra("TEKS_CLIPBOARD");
            
            ArrayList<String> list_Link = extractLink(str_teks);
            int count_Link = list_Link.size();
            
            if (count_Link > 1)
            {
                pop(list_Link);
            }
            else if (count_Link == 1)
            {
                showToast(list_Link.get(0));
                openURL(list_Link.get(0));
            }
        }
        
        return START_STICKY;
    }
    
    private void pop(ArrayList<String> list_Link)
    {
        showToast("List");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.list, null);
        //builder.setView(layout);
        //builder.setCancelable(true);

        /*builder.setPositiveButton("TUTUP", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface p1, int p2)
                {
                    p1.dismiss();
                }
            });*/

        //LinearLayout mainLayout = layout.findViewById(R.id.listMainLayout);
        Button btn_X = layout.findViewById(R.id.listButtonX);
        Button btn_Collapse = layout.findViewById(R.id.listButtonCollapse);
        Button btn_Move = layout.findViewById(R.id.listButtonMove);
        final LinearLayout theLayout = layout.findViewById(R.id.theList);

        for (final String linkURL : list_Link)
        {
            Button btn_Link = new Button(this);
            btn_Link.setText(linkURL);
            btn_Link.setTextColor(Color.BLUE);
            btn_Link.setTextSize(15);
            btn_Link.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
            btn_Link.setId(View.generateViewId());
            btn_Link.setAllCaps(false);
            btn_Link.setMaxLines(2);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 1, 0, 1);
            theLayout.addView(btn_Link, params);

            btn_Link.setOnClickListener(new View.OnClickListener() 
                {
                    @Override
                    public void onClick(View p1)
                    {
                        showToast(linkURL);
                        openURL(linkURL);
                    }
                });
        }
        
        final AlertDialog dialog = builder.create();
        dialog.setCancelable(true);
        
        Window window = dialog.getWindow();
        window.setGravity(Gravity.CENTER_HORIZONTAL);
        
        if (window != null)
        {
                final WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            );

            // Anda perlu menyesuaikan ini dengan WindowManager milik Anda
            final WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            windowManager.addView(layout, layoutParams);
            
            /*Button btn_X = new Button(this);
            btn_X.setText("X");
            btn_X.setBackgroundColor(Color.DKGRAY);
            btn_X.setTextColor(Color.RED);
            btn_X.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            btn_X.setTextSize(20);
            btn_X.setId(View.generateViewId());
            btn_X.setAllCaps(true);
            btn_X.setPadding(1,1,1,1);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0,5,0,5);
            mainLayout.addView(btn_X, params);*/

            btn_X.setOnClickListener(new Button.OnClickListener() 
                {
                    @Override
                    public void onClick(View p1)
                    {
                        windowManager.removeView(layout);
                        
                        stopSelf();
                    }
                }); 

            //window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            //window.setDimAmount(0.0f);
            //window.setGravity(Gravity.CENTER);
            window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            //window.setBackgroundDrawableResource(android.R.color.darker_gray);
            
            btn_Move.setOnTouchListener(new View.OnTouchListener() {
                    private int initialX;
                    private int initialY;
                    private float initialTouchX;
                    private float initialTouchY;

                    @Override
                    public boolean onTouch(View v, MotionEvent event)
                    {                                    
                        switch (event.getActionMasked())
                        {
                            case MotionEvent.ACTION_DOWN:
                                initialX = layoutParams.x;
                                initialY = layoutParams.y;
                                initialTouchX = event.getRawX();
                                initialTouchY = event.getRawY();
                                
                                v.setBackgroundColor(Color.GREEN);
                                
                                return true;

                            case MotionEvent.ACTION_UP:
                                v.setBackgroundColor(Color.TRANSPARENT);

                                return true;
                                
                            case MotionEvent.ACTION_CANCEL:
                                v.setBackgroundColor(Color.TRANSPARENT);

                                return true;
                                
                            case MotionEvent.ACTION_MOVE:
                                int newX = initialX+(int) (event.getRawX()-initialTouchX);
                                int newY = initialY+(int) (event.getRawY()-initialTouchY);
                                layoutParams.x = newX;
                                layoutParams.y = newY;
                                windowManager.updateViewLayout(layout, layoutParams);

                                return true;
                        }

                        return false;
                    }       
                });
            
            btn_Collapse.setOnClickListener(new Button.OnClickListener()
                {
                    @Override
                    public void onClick(View p1)
                    {
                        //int layoutVis = layout.getVisibility();
                        
                        if (theLayout.getVisibility() == View.VISIBLE)
                        {
                            theLayout.setVisibility(View.GONE);                            
                            ((Button) p1).setText("➕");
                        }
                        else
                        {
                            theLayout.setVisibility(View.VISIBLE);
                            ((Button) p1).setText("➖");
                        }
                    }            
            });
        }
        
        //dialog.show();
    }

    private ArrayList<String> extractLink(String teks)
    {
        Pattern pattern = Pattern.compile("https?:\\/\\/(?:www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b(?:[-a-zA-Z0-9()@:%_\\+.~#?&\\/=]*)");
        Matcher matcher = pattern.matcher(teks);

        ArrayList<String> the_Link = new ArrayList<String>();  

        while (matcher.find())
        {
            the_Link.add(matcher.group());
        }

        return the_Link;
    }

    private void openURL(String theURL)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(theURL));
        startActivity(intent);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        stopSelf();
        // Hentikan layanan jika tidak diperlukan lagi
    }
}
