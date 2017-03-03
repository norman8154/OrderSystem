package com.norman.ordersystem;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
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
 * {@link Item_manage_Fragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Item_manage_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Item_manage_Fragment extends Fragment {
    ListView lv_item_manage;
    Spinner spinner;
    HashMap<String,Item[]> map;
    Item[] items_all,current_items;
    int total_size = 0;
    MyAdapter myAdapter;

    private OnFragmentInteractionListener mListener;

    public Item_manage_Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Item_manage_Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Item_manage_Fragment newInstance(String param1, String param2) {
        Item_manage_Fragment fragment = new Item_manage_Fragment();
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
        View v = inflater.inflate(R.layout.fragment_item_manage_, container, false);

        String categorys[] = {"全部","古早味","雞料理","鴨料理","魚料理","丸子類","年菜料理"};

        mListener.set_toolbar("商品管理");
        mListener.set_toolbarButton(0,0);

        lv_item_manage = (ListView) v.findViewById(R.id.lv_item_manage);
        spinner = (Spinner) v.findViewById(R.id.item_manage_spinner);

        Bundle bundle = this.getArguments();
        map = (HashMap<String,Item[]>) bundle.getSerializable("Items");
        if(!map.isEmpty() && total_size == 0){
            for(Map.Entry<String,Item[]> entry : map.entrySet()){
                total_size += entry.getValue().length;
            }
            items_all = new Item[total_size];
            int i = 0;
            for(Map.Entry<String,Item[]> entry : map.entrySet()){
                Item[] temp = entry.getValue();
                for(int j = 0;j < temp.length;j++){
                    items_all[i] = temp[j];
                    i++;
                }
            }
            current_items = items_all;
            myAdapter = new MyAdapter(current_items);
            lv_item_manage.setAdapter(myAdapter);
        }else if(map.isEmpty()){
            items_all = new Item[0];
            current_items = items_all;
            myAdapter = new MyAdapter(current_items);
            lv_item_manage.setAdapter(myAdapter);
        }

        final ArrayAdapter<String> category_list = new ArrayAdapter<String>(this.getContext(),android.R.layout.simple_spinner_dropdown_item,categorys);
        spinner.setAdapter(category_list);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(spinner.getSelectedItem().toString().equals("全部")){
                    current_items = items_all;
                    myAdapter = new MyAdapter(current_items);
                    lv_item_manage.setAdapter(myAdapter);
                }else{
                    current_items = map.get(spinner.getSelectedItem().toString());
                    myAdapter = new MyAdapter(current_items);
                    lv_item_manage.setAdapter(myAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                return ;
            }
        });

        lv_item_manage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mListener.admin_item_info_swap(current_items[i]);
            }
        });

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
        void admin_item_info_swap(Item item);
        void set_toolbar(String tag);
        void set_toolbarButton(int state,int shopping_cart);
    }

    private class MyAdapter extends BaseAdapter {
        private Item[] items;

        public MyAdapter(Item[] items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Item getItem(int position) {
            return items[position];
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

            Item item = getItem(position);

            TextView order_dis_1 = (TextView) v.findViewById(R.id.tv_order_dis_1);
            TextView order_dis_2 = (TextView) v.findViewById(R.id.tv_order_dis_2);

            order_dis_1.setText(item.getName());
            order_dis_2.setText(item.getPrice());

            return v;
        }
    }
}
