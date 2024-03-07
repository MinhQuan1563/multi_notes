package com.example.multi_notes;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AlarmReceiver extends BroadcastReceiver {
    MediaPlayer mediaPlayer;

    @Override
    public void onReceive(Context context, Intent intent) {
        NoteAdapter noteAdapter = new NoteAdapter(context, new DBHelper(context).getAllNotes());
        if (noteAdapter != null) {
            List<Note> notes = noteAdapter.getNoteList();
            for (Note note : notes) {
                if (isCurrentTime(note.getReminder())) {
                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    Notification.Builder nbuilder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        String channelId = "Quan";
                        NotificationChannel channel = notificationManager.getNotificationChannel(channelId);
                        if (channel == null) {
                            notificationManager.createNotificationChannel(new NotificationChannel(channelId, "Reminder", NotificationManager.IMPORTANCE_DEFAULT));
                        }
                        nbuilder = new Notification.Builder(context, channelId);
                    } else {
                        nbuilder = new Notification.Builder(context);
                    }

                    nbuilder.setSmallIcon(R.drawable.clock2)
                            .setContentTitle(note.getTitle())
                            .setContentText(note.getContent())
                            .setPriority(Notification.PRIORITY_DEFAULT);

                    notificationManager.notify(R.id.notification_reminder, nbuilder.build());
                    playSound(context);

                    break;
                }
            }
        }
    }

    private void playSound(Context context) {
        mediaPlayer = MediaPlayer.create(context, R.raw.alarm_sound);

        mediaPlayer.start();

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            mediaPlayer.stop();
            mediaPlayer.release();
        }, 10000);
    }

    private boolean isCurrentTime(String reminderTimeString) {
        Calendar currentTime = Calendar.getInstance();

        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        Calendar reminderTime = Calendar.getInstance();
        try {
            reminderTime.setTime(format.parse(reminderTimeString));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return currentTime.get(Calendar.YEAR) == reminderTime.get(Calendar.YEAR)
                && currentTime.get(Calendar.MONTH) == reminderTime.get(Calendar.MONTH)
                && currentTime.get(Calendar.DAY_OF_MONTH) == reminderTime.get(Calendar.DAY_OF_MONTH)
                && currentTime.get(Calendar.HOUR_OF_DAY) == reminderTime.get(Calendar.HOUR_OF_DAY)
                && currentTime.get(Calendar.MINUTE) == reminderTime.get(Calendar.MINUTE);
    }
}
