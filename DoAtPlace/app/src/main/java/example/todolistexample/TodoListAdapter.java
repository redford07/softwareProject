package example.todolistexample;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;

public class TodoListAdapter extends RealmRecyclerViewAdapter<ToDoList,TodoListAdapter.ViewHolder> implements ItemActionListener{

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView content;
        TextView modifyDate;
        TextView checkDate;
        View layout;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.rl_title);
            content = itemView.findViewById(R.id.rl_content);
            modifyDate = itemView.findViewById(R.id.rl_modify_date);
            checkDate = itemView.findViewById(R.id.rl_check_date);
            layout = itemView;
        }
    }

    public TodoListAdapter(OrderedRealmCollection<ToDoList> data){
        super(data, true);
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.relative_item, parent, false);
        ViewHolder vh = new ViewHolder(view);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){
        ToDoList toDoList = getItem(position);

        long id = toDoList.getId();
        String title = toDoList.getTitle();
        String contents = toDoList.getContents();
        Date modifyDate = toDoList.getModifyDate();
        boolean checkDone = toDoList.getCheckDone();
        Date checkDate = toDoList.getCheckDate();

        holder.title.setText(title);
        holder.content.setText(contents);
        if(modifyDate!=null) {
            holder.modifyDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(modifyDate));
        }
        if(checkDone){
            if(checkDate!=null) {
                holder.checkDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(checkDate));
            }
            holder.title.setPaintFlags(holder.title.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
            holder.content.setPaintFlags(holder.content.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
        }else{
            holder.checkDate.setText("");
            holder.title.setPaintFlags(0);
            holder.content.setPaintFlags(0);
        }
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(),AddToDoList.class);
                intent.putExtra("id",id);
                intent.putExtra("date",modifyDate);
                view.getContext().startActivity(intent);
            }
        });
        holder.layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Realm realm = Realm.getDefaultInstance();
                ToDoList tDL = realm.where(ToDoList.class).equalTo("id",id).findFirst();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        if(tDL.getCheckDone()){

                        }else{
                            tDL.setCheckDone(true);
                            tDL.setCheckDate(new Date(System.currentTimeMillis()));
                        }
                    }
                });
                realm.beginTransaction();
                realm.commitTransaction();
                realm.close();
                return true;
            }
        });
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition){
        return true;
    }

    @Override
    public void onItemSwipe(int position, int direction){
        ToDoList toDoList = getItem(position);
        long id = toDoList.getId();

        Realm realm = Realm.getDefaultInstance();
        ToDoList result = realm.where(ToDoList.class).equalTo("id",id).findFirst();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                result.deleteFromRealm();
            }
        });
        realm.beginTransaction();
        realm.commitTransaction();
        realm.close();
        notifyItemRemoved(position);
        notifyItemRangeChanged(position,getItemCount());
    }
}
