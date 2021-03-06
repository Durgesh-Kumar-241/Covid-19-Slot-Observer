package com.dktechhub.mnnit.ee.statussaver;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Threader extends Thread{
    public boolean isCancelled = false;
    int disttID=-1,pin_code=-1;
    boolean pin_code_mode;
    private final String link;
    private final int dose;
    private final int min_age_lim;
    private final Caller caller;

    public Threader(Context context,Caller caller) {
        super();
        this.caller=caller;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        pin_code_mode = sharedPreferences.getBoolean("pin_code_mode",true);
        dose=sharedPreferences.getInt("dose",1);
        min_age_lim=sharedPreferences.getInt("min_age_lim",18);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        sdf.setTimeZone(TimeZone.getDefault());
        String date = sdf.format(new Date());
        Log.d("Theader", date);


        if(pin_code_mode)
        {
            pin_code=sharedPreferences.getInt("pin_code",-1);
            link = "https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/calendarByPin?pincode="+pin_code+"&date="+ date;

        }else {
            disttID=sharedPreferences.getInt("disttID",-1);
            link="https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/calendarByDistrict?district_id="+disttID+"&date="+ date;
        }

        Log.d("Threader","age "+min_age_lim+" dose "+dose+" pin "+pin_code+" pin code mode "+pin_code_mode);


    }

    @Override
    public void run() {
        super.run();

        if(pin_code_mode)
        {
            if(pin_code==-1)
            {
                return;
            }
        }else {
            if(disttID==-1)
                return ;
        }

        while(!isCancelled)
        {
            getSlotdetailsfetch();
            try {
                sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }



    public interface OnResultAvailable{
        void onslotAvailabel();
    }


    public  void getSlotdetailsfetch()
    {
        try{    URL url = new URL(link);
            HttpURLConnection con2 =(HttpURLConnection) url.openConnection();
            con2.setRequestMethod("GET");
            con2.setRequestProperty("accept","application/json, text/plain, */*");
            con2.setRequestProperty("accept-encoding","gzip, deflate, br");
            con2.setRequestProperty("accept-language","en-US,en;q=0.9");
            con2.setRequestProperty("origin","https://selfregistration.cowin.gov.in");
            con2.setRequestProperty("referer","https://selfregistration.cowin.gov.in/");
            con2.setRequestProperty("sec-ch-ua","Chromium\";v=\"92\", \" Not A;Brand\";v=\"99\", \"Microsoft Edge\";v=\"92\"");
            con2.setRequestProperty("sec-ch-ua-mobile","?0");
            con2.setRequestProperty("sec-fetch-dest","empty");
            con2.setRequestProperty("sec-fetch-mode","cors");
            con2.setRequestProperty("sec-fetch-site","cross-site");
            con2.setRequestProperty("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.159 Safari/537.36 Edg/92.0.902.78");
            p("con2: "+con2.getResponseCode());
           if(con2.getResponseCode()==200)
            {
                try(BufferedReader br=new BufferedReader(new InputStreamReader(con2.getInputStream())))
                {
                    StringBuilder sb = new StringBuilder();
                    String s = null;
                    while((s=br.readLine())!=null)
                    {
                        sb.append(s.trim());
                    }

                     parse(sb.toString());
                  }
            }

        }   catch(Exception e)
        {
            e.printStackTrace();
        }

    }

    public void parse(String s)
    {
        try{
            JSONObject main = new JSONObject(s);
            JSONArray list = main.getJSONArray("centers");
            for(int i=0;i<list.length();i++)
            {
                String name = list.getJSONObject(i).getString("name");
                Log.d("Threader","Results for center.."+name);
                JSONArray sessions = list.getJSONObject(i).getJSONArray("sessions");
                for(int j=0;j<sessions.length();j++)
                {
                    int total_avalable = sessions.getJSONObject(j).getInt("available_capacity");
                    int min_age_limit = sessions.getJSONObject(j).getInt("min_age_limit");
                    int dose1=sessions.getJSONObject(j).getInt("available_capacity_dose1");
                    int  dose2=sessions.getJSONObject(j).getInt("available_capacity_dose2");

                    Log.d("Threader","total: "+total_avalable+" min_age_limit: "+min_age_limit+" dose1 "+dose1+" dose2 "+dose2);

                    if((min_age_lim==min_age_limit)||min_age_lim==-1)
                    {
                        if((dose==1&&dose1>0)||(dose==2&&dose2>0))
                            if(caller!=null)
                                caller.onSlotAvailable(dose,min_age_lim,name);
                    }
                }

            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void p(String m)
    {
        Log.d("Threader",m);
    }

    public interface Caller{
        void onSlotAvailable(int dose,int min,String center);
    }
}
