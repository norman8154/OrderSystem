package com.norman.ordersystem;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {
    GridView gv_Items;
    ImageButton btn_pageup,btn_pagedown;
    Spinner spinner;
    int page = 1,total_size = 0;
    HashMap<String,Item[]> map;
    Item[] items_all,current_items;
    myAdapter myAdapter;

    private OnFragmentInteractionListener mListener;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);

        String categorys[] = {"全部","古早味","雞料理","鴨料理","魚料理","丸子類","年菜料理"};

        gv_Items = (GridView) v.findViewById(R.id.gv_Items_main);
        btn_pagedown = (ImageButton) v.findViewById(R.id.btn_pagedown);
        btn_pageup = (ImageButton) v.findViewById(R.id.btn_pageup);
        spinner = (Spinner) v.findViewById(R.id.category_spinner);

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
            myAdapter = new myAdapter(current_items,page);
            gv_Items.setAdapter(myAdapter);
        }else if(map.isEmpty()){
            items_all = new Item[0];
            current_items = items_all;
            myAdapter = new myAdapter(current_items,page);
            gv_Items.setAdapter(myAdapter);
        }

        final int isAdmin = bundle.getInt("isAdmin");

        mListener.set_toolbar("首頁");
        if(isAdmin == 0)
            mListener.set_toolbarButton(0,1);
        else
            mListener.set_toolbarButton(0,0);

        final ArrayAdapter<String> category_list = new ArrayAdapter<String>(this.getContext(),android.R.layout.simple_spinner_dropdown_item,categorys);
        spinner.setAdapter(category_list);

        if(page == 1){
            btn_pageup.setVisibility(View.INVISIBLE);
            btn_pageup.setEnabled(false);
        }
        if(current_items.length <= page * 10){
            btn_pagedown.setVisibility(View.INVISIBLE);
            btn_pagedown.setEnabled(false);
        }

        if(current_items.length < 11) {
            btn_pagedown.setVisibility(View.INVISIBLE);
            btn_pagedown.setEnabled(false);
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(spinner.getSelectedItem().toString().equals("全部")){
                    current_items = items_all;
                    page = 1;

                    btn_pageup.setVisibility(View.INVISIBLE);
                    btn_pageup.setEnabled(false);
                    if(current_items.length <= page * 10){
                        btn_pagedown.setVisibility(View.INVISIBLE);
                        btn_pagedown.setEnabled(false);
                    }else{
                        btn_pagedown.setVisibility(View.VISIBLE);
                        btn_pagedown.setEnabled(true);
                    }

                    myAdapter = new myAdapter(current_items,page);
                    gv_Items.setAdapter(myAdapter);
                }else{
                    current_items = map.get(spinner.getSelectedItem().toString());
                    page = 1;

                    btn_pageup.setVisibility(View.INVISIBLE);
                    btn_pageup.setEnabled(false);
                    if(current_items.length <= page * 10){
                        btn_pagedown.setVisibility(View.INVISIBLE);
                        btn_pagedown.setEnabled(false);
                    }else{
                        btn_pagedown.setVisibility(View.VISIBLE);
                        btn_pagedown.setEnabled(true);
                    }

                    myAdapter = new myAdapter(current_items,page);
                    gv_Items.setAdapter(myAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                return ;
            }
        });

        btn_pagedown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(page == 1){
                    btn_pageup.setVisibility(View.VISIBLE);
                    btn_pageup.setEnabled(true);
                    btn_pagedown.setVisibility(View.VISIBLE);
                    btn_pagedown.setEnabled(true);
                }
                page++;
                if(current_items.length <= page * 10){
                    btn_pagedown.setVisibility(View.INVISIBLE);
                    btn_pagedown.setEnabled(false);
                }
                myAdapter = new myAdapter(current_items,page);
                gv_Items.setAdapter(myAdapter);
            }
        });

        btn_pageup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                page--;
                if(page == 1){
                    btn_pageup.setVisibility(View.INVISIBLE);
                    btn_pageup.setEnabled(false);
                    btn_pagedown.setVisibility(View.VISIBLE);
                    btn_pagedown.setEnabled(true);
                }else{
                    btn_pagedown.setVisibility(View.VISIBLE);
                    btn_pagedown.setEnabled(true);
                }
                myAdapter = new myAdapter(current_items,page);
                gv_Items.setAdapter(myAdapter);
            }
        });

        final GestureDetector gesture = new GestureDetector(getActivity(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDown(MotionEvent e) {
                        return false;
                    }

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                           float velocityY) {
                        final int FLING_MIN_DISTANCE_X = 100;
                        final int FLING_MIN_VELOCITY = 150;
                        final int FLING_MIN_DISTANCE_Y = 300;

                        if (Math.abs(e1.getY() - e2.getY()) > FLING_MIN_DISTANCE_Y)
                            return false;

                        if((e1.getX() - e2.getX()) > FLING_MIN_DISTANCE_X && Math.abs(velocityX) > FLING_MIN_VELOCITY){
                            if(current_items.length > page * 10){
                                if(page == 1){
                                    btn_pageup.setVisibility(View.VISIBLE);
                                    btn_pageup.setEnabled(true);
                                    btn_pagedown.setVisibility(View.VISIBLE);
                                    btn_pagedown.setEnabled(true);
                                }
                                page++;
                                if(current_items.length <= page * 10){
                                    btn_pagedown.setVisibility(View.INVISIBLE);
                                    btn_pagedown.setEnabled(false);
                                }
                                myAdapter = new myAdapter(current_items,page);
                                gv_Items.setAdapter(myAdapter);
                            }
                        }else if((e1.getX() - e2.getX()) < (-1 * FLING_MIN_DISTANCE_X) && Math.abs(velocityX) > FLING_MIN_VELOCITY){
                            if(page != 1){
                                page--;
                                if(page == 1){
                                    btn_pageup.setVisibility(View.INVISIBLE);
                                    btn_pageup.setEnabled(false);
                                    btn_pagedown.setVisibility(View.VISIBLE);
                                    btn_pagedown.setEnabled(true);
                                }else{
                                    btn_pagedown.setVisibility(View.VISIBLE);
                                    btn_pagedown.setEnabled(true);
                                }
                                myAdapter = new myAdapter(current_items,page);
                                gv_Items.setAdapter(myAdapter);
                            }
                        }
                        return true;
                    }
                });

        gv_Items.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gesture.onTouchEvent(event);
            }
        });

        gv_Items.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mListener.fragment_swap(current_items[(page - 1) * 10 + i],isAdmin);
            }
        });

        return v;
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
        void fragment_swap(Item item,int isAdmin);
        void set_toolbar(String tag);
        void set_toolbarButton(int state,int shopping_cart);
    }

    public void updateItems(final HashMap<String,Item[]> map){
        this.map = map;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
                myAdapter = new myAdapter(current_items,page);
            }
        });
    }

    public int getPage(){
        return page;
    }

    public void page_up(){
        page--;
        if(page == 1){
            btn_pageup.setVisibility(View.INVISIBLE);
            btn_pageup.setEnabled(false);
            btn_pagedown.setVisibility(View.VISIBLE);
            btn_pagedown.setEnabled(true);
        }else{
            btn_pagedown.setVisibility(View.VISIBLE);
            btn_pagedown.setEnabled(true);
        }
        myAdapter = new myAdapter(current_items,page);
        gv_Items.setAdapter(myAdapter);
    }

    class myAdapter extends BaseAdapter {
        Item[] items;
        int page;

        public myAdapter(Item[] items,int page){
            this.items = items;
            this.page = page;
        }

        @Override
        public int getCount() {
            if(items.length > page * 10)
                return 10;
            else
                return items.length - (page - 1) * 10;
        }

        @Override
        public Item getItem(int position) {
            return items[(page - 1) * 10 + position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View v;

            if (convertView == null) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_display, parent, false);
            } else {
                v = convertView;
            }

            Item item = getItem(position);

            ImageView ivItem = (ImageView) v.findViewById(R.id.ivItem);
            TextView item_name = (TextView) v.findViewById(R.id.Item_name);
            TextView item_price = (TextView) v.findViewById(R.id.Item_price);

            Glide.with(ivItem.getContext())
                    .load(item.getURL())
                    .into(ivItem);
            item_name.setText(item.getName());
            String sprice = item.getPrice() + "元";
            item_price.setText(sprice);

            return v;
        }
    }
}
