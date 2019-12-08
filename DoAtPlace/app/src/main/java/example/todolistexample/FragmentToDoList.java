package example.todolistexample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class FragmentToDoList extends Fragment {

    Context context;
    Realm realm;
    TodoListAdapter toDoListAdapter;

    RecyclerView recyclerView;
    ImageView ivAdd;

    public FragmentToDoList(Context context){
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.todolist, container, false);

        ivAdd = view.findViewById(R.id.btn_add_list);
        recyclerView = view.findViewById(R.id.rv1);

        ivAdd.setOnClickListener(onClickListener);

        realm = Realm.getDefaultInstance();
        setRecyclerView();

        realm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm realm) {
                toDoListAdapter.notifyDataSetChanged();
            }
        });

        return view;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        realm.close();
    }

    ImageView.OnClickListener onClickListener = new ImageView.OnClickListener(){
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(context, AddToDoList.class);
            intent.putExtra("date", new Date(System.currentTimeMillis()));
            startActivity(intent);
        }
    };

    protected void setRecyclerView(){
        RealmResults<ToDoList> toDoLists = realm
                .where(ToDoList.class)
                .findAll().sort("checkDone", Sort.ASCENDING,"modifyDate",Sort.ASCENDING);
        toDoListAdapter = new TodoListAdapter(toDoLists);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(toDoListAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(context,DividerItemDecoration.VERTICAL));

        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(toDoListAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
}
