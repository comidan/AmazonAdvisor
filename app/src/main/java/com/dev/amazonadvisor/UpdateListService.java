package com.dev.amazonadvisor;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by daniele on 14/06/2017.
 */

public class UpdateListService extends Service {

        @Override
        public void onCreate() {
            super.onCreate();
            Intent myService = new Intent(this, UpdateListService.class);
            PendingIntent pendingIntent = PendingIntent.getService(
                    this, 0, myService, 0);

            AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HALF_HOUR,
                    AlarmManager.INTERVAL_HALF_HOUR, pendingIntent);
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            final String listAddress = getSharedPreferences("LIST_DATA", Activity.MODE_PRIVATE).getString("LIST_LINK", "");

                new AsyncTask<Void, Void, Void>()
                {

                    @Override
                    protected Void doInBackground(Void... voids) {
                        try
                        {
                            String fetchedListAddress;
                            if (listAddress.equals(""))
                                fetchedListAddress = AmazonListAPI.fetchListLink(UpdateListService.this);
                            else
                                fetchedListAddress = listAddress;
                            BufferedReader listResponse = AmazonListAPI.initConnection(fetchedListAddress);
                            ArrayList<String> productIDs = AmazonListAPI.fetchListProductsASIN(listResponse);
                            AmazonListAPI.hiddenUpdateListContent(productIDs, UpdateListService.this);
                        }
                            catch (IOException exc)
                            {
                                exc.printStackTrace();
                            }
                            return null;
                    }
                }.execute();

            return START_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
}
