package example.todolistexample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.todolistexample.decorate.SaturdayDecorator;
import com.example.todolistexample.decorate.SundayDecorator;
import com.example.todolistexample.decorate.ToDoListDecorator;
import com.example.todolistexample.decorate.TodayDecorator;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.format.DateFormatTitleFormatter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class FragmentCalendar extends Fragment {
    Context context;
    MaterialCalendarView calendarView;
    ImageView ivadd;
    Realm realm;
    RealmResults<ToDoList> toDoLists;

    public FragmentCalendar(Context context){
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle){
        View view = inflater.inflate(R.layout.calendar, container, false);

        calendarView = view.findViewById(R.id.calendar);
        ivadd = view.findViewById(R.id.btn_add_calendar);
        ivadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AddToDoList.class);
                intent.putExtra("date", new Date(System.currentTimeMillis()));
                startActivity(intent);
            }
        });

        calendarView.state().edit().setFirstDayOfWeek(Calendar.SUNDAY).setCalendarDisplayMode(CalendarMode.MONTHS);

        DateFormatTitleFormatter dateFormatTitleFormatter = new DateFormatTitleFormatter(new SimpleDateFormat("yyyy년 MM월", Locale.getDefault()));
        calendarView.setTitleFormatter(dateFormatTitleFormatter);

        realm = Realm.getDefaultInstance();
        toDoLists = realm.where(ToDoList.class).equalTo("checkDone",false).distinct("modifyDate").findAll();
        realm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm realm) {
                setDecorate();
            }
        });
        setDecorate();

        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                Calendar calendar = date.getCalendar();
                calendar.add(calendar.DATE,1);
                Date nextDate = new Date(calendar.getTimeInMillis());
                long countList = realm.where(ToDoList.class).greaterThanOrEqualTo("modifyDate", date.getDate()).lessThan("modifyDate",nextDate).count();
                if(countList > 0 ){
                    Intent intent = new Intent(getActivity(),Popuplist.class);
                    intent.putExtra("date",date.getDate());
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(getActivity(),AddToDoList.class);
                    intent.putExtra("date",date.getDate());
                    startActivity(intent);
                }
                calendarView.clearSelection();
            }
        });
        return view;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        realm.close();
    }

    public void setDecorate(){
        HashSet<CalendarDay> days = new HashSet<>();
        for(ToDoList tDL : toDoLists){
            days.add(CalendarDay.from(tDL.getModifyDate()));
        }
        calendarView.removeDecorators();
        calendarView.addDecorators(new TodayDecorator(),new SaturdayDecorator(),new SundayDecorator(),new ToDoListDecorator(days));
    }
}
