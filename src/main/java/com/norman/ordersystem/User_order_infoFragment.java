package com.norman.ordersystem;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link User_order_infoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link User_order_infoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class User_order_infoFragment extends Fragment {
    TextView order_id,tv_total_price;
    ListView lv_order_info;
    Order_info order;
    MyAdapter myadapter;

    private OnFragmentInteractionListener mListener;

    public User_order_infoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment User_order_infoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static User_order_infoFragment newInstance(String param1, String param2) {
        User_order_infoFragment fragment = new User_order_infoFragment();
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
        View v = inflater.inflate(R.layout.fragment_user_order_info, container, false);

        setHasOptionsMenu(true);
        mListener.set_toolbarButton(1,1);

        order_id = (TextView) v.findViewById(R.id.tv_uorder_id);
        tv_total_price = (TextView) v.findViewById(R.id.tv_uorder_total_price);
        lv_order_info = (ListView) v.findViewById(R.id.lv_uorder_info);

        Bundle b;
        b = this.getArguments();
        order = b.getParcelable("訂單資料");

        String order_id_text = "訂單編號:\n" + b.getString("編號");
        String total_price_text = "總金額:" + order.getItems().get("總金額") + "元";

        order_id.setText(order_id_text);
        tv_total_price.setText(total_price_text);

        order.getItems().remove("總金額");

        myadapter = new MyAdapter(order);
        lv_order_info.setAdapter(myadapter);

        return v;
    }

    private void closefragment(){
        getFragmentManager().popBackStack();
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
        void set_toolbarButton(int state,int shopping_cart);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                closefragment();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    class MyAdapter extends BaseAdapter {
        private final ArrayList mData;

        public MyAdapter(Order_info map) {
            mData = new ArrayList();
            mData.addAll(map.getItems().entrySet());
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Map.Entry<String,Integer> getItem(int position) {
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

            Map.Entry<String,Integer> order = getItem(position);

            TextView order_dis_1 = (TextView) v.findViewById(R.id.tv_order_dis_1);
            TextView order_dis_2 = (TextView) v.findViewById(R.id.tv_order_dis_2);

            String key = order.getKey();
            String dis_2text;

            order_dis_1.setText(order.getKey());

            dis_2text = order.getValue() + "份";
            order_dis_2.setText(dis_2text);

            return v;
        }
    }
}
