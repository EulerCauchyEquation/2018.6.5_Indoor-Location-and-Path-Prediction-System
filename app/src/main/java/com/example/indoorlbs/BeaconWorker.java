package com.example.indoorlbs;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;

import java.util.List;
import java.util.UUID;



public class BeaconWorker
{

    // beacon 변수
    private BeaconManager beaconManager;
    private Region region;
    private TextView tvId;
    private boolean isConnected;


    // beacon 식별 변수
    private MainActivity mainActivity;
    private Calculator cal;
    private String uuid;
    private int major;
    private int minor;
    private float dx;
    private float dy;
    private int floor;
    private int name;

    public BeaconWorker(MainActivity _mainActivity, Calculator _cal, String _uuid, int _major, int _minor, float _dx, float _dy, int _name)
    {
        mainActivity         = _mainActivity;
        cal                  = _cal;
        uuid                 = _uuid;
        major                = _major;
        minor                = _minor;
        dx                   = _dx;
        dy                   = _dy;
        floor                = 0;
        name                 = _name;

        init();
    }

    public BeaconWorker(MainActivity _mainActivity, Calculator _cal, String _uuid, int _major, int _minor, float _dx, float _dy, int _name, int _floor)
    {
        mainActivity         = _mainActivity;
        cal                  = _cal;
        uuid                 = _uuid;
        major                = _major;
        minor                = _minor;
        dx                   = _dx;
        dy                   = _dy;
        name                 = _name;
        floor                = _floor;

        init();
    }

    public void init()
    {

        // tvId = (TextView) findViewById(R.id.textDirection);
        beaconManager = new BeaconManager(mainActivity);

        // add this below:
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(com.estimote.sdk.Region region, List<Beacon> list) {
                if (!list.isEmpty()) {
                    Beacon nearestBeacon = list.get(0);
                    Log.d("Airport", "Nearest places: " + nearestBeacon.getRssi());
                    // direction.setText(nearestBeacon.getRssi()+""+"\n\n");

                    // 수신강도가 -70 이상일때 알림창을 띄운다.
                    if (!isConnected && nearestBeacon.getRssi() > -60) {
                        isConnected = true;

                        /*
                        if(floor == 7)
                        {
                            mainActivity.setFloor(7);
                           // Toast.makeText(mainActivity, "현재 층수는 7층입니다.", Toast.LENGTH_SHORT).show();
                        }*/

                        cal.init();
                        mainActivity.drawBeaconPos(dx,dy);  // 비콘에 유저 위치
                        mainActivity.beaconFloorShow();


                        /* 대기중 비콘표시
                        AlertDialog.Builder dialog = new AlertDialog.Builder(mainActivity);
                        dialog.setTitle("알림")
                                .setMessage(minor +" 비콘이 연결되었습니다.")
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .create().show();*/
                    } else if (isConnected && nearestBeacon.getRssi() < -60) {
                        //Toast.makeText(mainActivity, "연결이 끊어졌습니다.", Toast.LENGTH_SHORT).show();
                        isConnected = false;



                        cal.setIsMoved(false);
                        mainActivity.drawBeaconPos(dx, dy);


                    }
                }
            }
        });

        region = new Region("ranged region",
                UUID.fromString(uuid), major, minor);
    }


    public void BeaconOnResume()
    {
        SystemRequirementsChecker.checkWithDefaultDialogs(mainActivity);
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
    }

    public void BeaconOnPause()
    {
        beaconManager.stopRanging(region);
    }



    public boolean getIsConneted() { return  isConnected;}
    public float getX() { return dx; }
    public float getY() { return dy; }
    public int getNameNode() { return name;}
    public int getFloor() { return  floor;}
}
