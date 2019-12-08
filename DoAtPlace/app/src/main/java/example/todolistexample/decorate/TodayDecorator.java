package example.todolistexample.decorate;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

public class TodayDecorator implements DayViewDecorator {
    private CalendarDay day;

    public TodayDecorator(){
        day = CalendarDay.today();
    }

    @Override
    public boolean shouldDecorate(CalendarDay calendarDay){
        return calendarDay.equals(day);
    }

    @Override
    public void decorate(DayViewFacade viewFacade){
        viewFacade.addSpan(new ForegroundColorSpan(Color.GREEN));
        viewFacade.addSpan((new RelativeSizeSpan(1.4f)));
        viewFacade.addSpan(new StyleSpan(Typeface.BOLD));
    }
}
