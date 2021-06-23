package com.example.exampleproject;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;

import static android.media.CamcorderProfile.get;

public class MainActivity extends AppCompatActivity {
    private int count = 0;
    private ShortcutManager shortcutManager;
    ShortcutInfoCompat shortcut;
    private final String shortcut_id = "id_shortcut";

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shortcutAdd();
            }
        });
    }

    private void shortcutAdd() {
        // Intent to be send, when shortcut is pressed by user ("launched")
        Icon icon = Icon.createWithResource(this,R.mipmap.ic_launcher_2_round);
        ((ImageView) findViewById(R.id.icon)).setImageIcon(icon);

        shortcut = new ShortcutInfoCompat.Builder(this,shortcut_id)
                .setShortLabel("second example")
                .setIcon(IconCompat.createFromIcon(this,icon))
                .setIntent(new Intent(Intent.ACTION_VIEW,null,this,MainActivity.class))
                .build();
        if (Build.VERSION.SDK_INT >= 28) {
            shortcutPin(getApplicationContext(), shortcut_id, 0);
        }
    }

    private void shortcutPin(Context context, String shortcutId, int requestCode){
        ShortcutManagerCompat shortcutManager = getSystemService(ShortcutManagerCompat.class);
        if(shortcutManager.isRequestPinShortcutSupported(this)){
            Intent pinnedShortcutCallbackIntent = shortcutManager.createShortcutResultIntent(context,shortcut);
            PendingIntent successCallback = PendingIntent.getBroadcast(context,requestCode,pinnedShortcutCallbackIntent,0);
            shortcutManager.requestPinShortcut(context,shortcut, successCallback.getIntentSender());
            Toast.makeText(context,"Shortcut Successfully Added!",Toast.LENGTH_LONG).show();
        }
    }
}