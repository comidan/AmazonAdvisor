package com.dev.amazonadvisor;

import android.app.Activity;

import java.util.Locale;

/**
 * Created by daniele on 02/05/2017.
 */

public class AmazonLocaleUtils {

    static Locale locale = null;
    private static Activity activity;

    static void setLocale(Activity actitity)
    {
        AmazonLocaleUtils.activity = actitity;
        locale = getLocale(actitity);
    }

    private static Locale getLocale(Activity activity)
    {
        return activity.getResources().getConfiguration().locale;
    }

    static String getLocalizedAWSURL()
    {
        if(locale == null)
            locale = getLocale(activity);

        if(locale.equals(Locale.ITALY))
            return "webservices.amazon.it";
        if(locale.equals(Locale.US))
            return "ecs.amazonaws.com";
        if(locale.equals(Locale.CANADA))
            return "ecs.amazonaws.ca";
        if(locale.equals(Locale.UK))
            return "ecs.amazonaws.co.uk";
        if(locale.equals(Locale.FRANCE))
            return "ecs.amazonaws.fr";
        if(locale.equals(Locale.GERMANY))
            return "ecs.amazonaws.de";
        if(locale.equals(Locale.JAPAN))
            return "ecs.amazonaws.jp";
        return "ecs.amazonaws.com";
    }

    static String getLocalizedURL()
    {
        if(locale == null)
            locale = getLocale(activity);

        if(locale.equals(Locale.ITALY))
            return "www.amazon.it";
        if(locale.equals(Locale.US))
            return "www.amazon.com";
        if(locale.equals(Locale.CANADA))
            return "www.amazon.ca";
        if(locale.equals(Locale.UK))
            return "www.amazon.co.uk";
        if(locale.equals(Locale.FRANCE))
            return "www.amazon.fr";
        if(locale.equals(Locale.GERMANY))
            return "www.amazon.de";
        if(locale.equals(Locale.JAPAN))
            return "www.amazon.jp";
        return "www.amazon.com";
    }
}
