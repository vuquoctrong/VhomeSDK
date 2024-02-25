package com.viettel.vht.sdk.ui.jfcameradetail.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.text.TextUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by SJ on 2017-10-30.
 */

public class RingUtils {
    public static final List<Ring> getSystemRings(Context context) {
        RingtoneManager ringtoneManager = new RingtoneManager(context.getApplicationContext());
        Cursor cursor = ringtoneManager.getCursor();
        List<Ring> mRings = new ArrayList<>();
        int pos = 0;
        Ring r = new Ring();
        r.setID(-1);
        r.setTitle("Follow System");
        r.setPos(-1);
        r.setContent(null);
        r.setTitleKey(null);
        mRings.add(r);
        while (cursor != null && cursor.moveToNext()) {
            Ring ring = new Ring();
            int idIndex = cursor.getColumnIndex("_id");
            if (idIndex == -1)
                continue;
            ring.setID(cursor.getInt(idIndex));
            int titleIndex = cursor.getColumnIndex("title");
            ring.setTitle(cursor.getString(titleIndex));
            ring.setPos(pos++);
            int title_key = cursor.getColumnIndex("title_key");
            if (title_key != -1)
                ring.setTitleKey(cursor.getString(title_key));
            mRings.add(ring);
        }
        cursor.close();
        return mRings;
    }

    public static final Uri getDefaultRingtoneUri(Context context) {
        Uri uri = RingtoneManager.getActualDefaultRingtoneUri(context,
                RingtoneManager.TYPE_RINGTONE);
        return uri;
    }

    public static final Ringtone getDefaultRingtone(Context context) {
        try {
            Uri uri = RingtoneManager.getActualDefaultRingtoneUri(context,
                    RingtoneManager.TYPE_RINGTONE);
            if (uri == null || TextUtils.isEmpty(uri.toString())) {
                return null;
            }
            Ringtone ringtone = RingtoneManager.getRingtone(context.getApplicationContext(), uri);
            AudioAttributes.Builder builder = new AudioAttributes.Builder();
            builder.setContentType(AudioAttributes.CONTENT_TYPE_SPEECH);
            AudioAttributes audioAttributes = builder.build();
            ringtone.setAudioAttributes(audioAttributes);
            return ringtone;
        }catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static final Uri getRingtoneUri(Context context, int pos) {
        Uri uri = null;
        try {
            RingtoneManager ringtoneManager = new RingtoneManager(context.getApplicationContext());
            Cursor cursor = ringtoneManager.getCursor();
            uri = ringtoneManager.getRingtoneUri(pos);
            cursor.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return uri;
    }

    public static final Ringtone getRingtone(Context context, int pos) {
        try {
            RingtoneManager ringtoneManager = new RingtoneManager(context.getApplicationContext());
            Cursor cursor = ringtoneManager.getCursor();
            Uri uri = ringtoneManager.getRingtoneUri(pos);
            cursor.close();
            if (uri == null || TextUtils.isEmpty(uri.toString())) {
                return null;
            }
            Ringtone ringtone = RingtoneManager.getRingtone(context.getApplicationContext(), uri);
            AudioAttributes.Builder builder = new AudioAttributes.Builder();
            builder.setContentType(AudioAttributes.CONTENT_TYPE_SPEECH);
            AudioAttributes audioAttributes = builder.build();
            ringtone.setAudioAttributes(audioAttributes);
            return ringtone;
        }catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static final void setRingtoneRepeat(Ringtone ringtone, boolean isLoop) {
        Class<Ringtone> clazz = Ringtone.class;
        try {
            @SuppressLint("SoonBlockedPrivateApi") Field field = clazz.getDeclaredField("mLocalPlayer");//返回一个 Field 对象，它反映此 Class 对象所表示的类或接口的指定公共成员字段（※这里要进源码查看属性字段）
            field.setAccessible(true);
            MediaPlayer target = (MediaPlayer) field.get(ringtone);//返回指定对象上此 Field 表示的字段的值
            target.setLooping(isLoop);//设置循环
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            //target 为null
            e.printStackTrace();
        }
    }

    public final static class Ring {
        private int mID;
        private int mPos;
        private String mTitle;
        private String mContent;
        private String mTitleKey;


        public int getID() {
            return mID;
        }

        public void setID(int ID) {
            mID = ID;
        }

        public int getPos() {
            return mPos;
        }

        public void setPos(int pos) {
            mPos = pos;
        }

        public String getTitle() {
            return mTitle;
        }

        public void setTitle(String title) {
            mTitle = title;
        }

        public String getContent() {
            return mContent;
        }

        public void setContent(String content) {
            mContent = content;
        }

        public String getTitleKey() {
            return mTitleKey;
        }

        public void setTitleKey(String titleKey) {
            mTitleKey = titleKey;
        }
    }
}
