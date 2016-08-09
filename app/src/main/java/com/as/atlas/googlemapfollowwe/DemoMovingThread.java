package com.as.atlas.googlemapfollowwe;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * Created by atlas on 2016/8/9.
 */
public class DemoMovingThread extends Thread {

    Handler mHandler;
    public DemoMovingThread(Handler mHandler) {
        this.mHandler = mHandler;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        super.run();
        try {
            for (int i = 0; i < 10; i++) {
                Thread.sleep(1000);
                Bundle countBundle = new Bundle();
                countBundle.putInt("count", i+1);

                Message msg = new Message();
                msg.setData(countBundle);
                mHandler.sendMessage(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
