package com.example.froject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;


public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    private FirebaseAuth mAuth;
    private Context mContext = SignupActivity.this;
    private FirebaseFirestore db;
    private EditText etEmail, etPassword, etPasswordCheck;

    FirebaseUser user;

    private boolean is_has_email = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.setEmail);
        etPassword = findViewById(R.id.setPass);
        etPasswordCheck = findViewById(R.id.passCheck);

        etEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b == false) {
                    check_pass(etEmail.getText().toString());
                }
            }
        });

        etEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b == false) {
                    if (etEmail.getText().equals(etPasswordCheck.getText()))
                        startToast("??????????????? ???????????? ????????????");
                }
            }
        });

        findViewById(R.id.nextSignUp).setOnClickListener(onClickListener);
        findViewById(R.id.sendMail).setOnClickListener(onClickListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    @Override
    public void onBackPressed() {
        backAlert();
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.nextSignUp:
                    //sign_Up();
                    sign_up2();
                    break;
                case R.id.sendMail:
                    send_email();

                    //send_mail();
                    break;
            }
        }
    };

    /*private void set_date() {
        EditText date = ((EditText)findViewById(R.id.setDate));

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.datepicker ,new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                date.setText(year + "??? " + (month+1) + "??? " + dayOfMonth + "???");
            }
        }, year, month, day);

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });
    }*/

    private void send_email() {
        String email = ((EditText) findViewById(R.id.setEmail)).getText().toString();
        if (!check_email(email)) {
            startToast("?????? ????????? ??????????????????\n(ex. abcd@korea.ac.kr / abc@korea.edu)");
            return;
        }
        mAuth.createUserWithEmailAndPassword(email,"000000").addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    user = mAuth.getCurrentUser();
                    db = FirebaseFirestore.getInstance();

                    user.sendEmailVerification()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        checkAlert(email);

                                        ((Button)findViewById(R.id.sendMail)).setText("????????????");

                                        ((Button)findViewById(R.id.sendMail)).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                user.reload();
                                                if (user.isEmailVerified()) {
                                                    findViewById(R.id.setPass).setVisibility(View.VISIBLE);
                                                    findViewById(R.id.passCheck).setVisibility(View.VISIBLE);

                                                    ((Button)findViewById(R.id.sendMail)).setBackground(getDrawable(R.drawable.login_unbox));
                                                    ((Button)findViewById(R.id.sendMail)).setClickable(false);
                                                    ((Button)findViewById(R.id.sendMail)).setText("????????????");

                                                    AlertDialog.Builder msgBuilder = new AlertDialog.Builder(SignupActivity.this)
                                                            .setTitle("????????? ?????? ??????")
                                                            .setMessage("????????? ????????? ?????????????????????\n??????????????? ??????????????????")
                                                            .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int i) {
                                                                }
                                                            });
                                                    AlertDialog msgDlg = msgBuilder.create();
                                                    msgDlg.show();
                                                    Info info = new Info(email);
                                                    db.collection("users").document(user.getEmail()).set(info);

                                                }
                                                else
                                                    startToast("????????? ???????????????????????????.");
                                            }
                                        });

                                    }
                                }
                            });
                } else {
                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        startToast("???????????? ?????? ?????????????????????");
                    }
                    else if (task.getException() != null) {
                        startToast(task.getException().toString());
                    }
                }
            }
        });
    }


    private void sign_up2() {
        String password = ((EditText) findViewById(R.id.setPass)).getText().toString();
        String checkpass = ((EditText) findViewById(R.id.passCheck)).getText().toString();

        Log.d("123",password+checkpass);

        if(check_pass(password)) { return; }
        else if (!password.equals(checkpass))
            startToast("??????????????? ???????????? ????????????");
        else {
            mAuth.confirmPasswordReset(user.getEmail(),password);
            startToast("??????????????? ?????????????????????.");
            startActivity(UserinfoActivity.class);
        }
    }

    private boolean check_pass(String pass) {
        String pattern = "(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]+";

        if (pass.length() < 6 ) {
            startToast("??????????????? 6????????? ????????? ?????????.");
            return true;
        }
        else if ( !Pattern.matches(pattern,pass)) {
            startToast("????????? ????????? ???????????? ??????????????????.");
            return true;
        }
        return false;
    }

    private void send_mail() {
        String email = ((EditText) findViewById(R.id.setEmail)).getText().toString();
        String password = ((EditText) findViewById(R.id.setPass)).getText().toString();
        String checkpass = ((EditText) findViewById(R.id.passCheck)).getText().toString();

        //????????? ?????? & ??????

        if(email.length()>0 && password.length()>0 && checkpass.length()>0) {
            if(!check_email(email)) {
                startToast("??????????????? ?????????????????? ac.kr");
            }
            else if (password.length() >= 6) {
                if(password.equals(checkpass)) {
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this, (task) -> {
                                if(task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    Info info = new Info(email);
                                    db.collection("users").document(user.getEmail()).set(info);
                                    user.sendEmailVerification()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                    if(task.isSuccessful()) {
                                                        checkAlert(email);

                                                    }
                                                }
                                            });
                                } else {
                                    if(task.getException() instanceof FirebaseAuthUserCollisionException) {
                                        startToast("???????????? ?????? ?????????????????????");
                                    }
                                    else if (task.getException() != null) {
                                        startToast(task.getException().toString());
                                    }
                                }
                            });
                } else {
                    startToast("??????????????? ???????????? ????????????.");
                }
            } else {
                startToast("??????????????? 6????????? ????????? ?????????.");
            }
        } else {
            startToast("???????????? ?????? ????????? ????????????.");
        }
    }
    private void sign_Up() { // ????????????
        startActivity(LoginActivity.class);
        /*FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if(firebaseUser.isEmailVerified()) {
            startToast("????????? ?????? ??????");
            startActivity(UserinfoActivity.class);
        } else {
            startToast("????????? ????????? ??????????????????.");
        }*/
    }

    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void startActivity(Class c) {
        Intent intent = new Intent(this, c);
        startActivity(intent);
    }

    private void backAlert() {
        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(SignupActivity.this)
                .setTitle("?????????")
                .setMessage("???????????? ????????? ???????????????. \n?????? ??? ?????? ??????????????????????")
                .setPositiveButton("???", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        startActivity(LoginActivity.class);
                    }
                })
                .setNegativeButton("?????????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                    }
                });
        AlertDialog msgDlg = msgBuilder.create();
        msgDlg.show();
    }

    private void checkAlert(String email) {
        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(SignupActivity.this)
                .setTitle("????????? ??????")
                .setMessage(email+"\n???????????? ??????????????????")
                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                    }
                });
        AlertDialog msgDlg = msgBuilder.create();
        msgDlg.show();

    }

    private boolean check_email(String email) {
        String pattern = "[0-9a-zA-Z]*\\@[0-9a-zA-Z]*\\.ac\\.kr"; //--@--.ac.kr
        String pattern2 = "[0-9a-zA-Z]*\\@[0-9a-zA-Z]*\\.edu";    //--@--.edu
        //?????? ????????? ?????? ??????, ?????? ??????
        String pattern3 = "[0-9a-zA-Z]*\\@naver\\.com";    //--@naver.com
        boolean regex = Pattern.matches(pattern, email) || Pattern.matches(pattern2,email) || Pattern.matches(pattern3,email);

        return regex;
    }
}
