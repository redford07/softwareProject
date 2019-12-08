package example.todolistexample;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class Popuplist extends AppCompatActivity {
    Date date;
    Date nextDate;
    Calendar calendar;
    RecyclerView recyclerView;
    Realm realm;
    TodoListAdapter todoListAdapter;

    @Override
    protected void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.popup_list);

        Intent intent = getIntent();
        date = (Date) intent.getSerializableExtra("date");
        calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE,1);
        nextDate = new Date(calendar.getTimeInMillis());

        recyclerView = findViewById(R.id.rv_popup_list);
        realm = Realm.getDefaultInstance();
        setRecyclerView();

        realm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm realm) {
                todoListAdapter.notifyDataSetChanged();
                if(todoListAdapter.getItemCount() == 0){
                    finish();
                }
            }
        });
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        realm.close();
    }

    private void setRecyclerView(){
        RealmResults<ToDoList> toDoLists = realm.where(ToDoList.class).greaterThanOrEqualTo("modifyDate", date).lessThan("modifyDate",nextDate).sort("checkDone", Sort.ASCENDING,"modifyDate",Sort.ASCENDING).findAll();
        todoListAdapter = new TodoListAdapter(toDoLists);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(todoListAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(todoListAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
}
