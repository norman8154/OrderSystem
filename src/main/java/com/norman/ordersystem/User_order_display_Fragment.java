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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link User_order_display_Fragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link User_order_display_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class User_order_display_Fragment extends Fragment {
    ListView lv_unp_orders,lv_inp_orders;
    HashMap<String,Order_info> unprogress_orders;
    HashMap<String,Order_info> inprogress_orders;
    String uid;
    MyAdapter unpAdapter,inpAdapter;
    int mode;

    private OnFragmentInteractionListener mListener;

    public User_order_display_Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment User_order_display_Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static User_order_display_Fragment newInstance(String param1, String param2) {
        User_order_display_Fragment fragment = new User_order_display_Fragment();
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
        View v = inflater.inflate(R.layout.fragment_user_order_display_, container, false);
        lv_unp_orders = (ListView) v.findViewById(R.id.lv_unp_orders);
        lv_inp_orders = (ListView) v.findViewById(R.id.lv_inp_orders);
        unprogress_orders = new HashMap<String, Order_info>();
        inprogress_orders = new HashMap<String, Order_info>();

        Bundle b = this.getArguments();
        uid = b.getString("UID");
        mode = b.getInt("mode");

        mListener.set_toolbarButton(0,1);

        if(mode == 0) {
            mListener.set_toolbar("處理中訂單");
            Thread thread = new Thread() {
                @Override
                public void run() {
                    get_inprogress_orders();
                }
            };
            thread.run();

            get_unprogress_orders();
        }else{
            mListener.set_toolbar("歷史訂單");
            get_done_orders();
        }


        lv_unp_orders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Map.Entry<String,Order_info> order = (Map.Entry) adapterView.getItemAtPosition(i);
                Order_info order_info = order.getValue();
                String order_id = order.getKey();
                mListener.order_info_swap(order_info,order_id);
            }
        });

        lv_inp_orders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Map.Entry<String,Order_info> order = (Map.Entry) adapterView.getItemAtPosition(i);
                Order_info order_info = order.getValue();
                String order_id = order.getKey();
                mListener.order_info_swap(order_info,order_id);
            }
        });

        return v;
    }

    private void closefragment(){
        getFragmentManager().popBackStack();
    }

    public void get_unprogress_orders(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Orders").child("處理中");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds_year : dataSnapshot.getChildren() ){
                    for(DataSnapshot ds_month : ds_year.getChildren()) {
                        for (DataSnapshot ds_date : ds_month.getChildren()) {
                            for (DataSnapshot ds_order : ds_date.child(uid).getChildren()){
                                unprogress_orders.put(ds_order.getKey(), ds_order.getValue(Order_info.class));
                            }
                        }
                    }
                }
                unpAdapter = new MyAdapter(unprogress_orders,0);
                lv_unp_orders.setAdapter(unpAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void get_inprogress_orders(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Orders").child("已運送");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds_year : dataSnapshot.getChildren() ){
                    for(DataSnapshot ds_month : ds_year.getChildren()) {
                        for (DataSnapshot ds_date : ds_month.getChildren()) {
                            for (DataSnapshot ds_order : ds_date.child(uid).getChildren()){
                                inprogress_orders.put(ds_order.getKey(), ds_order.getValue(Order_info.class));
                            }
                        }
                    }
                }
                inpAdapter = new MyAdapter(inprogress_orders,1);
                lv_inp_orders.setAdapter(inpAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void get_done_orders(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Orders").child("已完成");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds_year : dataSnapshot.getChildren() ){
                    for(DataSnapshot ds_month : ds_year.getChildren()) {
                        for (DataSnapshot ds_date : ds_month.getChildren()) {
                            for (DataSnapshot ds_order : ds_date.child(uid).getChildren()){
                                unprogress_orders.put(ds_order.getKey(), ds_order.getValue(Order_info.class));
                            }
                        }
                    }
                }
                unpAdapter = new MyAdapter(unprogress_orders,2);
                lv_unp_orders.setAdapter(unpAdapter);
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void order_info_swap(Order_info order,String id);
        void set_toolbar(String tag);
        void set_toolbarButton(int state,int shopping_cart);
    }

    private class MyAdapter extends BaseAdapter {
        private final ArrayList mData;
        int mode;

        public MyAdapter(Map<String,Order_info> map,int mode) {
            this.mode = mode;
            mData = new ArrayList();
            mData.addAll(map.entrySet());
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Map.Entry<String,Order_info> getItem(int position) {
            return (Map.Entry) mData.get(position);
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

            Map.Entry<String,Order_info> order = getItem(position);

            TextView order_dis_1 = (TextView) v.findViewById(R.id.tv_order_dis_1);
            TextView order_dis_2 = (TextView) v.findViewById(R.id.tv_order_dis_2);

            order_dis_1.setText(order.getKey());
            if(mode == 0)
                order_dis_2.setText("處理中");
            else if(mode == 1)
                order_dis_2.setText("已運送");
            else
                order_dis_2.setText("已完成");

            return v;
        }
    }
}
