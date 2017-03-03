package com.norman.ordersystem;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Admin_order_display_Fragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Admin_order_display_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Admin_order_display_Fragment extends Fragment {
    ListView lv_orders;
    ArrayList<HashMap<String,Order_info>> orders;
    int mode;
    MyAdapter myAdapter;

    private OnFragmentInteractionListener mListener;

    public Admin_order_display_Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Admin_order_display_Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Admin_order_display_Fragment newInstance(String param1, String param2) {
        Admin_order_display_Fragment fragment = new Admin_order_display_Fragment();
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
        View v = inflater.inflate(R.layout.fragment_admin_order_display_, container, false);

        mListener.set_toolbarButton(0,0);

        lv_orders = (ListView) v.findViewById(R.id.lv_admin_order_dis);
        orders = new ArrayList<HashMap<String, Order_info>>();

        Bundle b = this.getArguments();
        mode = b.getInt("mode");

        get_orders(mode);

        lv_orders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Map<String,Order_info> order = (Map<String,Order_info>) adapterView.getItemAtPosition(i);
                Iterator<Map.Entry<String,Order_info>> iterator = order.entrySet().iterator();
                Map.Entry<String,Order_info> temp = iterator.next();
                Order_info order_info = temp.getValue();
                String order_id = temp.getKey();
                if(order_info != null){
                    String[] date = iterator.next().getKey().split("/");
                    mListener.admin_order_info_swap(order_info,order_id,date,mode);
                }else{
                    if(iterator.hasNext()){
                        String[] date = order_id.split("/");
                        temp = iterator.next();
                        order_info = temp.getValue();
                        order_id = temp.getKey();
                        mListener.admin_order_info_swap(order_info,order_id,date,mode);
                    }
                }

            }
        });

        return v;
    }

    private void closefragment(){
        getFragmentManager().popBackStack();
    }

    public void get_orders(int mode){
        DatabaseReference databaseReference;
        if(mode == 0){
            mListener.set_toolbar("處理中訂單");
            databaseReference = FirebaseDatabase.getInstance().getReference("Orders").child("處理中");
        }else if(mode == 1){
            mListener.set_toolbar("已運送訂單");
            databaseReference = FirebaseDatabase.getInstance().getReference("Orders").child("已運送");
        }else {
            mListener.set_toolbar("已完成訂單");
            databaseReference = FirebaseDatabase.getInstance().getReference("Orders").child("已完成");
        }
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds_year : dataSnapshot.getChildren() ){
                    for(DataSnapshot ds_month : ds_year.getChildren()) {
                        for (DataSnapshot ds_date : ds_month.getChildren()) {
                            String sdate = ds_year.getKey() + "/" + ds_month.getKey() + "/" + ds_date.getKey();
                            HashMap<String,Order_info> date = new HashMap<String, Order_info>();
                            date.put(sdate,null);
                            orders.add(date);
                            for (DataSnapshot ds_uid : ds_date.getChildren()){
                                for(DataSnapshot ds_order : ds_uid.getChildren()){
                                    HashMap<String,Order_info> order = new HashMap<String, Order_info>();
                                    order.put(ds_order.getKey(),ds_order.getValue(Order_info.class));
                                    order.put(sdate,null);
                                    orders.add(order);
                                }
                            }
                        }
                    }
                }
                myAdapter = new MyAdapter(orders);
                lv_orders.setAdapter(myAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
        void admin_order_info_swap(Order_info order,String id,String[] date,int mode);
        void set_toolbar(String tag);
        void set_toolbarButton(int state,int shopping_cart);
    }

    private class MyAdapter extends BaseAdapter {
        private final ArrayList<HashMap<String,Order_info>> mData;

        public MyAdapter(ArrayList<HashMap<String,Order_info>> map) {
            mData = map;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Map<String,Order_info> getItem(int position) {
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

            Map<String,Order_info> order = getItem(position);
            Iterator<Map.Entry<String,Order_info>> iterator = order.entrySet().iterator();
            Map.Entry<String,Order_info> entry = iterator.next();

            TextView order_dis_1 = (TextView) v.findViewById(R.id.tv_order_dis_1);
            TextView order_dis_2 = (TextView) v.findViewById(R.id.tv_order_dis_2);

            order_dis_1.setText(entry.getKey());
            order_dis_2.setText("");
            if(iterator.hasNext()){
                entry = iterator.next();
                if(entry.getValue() != null){
                    order_dis_1.setText(entry.getKey());
                }
            }else if(entry.getValue() == null){
                order_dis_1.setTextSize(30);
            }


            return v;
        }
    }
}
