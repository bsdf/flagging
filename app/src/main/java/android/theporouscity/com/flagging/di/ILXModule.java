package android.theporouscity.com.flagging.di;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.theporouscity.com.flagging.ILXRequestor;
import android.theporouscity.com.flagging.PollClosingDate;
import android.theporouscity.com.flagging.UserAppSettings;

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
        UserAppSettings settings = new UserAppSettings(ilxRequestor);
        Context context = application.getApplicationContext();
        SharedPreferences mPreferences = context.getSharedPreferences(ILXRequestor.ILX_SERVER_TAG, Context.MODE_PRIVATE);

        int loadPrettyPictures = mPreferences.getInt(UserAppSettings.LoadPrettyPicturesSettingKey, -1);
        if (loadPrettyPictures == -1 || loadPrettyPictures == 0) {
            settings.setLoadPrettyPicturesSetting(UserAppSettings.LoadPrettyPicturesSetting.NEVER);
        } else if (loadPrettyPictures == 1) {
            settings.setLoadPrettyPicturesSetting(UserAppSettings.LoadPrettyPicturesSetting.ALWAYS);
        } else if (loadPrettyPictures == 2) {
            settings.setLoadPrettyPicturesSetting(UserAppSettings.LoadPrettyPicturesSetting.WIFI);
        }

        int pretendToBeLoggedIn = mPreferences.getInt(UserAppSettings.PretendToBeLoggedInKey, -1);
        if (pretendToBeLoggedIn == 1) {
            settings.setPretendToBeLoggedInSetting(true);
        } else {
            settings.setPretendToBeLoggedInSetting(false);
        }

        return settings;
    }
}
