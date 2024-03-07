package com.example.multi_notes;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NoteAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Note> noteList;

    public NoteAdapter(Context context, ArrayList<Note> noteList) {
        this.context = context;
        this.noteList = noteList;
    }

    public ArrayList<Note> getNoteList() {
        return noteList;
    }

    public void setNoteList(ArrayList<Note> notes) {
        this.noteList = notes;
    }

    @Override
    public int getCount() {
        return noteList.size();
    }

    @Override
    public Object getItem(int position) {
        return noteList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    public void removeItem(int position) {
        noteList.remove(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_note, null);
        }

        TextView titleView = convertView.findViewById(R.id.titleView);
        TextView timeView = convertView.findViewById(R.id.timeView);
        ImageView clockView = convertView.findViewById(R.id.clockView);
        ImageView imageView = convertView.findViewById(R.id.imageView);

        Note note = noteList.get(position);

        titleView.setText(note.getTitle());
        timeView.setText(note.getTimestamp());

        String reminder = note.getReminder();
        if (reminder != null && !reminder.isEmpty()) {
            clockView.setVisibility(View.VISIBLE);
        } else {
            clockView.setVisibility(View.GONE);
        }

        String images = note.getImages();

        if (images != null && !images.isEmpty()) {
            String[] Pilgrimages = images.split(", ");
            Uri uri = Uri.parse(Pilgrimages[0]);
            Picasso.get().load(uri)
                    .into(imageView);
            imageView.setVisibility(View.VISIBLE);
        } else {
            imageView.setVisibility(View.GONE);
        }

        return convertView;
    }

}