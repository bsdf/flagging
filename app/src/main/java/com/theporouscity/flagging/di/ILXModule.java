package com.theporouscity.flagging.di;

import android.app.Application;
import android.content.Context;

import com.theporouscity.flagging.util.ILXRequestor;
import com.theporouscity.flagging.util.PollClosingDate;
import com.theporouscity.flagging.util.UserAppSettings;

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

        return new OkHttpClient.Builder()
                .build();
    }

    @Provides
    @Singleton
    public ILXRequestor ilxRequestor(Persister persister, OkHttpClient client) {
        return new ILXRequestor(persister, client);
    }

    @Provides
    @Singleton
    public UserAppSettings userAppSettings(ILXRequestor ilxRequestor) {
        Context context = application.getApplicationContext();
        return ilxRequestor.getUserAppSettings(context);
    }
}
