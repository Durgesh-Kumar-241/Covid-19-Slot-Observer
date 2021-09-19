package com.dktechhub.mnnit.ee.statussaver;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class MainActivity extends AppCompatActivity {
    LinearLayout pin,dist;
    SharedPreferences sharedPreferences;
    AutoCompleteTextView ages,doses,states,districts;
    com.google.android.material.textfield.TextInputEditText pin_code;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button start,stop;
        start=findViewById(R.id.start);
        stop=findViewById(R.id.stop);

        pin = findViewById(R.id.mpin);
        dist=findViewById(R.id.mdistt);
        sharedPreferences =PreferenceManager.getDefaultSharedPreferences(this);

        RadioGroup rg = findViewById(R.id.opt_method_preff);

        rg.setOnCheckedChangeListener((group, checkedId) -> {
            if(checkedId==R.id.opt_pin_code)
            {
                pin.setVisibility(View.VISIBLE);
                dist.setVisibility(View.GONE);
                sharedPreferences.edit().putBoolean("pin_code_mode",true).apply();

            }else {
                pin.setVisibility(View.GONE);
                dist.setVisibility(View.VISIBLE);
                sharedPreferences.edit().putBoolean("pin_code_mode",false).apply();
                switchFromPinCodeMode();
            }
        });
        ArrayList<Integer> agl = new ArrayList<>();
        agl.add(18);
        agl.add(45);
        ArrayAdapter<Integer> arrayAdapter = new ArrayAdapter<>(this,R.layout.item_drop,agl);
        ages = findViewById(R.id.age);
        ages.setText(String.valueOf(sharedPreferences.getInt("min_age_lim",18)));
        ages.setAdapter(arrayAdapter);

        ages.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().length()>0)
                {

                    try{
                        int agen=Integer.parseInt(s.toString());
                        if(agen==18||agen==45)
                        {
                            sharedPreferences.edit().putInt("min_age_lim",agen).apply();
                            Toast.makeText(getApplicationContext(), "minimum age set to "+ agen+ " years", Toast.LENGTH_SHORT).show();

                        }
                    }catch (Exception ignored)
                    {

                    }
                }
            }
        });



        ArrayList<Integer> dll = new ArrayList<>();
        dll.add(1);
        dll.add(2);
        ArrayAdapter<Integer> arrayAdapter2 = new ArrayAdapter<>(this,R.layout.item_drop,dll);

        doses = findViewById(R.id.dose);
        doses.setText(String.valueOf(sharedPreferences.getInt("dose",1)));
        doses.setAdapter(arrayAdapter2);

        doses.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().length()>0)
                {

                    try{
                        int dosen=Integer.parseInt(s.toString());
                        if(dosen==1||dosen==2)
                        {
                            sharedPreferences.edit().putInt("dose",dosen).apply();

                            Toast.makeText(getApplicationContext(), "dose set to "+ dosen, Toast.LENGTH_SHORT).show();

                        }
                    }catch (Exception ignored)
                    {

                    }
                }
            }
        });

        //doses.s





        pin_code = findViewById(R.id.pin_code_input);
        pin_code.setText(String.valueOf(sharedPreferences.getInt("pin_code",277001)));

        pin_code.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String s1 = s.toString();
                if(s1.matches("^[1-9][0-9]{2}[0-9]{3}$"))
                {
                    try{
                        int npin= Integer.parseInt(s1);
                        sharedPreferences.edit().putInt("pin_code",npin).apply();
                        Toast.makeText(getApplicationContext(), "Pin code set to "+npin, Toast.LENGTH_SHORT).show();
                    }catch (Exception e)
                    {
                        Toast.makeText(getApplicationContext(), "Please enter valid pin code", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        start.setOnClickListener(v -> startService());
        stop.setOnClickListener(v -> stopService());


    }



    public void startService() {

        Intent serviceIntent = new Intent(this, Background.class);
        serviceIntent.putExtra("inputExtra", "Running observer in background");
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void stopService() {
        Intent serviceIntent = new Intent(this, Background.class);
        stopService(serviceIntent);
    }


    public void switchFromPinCodeMode()
    {
        if(states==null)
        {
            states=findViewById(R.id.state);
        }

        if(districts==null)
        {
            districts=findViewById(R.id.distt);
        }
        loadStates();
    }

    public void loadStates()
    {
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMax(100);
        progressDialog.setMessage("Loading states....");
        if(states==null)
        {
            states=findViewById(R.id.state);
        }
        @SuppressLint("StaticFieldLeak") AsyncTask<Void,Void,List<Pair>> asyncTask = new AsyncTask<Void, Void, List<Pair>>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog.show();
            }

            @Override
            protected List<Pair> doInBackground(Void... voids) {
                List<Pair> l = new ArrayList<>();
                String link = "https://cdn-api.co-vin.in/api/v2/admin/location/states";
                try{
                    URL url = new URL(link);
                    HttpURLConnection con2 =(HttpURLConnection) url.openConnection();
                     con2.setRequestMethod("GET"); 
                     con2.setRequestProperty("accept","application/json, text/plain, */*");
                     con2.setRequestProperty("accept-encoding","gzip, deflate, br");
                     con2.setRequestProperty("accept-language","en-US,en;q=0.9");
                    con2.setRequestProperty("origin","https://www.cowin.gov.in");
                     con2.setRequestProperty("referer","https://www.cowin.gov.in");
                     con2.setRequestProperty("sec-ch-ua","Chromium\";v=\"92\", \" Not A;Brand\";v=\"99\", \"Microsoft Edge\";v=\"92\"");
                     con2.setRequestProperty("sec-ch-ua-mobile","?0");
                     con2.setRequestProperty("sec-fetch-dest","empty");
                     con2.setRequestProperty("sec-fetch-mode","cors");
                     con2.setRequestProperty("sec-fetch-site","cross-site");
                     con2.setRequestProperty("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.159 Safari/537.36 Edg/92.0.902.78");
                      //p("con2: "+con2.getResponseCode());
                      if(con2.getResponseCode()==200)
                      {
                        try(BufferedReader br=new BufferedReader(new InputStreamReader(new GZIPInputStream(con2.getInputStream()))))
                        {
                            StringBuilder sb = new StringBuilder();
                            String s;
                            while((s=br.readLine())!=null)
                            {
                                sb.append(s.trim());
                            }

                            JSONObject main = new JSONObject(sb.toString());
                            JSONArray stateList = main.getJSONArray("states");
                            for(int i=0;i<stateList.length();i++)
                            {
                                JSONObject st = stateList.getJSONObject(i);
                                l.add(new Pair(st.getString("state_name"),st.getInt("state_id")));
                            }
                
                            //p("respoce data: "+sb.toString());
                        }
                      }else cancel(true);
                      
                }catch(Exception e)
                {
                    e.printStackTrace();
                    cancel(true);
                }
                return l;
            }

            @Override
            protected void onPostExecute(List<Pair> statesl) {
                super.onPostExecute(statesl);
                ArrayAdapter<Pair> adapter = new ArrayAdapter<>(getApplicationContext(),R.layout.item_drop,statesl);
                states.setAdapter(adapter);

                states.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String st = s.toString();
                        if(st.length()>0)
                        {   int id = Integer.parseInt(st.substring(st.indexOf('(')+1,st.length()-1));
                            Toast.makeText(getApplicationContext(),"State choosen : "+st.substring(0,st.indexOf('(')), Toast.LENGTH_SHORT).show();
                            loadDistricts(id);
                        }
                    }
                });
                progressDialog.cancel();
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                progressDialog.cancel();
                Toast.makeText(getApplicationContext(), "Could not load list of states...please try again later", Toast.LENGTH_SHORT).show();
            }
        };



        asyncTask.execute();
    }




    public void loadDistricts(int stateId)
    {
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMax(100);
        progressDialog.setMessage("Loading states....");
        if(districts==null)
        {
            districts=findViewById(R.id.distt);
        }
        @SuppressLint("StaticFieldLeak") AsyncTask<Void,Void,List<Pair>> asyncTask = new AsyncTask<Void, Void, List<Pair>>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog.show();
            }

            @Override
            protected List<Pair> doInBackground(Void... voids) {
                List<Pair> l = new ArrayList<>();
                String link = "https://cdn-api.co-vin.in/api/v2/admin/location/districts/"+stateId;
                try{
                    URL url = new URL(link);
                    HttpURLConnection con2 =(HttpURLConnection) url.openConnection();
                    con2.setRequestMethod("GET");
                    con2.setRequestProperty("accept","application/json, text/plain, */*");
                    con2.setRequestProperty("accept-encoding","gzip, deflate, br");
                    con2.setRequestProperty("accept-language","en-US,en;q=0.9");
                    con2.setRequestProperty("origin","https://www.cowin.gov.in");
                    con2.setRequestProperty("referer","https://www.cowin.gov.in");
                    con2.setRequestProperty("sec-ch-ua","Chromium\";v=\"92\", \" Not A;Brand\";v=\"99\", \"Microsoft Edge\";v=\"92\"");
                    con2.setRequestProperty("sec-ch-ua-mobile","?0");
                    con2.setRequestProperty("sec-fetch-dest","empty");
                    con2.setRequestProperty("sec-fetch-mode","cors");
                    con2.setRequestProperty("sec-fetch-site","cross-site");
                    con2.setRequestProperty("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.159 Safari/537.36 Edg/92.0.902.78");
                    //p("con2: "+con2.getResponseCode());
                    if(con2.getResponseCode()==200)
                    {
                        try(BufferedReader br=new BufferedReader(new InputStreamReader(new GZIPInputStream(con2.getInputStream()))))
                        {
                            StringBuilder sb = new StringBuilder();
                            String s;
                            while((s=br.readLine())!=null)
                            {
                                sb.append(s.trim());
                            }

                            JSONObject main = new JSONObject(sb.toString());
                            JSONArray stateList = main.getJSONArray("districts");
                            for(int i=0;i<stateList.length();i++)
                            {
                                JSONObject st = stateList.getJSONObject(i);
                                l.add(new Pair(st.getString("district_name"),st.getInt("district_id")));
                            }

                            //p("respoce data: "+sb.toString());
                        }
                    }else cancel(true);

                }catch(Exception e)
                {
                    e.printStackTrace();
                    cancel(true);
                }
                return l;
            }

            @Override
            protected void onPostExecute(List<Pair> statesl) {
                super.onPostExecute(statesl);
                ArrayAdapter<Pair> adapter = new ArrayAdapter<>(getApplicationContext(),R.layout.item_drop,statesl);
                districts.setAdapter(adapter);
                districts.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String st = s.toString();
                        if(st.length()>0)
                        {   int id = Integer.parseInt(st.substring(st.indexOf('(')+1,st.length()-1));
                            Toast.makeText(getApplicationContext(),"District set to "+st.substring(0,st.indexOf('(')), Toast.LENGTH_SHORT).show();
                            sharedPreferences.edit().putInt("disttID",id).apply();
                        }
                    }
                });
                progressDialog.cancel();
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                progressDialog.cancel();
                Toast.makeText(getApplicationContext(), "Could not load list of districts...please try again later", Toast.LENGTH_SHORT).show();
            }
        };



        asyncTask.execute();
    }



}