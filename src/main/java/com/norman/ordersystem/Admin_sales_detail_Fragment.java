package com.norman.ordersystem;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;


public class Admin_sales_detail_Fragment extends Fragment {
    ListView lv_items;
    ArrayList<HashMap<String,Integer>> toDeliverList;
    HashMap<String,Integer> monthly_sold;

    private OnFragmentInteractionListener mListener;

    public Admin_sales_detail_Fragment() {
        // Required empty public constructor
    }

    public static Admin_sales_detail_Fragment newInstance(String param1, String param2) {
        Admin_sales_detail_Fragment fragment = new Admin_sales_detail_Fragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin_sales_detail_, container, false);

        lv_items = (ListView) v.findViewById(R.id.lv_items_sales_detail);

        Bundle b = this.getArguments();
        int mode = b.getInt("mode");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.TAIWAN);
        String[] date = sdf.format(new java.util.Date()).split("-");

        mListener.set_toolbarButton(0,1);

        if(mode == 0){
            mListener.set_toolbar("待出貨清單");
            toDeliverList = new ArrayList<>();
            get_toDeliver_list(date,mode);
        }else{
            mListener.set_toolbar("月銷售量");
            monthly_sold = new HashMap<>();
            get_monthly_sold(date,mode);
        }

        return v;
    }

    private void get_toDeliver_list(String[] date,final int mode){
        if(date[1].charAt(0) == '0'){
            date[1] = date[1].substring(1);
        }
        if(date[2].charAt(0) == '0'){
            date[2] = date[2].substring(1);
        }
        for(int i = 0;i < 3;i++){
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("待出貨").child(date[0]).child(date[1]).child(String.valueOf(Integer.valueOf(date[2]) + i));
            final String sdate = date[0] + "/" + date[1] + "/" + String.valueOf(Integer.valueOf(date[2]) + i);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    HashMap<String,Integer> date_item = new HashMap<>();
                    date_item.put(sdate,null);
                    toDeliverList.add(date_item);
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        HashMap<String,Integer> temp = new HashMap<String, Integer>();
                        temp.put(ds.getKey(),ds.getValue(Integer.class));
                        toDeliverList.add(temp);
                    }
                    Log.d("Deliver list",toDeliverList.toString());
                    MyAdapter<HashMap<String,Integer>> myAdapter = new MyAdapter<>(toDeliverList,mode);
                    lv_items.setAdapter(myAdapter);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void get_monthly_sold(String[] date,final int mode){
        if(date[1].charAt(0) == '0'){
            date[1] = date[1].substring(1);
        }
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("月銷售").child(date[0]).child(date[1]);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    monthly_sold.put(ds.getKey(),ds.getValue(Integer.class));
                }
                ArrayList<Map.Entry<String,Integer>> entry = new ArrayList<>();
                entry.addAll(monthly_sold.entrySet());
                MyAdapter<Map.Entry<String,Integer>> myAdapter = new MyAdapter<>(entry,mode);
                lv_items.setAdapter(myAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
        void set_toolbar(String tag);
        void set_toolbarButton(int state,int shopping_cart);
    }

    private class MyAdapter<T> extends BaseAdapter {
        private final ArrayList<T> mData;
        private int mode;

        public MyAdapter(ArrayList<T> list,int mode) {
            this.mode = mode;
            mData = list;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public T getItem(int position) {
                return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View v;

            if (convertView == null) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.orders_display, parent, false);
            } else {
                v = convertView;
            }

            TextView order_dis_1 = (TextView) v.findViewById(R.id.tv_order_dis_1);
            TextView order_dis_2 = (TextView) v.findViewById(R.id.tv_order_dis_2);

            T item = getItem(position);

            if(mode == 0){
                Iterator<Map.Entry<String,Integer>> iterator = ((HashMap<String,Integer>)item).entrySet().iterator();
                Map.Entry<String,Integer> entry = iterator.next();

                if(entry.getValue() != null){
                    String count_text = entry.getValue() + "份";

                    order_dis_1.setText(entry.getKey());
                    order_dis_2.setText(count_text);
                }else{
                    order_dis_1.setText(entry.getKey());
                    order_dis_2.setText("");

                    order_dis_1.setTextSize(30);
                }
            }else{
                String count_text = ((Map.Entry<String,Integer>)item).getValue() + "份";

                order_dis_1.setText(((Map.Entry<String,Integer>)item).getKey());
                order_dis_2.setText(count_text);
            }

            return v;
        }
    }
}
