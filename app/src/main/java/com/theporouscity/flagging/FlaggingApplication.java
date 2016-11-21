package com.theporouscity.flagging;

import android.app.Application;
import com.theporouscity.flagging.di.DaggerILXComponent;
import com.theporouscity.flagging.di.ILXComponent;
import com.theporouscity.flagging.di.ILXModule;

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
