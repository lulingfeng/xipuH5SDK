package com.startobj.util.lang;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.text.TextUtils;

import com.startobj.util.common.SOCommonUtil;
import com.startobj.util.config.Config;

import java.util.Locale;

public class LocaleHelper {

    private static LocaleHelper instance;
    /*
     * 默认系统地区
     */
    private static Locale mDefaultLocale;

    public static LocaleHelper getInstance() {
        if (instance == null) {
            synchronized (LocaleHelper.class) {
                if (instance == null) {
                    instance = new LocaleHelper();
                }
            }
        }
        return instance;
    }

    public void setDefaultLanguage(Locale local) {
        mDefaultLocale = local;
    }

    /*
     * 获取当前地区context
     * @param context
     * @return
     */
    public Context getLocaleContext(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources(context);
        }
        return updateResourcesLegacy(context);
    }

    /*
     * 获取语言
     */
    public String getLanguage(Context context) {
        if (!SOCommonUtil.hasContext(context))
            return "";
        SharedPreferences instance = context.getSharedPreferences(Config.SDK_SP_NAME, Context.MODE_PRIVATE);
        return instance.getString(Config.SP_LANGUAGE, "");
    }

    /*
     * 设置语言
     */
    public void setLanguage(Context context, String language) {
        if (!SOCommonUtil.hasContext(context) && TextUtils.isEmpty(language))
            return;
        SharedPreferences instance = context.getSharedPreferences(Config.SDK_SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor userEditor = instance.edit();
        userEditor.putString(Config.SP_LANGUAGE, language).apply();
    }

    /*
     * 获取语言对应 locale
     * @param language
     * @return
     */
    public Locale getLocale(Context context) {
        String language = getLanguage(context);
        switch (language) {
            case LanguageType.EN:
                return Locale.ENGLISH;
            case LanguageType.IN_ID:
                return new Locale("in","ID");
            case LanguageType.TH_TH:
                return new Locale("th","TH");
            case LanguageType.ZH_CN:
                return Locale.SIMPLIFIED_CHINESE;
            case LanguageType.ZH_HK:
                return new Locale("zh","HK");
            case LanguageType.ZH_MO:
                return new Locale("zh","MO");
            case LanguageType.ZH_TW:
                return Locale.TRADITIONAL_CHINESE;
            default:
                return mDefaultLocale != null ? mDefaultLocale : Locale.getDefault();
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private Context updateResources(Context context) {
        Locale locale = getLocale(context);
        Locale.setDefault(locale);

        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        configuration.setLocales(new LocaleList(locale));
        return context.createConfigurationContext(configuration);
    }

    @SuppressWarnings("deprecation")
    private Context updateResourcesLegacy(Context context) {
        Locale locale = getLocale(context);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale);
            configuration.setLayoutDirection(locale);
        } else {
            configuration.locale = locale;
        }
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        return context;
    }
}
