package com.lunartech.lovelymalang;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by aryo on 8/28/16.
 */
public class BlackholeCookieStore implements CookieStore {

    @Override
    public void addCookie(Cookie cookie) {
    }

    @Override
    public List<Cookie> getCookies() {
        return new ArrayList<Cookie>();
    }

    @Override
    public boolean clearExpired(Date date) {
        return true;
    }

    @Override
    public void clear() {
    }
}