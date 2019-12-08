package example.todolistexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;

public class AddToDoList extends AppCompatActivity {

    Realm realm;

    long id;
    Date initDate;
    Date setDate;

    Toolbar toolbar;
    DatePicker datePicker;
    EditText title;
    EditText contents;

    Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedinstanceState) {
        super.onCreate(savedinstanceState);
        setContentView(R.layout.add_todolist);

        Intent intent = getIntent();
        id = intent.getLongExtra("id",0);
        initDate = (Date)intent.getSerializableExtra("date");

        toolbar = findViewById(R.id.add_toolbar);
        datePicker = findViewById(R.id.add_date_picker);
        title = findViewById(R.id.add_title);
        contents = findViewById(R.id.add_contents);

        setSupportActionBar(toolbar);

        calendar.setTime(initDate);
        setDate = new Date(calendar.getTimeInMillis());
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int year, int month, int day) {
                setDate(year,month,day);
            }
        });

        realm = Realm.getDefaultInstance();
        if(id>0){
            ToDoList tDL = realm.where(ToDoList.class).equalTo("id",id).findFirst();

            title.setText(tDL.getTitle());
            contents.setText(tDL.getTitle());
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.add_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menu_save:
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        if(id==0){
                            Number currentId = realm.where(ToDoList.class).max("id");
                            long nextId;
                            if(currentId == null){
                                nextId = 1;
                            }else{
                                nextId = currentId.longValue() + 1;
                            }
                            Log.d("TESTLOG", "nextId: " + nextId);
                            ToDoList toDoList = new ToDoList();
                            toDoList.setId(nextId);
                            toDoList.setTitle(title.getText().toString());
                            toDoList.setContents(contents.getText().toString());
                            toDoList.setModifyDate(setDate);
                            toDoList.setCheckDone(false);
                            toDoList.setCheckDate(null);
                            realm.insert(toDoList);
                        } else{
                            ToDoList toDoList = realm.where(ToDoList.class).equalTo("id",id).findFirst();
                            toDoList.setTitle(title.getText().toString());
                            toDoList.setContents(contents.getText().toString());
                            toDoList.setModifyDate(setDate);
                        }
                    }
                });
                realm.beginTransaction();
                realm.commitTransaction();
                Toast.makeText(this,"저장되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        realm.close();
    }

    private void setDate(int year, int month, int day){
        Calendar calendar = Calendar.getInstance();
        calendar.set(year,month,day);
        this.setDate = new Date(calendar.getTimeInMillis());
    }
}
