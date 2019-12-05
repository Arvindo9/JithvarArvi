package org.ptindia.jithvar.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.DatePicker;

import org.ptindia.jithvar.interfaceClass.DateInterface;

import java.util.Calendar;

/**
 * Created by Arvindo Mondal on 23/8/17.
 * Company name Jithvar
 * Email arvindo@jithvar.com
 */
public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    public DateInterface dateInterface;

    public interface DateInterface {

        void DateSet(String date);

    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

//            Log.e("date select-----", year + "-" + month + "-" + day);
        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
        month++;
        String monthStr = String.valueOf(month);
        String dayStr = String.valueOf(day);
        if(monthStr.length() < 2){
            monthStr = "0" + String.valueOf(month);
        }
        if(dayStr.length() < 2){
            dayStr = "0" + String.valueOf(day);
        }

        String dateToTrack = year + "-" + monthStr + "-" + dayStr;
//            DateInterface dateInterface1 = new DateInterface() {
//                @Override
//                public void DateSet(String date) {
//
//                }
//            };


//        dateInterface = (DateInterface) getActivity();
//            dateInterface = (DateInterface)  this;
        dateInterface.DateSet(dateToTrack);
//            org.ptindia.jithvar.interfaceClass.DateInterface.DateSet(dateToTrack);
//            Log.e("month", String.valueOf(month));
        Log.e("date select", dateToTrack);
    }
}
