package com.theporouscity.flagging.util;

import com.theporouscity.flagging.ilx.ILXAccount;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * Created by lukas on 12/10/16.
 */

public class CookieGrabber implements CookieJar {
    private HashMap<ILXAccount, List<Cookie>> cookieStore = new HashMap<>();
    private ILXAccount mActiveAccount = null;

    public void setActiveAccount(ILXAccount activeAccount) {
        mActiveAccount = activeAccount;
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        if (mActiveAccount != null) {
            cookieStore.put(mActiveAccount, cookies);
        }
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        if (mActiveAccount != null) {
            List<Cookie> cookies = cookieStore.get(mActiveAccount);
            return cookies != null ? cookies : new ArrayList<Cookie>();
        }
        return new ArrayList<Cookie>();
    }

    public List<Cookie> getAccountCookies(ILXAccount account) {
        return cookieStore.get(account);
    }
}
