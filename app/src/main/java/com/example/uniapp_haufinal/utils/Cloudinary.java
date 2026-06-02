package com.example.uniapp_haufinal.utils;

import android.app.Application;

import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;


public class Cloudinary extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
//        drce2oqs2
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name","drce2oqs2");
        config.put("api_key","364331681112955");
        config.put("api_secret","MFHbMoEjC0_ZznxBEpusq6MS2c8 ");
        MediaManager.init(this,config);
    }


}
