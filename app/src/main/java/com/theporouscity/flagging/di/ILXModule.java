package com.theporouscity.flagging.di;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import com.theporouscity.flagging.ILXRequestor;
import com.theporouscity.flagging.PollClosingDate;
import com.theporouscity.flagging.UserAppSettings;

import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.transform.Matcher;
import org.simpleframework.xml.transform.Transform;

import java.util.Date;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

@Module
public class ILXModule {

    private Application application;

    public ILXModule(Application application) {
        this.application = application;
    }

    @Provides
    @Singleton
    public Persister persister() {
        ILXRequestor.ILXDateTransform transform1 = new ILXRequestor.ILXDateTransform();
        ILXRequestor.ILXPollDateTransform transform2 = new ILXRequestor.ILXPollDateTransform();

        return new Persister(new Matcher() {
            @Override
            public Transform match(Class cls) throws Exception {
                if (cls == Date.class) {
                    return transform1;
                } else if (cls == PollClosingDate.class) {
                    return transform2;
                }
                return null;
            }
        });
    }

    @Provides
    @Singleton
    public OkHttpClient okHttpClient() {
        return new OkHttpClient();
    }

    @Provides
    @Singleton
    public ILXRequestor ilxRequestor(OkHttpClient okHttpClient, Persister persister) {
        return new ILXRequestor(okHttpClient, persister);
    }

    @Provides
    @Singleton
    public UserAppSettings userAppSettings(ILXRequestor ilxRequestor) {
        Context context = application.getApplicationContext();
        return ilxRequestor.getUserAppSettings(context);
    }
}
