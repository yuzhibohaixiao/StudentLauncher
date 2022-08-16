/*
package com.alight.android.aoa_launcher.utils;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ReceiverUtil {
    */
/**
     * action IntentFilter中的某一个action，因为获取到的是IntentFilter的所有action，所以只要匹配一个就可以
     *//*


    private boolean isRegister(LocalBroadcastManager manager, String action) {

        boolean isRegister = false;

        try {

            Field mReceiversField = manager.getClass().getDeclaredField("mReceivers");

            mReceiversField.setAccessible(true);

// String name = mReceiversField.getName();

            HashMap <> mReceivers = (HashMap <>) mReceiversField.get(manager);

            Iterator<Map.Entry< Integer, String >> iterator = mReceivers.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry< Integer, String > entry = iterator.next();
                System.out.println(entry.getKey());
                System.out.println(entry.getValue());
            }

            for (int i = 0; mReceivers.size() > i; i++) {

                ArrayList intentFilters = mReceivers.get(key);

                for (int i = 0; i < intentFilters.size(); i++) {

                    IntentFilter intentFilter = intentFilters.get(i);

                    Field mActionsField = intentFilter.getClass().getDeclaredField("mActions");

                    mActionsField.setAccessible(true);

                    ArrayList mActions = (ArrayList) mActionsField.get(intentFilter);

                    for (int j = 0; j < mActions.size(); j++) {

                        if (mActions.get(i).equals(action)) {

                            isRegister = true;

                            break;

                        }

                    }

                }

            }

        } catch (NoSuchFieldException e) {

            e.printStackTrace();

        } catch (IllegalAccessException e) {

            e.printStackTrace();

        }

        return isRegister;

    }
}
*/
