package com.dev.amazonadvisor;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by daniele on 14/06/2017.
 */

public class ServiceStarter extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("Hello motherfuckers");
        if(!isMyServiceRunning(UpdateListService.class,context))
            context.startService(new Intent(context,UpdateListService.class));
    }

    public static void startServiceWithRunningControl(Intent intent, Class<?> serviceClass, Context context)
    {
        if(!isMyServiceRunning(serviceClass, context))
            context.startService(intent);
    }

    private static boolean isMyServiceRunning(Class<?> serviceClass,Context context) {
        ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}