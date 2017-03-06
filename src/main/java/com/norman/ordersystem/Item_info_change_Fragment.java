package com.norman.ordersystem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Item_info_change_Fragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Item_info_change_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Item_info_change_Fragment extends Fragment {
    TextView tv_price,tv_name,tv_desc,tv_remain;
    ImageView iv_item;
    ListView lv_items;
    EditText ed_input;
    Button btn_change,btn_make_change,btn_item_delete;
    Item item;
    String[] info_list;
    String old_name;
    Uri photo_uri;
    int photo_changed;

    private OnFragmentInteractionListener mListener;

    public Item_info_change_Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Item_info_change_Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Item_info_change_Fragment newInstance(String param1, String param2) {
        Item_info_change_Fragment fragment = new Item_info_change_Fragment();
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
        final View v = inflater.inflate(R.layout.fragment_item_info_change_, container, false);
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        final View info_change = layoutInflater.inflate(R.layout.list_items,null);
        final View info_input = layoutInflater.inflate(R.layout.info_input,null);

        setHasOptionsMenu(true);
        mListener.set_toolbarButton(1,0);

        tv_price = (TextView) v.findViewById(R.id.item_info_change_price);
        tv_desc = (TextView) v.findViewById(R.id.item_info_change_desc);
        tv_name = (TextView) v.findViewById(R.id.item_info_change_name);
        tv_remain = (TextView) v.findViewById(R.id.item_info_change_remain);

        iv_item = (ImageView) v.findViewById(R.id.item_info_change_photo);
        lv_items = (ListView) info_change.findViewById(R.id.lv_items);
        ed_input = (EditText) info_input.findViewById(R.id.ed_info_input);

        btn_change = (Button) v.findViewById(R.id.btn_item_info_change);
        btn_make_change = (Button) v.findViewById(R.id.item_change_made);
        btn_item_delete = (Button) v.findViewById(R.id.btn_item_delete);

        final AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(info_change)
                .create();

        final AlertDialog dialog_input = new AlertDialog.Builder(getContext())
                .setView(info_input)
                .create();

        Bundle b = this.getArguments();
        item = b.getParcelable("Item");
        photo_changed = 0;
        old_name = item.getName();

        Glide.with(iv_item.getContext())
                .load(item.getURL())
                .into(iv_item);

        tv_name.setText(item.getName());
        String price_text = "單價:" + item.getPrice() + "元";
        tv_price.setText(price_text);
        String desc_text = "簡介:\n" + item.getDescription();
        tv_desc.setText(desc_text);
        String remain_text = "(剩餘數量:" + item.getRemain() + "份)";
        tv_remain.setText(remain_text);

        info_list = new String[5];
        info_list[0] = "商品名稱";
        info_list[1] = "價格";
        info_list[2] = "簡介";
        info_list[3] = "庫存";
        info_list[4] = "圖片";

        ArrayAdapter adapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, info_list);
        lv_items.setAdapter(adapter);

        btn_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.setTitle("請選擇要更改的項目");
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.cancel();
                    }
                });
                dialog.show();
            }
        });

        btn_make_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update_info();
                final AlertDialog dialog_update = new AlertDialog.Builder(getContext()).create();
                dialog_update.setButton(DialogInterface.BUTTON_POSITIVE, "確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog_update.cancel();
                        mListener.onItemAdded();
                        closefragment();
                    }
                });
                dialog_update.setMessage("修改成功!!");
                dialog_update.show();
            }
        });

        btn_item_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog dialog_check = new AlertDialog.Builder(getContext()).create();
                dialog_check.setButton(DialogInterface.BUTTON_POSITIVE, "確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference storageRef = storage.getReferenceFromUrl("gs://ordersystem-970fe.appspot.com");
                        StorageReference ImageRef = storageRef.child("images/" + old_name + ".jpg");

                        ImageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                delete_item();
                                final AlertDialog dialog_delete = new AlertDialog.Builder(getContext()).create();
                                dialog_delete.setMessage("刪除成功！");
                                dialog_delete.setButton(DialogInterface.BUTTON_POSITIVE, "確定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialog_delete.cancel();
                                        mListener.onItemAdded();
                                        closefragment();
                                    }
                                });
                                dialog_delete.show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                final AlertDialog dialog_delete = new AlertDialog.Builder(getContext()).create();
                                dialog_delete.setMessage("刪除失敗！");
                                dialog_delete.setButton(DialogInterface.BUTTON_POSITIVE, "確定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialog_delete.cancel();
                                    }
                                });
                                dialog_delete.show();
                            }
                        });
                    }
                });
                dialog_check.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog_check.cancel();
                    }
                });
                dialog_check.setMessage("確定要刪除此商品嗎?");
                dialog_check.show();
            }
        });

        lv_items.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final int onClick_position = i;
                if(i != 4){
                    dialog_input.setTitle("請輸入新的資料");
                    dialog_input.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialog_input.cancel();
                            dialog.cancel();
                        }
                    });
                    dialog_input.setButton(DialogInterface.BUTTON_POSITIVE, "確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                          Log.d("輸入資料","輸入:" + ed_input.getText().toString());
                            int[] status = info_change(ed_input.getText().toString(),onClick_position);
                            final AlertDialog dialog_info_change = new AlertDialog.Builder(getContext()).create();
                            if(status[0] != -1){
                                dialog_input.cancel();
                                dialog.cancel();
                                ed_input.setText("");
                            }else{
                                if(status[1] == -1){
                                    dialog_info_change.setMessage("輸入資料不得為空！！");
                                }else if(status[1] == 1){
                                    dialog_info_change.setMessage("單價必需大於0元！！");
                                }else if(status[1] == 3){
                                    dialog_info_change.setMessage("庫存不得小於0！！");
                                }
                                dialog_info_change.setButton(DialogInterface.BUTTON_POSITIVE, "確定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialog_info_change.cancel();
                                        ed_input.setText("");
                                    }
                                });
                                dialog_info_change.show();
                            }
                        }
                    });
                    dialog_input.show();
                }else{
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    dialog.cancel();

                    startActivityForResult(intent,0);
                }

            }
        });

        return v;
    }

    private void closefragment(){
        getFragmentManager().popBackStack();
    }

    public int[] info_change(final String text,int position){
        int[] result = {0,0};
        Log.d("得到資料","in info_change:" + text);
        if(text.length() == 0){
            result[0] = -1;
            result[1] = -1;
            return result;
        }

        switch (position){
            case 0:
                item.setName(text);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_name.setText(text);
                    }
                });
                break;
            case 1:
                int new_pirce = Integer.valueOf(text);
                if(new_pirce <= 0){
                    result[0] = -1;
                    result[1] = position;
                }else{
                    item.setPrice(text);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String price_text = "單價:" + text + "元";
                            tv_price.setText(price_text);
                        }
                    });
                }
                break;
            case 2:
                item.setDescription(text);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String desc_text = "簡介:\n" + text;
                        tv_desc.setText(desc_text);
                    }
                });
                break;
            case 3:
                int remain = Integer.valueOf(text);
                if(remain < 0){
                    result[0] = -1;
                    result[1] = position;
                }else{
                    item.setRemain(text);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String remain_text = "(剩餘數量:" + item.getRemain() + "份)";
                            tv_remain.setText(remain_text);
                        }
                    });
                }
        }

        return result;
    }

    public void delete_item(){
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Items");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(final DataSnapshot ds : dataSnapshot.getChildren()){
                    if(ds.hasChild(item.getName())){
                        DatabaseReference todelete_item = FirebaseDatabase.getInstance().getReference("Items").child(ds.getKey()).child(old_name);
                        todelete_item.removeValue();
                        mListener.onItemAdded();
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void update_info(){
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Items");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(final DataSnapshot ds : dataSnapshot.getChildren()){
                    if(ds.hasChild(item.getName())){
                        if(photo_changed == 0){
                            DatabaseReference old_value = FirebaseDatabase.getInstance().getReference("Items").child(ds.getKey()).child(old_name);
                            old_value.removeValue();
                            databaseReference.child(ds.getKey()).child(item.getName()).setValue(item);
                        }else{
                            FirebaseStorage storage = FirebaseStorage.getInstance();
                            StorageReference storageRef = storage.getReferenceFromUrl("gs://ordersystem-970fe.appspot.com");
                            StorageReference oldImageRef = storageRef.child("images/" + old_name + ".jpg");
                            StorageReference newImageRef = storageRef.child("images/" + item.getName() + ".jpg");

                            oldImageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                }
                            });

                            iv_item.setDrawingCacheEnabled(true);
                            iv_item.buildDrawingCache();
                            Bitmap bitmap = ((BitmapDrawable) iv_item.getDrawable()).getBitmap();
                            ByteArrayOutputStream out_stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out_stream);
                            byte[] data = out_stream.toByteArray();

                            UploadTask uploadTask = newImageRef.putBytes(data);
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {

                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                    item.setURL(downloadUrl.toString());

                                    DatabaseReference old_value = FirebaseDatabase.getInstance().getReference("Items").child(ds.getKey()).child(old_name);
                                    old_value.removeValue();
                                    databaseReference.child(ds.getKey()).child(item.getName()).setValue(item);
                                }
                            });
                        }
                        break;
                    }
                }
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
        void onItemAdded();
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( requestCode == 0 && resultCode == Activity.RESULT_OK ) {
            photo_uri = data.getData();
            if( photo_uri != null ) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        iv_item.setImageURI(photo_uri);
                    }
                });
                photo_changed = 1;
            }
        }
        else {
            Toast.makeText(getContext(),"取消選擇檔案！",Toast.LENGTH_LONG);
        }
    }
}
