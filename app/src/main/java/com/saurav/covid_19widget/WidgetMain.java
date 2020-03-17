package com.saurav.covid_19widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Implementation of App Widget functionality.
 */
public class WidgetMain extends AppWidgetProvider {
    private static final String REST_END_POINT = "https://coronavirus-19-api.herokuapp.com/countries";
    public int total, cases_today, recovered, deaths;
    private final int INTERVAL_MILLIS = 10000;
    public static final String ACTION_AUTO_UPDATE = "com.saurav.covid_19widget.AUTO_UPDATE";
    private final int ALARM_ID = 0;

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                         int appWidgetId) {

        //CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        //views.setTextViewText(R.id.heading, widgetText);
        //if(total != 0) {
        views.setTextViewText(R.id.total, "Total: " + total);
        views.setTextViewText(R.id.cases_today, "Cases Today: " + cases_today);
        views.setTextViewText(R.id.discharged, "Discharged: " + recovered);
        views.setTextViewText(R.id.deaths, "Deaths: " + deaths);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
        //}
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        //super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.d("s@urax", "Updating WIdget!");
        //context.registerReceiver(this,new IntentFilter(ACTION_AUTO_UPDATE));
        //startAlarm(context);
        fetchDataFromWeb(context, appWidgetManager, appWidgetIds);
       /* for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }*/
    }

    @Override
    public void onEnabled(Context context) {
        Log.d("s@urax", "onenabled! Widget!");
        //startAlarm(context);
        //context.getApplicationContext().registerReceiver(this,new IntentFilter(ACTION_AUTO_UPDATE));
        //startAlarm(context);
    }

    @Override
    public void onDisabled(Context context) {
        Log.d("s@urax", "DIsabled WIdget!");
        // Enter relevant functionality for when the last widget is disabled
    }

    public void fetchDataFromWeb(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
       /* OkHttpClient client = new OkHttpClient();
        /*client.setConnectTimeout(2, TimeUnit.MINUTES);
        client.setReadTimeout(2,TimeUnit.MINUTES);
        client.setWriteTimeout(2, TimeUnit.MINUTES);*/


        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(REST_END_POINT)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("s@urax", "Request Failed:" + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                Log.d("s@urax", "Response:" + responseStr);
                try {
                    JSONArray jsonarray = new JSONArray(responseStr);
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonobject = jsonarray.getJSONObject(i);
                        if (jsonobject.get("country").equals("India")) {
                            Log.d("s@urax", "Json:" + jsonobject.toString());
                            total = jsonobject.getInt("cases");
                            cases_today = jsonobject.getInt("todayCases");
                            recovered = jsonobject.getInt("recovered");
                            deaths = jsonobject.getInt("deaths");
                            for (int appWidgetId : appWidgetIds) {
                                updateAppWidget(context, appWidgetManager, appWidgetId);
                            }
                        }
                    }

                } catch (JSONException e) {
                    Log.d("s@urax", "Exception:" + e);
                    e.printStackTrace();
                }
            }
        });
    }

    public void startAlarm(Context mCtx) {
        Log.d("s@urax", "Starting Alarm!");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MILLISECOND, INTERVAL_MILLIS);

        Intent alarmIntent = new Intent(ACTION_AUTO_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mCtx, ALARM_ID, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) mCtx.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 5000
                , 2 * 60 * 1000,
                pendingIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        //This is never fired as of now
        //Updating at most on 30 mins as per developer docs:
        //https://developer.android.com/reference/android/appwidget/AppWidgetProviderInfo.html#updatePeriodMillis

        Log.d("s@urav ", "Widget Receiver:" + intent.getAction());
        if (intent.getAction().equals(ACTION_AUTO_UPDATE)) {
            //Log.d("s@urav ", "Alarm Fired ");
            /*Intent updateIntent = new Intent(context.getApplicationContext(), WidgetMain.class);
            intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
            int ids[] = AppWidgetManager.getInstance(context.getApplicationContext()).getAppWidgetIds(new ComponentName(context.getApplicationContext(), WidgetMain.class));
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
            context.getApplicationContext().sendBroadcast(intent);*/
           /* Intent broadcast = new Intent(ACTION_AUTO_UPDATE);
            ComponentName componentName = new ComponentName(context, WidgetMain.class);
            broadcast.setComponent(componentName);
            context.sendBroadcast(broadcast);*/
        }
    }
}

