package com.theporouscity.flagging.di;

import com.theporouscity.flagging.ActivityMainTabs;
import com.theporouscity.flagging.AddEditAccountFragment;
import com.theporouscity.flagging.SettingsFragment;
import com.theporouscity.flagging.StartActivity;
import com.theporouscity.flagging.ThreadReplyFragment;
import com.theporouscity.flagging.ViewBoardsFragment;
import com.theporouscity.flagging.ViewMessageFragment;
import com.theporouscity.flagging.ViewThreadFragment;
import com.theporouscity.flagging.ViewThreadsFragment;
import com.theporouscity.flagging.ilx.ILXAccount;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = ILXModule.class)
public interface ILXComponent {
    void inject(ActivityMainTabs activityMainTabs);
    void inject(ViewThreadsFragment viewThreadsFragment);
    void inject(ViewMessageFragment viewMessageFragment);
    void inject(SettingsFragment settingsFragment);
    void inject(AddEditAccountFragment editAccountFragment);
    void inject(ViewThreadFragment viewThreadFragment);
    void inject(ViewBoardsFragment viewBoardsFragment);
    void inject(StartActivity startActivity);
    void inject(ThreadReplyFragment threadReplyFragment);
}
