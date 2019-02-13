package com.example.indoorlbs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class CustomListView extends Dialog implements View.OnClickListener{
    private ListView listView;
    private EditText destEt, currentEt;
    private Button contactBtn;
    private Context context;
    private List<String> list;
    private ArrayList<String> arrayList;


    MyAdapter adapter;
    NaviWorker nw;
    MainActivity mainActivity;


    public CustomListView(@NonNull Context context, MainActivity _mainActivity) {
        super(context);
        this.context = context;
        mainActivity = _mainActivity;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_list_view);

        listView = (ListView)findViewById(R.id.pathListView);
        currentEt = (EditText)findViewById(R.id.currentEt);      // 현재 위치
        contactBtn = (Button)findViewById(R.id.contactBtn);      // 경로 검색 버튼

        list = new ArrayList<String>();
        settingList();

        arrayList = new ArrayList<>();
        arrayList.addAll(list);

        adapter = new MyAdapter(list,context);

        listView.setAdapter(adapter);
        contactBtn.setOnClickListener(this);


        init();

        destEt = (EditText)findViewById(R.id.destEt);       // 목적지 위치

        destEt.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String filterText = destEt.getText().toString();

                search(filterText);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String data = arrayList.get(position).toString();

                        destEt.setText(data);
                    }
                });
            }
        });
    }

    public void init()
    {
        nw = new NaviWorker();
        nw.init();

    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.contactBtn){

            nw.init();
            String curStr= currentEt.getText().toString();
            String dstStr = destEt.getText().toString();
            mainActivity.setStart(Integer.parseInt(curStr));
            mainActivity.setDst(Integer.parseInt(dstStr));
            nw.dijkstra(Integer.parseInt(curStr), Integer.parseInt(dstStr));

            mainActivity.setPath(nw.getPath());

            Intent intent = new Intent(context,MainActivity.class );
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);  // 액티비티 새로 생성안하고 자기자신 호출
          //  intent.putExtra("Destination",destEt.getText());

            context.startActivity(intent);
            this.dismiss();


        }
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
    }
    private void settingList(){
        list.add("8층 814호");
        list.add("8층 815호");
        list.add("8층 816호");
        list.add("8층 817호");
        list.add("8층 715호");
        list.add("8층 716호");
        list.add("8층 717호");
        list.add("8층 718호");

    }
}
