package com.norman.ordersystem;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Shopping_cart_Fragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Shopping_cart_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Shopping_cart_Fragment extends Fragment {
    TextView cart_total_price,textView15;
    ListView lv_cart;
    Button btn_make_order;
    HashMap<String,Double[]> cart;
    HashMap<String,Integer> order;
    HashMap<String,Integer> toDeliver_list;
    int total_price = 0;
    Integer serial_id_count = 0;
    String uid,name,phone,addr,time,serial_id;

    RadioGroup rbGroup_time,rbGroup_approach;
    DatePicker datePicker;

    private OnFragmentInteractionListener mListener;

    public Shopping_cart_Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Shopping_cart_Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Shopping_cart_Fragment newInstance(String param1, String param2) {
        Shopping_cart_Fragment fragment = new Shopping_cart_Fragment();
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
        View v = inflater.inflate(R.layout.fragment_shopping_cart_, container, false);
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        final View date_select = layoutInflater.inflate(R.layout.date_select,null);

        mListener.set_toolbar("購物車");
        mListener.set_toolbarButton(0,0);

        cart_total_price = (TextView) v.findViewById(R.id.cart_total_price);
        textView15 = (TextView) date_select.findViewById(R.id.textView15);
        lv_cart = (ListView) v.findViewById(R.id.lv_cart_items);
        btn_make_order = (Button) v.findViewById(R.id.btn_make_order);

        rbGroup_time = (RadioGroup) date_select.findViewById(R.id.rbGroup);
        rbGroup_approach = (RadioGroup) date_select.findViewById(R.id.radioGroup3);
        datePicker = (DatePicker) date_select.findViewById(R.id.datePicker);

        order = new HashMap<String, Integer>();
        serial_id = "";
        toDeliver_list = new HashMap<>();

        Bundle b = new Bundle();
        b = this.getArguments();
        cart = (HashMap<String, Double[]>) b.getSerializable("購物車");
        uid = b.getString("UID");
        name = b.getString("姓名");
        phone = b.getString("電話");
        addr = b.getString("地址");

        MyAdapter myAdapter = new MyAdapter(cart);
        lv_cart.setAdapter(myAdapter);

        if(cart.size() == 0){
            btn_make_order.setEnabled(false);
        }else{
            btn_make_order.setEnabled(true);
        }

        rbGroup_approach.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (rbGroup_approach.getCheckedRadioButtonId()){
                    case R.id.rbtn5:
                        textView15.setVisibility(View.VISIBLE);
                        rbGroup_time.setVisibility(View.VISIBLE);
                        rbGroup_time.setEnabled(true);
                        break;
                    case R.id.rbtn6:
                        textView15.setVisibility(View.INVISIBLE);
                        rbGroup_time.setVisibility(View.INVISIBLE);
                        rbGroup_time.setEnabled(false);
                        break;
                }
            }
        });

        btn_make_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.setMessage("確定要訂貨嗎?");
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.TAIWAN);
                        final String[] date = sdf.format(new java.util.Date()).split("-");
                        datePicker.updateDate(Integer.valueOf(date[0]),Integer.valueOf(date[1]) - 1,Integer.valueOf(date[2]));
                        datePicker.setMinDate(System.currentTimeMillis() - 1000);
                        final AlertDialog dialog_date = new AlertDialog.Builder(getContext()).setView(date_select).create();
                        dialog_date.setTitle("請選擇送貨日期與時間");
                        dialog_date.setButton(DialogInterface.BUTTON_POSITIVE, "確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String approach = "";
                                final String selected_date = datePicker.getYear() + "/" + (datePicker.getMonth() + 1) + "/" + datePicker.getDayOfMonth();
                                if(rbGroup_time.isEnabled()){
                                    switch (rbGroup_time.getCheckedRadioButtonId()){
                                        case R.id.rbtn1:
                                            time = "不指定";
                                            break;
                                        case R.id.rbtn2:
                                            time = "中午前";
                                            break;
                                        case R.id.rbtn3:
                                            time = "12:00~17:00";
                                            break;
                                        case R.id.rbtn4:
                                            time = "17:00~20:00";
                                            break;
                                    }
                                }

                                switch (rbGroup_approach.getCheckedRadioButtonId()){
                                    case R.id.rbtn5:
                                        approach = "宅配";
                                        break;
                                    case R.id.rbtn6:
                                        approach = "自取";
                                        time = "";
                                        break;
                                }

                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Orders");
                                order.put("總金額",total_price);

                                Order_info order_info = new Order_info();
                                order_info.setName(name);
                                order_info.setAddr(addr);
                                order_info.setPhone(phone);
                                order_info.setUID(uid);
                                order_info.setTime(time);
                                order_info.setItems(order);
                                order_info.setApproach(approach);

                                saveOrder(date,selected_date,order_info,databaseReference);

                                Thread thread = new Thread(){
                                    @Override
                                    public void run(){
                                        update_deliver_list(selected_date);
                                    }
                                };
                                thread.start();
                                update_remain();

                                final AlertDialog dialog_upload = new AlertDialog.Builder(getContext()).create();
                                dialog_upload.setCanceledOnTouchOutside(false);
                                dialog_upload.setMessage("訂貨成功");
                                dialog_upload.setButton(DialogInterface.BUTTON_POSITIVE, "確定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        closefragment();
                                        mListener.Order_made();
                                        dialog_upload.cancel();
                                        dialog_date.cancel();
                                        dialog.cancel();
                                    }
                                });
                                dialog_upload.show();
                            }
                        });
                        dialog_date.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialog_date.cancel();
                                dialog.cancel();
                            }
                        });
                        dialog_date.show();
                    }
                });
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.cancel();
                    }
                });
                dialog.show();
            }
        });

        return v;
    }

    private void closefragment(){
        getFragmentManager().popBackStack();
    }

    private void update_remain(){
        for(final Map.Entry<String,Integer> entry : order.entrySet()){
            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Items");
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        if(ds.hasChild(entry.getKey())){
                            String s = ds.child(entry.getKey()).child("remain").getValue(String.class);
                            Integer remain_temp = Integer.valueOf(s);
                            remain_temp = remain_temp - entry.getValue();
                            databaseReference.child(ds.getKey()).child(entry.getKey()).child("remain").setValue(remain_temp.toString());
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void saveOrder(final String[] date, final String selected_date, final Order_info order_to_save, final DatabaseReference save_ref){
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("SerialID").child(date[0]).child(date[1]).child(date[2]).child("count");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    serial_id_count = dataSnapshot.getValue(Integer.class) + 1;

                    String serial_id_temp = date[0] + date[1] + date[2];

                    for(int i = 0;i < 5 - serial_id_count.toString().length();i++)
                        serial_id_temp += "0";

                    serial_id_temp += serial_id_count;
                    databaseReference.setValue(serial_id_count);

                    save_ref.child("處理中").child(selected_date).child(uid).child(serial_id_temp).setValue(order_to_save);
                }else{
                    serial_id_count++;
                    databaseReference.setValue(serial_id_count);

                    String serial_id_temp = date[0] + date[1] + date[2] + "00001";

                    save_ref.child("處理中").child(selected_date).child(uid).child(serial_id_temp).setValue(order_to_save);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                    Log.d("Error",databaseError.getMessage() + " " + databaseError.getDetails() + " " + databaseError.getCode());
            }
        });

        if(serial_id_count == 0){

        }
    }

    private void update_deliver_list(String date){
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("待出貨").child(date);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren() ){
                    toDeliver_list.put(ds.getKey(),ds.getValue(Integer.class));
                }

                for(Map.Entry<String,Integer> entry : order.entrySet()){
                    if(!entry.getKey().equals("總金額")){
                        if(toDeliver_list.containsKey(entry.getKey())){
                            toDeliver_list.put(entry.getKey(),toDeliver_list.get(entry.getKey()) + entry.getValue());
                        }else{
                            toDeliver_list.put(entry.getKey(),entry.getValue());
                        }
                    }
                }

                databaseReference.setValue(toDeliver_list);
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
        void Order_made();
        void set_toolbar(String tag);
        void set_toolbarButton(int state,int shopping_cart);
    }

    public class MyAdapter extends BaseAdapter {
        private final ArrayList mData;

        public MyAdapter(Map<String, Double[]> map) {
            mData = new ArrayList();
            mData.addAll(map.entrySet());
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Map.Entry<String, Double[]> getItem(int position) {
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
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_display, parent, false);
            } else {
                v = convertView;
            }

            Map.Entry<String,Double[]> item = getItem(position);

            TextView cart_dis_name = (TextView) v.findViewById(R.id.cart_dis_name);
            TextView cart_dis_count = (TextView) v.findViewById(R.id.cart_dis_count);
            TextView cart_dis_price = (TextView) v.findViewById(R.id.cart_dis_price);

            String count_text = item.getValue()[0].intValue() + "份";
            String price_text = String.valueOf(item.getValue()[1].intValue()) + "元";

            if(item.getValue()[0] != 0){
                cart_dis_name.setText(item.getKey());
                cart_dis_count.setText(count_text);
                cart_dis_price.setText(price_text);

                order.put(item.getKey(),item.getValue()[0].intValue());
                //remains.put(item.getKey(),item.getValue()[2].intValue());

                total_price += item.getValue()[1];
                String total_price_text = "總金額:" + total_price + "元";
                cart_total_price.setText(total_price_text);
            }

            return v;
        }
    }
}
