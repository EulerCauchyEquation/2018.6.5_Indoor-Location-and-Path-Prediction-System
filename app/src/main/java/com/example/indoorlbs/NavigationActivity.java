package com.example.indoorlbs;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class  NavigationActivity extends Activity
        implements View.OnClickListener
{


    private EditText dstStr;
    private EditText srcStr;
    private Button btnStart;
    private Button btnReturn;

    private NaviWorker nw;


    private MyAdapter adapter;
    private ListView listView;
    private List<String> list;
    private ArrayList<String> arrayList;

    int current_NODE_NAME = 0;
    int dest_NODE_NAME = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigator);

        Intent intent = getIntent();
        current_NODE_NAME = intent.getIntExtra("NODE_NAME",0);


        listView = (ListView) findViewById(R.id.list);

        list = new ArrayList<String>();
        settingList();

        arrayList = new ArrayList<>();
        arrayList.addAll(list);

        adapter = new MyAdapter(list,getApplicationContext());

        listView.setAdapter(adapter);


        dstStr = (EditText)findViewById(R.id.edtDst) ;

        dstStr.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable edit)
            {
                String filterText = dstStr.getText().toString();

                search(filterText);
            }
        });


        srcStr = (EditText) findViewById(R.id.edtStart);
        setCurrentPosName(current_NODE_NAME);

        btnStart = (Button)findViewById(R.id.btnInput);
        btnStart.setOnClickListener(this);
        btnReturn = (Button)findViewById(R.id.btnCancel);
        btnReturn.setOnClickListener(this);


        init();
    }

    public void search(String charText) {

        // 문자 입력시마다 리스트를 지우고 새로 뿌려준다.
        list.clear();

        // 문자 입력이 없을때는 모든 데이터를 보여준다.
        if (charText.length() == 0) {
            list.addAll(arrayList);
        }
        // 문자 입력을 할때..
        else
        {
            // 리스트의 모든 데이터를 검색한다.
            for(int i = 0;i < arrayList.size(); i++)
            {
                // arraylist의 모든 데이터에 입력받은 단어(charText)가 포함되어 있으면 true를 반환한다.
                if (arrayList.get(i).toLowerCase().contains(charText))
                {
                    // 검색된 데이터를 리스트에 추가한다.
                    list.add(arrayList.get(i));
                }
            }
        }
        // 리스트 데이터가 변경되었으므로 아답터를 갱신하여 검색된 데이터를 화면에 보여준다.
        adapter.notifyDataSetChanged();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String data = arrayList.get(position);

                dstStr.setText(data);
            }
        });
    }

    public void init()
    {
        nw = new NaviWorker();
        nw.init();
    }

    public void settingList(){
        list.add("8공 715호") ;
        list.add("8공 716호") ;
        list.add("8공 717호") ;
        list.add("8공 714호") ;
        list.add("8공 715호") ;
        list.add("8공 714호") ;
        list.add("8공 716호") ;
        list.add("8공 717호") ;
        list.add("8공 701호") ;
        list.add("8공 702호") ;
        list.add("8공 7층 엘리베이터");
        list.add("8공 7층 연구실");
        list.add("8공 7층 강의실");
    }

    @Override
    public void onClick(View v) {

        switch (v.getId())
        {
            // 경로 탐색 버튼을 실행시
            case R.id.btnInput:

                // 경로 계산부 초기화
                nw.init();

                switch (dstStr.getText().toString()){
                    case "8공 7층 연구실":{
                        dest_NODE_NAME = 0;
                        break;
                    }
                    case "8공 7층 엘리베이터":{
                        dest_NODE_NAME = 2;
                        break;
                    }
                    case "8공 7층 강의실":{
                        dest_NODE_NAME = 3;
                        break;
                    }
                }

                // 경로 지정후 다익스트라 알고리즘 실행
                nw.dijkstra(current_NODE_NAME, dest_NODE_NAME);


                Intent in = new Intent(
                        getBaseContext(),
                        MainActivity.class);

                in.putExtra("start",current_NODE_NAME );  // start 데이터 전달
                in.putExtra("destination",dest_NODE_NAME );  // dest(목적지) 전달
                in.putExtra("path",nw.getPath() );   // 경로 배열 전달
                in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);  // 액티비티 새로 생성안하고 자기자신 호출
                                                                                                 // 최상위 태스크로 위치

                startActivity(in);
                break;

            // 경로 취소 (돌아가기)
            case R.id.btnCancel:
                finish();
                break;

        }
    }
    public void setCurrentPosName(int current_NODE_NAME){
        switch (current_NODE_NAME){
            case 0:{
                srcStr.setText("8공 7층 연구실");
                break;
            }
            case 1:{
                srcStr.setText("8공 702호");
                break;
            }
            case 2:{
                srcStr.setText("8공 7층 엘리베이터");
                break;
            }
            case 3:{
                srcStr.setText("8공 7층 강의실");
                break;
            }
        }
    }


}
