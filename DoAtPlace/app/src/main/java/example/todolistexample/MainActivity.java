package example.todolistexample;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainActivity extends AppCompatActivity {

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    FragmentCalendar fragmentCalendar;
    FragmentToDoList fragmentToDoList;

    RelativeLayout lCalendar;
    RelativeLayout lList;

    ImageView ivCalendar;
    ImageView ivList;

    TextView tvCalendar;
    TextView tvList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lCalendar = findViewById(R.id.switch_calendar);
        lList = findViewById(R.id.switch_list);

        ivCalendar = findViewById(R.id.iv_calendar);
        ivList = findViewById(R.id.iv_list);

        tvCalendar = findViewById(R.id.tv_calendar);
        tvList = findViewById(R.id.tv_list);

        lCalendar.setOnClickListener(onClickListener);
        lList.setOnClickListener(onClickListener);

        fragmentCalendar = new FragmentCalendar(this);
        fragmentToDoList = new FragmentToDoList(this);
        setFragment(R.id.switch_calendar);

        Realm.init(this);
        RealmConfiguration configuration = new RealmConfiguration.Builder().name("todolist.realm").build();
        Realm.setDefaultConfiguration(configuration);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    private RelativeLayout.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.switch_calendar:
                    setFragment(R.id.switch_calendar);
                    break;
                case R.id.switch_list:
                    setFragment(R.id.switch_list);
                    break;
            }
        }
    };

    private void setFragment(int id){
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        switch (id){
            case R.id.switch_calendar:
                ivCalendar.setImageResource(R.drawable.calendar_icon_p);
                tvCalendar.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));
                ivList.setImageResource(R.drawable.list_icon);
                tvList.setTextColor(ContextCompat.getColor(this,R.color.colorBlack));
                fragmentTransaction.replace(R.id.main_frame,fragmentCalendar);
                fragmentTransaction.commit();
                break;
            case R.id.switch_list:
                ivCalendar.setImageResource(R.drawable.calendar_icon);
                tvCalendar.setTextColor(ContextCompat.getColor(this,R.color.colorBlack));
                ivList.setImageResource(R.drawable.list_icon_p);
                tvList.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));
                fragmentTransaction.replace(R.id.main_frame,fragmentToDoList);
                fragmentTransaction.commit();
                break;
        }
    }
}