package com.saurav.covid_19widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Toast;

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
    public static final String ACTION_AUTO_UPDATE = "com.saurav.covid_19widget.AUTO_UPDATE";
    public static final String ACTION_DATA_FETCHED = "com.saurav.covid_19widget.DATA_FETCHED";
    private final int ALARM_ID = 0;

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                         int appWidgetId) {
        SharedPreferences pref = context.getSharedPreferences("CovidwidgetPref", 0);
        String country = pref.getString("selected_country", "Nepal");

        String selected_country_with_id = pref.getString("widget_id_" + appWidgetId, "null");
        int begin = selected_country_with_id.length();
        int end = Integer.toString(appWidgetId).length();
        String country_from_pref = selected_country_with_id.substring(0, (begin - end));
        //CharSequence widgetText = context.getString(R.string.appwidget_text);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        //views.setTextViewText(R.id.heading, widgetText);
        //if(total != 0) {
        views.setOnClickPendingIntent(R.id.refresh, getPendingIntent(context));
        views.setTextViewText(R.id.heading, context.getString(R.string.title) + " : " + country_from_pref);
        views.setTextViewText(R.id.total, "Total: " + total);
        views.setTextViewText(R.id.cases_today, "Cases Today: " + cases_today);
        views.setTextViewText(R.id.discharged, "Discharged: " + recovered);
        views.setTextViewText(R.id.deaths, "Deaths: " + deaths);

       // views.setInt(R.id.background, "setColorFilter", R.color.colorPrimaryDark);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
        //}
    }

    protected PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(ACTION_AUTO_UPDATE);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        //super.onUpdate(context, appWidgetManager, appWidgetIds);
        //context.registerReceiver(this,new IntentFilter(ACTION_AUTO_UPDATE));
        //startAlarm(context);
        SharedPreferences pref = context.getSharedPreferences("CovidwidgetPref", 0);
        String country = pref.getString("selected_country", "null");
        //Log.d("s@urax", "Updating Widget for:" + country);
        if (!country.equals(null)) {
            Log.d("s@urax", "Calling fetchdatafrom web call!");
            fetchDataFromWeb(context, appWidgetManager, appWidgetIds);
        }
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
        Log.d("s@urax", "Disabled Widget!");
        // Enter relevant functionality for when the last widget is disabled
    }

    public void fetchDataFromWeb(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
       /* OkHttpClient client = new OkHttpClient();
        /*client.setConnectTimeout(2, TimeUnit.MINUTES);
        client.setReadTimeout(2,TimeUnit.MINUTES);
        client.setWriteTimeout(2, TimeUnit.MINUTES);*/
        SharedPreferences pref = context.getSharedPreferences("CovidwidgetPref", 0);
        String country = pref.getString("selected_country", "null");
        String selected_country_with_id = pref.getString("selected_country", "null");
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
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
                //Log.d("s@urax", "Response:" + responseStr);
                Intent broadcast = new Intent(ACTION_DATA_FETCHED);
                ComponentName componentName = new ComponentName(context, WidgetMain.class);
                broadcast.setComponent(componentName);
                context.sendBroadcast(broadcast);
                try {
                    JSONArray jsonarray = new JSONArray(responseStr);
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonobject = jsonarray.getJSONObject(i);
                        for (int appWidgetId : appWidgetIds) {
                            //Log.d("s@urax", "All widgets-------------->" + pref.getString("selected_country_with_id", "Nepal"));
                            String selected_country_with_id = pref.getString("widget_id_" + appWidgetId, "null");
                            int begin = selected_country_with_id.length();
                            int end = Integer.toString(appWidgetId).length();
                            String country_from_pref = selected_country_with_id.substring(0, (begin - end));
                            Log.d("s@urax", "Updating for Country:" + country_from_pref);
                            //Log.d("s@urax","Selected Countries from pref are:"+selected_country_with_id);
                            if (jsonobject.get("country").equals(country_from_pref)) {
                                total = jsonobject.getInt("cases");
                                cases_today = jsonobject.getInt("todayCases");
                                recovered = jsonobject.getInt("recovered");
                                deaths = jsonobject.getInt("deaths");
                                updateAppWidget(context, appWidgetManager, appWidgetId);
                            }
                        }
                        //}
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
        calendar.add(Calendar.MILLISECOND, 10000);

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
        //Updating at most on 30 mins as per developer docs:
        //https://developer.android.com/reference/android/appwidget/AppWidgetProviderInfo.html#updatePeriodMillis

        Log.d("s@urav ", "Widget Receiver:" + intent.getAction());
        if (intent.getAction().equals(ACTION_AUTO_UPDATE)) {
            Log.d("s@urav", "Inside OnUpdate!!");
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int appWidgetIds[] = AppWidgetManager.getInstance(context.getApplicationContext()).getAppWidgetIds(new ComponentName(context.getApplicationContext(), WidgetMain.class));
            fetchDataFromWeb(context, appWidgetManager, appWidgetIds);

            Toast.makeText(context, "Fetching data from server.", Toast.LENGTH_SHORT).show();
        } else if (intent.getAction().equals(ACTION_DATA_FETCHED)) {
            Toast.makeText(context, "New data fetched.", Toast.LENGTH_SHORT).show();
        }
    }
}

