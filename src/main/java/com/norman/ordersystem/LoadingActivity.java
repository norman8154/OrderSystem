package com.norman.ordersystem;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class LoadingActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    public User user_info = null;
    public HashMap<String,Item[]> items;
    String uid;
    String[] lvitems;
    boolean item_done = false;
    TextView textView1,textView2;
    Timer timer;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        Thread thread = new Thread(){
            @Override
            public void run(){
                get_Item_info();
            }
        };
        thread.start();

        textView1 = (TextView) findViewById(R.id.textView14);
        textView2 = (TextView) findViewById(R.id.textView16);

        textView1.setText("資料讀取中，請稍候");

        intent = new Intent();
        items = new HashMap<>();
        timer = new Timer();
        timer.schedule(task,10000,10000);
        Bundle bundle = getIntent().getExtras();

        get_user_info(bundle.getString("uid"));
    }

    private TimerTask task = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView2.setText("網路有點不給力呢，再一下下就好了...吧");
                }
            });
        }
    };

    private void get_user_info(final String uid){
        DatabaseReference ds_ref = FirebaseDatabase.getInstance().getReference("users").child(uid);
        ds_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user_info = dataSnapshot.getValue(User.class);

                Log.e("data",dataSnapshot.toString());

                if(user_info.getIsAdmin() == 0){
                    lvitems = new String[6];
                    lvitems[0] = "首頁";
                    lvitems[1] = "購物車";
                    lvitems[2] = "個人資料";
                    lvitems[3] = "處理中訂單";
                    lvitems[4] = "歷史訂單";
                    lvitems[5] = "登出";
                }else{
                    lvitems = new String[9];
                    lvitems[0] = "首頁";
                    lvitems[1] = "處理中訂單";
                    lvitems[2] = "已運送訂單";
                    lvitems[3] = "已完成訂單";
                    lvitems[4] = "新增商品";
                    lvitems[5] = "商品管理";
                    lvitems[6] = "待出貨清單";
                    lvitems[7] = "月銷售量";
                    lvitems[8] = "登出";
                }

                while(item_done == false){

                }

                if(user_info != null){
                    Bundle returnbundle = new Bundle();

                    returnbundle.putSerializable("items",items);
                    returnbundle.putStringArray("lvitems",lvitems);
                    returnbundle.putParcelable("info",user_info);

                    intent.putExtras(returnbundle);
                    setResult(RESULT_OK,intent);
                    timer.cancel();
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("讀取失敗","read User info failed.");
            }
        });
    }

    private void get_Item_info(){
        DatabaseReference ds_ref = FirebaseDatabase.getInstance().getReference("Items");
        ds_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds_cate : dataSnapshot.getChildren() ){
                    int i = 0;
                    Item[] item_temp = new Item[(int) ds_cate.getChildrenCount()];
                    for(DataSnapshot ds_item : ds_cate.getChildren()){
                        item_temp[i] = ds_item.getValue(Item.class);
                        i++;
                    }
                    items.put(ds_cate.getKey(),item_temp);
                }
                item_done = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
