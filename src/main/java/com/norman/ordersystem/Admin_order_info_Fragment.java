package com.norman.ordersystem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Admin_order_info_Fragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Admin_order_info_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Admin_order_info_Fragment extends Fragment {
    TextView tv_total,tv_client_info,tv_order_id;
    ListView lv_order_info;
    Button btn_change_status,btn_order_edit;
    Order_info order;
    MyAdapter myAdapter;
    int mode;
    String order_id;
    Integer price;
    String[] date;
    HashMap<String,Integer> monthly_sold;

    private OnFragmentInteractionListener mListener;

    public Admin_order_info_Fragment() {
        // Required empty public constructor
    }

    public static Admin_order_info_Fragment newInstance(String param1, String param2) {
        Admin_order_info_Fragment fragment = new Admin_order_info_Fragment();
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
        View v = inflater.inflate(R.layout.fragment_admin_order_info_, container, false);

        setHasOptionsMenu(true);
        mListener.set_toolbarButton(1,0);

        tv_client_info = (TextView) v.findViewById(R.id.tv_aorder_info_client_info);
        tv_order_id = (TextView) v.findViewById(R.id.tv_aorder_info_id);
        tv_total = (TextView) v.findViewById(R.id.tv_aorder_info_total);

        btn_order_edit = (Button) v.findViewById(R.id.btn_edit_order);
        btn_change_status = (Button) v.findViewById(R.id.btn_order_info_make);
        lv_order_info = (ListView) v.findViewById(R.id.lv_aorder_info);

        Bundle b = this.getArguments();
        order = b.getParcelable("訂單資料");
        mode = b.getInt("mode");
        order_id = b.getString("編號");
        price = order.getItems().get("總金額");
        date = b.getStringArray("日期");
        monthly_sold = new HashMap<>();

        String order_id_text = "訂單編號:\n" + order_id;
        String total_price_text = "總金額:" + price + "元";
        String client_info_text;

        if(order.getApproach().equals("宅配")){
            client_info_text = "姓名:" + order.getName() + "\n電話:" + order.getPhone() + "\n取貨方式:宅配" +"\n地址:" + order.getAddr()
                    + "\n送達時間:" + order.getTime();
        }else {
            client_info_text = "姓名:" + order.getName() + "\n電話:" + order.getPhone() + "\n取貨方式:自取";
        }

        tv_order_id.setText(order_id_text);
        tv_total.setText(total_price_text);
        tv_client_info.setText(client_info_text);

        myAdapter = new MyAdapter(order);
        lv_order_info.setAdapter(myAdapter);
        setListViewHeightBasedOnChildren(lv_order_info);

        if(mode == 2){
            btn_change_status.setEnabled(false);
            btn_change_status.setVisibility(View.INVISIBLE);
            btn_order_edit.setEnabled(false);
            btn_order_edit.setVisibility(View.INVISIBLE);
        }else{
            btn_change_status.setEnabled(true);
            btn_change_status.setVisibility(View.VISIBLE);
            btn_order_edit.setEnabled(true);
            btn_order_edit.setVisibility(View.VISIBLE);
        }

        btn_order_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                order.getItems().put("總金額",price);
                mListener.order_edit_swap(order,order_id,mode,date);
            }
        });

        btn_change_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE,"取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        change_status();

                        final AlertDialog dialog_upload = new AlertDialog.Builder(getContext()).create();
                        dialog_upload.setCanceledOnTouchOutside(false);
                        dialog_upload.setMessage("修改成功");
                        dialog_upload.setButton(DialogInterface.BUTTON_POSITIVE, "確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialog_upload.cancel();
                                dialog.cancel();
                                closefragment();
                            }
                        });
                        dialog_upload.show();
                    }
                });
                if(mode == 0){
                    dialog.setMessage("確定要把訂單狀態改為\"已運送\"嗎?");
                }else{
                    dialog.setMessage("確定要把訂單狀態改為\"已完成\"嗎?");
                }
                dialog.show();
            }
        });

        return v;
    }

    public void reset_order(Order_info old){
        order = old;
        myAdapter = new MyAdapter(order);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lv_order_info.setAdapter(myAdapter);
            }
        });
    }

    private void closefragment(){
        getFragmentManager().popBackStack();
    }

    public void change_status(){
        DatabaseReference old_root,new_root;

        if(mode == 0){
            old_root = FirebaseDatabase.getInstance().getReference("Orders").child("處理中").child(date[0]).child(date[1]).child(date[2]).child(order.getUID()).child(order_id);
            new_root = FirebaseDatabase.getInstance().getReference("Orders").child("已運送").child(date[0]).child(date[1]).child(date[2]).child(order.getUID()).child(order_id);
        }else{
            old_root = FirebaseDatabase.getInstance().getReference("Orders").child("已運送").child(date[0]).child(date[1]).child(date[2]).child(order.getUID()).child(order_id);
            new_root = FirebaseDatabase.getInstance().getReference("Orders").child("已完成").child(date[0]).child(date[1]).child(date[2]).child(order.getUID()).child(order_id);
            update_monthly_sold();
        }

        order.getItems().put("總金額",price);
        new_root.setValue(order);
        old_root.removeValue();
    }

    private void update_monthly_sold(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("月銷售").child(date[0]).child(date[1]);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren() ){
                    monthly_sold.put(ds.getKey(),ds.getValue(Integer.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        for(Map.Entry<String,Integer> entry : order.getItems().entrySet()){
            if(monthly_sold.containsKey(entry.getKey())){
                monthly_sold.put(entry.getKey(),monthly_sold.get(entry.getKey()) + entry.getValue());
            }else{
                monthly_sold.put(entry.getKey(),entry.getValue());
            }
        }

        databaseReference.setValue(monthly_sold);
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            if (listItem instanceof ViewGroup) {
                listItem.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT));
            }
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
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
        void order_edit_swap(Order_info order,String id,int status,String[] date);
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
            map.getItems().remove("總金額");
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
