package com.dev.amazonadvisor;

import android.app.Activity;
import android.content.Context;

import java.util.Locale;

/**
 * Created by daniele on 02/05/2017.
 */

public class AmazonLocaleUtils {

    static Locale locale = null;
    private static Context context;

    static void setLocale(Context context)
    {
        AmazonLocaleUtils.context = context;
        locale = getLocale(context);
    }

    private static Locale getLocale(Context context)
    {
        return context.getResources().getConfiguration().locale;
    }

    static String getLocalizedAWSURL()
    {
        if(locale == null)
            locale = getLocale(context);

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
            locale = getLocale(context);

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

    static String getLocalizedCode()
    {
        if(locale == null)
            locale = getLocale(context);
        return locale.getLanguage();
    }
}
