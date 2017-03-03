package com.norman.ordersystem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Item_info_Fragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Item_info_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Item_info_Fragment extends Fragment {
    TextView item_info_name,item_info_price,item_info_desc,order_count,item_info_remain;
    ImageView item_info_photo;
    Button order_check;
    ImageButton order_decrease,order_add;
    Double count;
    Item item;

    private OnFragmentInteractionListener mListener;

    public Item_info_Fragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static Item_info_Fragment newInstance(String param1, String param2) {
        Item_info_Fragment fragment = new Item_info_Fragment();
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
        View v = inflater.inflate(R.layout.fragment_item_info_, container, false);

        item_info_desc = (TextView) v.findViewById(R.id.item_info_desc);
        item_info_name = (TextView) v.findViewById(R.id.item_info_name);
        item_info_photo = (ImageView) v.findViewById(R.id.item_info_photo);
        item_info_price = (TextView) v.findViewById(R.id.item_info_price);
        item_info_remain = (TextView) v.findViewById(R.id.item_info_remain);
        order_count = (TextView) v.findViewById(R.id.order_count);

        order_decrease = (ImageButton) v.findViewById(R.id.order_decrease);
        order_add = (ImageButton) v.findViewById(R.id.order_add);
        order_check = (Button) v.findViewById(R.id.order_check);

        Bundle b = this.getArguments();
        item = b.getParcelable("Item");
        count = b.getDouble("count");
        int isAdmin = b.getInt("isAdmin");

        setHasOptionsMenu(true);

        if(isAdmin == 1)
            mListener.set_toolbarButton(1,0);
        else
            mListener.set_toolbarButton(1,1);

        Glide.with(item_info_photo.getContext())
                .load(item.getURL())
                .into(item_info_photo);

        order_count.setText(String.valueOf(count.intValue()));
        item_info_name.setText(item.getName());
        String price_text = "單價:" + item.getPrice() + "元";
        item_info_price.setText(price_text);
        String desc_text = "簡介:\n" + item.getDescription();
        item_info_desc.setText(desc_text);
        String remain_text = "(剩餘數量:" + item.getRemain() + "份)";
        item_info_remain.setText(remain_text);

        if(count == 0)
            order_decrease.setEnabled(false);

        order_decrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count--;
                order_count.setText(String.valueOf(count.intValue()));
                if(count == 0)
                    order_decrease.setEnabled(false);
            }
        });

        order_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count++;
                order_count.setText(String.valueOf(count.intValue()));
                if(!order_decrease.isEnabled())
                    order_decrease.setEnabled(true);
            }
        });

        order_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Double[] temp = new Double[3];
                temp[0] = count;
                temp[1] = count * Double.valueOf(item.getPrice());
                temp[2] = Integer.valueOf(item.getRemain()) - count;
                if(temp[2] < 0){
                    final AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
                    dialog.setMessage("剩餘數量不足");
                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, "確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialog.cancel();
                        }
                    });
                    dialog.show();
                }else{
                    mListener.order_add(item.getName(),temp);
                    closefragment();
                }
            }
        });

        return v;
    }

    private void closefragment(){
        mListener.item_info_closed();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                closefragment();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
        void order_add(String name,Double info[]);
        void item_info_closed();
        void set_toolbarButton(int state,int shopping_cart);
    }
}
