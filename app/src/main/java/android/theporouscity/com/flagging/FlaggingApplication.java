package android.theporouscity.com.flagging;

import android.app.Application;
import android.theporouscity.com.flagging.di.DaggerILXComponent;
import android.theporouscity.com.flagging.di.ILXComponent;
import android.theporouscity.com.flagging.di.ILXModule;

public class FlaggingApplication extends Application {

    private ILXComponent ilxComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        ilxComponent = DaggerILXComponent.builder()
                .iLXModule(new ILXModule(this))
                .build();
    }

    public ILXComponent getILXComponent() {
        return ilxComponent;
    }
}
