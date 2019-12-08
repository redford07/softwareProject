package example.todolistexample.decorate;

import android.graphics.Color;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.HashSet;

public class ToDoListDecorator implements DayViewDecorator {

    private HashSet<CalendarDay> dates;

    public  ToDoListDecorator(HashSet<CalendarDay> dates){
        this.dates = dates;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day){
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade viewFacade){
        viewFacade.addSpan(new DotSpan(5, Color.RED));
    }
}
