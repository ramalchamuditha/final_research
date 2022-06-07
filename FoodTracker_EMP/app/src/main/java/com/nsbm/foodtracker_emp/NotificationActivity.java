package com.nsbm.foodtracker_emp;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class NotificationActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {

    Button btnDate,btnSetDate,btnCancel ;
    TextView textView5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        btnDate = findViewById(R.id.btnDate);
        btnSetDate = findViewById(R.id.btnDate1);
        btnCancel = findViewById(R.id.btnDate2);
        textView5 = findViewById(R.id.textView5);

        //createNotificationChannel();

//        btnDate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(NotificationActivity.this, "Reminder Set", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(NotificationActivity.this,MyNotificationPublisher.class);
//                PendingIntent pendingIntent = PendingIntent.getBroadcast(NotificationActivity.this,0,intent,0);
//
//                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//
//                long timeAtButtonClick = System.currentTimeMillis();
//                long tenSeconds = 1000 * 10;
//
//                alarmManager.set(AlarmManager.RTC_WAKEUP,
//                        timeAtButtonClick+tenSeconds,
//                        pendingIntent);
//            }
//        });

        btnSetDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment timePicker= new TimePickerFragment();
                timePicker.show(getSupportFragmentManager(),"time picker");
//                Calendar cal = Calendar.getInstance();
//
//                cal.setTimeInMillis(System.currentTimeMillis());
//                cal.set(cal.YEAR,2022);
//                cal.set(cal.MONTH,05);
//                cal.set(cal.DAY_OF_MONTH,23);
//                cal.set(cal.HOUR_OF_DAY,13);
//                cal.set(cal.MINUTE,36);
                //cal.clear();
                //cal.set(2022,5,23,13,28);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelAlarm();
            }
        });
    }

    private void createNotificationChannel()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            CharSequence name = "ExpireRemainderChannel";
            String description = "Channel for the expire Reminder";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("notifyEXP",name,importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date date = new Date();
        String myDate = "5/24/2022";
        try {
            date = sdf.parse(myDate);
//            Toast.makeText(NotificationActivity.this, "Null instance", Toast.LENGTH_SHORT).show();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY,22);
        c.set(Calendar.MINUTE,45);
        c.set(Calendar.SECOND,0);

        updateTimeText(c);
        startAlarm(c);

    }

    @SuppressLint("SetTextI18n")
    private void updateTimeText(Calendar c)
    {
        String timeText = "Alarm set for: ";
        timeText = timeText + DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime());

        //Toast.makeText(NotificationActivity.this, timeText, Toast.LENGTH_SHORT).show();
        textView5.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime())+" "
                +DateFormat.getDateInstance(DateFormat.SHORT).format(c.getTime()));
    }

    private void startAlarm(Calendar cal)
    {
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this,MyNotificationPublisher.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,1,intent,0);

//        alarmManager.setExact(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),pendingIntent);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),pendingIntent);
//        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
//                SystemClock.elapsedRealtime() +
//                        60 * 1000, pendingIntent);
//        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, cal.getTimeInMillis(), pendingIntent);
    }

    private void cancelAlarm()
    {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this,MyNotificationPublisher.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,1,intent,0);

        alarmManager.cancel(pendingIntent);

        Toast.makeText(NotificationActivity.this, "cancelled alarm", Toast.LENGTH_SHORT).show();
    }
}