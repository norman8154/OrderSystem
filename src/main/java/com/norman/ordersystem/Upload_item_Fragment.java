package com.norman.ordersystem;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Upload_item_Fragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Upload_item_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Upload_item_Fragment extends Fragment {
    ImageButton ibutton;
    Button btn_upload;
    EditText name,price,description;
    Spinner spinner;
    Uri photo_uri;
    FirebaseStorage storage = FirebaseStorage.getInstance();

    private OnFragmentInteractionListener mListener;

    public Upload_item_Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Upload_item_Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Upload_item_Fragment newInstance(String param1, String param2) {
        Upload_item_Fragment fragment = new Upload_item_Fragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mListener.set_toolbar("新增商品");
        mListener.set_toolbarButton(0,0);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        ibutton = (ImageButton) getActivity().findViewById(R.id.item_photo);

        btn_upload = (Button) getActivity().findViewById(R.id.btn_upload);
        name = (EditText) getActivity().findViewById(R.id.item_name);
        price = (EditText) getActivity().findViewById(R.id.price);
        description = (EditText) getActivity().findViewById(R.id.item_des);
        spinner = (Spinner) getActivity().findViewById(R.id.spinner_upload_item);

        ibutton.getLayoutParams().width = width / 2;
        ibutton.getLayoutParams().height = width / 2;
        btn_upload.getLayoutParams().width = width * 9 / 20;

        String categorys[] = {"古早味","雞料理","鴨料理","魚料理","丸子類","年菜料理"};
        ArrayAdapter<String> category_list = new ArrayAdapter<String>(this.getContext(),android.R.layout.simple_spinner_dropdown_item,categorys);
        spinner.setAdapter(category_list);

        final AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
        dialog.setCanceledOnTouchOutside(false);

        ibutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");

                startActivityForResult(intent,0);
            }
        });

        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StorageReference storageRef = storage.getReferenceFromUrl("gs://ordersystem-970fe.appspot.com");
                StorageReference mountainImagesRef = storageRef.child("images/" + name.getText().toString() + ".jpg");

                int status = check_input();

                if(status == 0){
                    ibutton.setDrawingCacheEnabled(true);
                    ibutton.buildDrawingCache();
                    Bitmap bitmap = ((BitmapDrawable) ibutton.getDrawable()).getBitmap();
                    ByteArrayOutputStream out_stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out_stream);
                    byte[] data = out_stream.toByteArray();

                    UploadTask uploadTask = mountainImagesRef.putBytes(data);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            dialog.setButton(DialogInterface.BUTTON_NEGATIVE,"確定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialog.cancel();
                                }
                            });
                            dialog.setMessage("上傳失敗,請重新上傳");
                            dialog.show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Items").child(spinner.getSelectedItem().toString());
                            Item item = new Item();
                            item.setName(name.getText().toString());
                            item.setPrice(price.getText().toString());
                            item.setDescription(description.getText().toString());
                            item.setURL(downloadUrl.toString());
                            item.setRemain("0");

                            databaseReference.child(item.getName()).setValue(item);

                            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "確定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    closefragment();
                                    mListener.onItemAdded();
                                    dialog.cancel();
                                }
                            });
                            dialog.setMessage("上傳成功!!");
                            dialog.show();
                        }
                    });
                }
            }
        });
    }

    private int check_input(){
        View focusView = null;
        if(name.getText().toString().length() < 1){
            name.setError("商品名稱不得為空");
            focusView = name;
            focusView.requestFocus();

            return -1;
        }else if(price.getText().toString().length() == 0){
            price.setError("價格不得為空");
            focusView = price;
            focusView.requestFocus();

            return -1;
        }else if(Integer.valueOf(price.getText().toString()) <= 0){
            price.setError("單價不得小於等於0");
            focusView = price;
            focusView.requestFocus();

            return -1;
        }

        return 0;
    }

    private void closefragment(){
        getFragmentManager().popBackStack();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_upload_item_, container, false);

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
        void onItemAdded();
        void set_toolbar(String tag);
        void set_toolbarButton(int state,int shopping_cart);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( requestCode == 0 && resultCode == Activity.RESULT_OK ) {
            photo_uri = data.getData();
            if( photo_uri != null ) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ibutton.setImageURI(photo_uri);
                    }
                });

                Toast.makeText(getContext(),"上傳成功！",Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(getContext(),"上傳失敗！",Toast.LENGTH_LONG);
            }
        }
        else {
            Toast.makeText(getContext(),"取消選擇檔案！",Toast.LENGTH_LONG);
        }
    }
}
