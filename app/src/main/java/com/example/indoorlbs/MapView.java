package com.example.indoorlbs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

public class MapView extends View {

    // path 배열의 빈공간 식별자
    private static int INF = 9999;

    public MapView(Context context) { super(context); }
    public MapView(Context context, AttributeSet att)  { super(context, att); }
    public MapView(Context context, AttributeSet att, int re) { super(context, att, re); }

    // 맵 이미지 로드 변수
    private Bitmap floor_eighth;
    private Bitmap floor_seventh;
    private Bitmap imageSize;
    private int floor;


    // 유저 좌표 변수
    public float x1 = 153.0f, y1 = 482.0f;
    public float addX = 0, addY = 0;   // 실시간 좌표 변환


    // 경로시 필요한 변수
    private int pathData[]; // 순차로 재배열된 경로 배열
    private Path path;  // 경로를 그릴 변수
    private boolean IsNaviOn; // 경로 탐색 여부
    private ArrayList<BeaconWorker> bk; // 비콘 집합 -> 비콘 좌표 획득
    public float startX, startY;   // 경로 시작지점



    // ArrayList<Test> arr = new ArrayList<Test>();



    public  void init()
    {

        IsNaviOn = false;


        /*
        arr.add(new Test(100.0f, 700.0f));
        arr.add(new Test(300.0f, 500.0f));
        arr.add(new Test(300.0f, 1000.0f));
        arr.add(new Test(500.0f, 300.0f));
        arr.add(new Test(500.0f, 700.0f));
        arr.add(new Test(800.0f, 500.0f));
        arr.add(new Test(800.0f, 1000.0f));
        arr.add(new Test(900.0f, 700.0f));
*/


    }

    // 경로 탐색시 시작지점
    public void setStartPos()
    {
        startX = bk.get(pathData[0]).getX();
        startY = bk.get(pathData[0]).getY();
    }


    public void setBeacon(ArrayList<BeaconWorker> bk)
    {
        this.bk = bk;
    }

    @Override
    public  void onDraw(Canvas canvas)
    {

        // paint
        Paint paint  = new Paint();
        paint.setColor(Color.rgb(255,94,0));



        /*
         *  map show 구간
         */

        // 사진 580 x 787 을 화면크기에 맞게 1.89배 조정
        if (floor == 7)
        {
            floor_seventh = BitmapFactory.decodeResource(getResources(), R.drawable.floor_7);
            imageSize = Bitmap.createScaledBitmap(floor_seventh,
                    580 * 192 / 100,
                    787 * 192 / 100,
                    true);
            canvas.drawBitmap(imageSize, -5 , -6 , null);
        }
        else if(floor == 8)
        {
            floor_eighth = BitmapFactory.decodeResource(getResources(), R.drawable.floor_8);
            imageSize = Bitmap.createScaledBitmap(floor_eighth,
                    580 * 189 / 100,
                    787 * 189 / 100,
                    true);
            canvas.drawBitmap(imageSize, 0 , 0 , null);
        }


        /*
         *  경로 show 구간
         */

        // 매 실행시 path 초기화
        path = new Path();

        // paint 객체 준비
        Paint pathPaint = new Paint();
        pathPaint.setColor(Color.GREEN);
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeWidth(10.0f);
        pathPaint.setStrokeCap(Paint.Cap.BUTT);
        pathPaint.setStrokeJoin(Paint.Join.MITER);

        // 네비 서비스 실행시만
        if(IsNaviOn) {

            // path 먼저 이어놓기
            path.moveTo(startX, startY);        // 시작점
            for (int i = 1; i < pathData.length; i++)
            {   // 나머지 경유지점 간선
                if (pathData[i] != INF)
                    path.lineTo(
                            bk.get(pathData[i]).getX(),
                            bk.get(pathData[i]).getY());
                else break;
            }


        }

        // 완성한 path 그리기
        canvas.drawPath(path, pathPaint);


        /*
         *  user show 구간
         */

        canvas.drawCircle(
                x1 + 33 * addX,
                y1 + 33 * addY,
                10,
                paint);

    }



    public void setFloor(int _floor) { floor = _floor; }

    public void setX(float _x1) {  addX = _x1;}
    public void setY(float _y1) {  addY = _y1;}

    public void setStartX(float _x1) {  x1 = _x1; addX = 0;}
    public void setStartY(float _y1) {  y1 = _y1; addY = 0;}



    public void setPath(int _path[]) { pathData = _path; }
    public void setIsNaviOn(boolean naviOn) { IsNaviOn = naviOn; }
}
