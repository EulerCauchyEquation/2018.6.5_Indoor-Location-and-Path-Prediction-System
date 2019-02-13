package com.example.indoorlbs;


import android.content.Context;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity
        implements SensorEventListener, View.OnClickListener{


    private static final int FLOOR_EIGHT_LABORATOY = 0;
    private static final int FLOOR_EIGHT_MID = 1;
    private static final int FLOOR_EIGHT_ELEVATOR = 2;
    private static final int FLOOR_EIGHT_CLASSROOM = 3;



    // 센서 관련 객체
    SensorManager m_sensor_manager;    // 센서 매니저 객체
    Sensor m_accelerometer;            // 가속도 센서
    Sensor m_orientSensor;             // 자이로 스코프 센서

    // 방위센서
    float azimuth = 0.0f;
    float temp = 0.0f;


    // button
    Button buttonNavi;
    Button buttonNaviCancel;


    // 실수의 출력 자리수를 지정하는 포맷 객체
    DecimalFormat m_format;


    // 걸음 수
    private int step;


    // 측정된 거리
    public double distance;


    // 경로 탐색시 필요한 변수
    private int path[] = new int[4];     // 계산후 저장된 배열
    private int newPath[] = new int[4];  // 표시부에 편리하게 그리기 위해 재배열 저장소
    private int pathIndex = 1;           // 재배열시 필요한 인덱스 식별자
    private boolean isRequested = false; // 경로 탐색 여부
    private int start;                   // 경로 시작점 - 재배열시 필요
    private int dst;                     // 경로 도착점 - 재배열시 필요


    // 각 클래스
    Calculator cal;             // 계산부
    MapView cv;                 // 표시부
    ArrayList<BeaconWorker> bk; // 비콘부


    // map floor
    private int floor = 0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        // 초기화
        init();



    }

    // 초기화 메소드
    public void init()
    {


        // 포맷 객체를 생성한다.
        m_format                   = new DecimalFormat();
        m_format.applyLocalizedPattern("0.##");    // 소수점 두자리까지 출력될 수 있는 형식을 지정한다.


        buttonNavi                 = (Button)findViewById(R.id.btnNavi);
        buttonNaviCancel           = (Button)findViewById(R.id.btnNaviCancel);
        buttonNavi.setOnClickListener(this);
        buttonNaviCancel.setOnClickListener(this);

        buttonNavi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // 센서 객체 선언
        m_sensor_manager           = (SensorManager)getSystemService(Context.SENSOR_SERVICE);       // 시스템서비스로부터 SensorManager 객체를 얻는다.
        m_accelerometer            = m_sensor_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);   // SensorManager 를 이용해서 가속도 센서 객체를 얻는다.
        m_orientSensor             = m_sensor_manager.getDefaultSensor(Sensor.TYPE_ORIENTATION);      // SensorManager 를 이용해서 자이로스코프 센서 객체를 얻는다.



        // 계산부 초기화
        cal                      = new Calculator();
        cal.init();

        // 비콘부 초기화
        bk                       = new ArrayList<BeaconWorker>();

        bk.add(new BeaconWorker(this,cal,"E2C56DB5-DFFB-48D2-B060-D0F5A71096E1",40001, 15310,  153.0f, 916.0f, FLOOR_EIGHT_LABORATOY,7));
        bk.add(new BeaconWorker(this,cal,"E2C56DB5-DFFB-48D2-B060-D0F5A71096E0",40001, 15314,  153.0f, 482.0f, FLOOR_EIGHT_MID, 7));
        bk.add(new BeaconWorker(this,cal,"E2C56DB5-DFFB-48D2-B060-D0F5A71096E2",10001, 196412, 291.0f, 482.0f , FLOOR_EIGHT_ELEVATOR,7));
        bk.add(new BeaconWorker(this,cal,"E2C56DB5-DFFB-48D2-B060-D0F5A71096E2",10001, 19641,  794.0f, 482.0f, FLOOR_EIGHT_CLASSROOM,7));

        bk.add(new BeaconWorker(this,cal,"E2C56DB5-DFFB-48D2-B060-D0F5A71096E2",10001, 196412,  500.0f, 700.0f, FLOOR_EIGHT_MID));
        bk.add(new BeaconWorker(this,cal,"E2C56DB5-DFFB-48D2-B060-D0F5A71096E2",10001, 196412,  800.0f, 500.0f, FLOOR_EIGHT_MID));
        bk.add(new BeaconWorker(this,cal,"E2C56DB5-DFFB-48D2-B060-D0F5A71096E2",10001, 196412,  800.0f, 1000.0f, FLOOR_EIGHT_MID));
        bk.add(new BeaconWorker(this,cal,"E2C56DB5-DFFB-48D2-B060-D0F5A71096E2",10001, 196412,  900.0f, 700.0f, FLOOR_EIGHT_MID));


        // 맵부
        cv = (MapView) findViewById(R.id.cv);
        cv.init();
        cv.setBeacon(bk);

    }


    public void drawUserPos()
    {

        // 층수문제
        if(floor == 7) cv.setFloor(7);
        else if(floor == 8) cv.setFloor(8);


        // 경로
        if(isRequested)
        {
            // 시작노드 저장
            newPath[0] = start;

            // 리커시브로 역추적하여 경유노드 load
            reArrange_Path(start, dst);

            // 추적이 끝나면 사용한 변수 초기화
            pathIndex = 1;

            // 재배열 완료된 path 전달후 표시부 세팅
            cv.setPath(newPath);
            cv.setStartPos();
            cv.setIsNaviOn(true);

        }


        // 사용자 좌표
        // 이동중인 상황만 좌표 업데이트
        if(cal.getIsMoved()) {
            cv.setX((float) cal.getDistance());
            /*
            // 방향을 저장해두어 이전 방향값이 같은 방향이면 이동거리 유지 다르면 방향 바뀌고 이동거리 재시작
            if (temp >= 0 && temp <= 40) {
                if (azimuth >= 0 && azimuth <= 40)
                    cv.setX((float) cal.getDistance());
                else {
                    cal.init();

                }

            } else if (temp >= 10 && temp <= 100) {
                if (azimuth >= 10 && azimuth <= 100)
                    cv.setX((float) cal.getDistance());
                else {
                    cal.init();

                }

            } else if (temp >= 100 && temp <= 190) {
                if (azimuth >= 100 && azimuth <= 190)
                    cv.setX((float) cal.getDistance());
                else {
                    cal.init();

                }

            }  else if (temp >= 240 && temp <= 290) {
                    if (azimuth >= 240 && azimuth <= 290)
                        cv.setY(-(float) cal.getDistance());
                    else {
                        cal.init();

                    }

            }



            temp = azimuth;
        }*/
        }
        // 모든 과정후 화면 업데이트
        cv.invalidate();

    }


    // user가 비콘 인식 범위에 접근했을 시
    public void drawBeaconPos(float dx, float dy)
    {

        cv.setStartX(dx);
        cv.setStartY(dy);
        cv.invalidate();



        // 목적지 도착시 알고리즘
        if(isRequested )
        {
            // 비콘 순차탐색
            for (int i = 0 ; i<bk.size() ; i++)
            {
                // 찾고자하는 비콘이 목적지와 동일한데 그 비콘이 인식범위안에 들어왔다면
                if(bk.get(i).getNameNode() == dst && bk.get(i).getIsConneted())
                {
                    // 경로 설정 서비스 종료
                    // 목적지 도착 알림
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setTitle("알림")
                            .setMessage("목적지에 도찪하였습니다.")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    isRequested = false;
                                    cv.setIsNaviOn(false);
                                }
                            })
                            .create().show();
                    break;
                }
            }
        }
    }


    // 층 관리 메소드
    public  void beaconFloorShow()
    {
        for(int i = 0 ; i< bk.size() ; i++)
        {
            if(bk.get(i).getIsConneted() && floor != bk.get(i).getFloor())
            {
                floor = bk.get(i).getFloor();
                cv.setFloor(floor);
                // toast
                Toast toast =  Toast.makeText(this, "현재 "+floor+"층 입니다.", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER,0,200);
                toast.show();

            }
        }
    }


    // 해당 액티비티가 포커스를 얻은 경우
    protected void onResume() {
        super.onResume();
        // 센서 값을 이 컨텍스트에서 받아볼 수 있도록 리스너를 등록한다.
        m_sensor_manager.registerListener(this, m_accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        m_sensor_manager.registerListener(this, m_orientSensor, SensorManager.SENSOR_DELAY_FASTEST);


        //블루투스 권한 및 활성화 코드드'
        for (int i = 0; i < bk.size(); i++)
            bk.get(i).BeaconOnResume();

    }


    // 해당 액티비티가 포커스를 잃으면
    protected void onPause()
    {
        super.onPause();

        // 센서 값이 필요하지 않는 시점에 리스너를 해제해준다.
        m_sensor_manager.unregisterListener(this);

        // 비콘 매니저 해제
        for (int i = 0; i < bk.size(); i++)
            bk.get(i).BeaconOnPause();
    }



    // 센서값이 변경될 때마다 메소드 실행
    @Override
    public void onSensorChanged(SensorEvent event)
    {

        // 센서들이 동시에 호출되므로 synchronized로 관리
        synchronized (this)
        {
            switch (event.sensor.getType())
            {

                // 가속 센서가 전달한 데이터인 경우
                case Sensor.TYPE_ACCELEROMETER:

                    // 사용자가 비콘에 있는지 확인
                    boolean IsLocatedBeacon = false;
                    for (int i = 0; i<bk.size() ; i++ )
                    {
                        if(bk.get(i).getIsConneted())
                        {
                            IsLocatedBeacon = true;
                            break;
                        }
                    }



                    // 사용자 비콘에 있지 않다면
                    if(!IsLocatedBeacon)
                    {
                        // 계산할 가속도 값을 넘기고 PDR법 계산 진행
                        cal.setAcc(event.values[0], event.values[1], event.values[2]);
                        cal.cal_PDR();
                    }

                    // 전체 캔버스 업데이트
                   drawUserPos();

                    break;


                // 자이로 스코프 센서가 전달한 데이터인 경우
                case Sensor.TYPE_ORIENTATION:
                    TextView tv = (TextView)findViewById(R.id.textTest);
                    tv.setText(String.valueOf(event.values[0]));

                    azimuth = event.values[0];

                    break;
            }
        }

    }

    // 정확도 변경시 호출되는 메소드. 잘 사용되지 않음
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}




    @Override
    public void onClick(View v)
    {

        // 식별 버튼
        Button btn = (Button)findViewById(v.getId());

        switch (v.getId())
        {

            // 경로 찾기 버튼 클릭시 activity 이동
            case R.id.btnNavi:
                Intent in = new Intent(
                        MainActivity.this,
                        NavigationActivity.class);

                int _name = 0;
                for(int i = 0 ; i < bk.size() ;i++){
                    if(bk.get(i).getIsConneted())
                    {
                        _name = bk.get(i).getNameNode();
                        break;
                    }
                }

                in.putExtra("NODE_NAME",_name);

                startActivity(in);
                break;

            // 경로 서비스 취소
            case R.id.btnNaviCancel:

                // 경로 여부 bool형 초기화
                cv.setIsNaviOn(false);
                isRequested = false;

                // toast
                Toast toast =  Toast.makeText(this, "경로 서비스가 취소됩니다.", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER,0,200);
                toast.show();

                cv.invalidate();
                break;
        }
    }



    // 인텐트 플래그가 FLAG_ACTIVITY_SINGLE_TOP이 실행될 때만
    // 해당 함수 호출 ( 이미 생성된 액티비티 사용하는 것 )
    @Override
    protected void onNewIntent(Intent intent)
    {
        setIntent(intent);
        processIntent();
        super.onNewIntent(intent);
    }


    private void processIntent()
    {

        // 인텐트 전달
        Intent receiveIntent = getIntent();

        // 키값을 이용한 전달값 인수
        Toast toast =  Toast.makeText(this, "경로 안내를 시작합니다.", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER,0,200);
        toast.show();



        // 목적지, 도착징 정보 받음
        start =receiveIntent.getIntExtra("start",0); // start 전달
        dst = receiveIntent.getIntExtra("destination",0); // start 전달


        // 계산된 경로 배열 받음
        path = receiveIntent.getIntArrayExtra("path");
        pathIndex = 1;
        for(int i = 0; i< newPath.length ;i++)
        {
            newPath[i] = 9999;  // 새로 저장할 경로 배열 초기화 ( 9999 = 빈공간 )
        }

        // 경로 탐색 여부 세팅
        cv.setIsNaviOn(true);
        isRequested = true;

    }


    // 경로 담아져있는 배열 순차로 재배열
    void reArrange_Path(int start,int end)
    {
        // 리커시브로 경로를 역추적
        if(path[end] != start)
            reArrange_Path(start,path[end]);

        // 역추적하면서 순차적으로 경유지점 저장
        newPath[pathIndex++] = end;


    }

    public void setPath(int _path[]) { path = _path;}
    public void setStart(int _start) { start = _start;}
    public void setDst(int _dst) { start = _dst;}
    public void setFloor(int _floor ) { floor = _floor; }


}
