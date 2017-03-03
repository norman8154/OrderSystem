package com.norman.ordersystem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Admin_order_edit_fragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Admin_order_edit_fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Admin_order_edit_fragment extends Fragment {
    ListView lv_items;
    TextView tv_price,tv_id;
    Button btn_make;
    Order_info order,new_order;
    Spinner category_spinner,item_spinner;
    EditText ed_item_count;
    HashMap<String,Item[]> items;
    Integer price;
    String order_id;
    String[] date;
    int mode;

    private OnFragmentInteractionListener mListener;

    public Admin_order_edit_fragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static Admin_order_edit_fragment newInstance(String param1, String param2) {
        Admin_order_edit_fragment fragment = new Admin_order_edit_fragment();
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
        View v = inflater.inflate(R.layout.fragment_admin_order_edit, container, false);
        final View add_item_view = inflater.inflate(R.layout.edit_add_item, container,false);

        final AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(add_item_view).create();

        //getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        setHasOptionsMenu(true);
        mListener.set_toolbarButton(1,0);

        lv_items = (ListView) v.findViewById(R.id.lv_aorder_edit);
        tv_price = (TextView) v.findViewById(R.id.tv_aorder_edit_total);
        tv_id = (TextView) v.findViewById(R.id.tv_aorder_edit_id);
        btn_make = (Button) v.findViewById(R.id.btn_order_edit_make);

        category_spinner = (Spinner) add_item_view.findViewById(R.id.edit_add_category);
        item_spinner = (Spinner) add_item_view.findViewById(R.id.edit_add_item);
        ed_item_count = (EditText) add_item_view.findViewById(R.id.edit_item_count);

        Bundle b = this.getArguments();
        order = b.getParcelable("訂單資料");
        items = (HashMap<String,Item[]>) b.getSerializable("Items");
        mode = b.getInt("訂單狀態");
        price = order.getItems().get("總金額");
        order_id = b.getString("訂單編號");
        date = b.getStringArray("日期");

        new_order = new Order_info();
        new_order.setApproach(order.getApproach());
        new_order.setPhone(order.getPhone());
        new_order.setName(order.getName());
        new_order.setAddr(order.getAddr());
        new_order.setTime(order.getTime());
        new_order.setUID(order.getUID());
        new_order.setItems(new HashMap<String, Integer>());
        for(Map.Entry<String,Integer> entry : order.getItems().entrySet())
            new_order.getItems().put(entry.getKey(),entry.getValue());

        final String order_id_text = "訂單編號:\n" + order_id;
        String total_price_text = "總金額:" + price + "元";
        tv_price.setText(total_price_text);
        tv_id.setText(order_id_text);

        MyAdapter myAdapter = new MyAdapter(new_order);
        lv_items.setAdapter(myAdapter);

        ArrayAdapter<String> category_list = new ArrayAdapter<String>(add_item_view.getContext(),android.R.layout.simple_spinner_dropdown_item,items.keySet().toArray(new String[items.size()]));
        category_spinner.setAdapter(category_list);

        category_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selected_category = category_spinner.getSelectedItem().toString();
                String[] item_list_temp = new String[items.get(selected_category).length];
                for(int j = 0;j < items.get(selected_category).length;j++)
                    item_list_temp[j] = items.get(selected_category)[j].getName();
                ArrayAdapter<String> item_list = new ArrayAdapter<String>(add_item_view.getContext(),android.R.layout.simple_spinner_dropdown_item,item_list_temp);
                item_spinner.setAdapter(item_list);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                return ;
            }
        });

        btn_make.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference databaseReference;
                if(new_order.getItems().containsKey("新增商品..."))
                    new_order.getItems().remove("新增商品...");
                if(!new_order.getItems().containsKey("總金額"))
                    new_order.getItems().put("總金額",price);

                if(mode == 0)
                   databaseReference = FirebaseDatabase.getInstance().getReference("Orders").child("處理中").child(date[0]).child(date[1]).child(date[2]).child(order.getUID()).child(order_id);
                else
                    databaseReference = FirebaseDatabase.getInstance().getReference("Orders").child("已運送").child(date[0]).child(date[1]).child(date[2]).child(order.getUID()).child(order_id);

                for (Map.Entry<String,Integer> entry : new_order.getItems().entrySet()){
                    if(entry.getValue() == 0)
                        new_order.getItems().remove(entry.getKey());
                }
                databaseReference.setValue(new_order);

                mListener.order_edited(new_order);

                closefragment();
            }
        });

        lv_items.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Map.Entry<String,Integer> entry = (Map.Entry<String,Integer>) adapterView.getItemAtPosition(i);

                if(entry.getValue() == null){
                    dialog.setButton(DialogInterface.BUTTON_NEGATIVE,"取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, "確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            new_order.getItems().put(item_spinner.getSelectedItem().toString(),Integer.valueOf(ed_item_count.getText().toString()));
                            Log.d("Price",items.get(category_spinner.getSelectedItem().toString())[item_spinner.getSelectedItemPosition()].getPrice());
                            price = Integer.valueOf(items.get(category_spinner.getSelectedItem().toString())[item_spinner.getSelectedItemPosition()].getPrice()) * Integer.valueOf(ed_item_count.getText().toString())
                                                + price;
                            new_order.getItems().put("總金額",price);
                            String s_price = "總金額:" + price + "元";
                            tv_price.setText(s_price);
                            MyAdapter myAdapter1 = new MyAdapter(new_order);
                            lv_items.setAdapter(myAdapter1);

                            dialog.cancel();
                        }
                    });

                    dialog.setTitle("請選擇要新增的商品");
                    dialog.show();
                }
            }
        });

        lv_items.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> adapterView, View view, int i, long l) {
                final Map.Entry<String,Integer> entry = (Map.Entry<String,Integer>) adapterView.getItemAtPosition(i);

                if(entry.getValue() != null){
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
                            new_order.getItems().remove(entry.getKey());
                            for(Map.Entry<String,Item[]> entry_list : items.entrySet()){
                                Item[] item_temp = entry_list.getValue();
                                for(int j = 0;j < item_temp.length;j++){
                                    if(item_temp[j].getName().compareTo(entry.getKey()) == 0){
                                        price -= (Integer.valueOf(item_temp[j].getPrice()) * entry.getValue());
                                        String s_price = "總金額:" + price + "元";
                                        tv_price.setText(s_price);
                                        new_order.getItems().put("總金額",price);
                                        break;
                                    }
                                }
                            }
                            MyAdapter myAdapter1 = new MyAdapter(new_order);
                            lv_items.setAdapter(myAdapter1);
                        }
                    });

                    dialog.setMessage("確定要刪除此商品嗎?");
                    dialog.show();

                    return true;
                }

                return false;
            }
        });

        return v;
    }

    public Order_info getOld_order(){
        return new_order;
    }

    private void closefragment(){
        getFragmentManager().popBackStack();
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
        void set_toolbarButton(int state,int shopping_cart);
        void order_edited(Order_info order);
    }

    class MyAdapter extends BaseAdapter {
        private final ArrayList<Map.Entry<String,Integer>> mData;

        public MyAdapter(Order_info map) {
            map.getItems().remove("總金額");
            mData = new ArrayList<>();
            mData.addAll(map.getItems().entrySet());
            Map.Entry<String,Integer> add_item = new Map.Entry<String, Integer>() {
                @Override
                public String getKey() {
                    return "新增商品...";
                }

                @Override
                public Integer getValue() {
                    return null;
                }

                @Override
                public Integer setValue(Integer integer) {
                    return null;
                }
            };
            mData.add(add_item);
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Map.Entry<String,Integer> getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View v;
            ViewHolder viewHolder = null;

            if (convertView == null) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.orders_edit, null, false);
            } else {
                v = convertView;
            }

            viewHolder = new ViewHolder();
            viewHolder.tv_order_edit = (TextView) v.findViewById(R.id.tv_order_edit);
            viewHolder.ed_order_edit = (EditText) v.findViewById(R.id.ed_order_edit);

            final Map.Entry<String,Integer> entry = getItem(position);

            String key = entry.getKey();
            String dis_2text;

            viewHolder.ed_order_edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if (!b){
                        ViewHolder viewHolder = new ViewHolder();
                        viewHolder.ed_order_edit = (EditText) v.findViewById(R.id.ed_order_edit);
                        if(viewHolder.ed_order_edit.getText().toString().length() > 0 && Integer.valueOf(viewHolder.ed_order_edit.getText().toString()) > 0){
                            for(Map.Entry<String,Item[]> entry_list : items.entrySet()){
                                Item[] item_temp = entry_list.getValue();
                                for(int j = 0;j < item_temp.length;j++){
                                    if(item_temp[j].getName().compareTo(entry.getKey()) == 0){
                                        price -= (Integer.valueOf(item_temp[j].getPrice()) * entry.getValue());
                                        price += (Integer.valueOf(item_temp[j].getPrice()) * Integer.valueOf(viewHolder.ed_order_edit.getText().toString()));
                                        String s_price = "總金額:" + price + "元";
                                        tv_price.setText(s_price);
                                        new_order.getItems().put("總金額",price);
                                        break;
                                    }
                                }
                            }
                            new_order.getItems().put(entry.getKey(),Integer.valueOf(viewHolder.ed_order_edit.getText().toString()));
                            viewHolder.ed_order_edit.setSelection(viewHolder.ed_order_edit.getText().length());
                        }else if(viewHolder.ed_order_edit.getText().toString().length() <= 0 | Integer.valueOf(viewHolder.ed_order_edit.getText().toString()) == 0){
                            for(Map.Entry<String,Item[]> entry_list : items.entrySet()){
                                Item[] item_temp = entry_list.getValue();
                                for(int j = 0;j < item_temp.length;j++){
                                    if(item_temp[j].getName().compareTo(entry.getKey()) == 0){
                                        price -= (Integer.valueOf(item_temp[j].getPrice()) * entry.getValue());
                                        String s_price = "總金額:" + price + "元";
                                        tv_price.setText(s_price);
                                        new_order.getItems().put("總金額",price);
                                        break;
                                    }
                                }
                            }
                            new_order.getItems().put(entry.getKey(),0);
                            viewHolder.ed_order_edit.setText("0");
                            viewHolder.ed_order_edit.setSelection(viewHolder.ed_order_edit.getText().length());
                        }
                    }
                }
            });

            if(position == mData.size() - 1){
                viewHolder.tv_order_edit.setText(key);
                viewHolder.ed_order_edit.setVisibility(View.INVISIBLE);
                viewHolder.ed_order_edit.setEnabled(false);
            }else{
                viewHolder.tv_order_edit.setText(key);

                dis_2text = entry.getValue().toString();
                viewHolder.ed_order_edit.setText(dis_2text);
                viewHolder.ed_order_edit.setSelection(viewHolder.ed_order_edit.getText().length());
                viewHolder.ed_order_edit.setVisibility(View.VISIBLE);
                viewHolder.ed_order_edit.setEnabled(true);
            }

            return v;
        }
    }

    class ViewHolder {
        TextView tv_order_edit;
        EditText ed_order_edit;
    }
}
