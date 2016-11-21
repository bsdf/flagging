package android.theporouscity.com.flagging.di;

import android.theporouscity.com.flagging.ActivityMainTabs;
import android.theporouscity.com.flagging.SettingsFragment;
import android.theporouscity.com.flagging.ViewBoardsFragment;
import android.theporouscity.com.flagging.ViewMessageFragment;
import android.theporouscity.com.flagging.ViewThreadFragment;
import android.theporouscity.com.flagging.ViewThreadsFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = ILXModule.class)
public interface ILXComponent {
    void inject(ActivityMainTabs activityMainTabs);
    void inject(ViewThreadsFragment viewThreadsFragment);
    void inject(ViewMessageFragment viewMessageFragment);
    void inject(SettingsFragment settingsFragment);
    void inject(ViewThreadFragment viewThreadFragment);
    void inject(ViewBoardsFragment viewBoardsFragment);
}
