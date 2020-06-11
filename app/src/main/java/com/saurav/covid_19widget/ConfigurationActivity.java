package com.saurav.covid_19widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class ConfigurationActivity extends AppCompatActivity {
    Button doneBtn;
    Spinner choosenCountry;
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_configuration);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        setTitle(R.string.choose_country);
        choosenCountry = findViewById(R.id.spinner);
        doneBtn = findViewById(R.id.done);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppWidgetProviderInfo appWidgetManager = AppWidgetManager.getInstance(getBaseContext()).getAppWidgetInfo(mAppWidgetId);
                SharedPreferences pref = getApplicationContext().getSharedPreferences("CovidwidgetPref", 0);
                SharedPreferences.Editor editor = pref.edit();
                Log.d("s@urax", "ChoosenCountry is:" + choosenCountry.getSelectedItem() + " Widgetid:" + mAppWidgetId);
                //selectedCountry = choosenCountry.getSelectedItem().toString();
                editor.putString("selected_country", choosenCountry.getSelectedItem().toString()/*+mAppWidgetId*/);
                editor.putString("widget_id_"+mAppWidgetId, choosenCountry.getSelectedItem().toString() + mAppWidgetId);
                editor.commit();
                Intent intent = new Intent();
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(Activity.RESULT_OK, intent);
                finish();
                updateWidgets(getApplicationContext());
                //return mAppWidgetId;
            }
        });
    }

    public static void updateWidgets(Context context) {
        Intent intent = new Intent(context, WidgetMain.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        //Background Hue chooser Logic

        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        int[] ids = widgetManager.getAppWidgetIds(new ComponentName(context, WidgetMain.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }

}
