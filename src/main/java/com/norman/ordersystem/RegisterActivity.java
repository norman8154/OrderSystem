package com.norman.ordersystem;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private EditText emailText,passwordText,nameText,phoneText,addrText;
    private Button register_button,cancel_button;
    private Toolbar toolbar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle("註冊");

        auth = FirebaseAuth.getInstance();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        emailText = (EditText) findViewById(R.id.emailText);
        passwordText = (EditText) findViewById(R.id.passwordText);
        nameText = (EditText) findViewById(R.id.nameText);
        phoneText = (EditText) findViewById(R.id.phoneText);
        addrText = (EditText) findViewById(R.id.addrText);

        toolbar = (Toolbar) findViewById(R.id.tbRegister);
        toolbar.setTitle("註冊");
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this,getResources().getInteger(R.integer.action_menu_item_text_size));

        register_button = (Button) findViewById(R.id.register_button);
        cancel_button = (Button) findViewById(R.id.cancel_button);

        register_button.getLayoutParams().width = width * 9 / 20;
        cancel_button.getLayoutParams().width = width * 9 / 20;

        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailText.getText().toString();
                String password = passwordText.getText().toString();

                int status = check_input();
                if(status == 0)
                    register(email,password);
            }
        });

        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    private int check_input(){
        View focusView = null;
        if(nameText.getText().toString().length() < 1){
            nameText.setError("姓名不得為空");
            focusView = nameText;
            focusView.requestFocus();

            return -1;
        }else if(passwordText.getText().toString().length() < 6){
            passwordText.setError("密碼必需至少6碼");
            focusView = passwordText;
            focusView.requestFocus();

            return -1;
        }else if(!emailText.getText().toString().contains("@")){
            emailText.setError("請輸入完整的email地址");
            focusView = emailText;
            focusView.requestFocus();

            return -1;
        }else if(phoneText.getText().toString().length() < 10){
            phoneText.setError("請輸入電話號碼，市話需加區碼");
            focusView = phoneText;
            focusView.requestFocus();

            return -1;
        }else if(addrText.getText().toString().length() < 8){
            addrText.setError("請輸入完整送貨地址");
            focusView = addrText;
            focusView.requestFocus();

            return -1;
        }

        return 0;
    }

    private void register(final String email, final String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    new AlertDialog.Builder(RegisterActivity.this)
                                            .setMessage("註冊成功！")
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    String name = nameText.getText().toString();
                                                    String phone = phoneText.getText().toString();
                                                    String addr = addrText.getText().toString();
                                                    Intent intent = new Intent();
                                                    Bundle bundle = new Bundle();

                                                    bundle.putString("name",name);
                                                    bundle.putString("phone",phone);
                                                    bundle.putString("addr",addr);
                                                    bundle.putString("email",email);
                                                    bundle.putString("password",password);
                                                    intent.putExtras(bundle);

                                                    setResult(RESULT_OK,intent);
                                                    finish();
                                                }
                                            })
                                            .setCancelable(false)
                                            .show();
                                }else{
                                    new AlertDialog.Builder(RegisterActivity.this)
                                            .setMessage("註冊失敗")
                                            .setPositiveButton("OK", null)
                                            .setCancelable(false)
                                            .show();
                                }
                            }
                        });
    }

}
