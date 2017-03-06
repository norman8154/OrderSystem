package com.norman.ordersystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity implements
        User_infoFragment.OnFragmentInteractionListener,
        Upload_item_Fragment.OnFragmentInteractionListener,
        MainFragment.OnFragmentInteractionListener,
        Item_info_Fragment.OnFragmentInteractionListener,
        Shopping_cart_Fragment.OnFragmentInteractionListener,
        User_order_display_Fragment.OnFragmentInteractionListener,
        User_order_infoFragment.OnFragmentInteractionListener,
        Admin_order_display_Fragment.OnFragmentInteractionListener,
        Admin_order_info_Fragment.OnFragmentInteractionListener,
        Item_manage_Fragment.OnFragmentInteractionListener,
        Item_info_change_Fragment.OnFragmentInteractionListener ,
        Admin_order_edit_fragment.OnFragmentInteractionListener,
        Admin_sales_detail_Fragment.OnFragmentInteractionListener{
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private DrawerLayout dlDrawer;
    private ListView lvDrawerItems;
    private Toolbar toolbar;
    private FrameLayout flFlag;
    ImageButton btn_cart;
    final int REQUEST_LOGIN = 1,REQUEST_LOADING = 2;
    String frag_tag;
    int item_done = 0;
    public User user_info = null;
    public HashMap<String,Item[]> items;
    public HashMap<String,Double[]> shopping_cart;
    String uid;
    String[] lvitems;
    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;
    SharedPreferences preferences;
    ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        shopping_cart = new HashMap<String, Double[]>();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        dlDrawer = (DrawerLayout) findViewById(R.id.dlDrawer);
        lvDrawerItems = (ListView) findViewById(R.id.lvDrawerItems);
        btn_cart = (ImageButton) findViewById(R.id.btn_cart);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this,getResources().getInteger(R.integer.action_menu_item_text_size));

        auth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user==null) {
                    startActivityForResult(new Intent(MainActivity.this, LoginActivity.class), REQUEST_LOGIN);
                }else{
                    if(lvitems == null){
                        Intent intent = new Intent(MainActivity.this, LoadingActivity.class);
                        Bundle bundle = new Bundle();

                        uid = user.getUid();
                        bundle.putString("uid",uid);

                        intent.putExtras(bundle);
                        startActivityForResult(intent, REQUEST_LOADING);
                    }

                }
            }
        };

        /*Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        btn_cart.getLayoutParams().width = height / 10;
        btn_cart.getLayoutParams().height = height / 10;*/

        mDrawerToggle = new ActionBarDrawerToggle(this,dlDrawer,R.string.app_name,
                R.string.app_name){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        dlDrawer.addDrawerListener(mDrawerToggle);

        mDrawerToggle.syncState();

        lvDrawerItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Fragment fragment = get_fragment_info(i);

                if(fragment != null){
                    mFragmentManager = getSupportFragmentManager();
                    mFragmentTransaction = mFragmentManager.beginTransaction();
                    mFragmentTransaction.replace(R.id.flFlag, fragment,frag_tag).addToBackStack(null).commit();
                    dlDrawer.closeDrawer(lvDrawerItems);
                }
            }
        });

        btn_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = get_fragment_info(1);

                if(fragment != null){
                    mFragmentManager = getSupportFragmentManager();
                    mFragmentTransaction = mFragmentManager.beginTransaction();
                    mFragmentTransaction.replace(R.id.flFlag, fragment,frag_tag).addToBackStack(null).commit();
                    dlDrawer.closeDrawer(lvDrawerItems);
            }
        }});
    }

    private void get_Item_info(){
        items = new HashMap<>();
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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onItemAdded() {
        MainFragment mainFrag = (MainFragment)
                getSupportFragmentManager().findFragmentByTag("首頁");

        get_Item_info();

        if (mainFrag != null) {
            //mainFrag.updateItems(items);
        } else {
            MainFragment newFragment = new MainFragment();
            Bundle b = new Bundle();
            b.putSerializable("Items",items);
            newFragment.setArguments(b);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.flFlag, newFragment,"首頁");
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    private Fragment get_fragment_info(int position){
        Fragment fragment;
        Bundle b = new Bundle();
        switch (user_info.getIsAdmin()){
            case 0:
                switch (position){
                    case 0:
                        fragment = new MainFragment();
                        frag_tag = "首頁";
                        toolbar.setTitle("首頁");
                        b.putSerializable("Items",items);
                        b.putInt("isAdmin",user_info.getIsAdmin());
                        fragment.setArguments(b);

                        return fragment;
                    case 1:
                        fragment = new Shopping_cart_Fragment();
                        frag_tag = "購物車";
                        toolbar.setTitle("購物車");
                        b.putSerializable("購物車",shopping_cart);
                        b.putString("UID",uid);
                        b.putString("姓名",user_info.getName());
                        b.putString("電話",user_info.getPhone());
                        b.putString("地址",user_info.getAddr());
                        fragment.setArguments(b);
                        btn_cart.setVisibility(View.INVISIBLE);
                        btn_cart.setEnabled(false);

                        return fragment;
                    case 2:
                        fragment = new User_infoFragment();
                        b.putString("name",user_info.getName());
                        b.putString("phone",user_info.getPhone());
                        b.putString("addr",user_info.getAddr());
                        fragment.setArguments(b);
                        toolbar.setTitle("個人資料");
                        frag_tag = "個人資料";
                        btn_cart.setVisibility(View.VISIBLE);
                        btn_cart.setEnabled(true);

                        return fragment;
                    case 3:
                        fragment = new User_order_display_Fragment();
                        b.putString("UID",uid);
                        b.putInt("mode",0);
                        fragment.setArguments(b);

                        toolbar.setTitle("處理中訂單");
                        frag_tag = "處理中訂單";

                        return fragment;
                    case 4:
                        fragment = new User_order_display_Fragment();
                        b.putString("UID",uid);
                        b.putInt("mode",1);
                        fragment.setArguments(b);

                        toolbar.setTitle("已完成訂單");
                        frag_tag = "已完成訂單";

                        return fragment;
                    case 5:
                        preferences = getSharedPreferences("favorite", 0);
                        preferences.edit().putString("email","").apply();
                        preferences.edit().putString("password","").apply();
                        auth.signOut();
                        startActivityForResult(new Intent(MainActivity.this, LoginActivity.class), REQUEST_LOGIN);
                        dlDrawer.closeDrawer(lvDrawerItems);
                        lvitems = null;
                        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        break;
                }
                break;
            case 1:
                switch (position){
                    case 0:
                        fragment = new MainFragment();
                        toolbar.setTitle("首頁");
                        b.putSerializable("Items",items);
                        b.putInt("isAdmin",user_info.getIsAdmin());
                        fragment.setArguments(b);
                        frag_tag = "首頁";

                        return fragment;
                    case 1:
                        fragment = new Admin_order_display_Fragment();
                        toolbar.setTitle("處理中訂單");
                        b.putInt("mode",0);
                        fragment.setArguments(b);
                        frag_tag = "處理中訂單";

                        return fragment;
                    case 2:
                        fragment = new Admin_order_display_Fragment();
                        toolbar.setTitle("已運送訂單");
                        b.putInt("mode",1);
                        fragment.setArguments(b);
                        frag_tag = "已運送訂單";

                        return fragment;
                    case 3:
                        fragment = new Admin_order_display_Fragment();
                        toolbar.setTitle("已完成訂單");
                        b.putInt("mode",2);
                        fragment.setArguments(b);
                        frag_tag = "已完成訂單";

                        return fragment;
                    case 4:
                        fragment = new Upload_item_Fragment();
                        toolbar.setTitle("新增商品");
                        frag_tag = "新增商品";

                        return fragment;
                    case 5:
                        fragment = new Item_manage_Fragment();
                        b.putSerializable("Items",items);
                        fragment.setArguments(b);

                        toolbar.setTitle("商品管理");
                        frag_tag = "商品管理";

                        return fragment;
                    case 6:
                        fragment = new Admin_sales_detail_Fragment();

                        b.putInt("mode",0);
                        fragment.setArguments(b);

                        toolbar.setTitle("待出貨清單");
                        frag_tag = "待出貨清單";

                        return fragment;
                    case 7:
                        fragment = new Admin_sales_detail_Fragment();

                        b.putInt("mode",1);
                        fragment.setArguments(b);

                        toolbar.setTitle("月銷售量");
                        frag_tag = "月銷售量";

                        return fragment;
                    case 8:
                        preferences = getSharedPreferences("favorite", 0);
                        preferences.edit().putString("email","").apply();
                        preferences.edit().putString("password","").apply();
                        auth.signOut();
                        startActivityForResult(new Intent(MainActivity.this, LoginActivity.class), REQUEST_LOGIN);
                        dlDrawer.closeDrawer(lvDrawerItems);
                        lvitems = null;
                        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        break;
                }
                break;
        }
        return null;
    }

    @Override
    public void fragment_swap(Item item,int isAdmin) {
        Item_info_Fragment fragment = new Item_info_Fragment();
        Bundle b = new Bundle();
        b.putParcelable("Item",item);
        b.putInt("isAdmin",isAdmin);

        if(shopping_cart.containsKey(item.getName())){
            b.putDouble("count",shopping_cart.get(item.getName())[0]);
        }else{
            b.putDouble("count",0);
        }

        fragment.setArguments(b);

        toolbar.setTitle(item.getName());

        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.flFlag, fragment,item.getName()).addToBackStack(null).commit();
    }

    @Override
    public void admin_item_info_swap(Item item){
        Item_info_change_Fragment fragment = new Item_info_change_Fragment();
        Bundle b = new Bundle();
        b.putParcelable("Item",item);

        fragment.setArguments(b);

        toolbar.setTitle("修改商品資料");

        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.flFlag, fragment,"修改商品資料").addToBackStack(null).commit();
    }

    @Override
    public void order_info_swap(Order_info order,String id) {
        User_order_infoFragment fragment = new User_order_infoFragment();
        Bundle b = new Bundle();
        b.putParcelable("訂單資料",order);
        b.putString("編號",id);
        fragment.setArguments(b);

        toolbar.setTitle("訂單資料");

        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.flFlag, fragment,"訂單資料").addToBackStack(null).commit();
    }

    @Override
    public void admin_order_info_swap(Order_info order,String id,String[] date,int mode){
       Admin_order_info_Fragment fragment = new Admin_order_info_Fragment();
        Bundle b = new Bundle();
        b.putParcelable("訂單資料",order);
        b.putString("編號",id);
        b.putInt("mode",mode);
        b.putStringArray("日期",date);
        fragment.setArguments(b);

        toolbar.setTitle("訂單資料");

        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.flFlag, fragment,"訂單資料").addToBackStack(null).commit();
    }

    @Override
    public void order_edit_swap(Order_info order,String id,int status,String[] date) {
        Admin_order_edit_fragment fragment = new Admin_order_edit_fragment();

        Bundle b = new Bundle();
        b.putSerializable("Items",items);
        b.putParcelable("訂單資料",order);
        b.putString("訂單編號",id);
        b.putInt("訂單狀態",status);
        b.putStringArray("日期",date);
        fragment.setArguments(b);

        toolbar.setTitle("修改訂單資料");

        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.flFlag, fragment,"修改訂單資料").addToBackStack(null).commit();
    }

    @Override
    public void order_add(String name, Double info[]) {
        if(info[0] != 0)
            shopping_cart.put(name,info);
        else
            shopping_cart.remove(name);
    }

    @Override
    public void order_edited(Order_info order) {
        Admin_order_info_Fragment order_info_fragment = (Admin_order_info_Fragment) getSupportFragmentManager().findFragmentByTag("訂單資料");
        order_info_fragment.reset_order(order);
    }

    @Override
    public void Order_made() {
        shopping_cart = new HashMap<String, Double[]>();

        get_Item_info();
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        Fragment fragment = get_fragment_info(0);

        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.flFlag, fragment,"首頁");
        mFragmentTransaction.addToBackStack(null).commit();
    }

    @Override
    public void set_toolbarButton(int state,int shopping_cart) {
        if (state == 1) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            mDrawerToggle.setDrawerIndicatorEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        } else {
            mDrawerToggle.setDrawerIndicatorEnabled(true);
        }

        if(shopping_cart == 1){
            btn_cart.setVisibility(View.VISIBLE);
            btn_cart.setEnabled(true);
        }else{
            btn_cart.setVisibility(View.INVISIBLE);
            btn_cart.setEnabled(false);
        }
    }

    @Override
    public void onBackPressed() {
        MainFragment fragment = (MainFragment) getSupportFragmentManager().findFragmentByTag("首頁");
        Admin_order_edit_fragment edit_fragment = (Admin_order_edit_fragment) getSupportFragmentManager().findFragmentByTag("修改訂單資料");
        if(dlDrawer.isDrawerOpen(GravityCompat.START)) {
            dlDrawer.closeDrawer(lvDrawerItems);
        }else if (fragment != null && fragment.isVisible()) {
            int page = fragment.getPage();
            if(page == 1){
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                super.onBackPressed();
            }else
                fragment.page_up();
        }else if(edit_fragment != null && edit_fragment.isVisible()){
            Admin_order_info_Fragment order_info_fragment = (Admin_order_info_Fragment) getSupportFragmentManager().findFragmentByTag("訂單資料");
            order_info_fragment.reset_order(edit_fragment.getOld_order());

            super.onBackPressed();
        }else
            super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void item_info_closed() {
        toolbar.setTitle("首頁");
    }

    @Override
    public void set_toolbar(String tag) {
        toolbar.setTitle(tag);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        switch (requestCode) {
            case REQUEST_LOGIN:
                if (resultCode != RESULT_OK){
                    finish();
                }
                break;
            case REQUEST_LOADING:
                Bundle bundle = data.getExtras();

                items = (HashMap<String,Item[]>) bundle.getSerializable("items");
                lvitems = bundle.getStringArray("lvitems");
                user_info = bundle.getParcelable("info");

                if(user_info.getIsAdmin() == 0){
                    btn_cart.setVisibility(View.VISIBLE);
                    btn_cart.setEnabled(true);
                }else{
                    btn_cart.setVisibility(View.INVISIBLE);
                    btn_cart.setEnabled(false);
                }

                lvDrawerItems = (ListView) findViewById(R.id.lvDrawerItems);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, lvitems);
                lvDrawerItems.setAdapter(adapter);

                Fragment fragment = get_fragment_info(0);

                mFragmentManager = getSupportFragmentManager();
                mFragmentTransaction = mFragmentManager.beginTransaction();
                mFragmentTransaction.replace(R.id.flFlag, fragment,"首頁");
                mFragmentTransaction.addToBackStack(null).commit();
                dlDrawer.closeDrawer(lvDrawerItems);
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        if (authStateListener != null){
            auth.removeAuthStateListener(authStateListener);
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        auth.signOut();
        super.onDestroy();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
}
