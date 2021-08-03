package com.example.froject;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ServerTimestamp;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE;

public class WriteActivity extends AppCompatActivity {

    private static final String TAG = "WriteActivity";
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference docRef = db.collection("users").document(user.getEmail());
    CollectionReference boardRef = docRef.collection("Board");

    LinearLayout contentslayout;
    PostData new_post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        findViewById(R.id.addContents).setOnClickListener(onClickListener);
        findViewById(R.id.finishcontents).setOnClickListener(onClickListener);

        db.collectionGroup("Board").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                Log.w(TAG,"omg"+task.getResult().getDocuments().size());
            }
        });

        contentslayout = findViewById(R.id.contentsLayout);

    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.addContents:
                    //add_contents();
                    break;
                case R.id.finishcontents:
                    @Nullable String title = ((EditText)findViewById(R.id.inputTitle)).getText().toString();
                    @Nullable String place = ((EditText)findViewById(R.id.inputPlace)).getText().toString();
                    @Nullable String period = ((EditText)findViewById(R.id.inputPeriod)).getText().toString();
                    @Nullable String count = ((EditText)findViewById(R.id.inputCount)).getText().toString();

                    new_post = new PostData(title,place,period);
                    //firebase::database::ServerTimestamp();
                    Date date = new Date(System.currentTimeMillis());
                    SimpleDateFormat sdate = new SimpleDateFormat("yy-MM-dd hh:mm:ss");


                    //boardRef.get().getResult().getDocuments().size();

                    boardRef.document(sdate.format(date)).set(new_post);
                    boardRef.document(sdate.format(date)).update("writetime",date);

                    Intent intent = getIntent();
                    intent.setClass(WriteActivity.this,MainActivity.class);
                    intent.putExtra("reload",true);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    };


    private void add_contents() {
        int id_contentlayout = 1;
        int id_contentfield = 10;
        int id_field1 = 100;
        int id_field2 = 1000;
        int id_contenttext = 10000;

        String personcount = ((EditText)findViewById(R.id.inputCount)).getText().toString();
        int index = Integer.parseInt(personcount);
        for(int i=0 ; i<index; i++) {
            // contentlayout 생성
            LinearLayout contentlayout = new LinearLayout(getApplicationContext());

            LinearLayout.LayoutParams param_contentlayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            contentlayout.setOrientation(LinearLayout.VERTICAL);
            contentlayout.setId(id_contentlayout+i);
            param_contentlayout.setMargins(0,0,0,getDp(10));

            contentlayout.setLayoutParams(param_contentlayout);
            contentslayout.addView(contentlayout);

            // contentfield 생성
            LinearLayout contentfield = new LinearLayout(getApplicationContext());

            LinearLayout.LayoutParams param_contentfield = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            contentfield.setOrientation(LinearLayout.HORIZONTAL);
            contentfield.setId(id_contentfield+i);

            contentfield.setLayoutParams(param_contentfield);
            contentlayout.addView(contentfield);

            // field1 생성
            TextView field1 = new TextView(getApplicationContext());

            LinearLayout.LayoutParams param_field1 = new LinearLayout.LayoutParams(getDp(100), LinearLayout.LayoutParams.WRAP_CONTENT);
            field1.setText("hello");
            field1.setTextSize(20);
            field1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            field1.setId(id_field1+i);

            field1.setLayoutParams(param_field1);
            contentfield.addView(field1);

            // field2 생성
            TextView field2 = new TextView(getApplicationContext());

            LinearLayout.LayoutParams param_field2 = new LinearLayout.LayoutParams(getDp(100), LinearLayout.LayoutParams.WRAP_CONTENT);
            field2.setText("world");
            field2.setTextSize(20);
            field2.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            field2.setId(id_field2+i);

            field2.setLayoutParams(param_field2);
            contentfield.addView(field2);

            // contenttext 생성
            EditText contenttext = new EditText(getApplicationContext());

            LinearLayout.LayoutParams param_contenttext = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            contenttext.setBackgroundResource(R.drawable.borderline);
            contenttext.setGravity(Gravity.START);
            contenttext.setHint("content");
            contenttext.setInputType(InputType.TYPE_CLASS_TEXT | TYPE_TEXT_FLAG_MULTI_LINE);
            contenttext.setLines(7);
            contenttext.setPadding(getDp(5), getDp(5), getDp(5), getDp(5));
            contenttext.setId(id_contenttext+i);

            contenttext.setLayoutParams(param_contenttext);
            contentlayout.addView(contenttext);
        }
    }












    private void finish_contents() {

    }




    private int getDp(Integer j) {
        int result = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, j, getResources().getDisplayMetrics());
        return result;
    }
}
