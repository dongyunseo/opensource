package com.example.wifiindoorposition_master;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.wifiindoorposition_master.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class OutActivity extends Calculate implements View.OnClickListener {


    public ImageView iv, wifia, wifib, wific;
    private WifiManager mainWifi;
    private WifiListReceiver receiverWifi;
    private final Handler handler = new Handler();
    private Button next;
    public List<ScanResult> results = new ArrayList<>();
    private WifiResultsAdapter wifiResultsAdapter = new WifiResultsAdapter();
    private boolean wifiWasEnabled;
    float triptx, tripty;
    float P1[] = {850, 900};
    float P2[] = {500, 700};
    float P3[] = {400, 950};

    public double d1;
    public double d2;
    public double d3;

    int wifia_num = 0;
    int wifib_num = 0;
    int wific_num = 0;

    String wifi1_BSSID = "90:9f:33:f2:a6:4e"; //wearable
    String wifi2_BSSID = "88:36:6c:0c:37:4e"; //Wearable
    String wifi3_BSSID = "90:9f:33:f2:a6:12"; //wabelab

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_out);
        initUI();

        mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        receiverWifi = new WifiListReceiver();

        wifiWasEnabled = mainWifi.isWifiEnabled();
        if (!mainWifi.isWifiEnabled()) {
            mainWifi.setWifiEnabled(true);
        }

    }

    @Override
    public void onClick(View view) {

    }

    class WifiListReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            results = mainWifi.getScanResults();
            Collections.sort(results, new Comparator<ScanResult>() {
                @Override
                public int compare(ScanResult scanResult, ScanResult scanResult2) {
                    if (scanResult.level > scanResult2.level) {
                        return -1;
                    } else if (scanResult.level < scanResult2.level) {
                        return 1;
                    }
                    return 0;
                }
            });

            wifiResultsAdapter.setResults(results);
            wifiResultsAdapter.notifyDataSetChanged();

            refresh();
        }
    }

    public void refresh() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mainWifi.startScan();
                position();
            }
        }, 1000);

    }



    @Override
    protected void onResume() {
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        refresh();
        position();
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiverWifi);
        super.onPause();
    }


    private void initUI() {

        iv=findViewById(R.id.imageView);

        wifia=findViewById(R.id.wifia);
        wifib=findViewById(R.id.wifib);
        wific=findViewById(R.id.wific);


        wifia.setX(P1[0]);
        wifia.setY(P1[1]);

        wifib.setX(P2[0]);
        wifib.setY(P2[1]);

        wific.setX(P3[0]);
        wific.setY(P3[1]);


        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),triptx+"/"+tripty,Toast.LENGTH_SHORT).show();

            }
        });


        wifia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplication(),String.valueOf(mainWifi.getScanResults().get(wifia_num).SSID),Toast.LENGTH_LONG).show();
            }
        });

        wifib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplication(),String.valueOf(mainWifi.getScanResults().get(wifib_num).SSID),Toast.LENGTH_LONG).show();

            }
        });

        wific.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplication(),String.valueOf(mainWifi.getScanResults().get(wific_num).SSID),Toast.LENGTH_LONG).show();

            }
        });




    }
    //601
    public void position() {
        int N = mainWifi.getScanResults().size();
        for(int i=0;i<N;i++) {
            if (mainWifi.getScanResults().get(i).BSSID.equals(wifi1_BSSID)) wifia_num = i;
            else if (mainWifi.getScanResults().get(i).BSSID.equals(wifi2_BSSID)) wifib_num = i;
            else if (mainWifi.getScanResults().get(i).BSSID.equals(wifi3_BSSID)) wific_num = i;}

/*
        d1=getDistance_float(mainWifi.getScanResults().get(wifia_num).frequency,  getrollingRssi(mainWifi.getScanResults().get(wifia_num).level));
        d2=getDistance_float(mainWifi.getScanResults().get(wifib_num).frequency,  getrollingRssi(mainWifi.getScanResults().get(wifib_num).level));
        d3=getDistance_float(mainWifi.getScanResults().get(wific_num).frequency,  getrollingRssi(mainWifi.getScanResults().get(wific_num).level));

        double[] a = trilateration(P1, P2, P3, d1 * 100, d2 * 100, d3 * 100);

        Toast.makeText(getApplicationContext(),a[0]+"/"+a[1],Toast.LENGTH_SHORT).show();
        triptx = (float) (a[0]);
        tripty = (float) (a[1]);
*/

        d1=KalmanFileter(getDistance_float(KalmanFileter(mainWifi.getScanResults().get(wifia_num).frequency),KalmanFileter(mainWifi.getScanResults().get(wifia_num).level)));
        d2=KalmanFileter(getDistance_float(KalmanFileter(mainWifi.getScanResults().get(wifib_num).frequency),KalmanFileter(mainWifi.getScanResults().get(wifib_num).level)));
        d3=KalmanFileter(getDistance_float(KalmanFileter(mainWifi.getScanResults().get(wific_num).frequency),KalmanFileter(mainWifi.getScanResults().get(wific_num).level)));

        double[] b = position_calculate(P1, P2, P3, d1 * 100, d2 * 100, d3 * 100);
        Toast.makeText(getApplicationContext(),b[0]+"/"+b[1],Toast.LENGTH_SHORT).show();

        triptx = KalmanFileter(b[0]);
        tripty = KalmanFileter(b[1]);
        Toast.makeText(getApplicationContext(),triptx+"/"+tripty,Toast.LENGTH_SHORT).show();

        iv.setX((float) (triptx));
        iv.setY((float) (tripty));

    }

    protected void onDestroy() {
        super.onDestroy();
        if (!wifiWasEnabled) {
            mainWifi.setWifiEnabled(false);
        }
    }

}
