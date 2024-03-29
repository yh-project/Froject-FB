package com.example.froject;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ServerTimestamp;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE;

public class WriteActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private WritingAdapter writingAdapter;
    private ArrayList<WriteData> list;
    private PostData[] postDataArray;
    private WriteHolder writeHolder;
    private TextView totalCount;
    private String author;
    private String email;
    private int totalPeople=0;
    private int count = 1;
    private CheckBox checkPeriod;
    private CheckBox checkVolunteer;
    private CheckBox checkContest;
    private boolean is_correction=false;

    private static final int MAX_COUNT = 10;

    private static final String TAG = "WriteActivity";
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference docRef = db.collection("users").document(user.getEmail());
    CollectionReference boardRef = docRef.collection("Board");
    private String getDocRef;

    //LinearLayout contentslayout;
    PostData new_post;
    Intent intent;

    ArrayAdapter<CharSequence> Bigadapter, Smalladapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        intent = getIntent();
        new_post = (PostData)intent.getSerializableExtra("postData");
        getDocRef = intent.getStringExtra("DocRef");

        setContentView(R.layout.activity_write);
        totalCount = findViewById(R.id.totalCount);
        checkPeriod = findViewById(R.id.checkperiod);
        checkContest = findViewById(R.id.checkcontest);
        checkVolunteer = findViewById(R.id.checkvolunteer);

        if(new_post!= null) {
            Log.d("WriteActivity","no post");
            totalCount.setText(new_post.getTotalPeople());
            checkPeriod.setChecked(new_post.periodNegotiable);
            checkContest.setChecked(new_post.contest);
            checkVolunteer.setChecked(new_post.volunteer);

            ((EditText)findViewById(R.id.inputTitle)).setText(new_post.getTitle());
            ((EditText)findViewById(R.id.inputPlace)).setText(new_post.getPlace());
            ((EditText)findViewById(R.id.inputPeriod)).setText(new_post.getPeriod());
            //((TextView)findViewById(R.id.totalCount)).setText(new_post.total;
            ((EditText)findViewById(R.id.inputContent)).setText(new_post.getMainContent());
            is_correction = true;
        }

        findViewById(R.id.minus).setOnClickListener(onCountListener);
        findViewById(R.id.plus).setOnClickListener(onCountListener);

        findViewById(R.id.finishcontents).setOnClickListener(onClickListener);
        findViewById(R.id.cancelWriting).setOnClickListener(onClickListener);

        recyclerView = findViewById(R.id.categoryContentRecyclerView);
        list = new ArrayList<>();
        WriteData writeData = null;
        if (new_post != null)
            for (int i=0;i<new_post.getBigCategory().size();i++) {
                writeData = new WriteData(new_post.getCategoryContent().get(i),
                        new_post.getBigCategory().get(i), new_post.getSmallCategory().get(i),
                        new_post.getCategoryPeople().get(i));
                list.add(writeData);
            }
        else {
            writeData = new WriteData();
            list.add(writeData);
        }
        writingAdapter = new WritingAdapter(list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        ItemHeightSpace itemHeightSpace = new ItemHeightSpace(50);
        recyclerView.addItemDecoration(itemHeightSpace);
        recyclerView.setAdapter(writingAdapter);

        checkVolunteer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {    //체크되었을 때 저장방식 다르게 하기?
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (count != 1)
                        findViewById(R.id.minus).setVisibility(View.VISIBLE);
                    if (count != MAX_COUNT)
                        findViewById(R.id.plus).setVisibility(View.VISIBLE);
                    ((CheckBox)findViewById(R.id.checkcontest)).setChecked(true);
                    findViewById(R.id.checkcontest).setClickable(false);
                    list.clear();
                    writingAdapter.notifyDataSetChanged();
                    totalCount.setText(count+"명");
                }
                else {
                    findViewById(R.id.minus).setVisibility(View.INVISIBLE);
                    findViewById(R.id.plus).setVisibility(View.INVISIBLE);
                    ((CheckBox)findViewById(R.id.checkcontest)).setChecked(false);
                    findViewById(R.id.checkcontest).setClickable(true);
                    if (list.isEmpty()) {
                        list.add(new WriteData());
                        writingAdapter.notifyDataSetChanged();
                    }
                    setTotalPeople();
                }
            }
        });

        writingAdapter.setDelClickListener(new DelClickListener() {
            @Override
            public void onDelClick(View view, int position) {
                AlertDialog.Builder msgBuilder = new AlertDialog.Builder(WriteActivity.this)
                        .setTitle("글 삭제")
                        .setMessage("해당 카테고리 글을 삭제하시겠습니까?")
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                for(int j=0;j<list.size();j++)
                                    Log.w("omg","left"+j+list.get(j).getContent());
                                list.remove(position);
                                for(int j=0;j<list.size();j++)
                                    Log.w("omg","right"+j+list.get(j).getContent());
                                writingAdapter.notifyDataSetChanged();
                                setTotalPeople();
                            }
                        })
                        .setNegativeButton("아니요", new DialogInterface.OnClickListener(){@Override public void onClick(DialogInterface dialog, int which) { }});
                AlertDialog.Builder msgBuilder_alone = new AlertDialog.Builder(WriteActivity.this)
                        .setTitle("내용 삭제")
                        .setMessage("해당 카테고리 내용을 초기화 하시겠습니까?")
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                list.remove(0);
                                list.add(new WriteData());
                                writingAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                AlertDialog msgDlg;
                if (list.size() == 1) {
                    msgDlg = msgBuilder_alone.create();
                } else {
                    msgDlg = msgBuilder.create();
                }
                msgDlg.show();
            }
        });

        writingAdapter.setAddClickListener(new AddClickListener() {
            @Override
            public void onAddClick(View view, int position) {
                for(int j=0;j<list.size();j++)
                    Log.w("omg","left"+j+list.get(j).getContent());
                list.add(new WriteData());
                for(int j=0;j<list.size();j++)
                    Log.w("omg","right"+j+list.get(j).getContent());
                writingAdapter.notifyDataSetChanged();
            }
        });

        writingAdapter.setCountChangedListener(new CountChangedListener() {
            @Override
            public void onCountChanged(int count, int position) {
                setTotalPeople();
            }
        });

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                author = task.getResult().getString("name");
                email = task.getResult().getString("email");
            }
        });

        //contentslayout = findViewById(R.id.contentsLayout);

    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.finishcontents:
                    String title = ((EditText)findViewById(R.id.inputTitle)).getText().toString();
                    String place = ((EditText)findViewById(R.id.inputPlace)).getText().toString();
                    String period = ((EditText)findViewById(R.id.inputPeriod)).getText().toString();
                    String totalCnt = ((TextView)findViewById(R.id.totalCount)).getText().toString();
                    String mainContent = ((EditText)findViewById(R.id.inputContent)).getText().toString();

                    if (!is_correction)
                        new_post = new PostData(title, place, period, mainContent, totalCnt);
                    else {
                        new_post.setTitle(title);
                        new_post.set_PostData(title,place,period,mainContent,totalCnt);
                    }
                    Date date = new Date(System.currentTimeMillis());
                    SimpleDateFormat sdate = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
                    sdate.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

                    //boardRef.get().getResult().getDocuments().size();

                    ArrayList<String> BigCategory = new ArrayList<>();
                    ArrayList<String> SmallCategory = new ArrayList<>();
                    ArrayList<String> CategoryContent = new ArrayList<>();
                    ArrayList<String> CountPeople = new ArrayList<>();

                    int total=0;
                    for(int i=0;i<list.size();i++) {
                        BigCategory.add(list.get(i).getBigCategory());
                        SmallCategory.add(list.get(i).getSmallCategory());
                        CategoryContent.add(list.get(i).getContent());
                        CountPeople.add(list.get(i).getCountPeople());
                        total += Integer.parseInt(list.get(i).getCountPeople());
                    }

                    new_post.setAuthor(author);
                    new_post.setEmail(email);
                    new_post.setBigCategory(BigCategory);
                    new_post.setSmallCategory(SmallCategory);
                    new_post.setCategoryContent(CategoryContent);
                    new_post.setCategoryPeople(CountPeople);
                    //new_post.setTotalPeople(""+total);
                    new_post.setTotalPeople(totalCount.getText().toString().replace("명",""));
                    new_post.setVolunteer(checkVolunteer.isChecked());
                    new_post.setContest(checkContest.isChecked());
                    new_post.setPeriodNegotiable(checkPeriod.isChecked());
                    new_post.settingSearchData();

                    if (!is_correction) {
                        Log.d("WriteActivity","new post update");
                        boardRef.document(sdate.format(date)).set(new_post);
                        boardRef.document(sdate.format(date)).update("writeTime", date);
                        db.collectionGroup("Board").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                                boardRef.document(sdate.format(date)).update("postNumber", "" + (task.getResult().getDocuments().size() + 1));
                            }
                        });
                    }
                    else {
                        Log.d("WriteActivity","correct post update");
                        boardRef.document(getDocRef).set(new_post);
                        boardRef.document(getDocRef).update("writeTime", date);

                    }

                    intent.setClass(WriteActivity.this,MainActivity.class);
                    //intent.putExtra("reload",true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                    break;
                case R.id.cancelWriting:
                    finish();
                    //finishAndRemoveTask();
            }
        }
    };

    View.OnClickListener onCountListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                //봉사활동 버튼이 체크되면 실행되야함.
                case R.id.minus:
                    count--;
                    break;
                case R.id.plus:
                    count++;
                    break;
            }

            switch (count) {
                case 1:
                    findViewById(R.id.minus).setVisibility(View.INVISIBLE);
                    break;
                case MAX_COUNT:
                    findViewById(R.id.plus).setVisibility(View.INVISIBLE);
                    break;
                default:
                    findViewById(R.id.minus).setVisibility(View.VISIBLE);
                    findViewById(R.id.plus).setVisibility(View.VISIBLE);
                    break;
            }

            totalCount.setText((count)+"명");
            /*if (count == 1)
                findViewById(R.id.minus).setVisibility(View.INVISIBLE);
                    //count 변화를 먼저 할 때
                    switch (count) {
                        case 1:
                        case 3:
                            findViewById(v.getId()).setVisibility(View.INVISIBLE);
                            break;
                        case 2:
                            findViewById(R.id.minus).setVisibility(View.VISIBLE);
                            findViewById(R.id.addContents).setVisibility(View.VISIBLE);
                            break;
                    }
                    //count 변화를 나중에 할 때
                    switch (count) {
                        case 1:
                            findViewById(R.id.minus).setVisibility(View.VISIBLE);
                            break;
                        case 2:
                            findViewById(v.getId()).setVisibility(View.INVISIBLE);
                            break;
                        case 3:
                            findViewById(R.id.addContents).setVisibility(View.VISIBLE);
                            break;
            }*/
        }
    };


    /*private void add_contents() {
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
    }*/

    private void setTotalPeople() {
        totalPeople = 0;
        for(int i=0;i<list.size();i++) {
            totalPeople += Integer.parseInt(list.get(i).getCountPeople());
        }
        totalCount.setText(totalPeople+"명");
    }

    private void finish_contents() {

    }

    private void startActivity(Class c) {
        Intent intent = new Intent(this, c);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }




    private int getDp(Integer j) {
        int result = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, j, getResources().getDisplayMetrics());
        return result;
    }
}
