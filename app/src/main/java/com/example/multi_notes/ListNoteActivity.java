package com.example.multi_notes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

public class ListNoteActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, DialogInterface.OnClickListener {

    private DBHelper dbHelper;
    private ImageButton btnAdd, btnDeletes, btnSearchInfo;
    private TextView emptyView;
    private ArrayList<Note> noteList;
    private NoteAdapter noteAdapter;
    private AlertDialog deleteNoteDialog;
    private int pendingDeleteIndex = -1;
    private ListView listView;
    private androidx.appcompat.widget.SearchView searchView;
    private ArrayList<Note> filteredNotes;
    private boolean isSearchVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_note);

        dbHelper = new DBHelper(this);
//        this.deleteDatabase("notes.db");
        noteList = new ArrayList<>();
        noteList = dbHelper.getAllNotes();
        noteAdapter = new NoteAdapter(this, noteList);

        emptyView = findViewById(R.id.emptyView);
        listView = findViewById(R.id.listView);
        listView.setEmptyView(emptyView);
        listView.setAdapter(noteAdapter);

        btnAdd = findViewById(R.id.btnAdd);
        btnDeletes = findViewById(R.id.btnDeletes);
        btnSearchInfo = findViewById(R.id.btnSearchInfo);

        btnAdd.setOnClickListener(this);
        btnDeletes.setOnClickListener(this);
        btnSearchInfo.setOnClickListener(this);

        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);

        searchView = findViewById(R.id.search_bar);

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterNotes(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterNotes(newText);
                return false;
            }
        });
    }

    private void filterNotes(String query) {
        filteredNotes = new ArrayList<>();

        for (Note note : noteList) {
            if (note.getTitle().toLowerCase().contains(query.toLowerCase())
                    || note.getTimestamp().toLowerCase().contains(query.toLowerCase())) {
                filteredNotes.add(note);
            }
        }

        NoteAdapter adapter = new NoteAdapter(this, filteredNotes);
        listView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.btnAdd) {
            Intent intent = new Intent(this, DetailNoteActivity.class);
            startActivityForResult(intent, DetailNoteActivity.REQUEST_CODE_ADD_NOTE);
        }
        else if(id == R.id.btnDeletes) {
            Toast.makeText(ListNoteActivity.this, "Chức năng không khả dụng", Toast.LENGTH_SHORT).show();
        }
        else if(id == R.id.btnSearchInfo) {
            if (isSearchVisible) {
                searchView.setVisibility(View.GONE);
                btnSearchInfo.setImageResource(R.drawable.search);
                isSearchVisible = false;
            } else {
                searchView.setVisibility(View.VISIBLE);
                btnSearchInfo.setImageResource(R.drawable.back);
                isSearchVisible = true;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && (requestCode == DetailNoteActivity.REQUEST_CODE_ADD_NOTE || requestCode == DetailNoteActivity.REQUEST_CODE_UPDATE_NOTE)) {
            noteList = dbHelper.getAllNotes();
            noteAdapter.setNoteList(noteList);
            listView.setAdapter(noteAdapter);
            noteAdapter.notifyDataSetChanged();
            if(requestCode == DetailNoteActivity.REQUEST_CODE_ADD_NOTE) {
                Toast.makeText(ListNoteActivity.this, "Thêm thành công", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(ListNoteActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Note selectedNote = filteredNotes == null ? noteList.get(position) : filteredNotes.get(position);

        Gson gson = new Gson();
        String selectedNoteJson = gson.toJson(selectedNote);

        Intent intent = new Intent(this, DetailNoteActivity.class);
        intent.putExtra(DetailNoteActivity.EXTRA_NOTE, selectedNoteJson);
        startActivityForResult(intent, DetailNoteActivity.REQUEST_CODE_UPDATE_NOTE);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (deleteNoteDialog == null) {
            deleteNoteDialog = new AlertDialog.Builder(this)
                    .setTitle("Xóa ghi chú")
                    .setPositiveButton("Delete", this)
                    .setNegativeButton("Cancel", this)
                    .create();
        }
        pendingDeleteIndex = position;
        Note note = filteredNotes == null ? noteAdapter.getNoteList().get(pendingDeleteIndex) : filteredNotes.get(pendingDeleteIndex);
        deleteNoteDialog.setMessage("\n" + note.getTitle() + "\nBạn có chắc chắn muốn xóa ghi chú này không?");
        deleteNoteDialog.show();
        return true;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            if (pendingDeleteIndex != -1) {
                Note note = filteredNotes == null ? noteAdapter.getNoteList().get(pendingDeleteIndex) : filteredNotes.get(pendingDeleteIndex);
                dbHelper.deleteNote(note.getId());
                noteList.remove(note);
                noteAdapter.notifyDataSetChanged();
                Toast.makeText(this, "Ghi chú đã được xóa", Toast.LENGTH_SHORT).show();
            }
        }
        else if (which == DialogInterface.BUTTON_NEGATIVE) {
            dialog.cancel();
        }

        pendingDeleteIndex = -1;
    }

}