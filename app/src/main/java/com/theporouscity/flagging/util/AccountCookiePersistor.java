package com.theporouscity.flagging.util;

import android.content.Context;

import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.theporouscity.flagging.ilx.ILXAccount;

/**
 * Created by lukas on 12/23/16.
 */

public class AccountCookiePersistor extends SharedPrefsCookiePersistor {
    public AccountCookiePersistor(Context context, ILXAccount account) {
        super(context.getSharedPreferences(account.getDomain() + account.getInstance() + account.getUsername(), Context.MODE_PRIVATE));
    }
}
