package com.norman.ordersystem;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link User_infoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link User_infoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class User_infoFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String name;
    private String phone;
    private String addr;
    TextView tvName,tvPhone,tvAddr;

    private OnFragmentInteractionListener mListener;

    public User_infoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment User_infoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static User_infoFragment newInstance(String param1, String param2) {
        User_infoFragment fragment = new User_infoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
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
        View v = inflater.inflate(R.layout.fragment_user_info, container, false);

        mListener.set_toolbar("個人資料");
        mListener.set_toolbarButton(0,1);

        tvName = (TextView) v.findViewById(R.id.tvName);
        tvPhone = (TextView) v.findViewById(R.id.tvPhone);
        tvAddr = (TextView) v.findViewById(R.id.tvAddr);

        Bundle bundle = this.getArguments();

        Log.d("user info","user info:" + bundle.getString("name") + " " + bundle.getString("phone"));

        tvName.setText(bundle.getString("name"));
        tvPhone.setText(bundle.getString("phone"));
        tvAddr.setText(bundle.getString("addr"));

        return v;
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
        void onFragmentInteraction(Uri uri);
        void set_toolbar(String tag);
        void set_toolbarButton(int state,int shopping_cart);
    }
}
