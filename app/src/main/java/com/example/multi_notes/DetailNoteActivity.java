package com.example.multi_notes;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DetailNoteActivity extends Activity implements View.OnClickListener {

    private DBHelper dbHelper;
    private NoteAdapter noteAdapter;
    static final int REQUEST_CODE_ADD_NOTE = 1;
    static final int REQUEST_CODE_UPDATE_NOTE = 2;
    static final int REQUEST_PICK_IMAGE = 3;
    public static final int REQUEST_REMINDER = 4;
    static final int REQUEST_UPDATE_IMAGE = 5;
    static final String EXTRA_NOTE = "selectedNote";

    private ImageButton btnSaveNote, btnInsertImage, btnRemind, btnBack;
    private EditText titleNote, contentNote;
    private TextView timestampView, wordCount, reminderView;
    private ImageView iconClock;
    private AlertDialog promptDateTimeDialog;
    private DatePicker pkrDate;
    private TimePicker pkrTime;
    private Locale curLocale;
    private String timestamp;
    private String urlImage = "";
    private ArrayList<Note> noteList;
    private int pendingInsertStart = -1, pendingInsertEnd = -1;
//    private int targetIndex = -1;
    private Note selectedNote = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_note);

        // Khởi tạo
        dbHelper = new DBHelper(this);
        noteList = new ArrayList<>();
        noteList = dbHelper.getAllNotes();
        noteAdapter = new NoteAdapter(this, noteList);

        titleNote = findViewById(R.id.titleNote);
        contentNote = findViewById(R.id.contentNote);
        timestampView = findViewById(R.id.timestampView);
        reminderView = findViewById(R.id.reminderView);
        wordCount = findViewById(R.id.wordCount);
        iconClock = findViewById(R.id.iconClock);

        btnSaveNote = findViewById(R.id.btnSaveNote);
        btnInsertImage = findViewById(R.id.btnInsertImage);
        btnRemind = findViewById(R.id.btnRemind);
        btnBack = findViewById(R.id.btnBack);

        timestamp = dbHelper.getCurrentTimestamp();
        timestampView.setText(timestamp);

        Configuration configuration = getResources().getConfiguration();
        curLocale = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                ? configuration.getLocales().get(0)
                : configuration.locale;

        // Set sự kiện
        btnSaveNote.setOnClickListener(this);
        btnInsertImage.setOnClickListener(this);
        btnRemind.setOnClickListener(this);
        btnBack.setOnClickListener(this);

        // Đăng ký sự kiện TextWatcher cho EditText contentNote
        contentNote.addTextChangedListener(textWatcher);

        // Cập nhật note
        String selectedNoteJson = getIntent().getStringExtra(EXTRA_NOTE);
        Gson gson = new Gson();
        selectedNote = gson.fromJson(selectedNoteJson, Note.class);

        if (selectedNote != null) {
            titleNote.setText(selectedNote.getTitle());
            timestampView.setText(selectedNote.getTimestamp());
            urlImage = selectedNote.getImages();

            String content = selectedNote.getContent();
            String[] urlImages = urlImage.split(", ");

            int urlIndex = 0;
            StringBuilder replacedTextBuilder = new StringBuilder(content);
            int index = replacedTextBuilder.indexOf("{img}");

            while (index != -1 && urlIndex < urlImages.length) {
                replacedTextBuilder.replace(index, index + "{img}".length(), urlImages[urlIndex]);
                urlIndex++;
                index = replacedTextBuilder.indexOf("{img}");
            }

            String replacedText = replacedTextBuilder.toString();
            contentNote.setText(replacedText);

//            parseAndSetImages(getApplicationContext(), replacedText, contentNote, selectedNote);


            if(selectedNote.getReminder() != null && !selectedNote.getReminder().equals("")) {
                reminderView.setText(selectedNote.getReminder());
                reminderView.setVisibility(View.VISIBLE);
                iconClock.setVisibility(View.VISIBLE);
            }
        }
        setResult(RESULT_CANCELED);

    }

    public static void parseAndSetImages(Context context, String text, EditText editText, Note selectedNote) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        String[] parts = text.split("\n");
        int i = 0;
        String[] arrImages = selectedNote.getImages().split(", ");
        Log.e("getImages", "getImages =" + selectedNote.getImages());

        for (String part : parts) {
            if (part.startsWith("content:")) {
                Uri uri = Uri.parse(arrImages[i]);
                i++;
                Log.e("URI", "uri =" + uri);

                Picasso.get().load(uri)
                        .into(new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                                bitmap = decodeUriToBitmap(context, uri);

                                Log.e("bitmap", "bitmap = " + bitmap);
                                if (bitmap != null) {
                                    ImageSpan imageSpan = new ImageSpan(context, uri);
                                    SpannableString spannableString = new SpannableString(part);
                                    spannableString.setSpan(imageSpan, 0, part.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    builder.append(spannableString);
                                    Log.e("spannableString", "spannableString = " + spannableString);
                                }
                            }
                            @Override
                            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                // Xử lý khi tải hình ảnh thất bại
                            }
                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {
                                // Xử lý trước khi tải hình ảnh
                            }
                        });

            } else {
                builder.append(part);
            }
            builder.append("\n");
        }

        editText.setText(builder);
    }

    private static Bitmap decodeUriToBitmap(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(inputStream, null, options);
                inputStream.close();

                int targetWidth = 100;
                int targetHeight = 100;
                int scaleFactor = Math.min(options.outWidth / targetWidth, options.outHeight / targetHeight);

                options.inJustDecodeBounds = false;
                options.inSampleSize = scaleFactor;

                inputStream = context.getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                inputStream.close();

                return bitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private SpannableString processImagesInText(String text) {
        Log.e("processImagesInText", "processImagesInText = ");

        SpannableString spannableString = new SpannableString(text);

        Pattern pattern = Pattern.compile("content://[^\\s]+");
        Matcher matcher = pattern.matcher(text);
        Log.e("matcher", "matcher = " + matcher);

        int spanStart = 0;
        while (matcher.find()) {
            String imagePath = matcher.group();
            Bitmap imageBitmap = getBitmapFromImagePath(imagePath);

            Log.e("imageBitmap", "imageBitmap = " + imageBitmap);

            // Hiển thị hình ảnh trong EditText
            if (imageBitmap != null) {
                ImageSpan imageSpan = new ImageSpan(this, imageBitmap);
                int start = matcher.start() - spanStart;
                int end = matcher.end() - spanStart;
                spannableString.setSpan(imageSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spanStart += "content://".length(); // Cộng dồn độ dài của "content://"
            }
        }

        return spannableString;
    }

    private Bitmap getBitmapFromImagePath(String imagePath) {
        try {
            // Tạo URL từ đường dẫn hình ảnh
            URL url = new URL(imagePath);
            InputStream inputStream = url.openStream();
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            int wordCount = countWords(s.toString());

            DetailNoteActivity.this.wordCount.setText("|   " + wordCount + " từ");
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private int countWords(String text) {
        String textWithoutSpaces = text.replaceAll("\\s+", "");
        String textWithoutImgTags = textWithoutSpaces.replaceAll("\\{img\\}", "");

        return textWithoutImgTags.length();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.btnSaveNote) {
            String dataTitle = titleNote.getText().toString().trim();
            String dataContent = contentNote.getText().toString().trim();
            String dataTimestamp = timestampView.getText().toString().trim();
            String dataReminder = reminderView.getText().toString().trim();

            noteList = noteAdapter.getNoteList();

            if(TextUtils.isEmpty(dataTitle)) {
                Toast.makeText(this, "Vui lòng nhập dữ liệu cho tiêu đề", Toast.LENGTH_SHORT).show();
            }
            else {
                if(selectedNote == null) {
                    Note note = new Note(0, dataTitle, dataContent, dataTimestamp, dataReminder, urlImage);
                    dbHelper.addNote(note);
                    noteList.add(note);
                }
                else {
                    Note note = selectedNote;
                    note.setTitle(titleNote.getText().toString());
                    note.setContent(contentNote.getText().toString());
                    note.setReminder(reminderView.getText().toString());
                    note.setTimestamp(timestampView.getText().toString());
                    note.setImages(urlImage);

                    dbHelper.updateNote(note);
                }
                setResult(RESULT_OK);
                finish();
            }
        }
        else if(id == R.id.btnInsertImage) {
            pendingInsertStart = contentNote.getSelectionStart();
            pendingInsertEnd = contentNote.getSelectionEnd();
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_PICK_IMAGE);
        }
        else if(id == R.id.btnRemind) {
            if(reminderView.getText() == null || reminderView.getText().equals("")) {
                showDateTimePicker();
            }
            else {
                updateDateTimePicker();
            }
        }
        else if(id == R.id.btnBack) {
            Intent intent = new Intent(this, ListNoteActivity.class);
            startActivity(intent);
        }
    }

    // Xử lý kết quả trả về sau khi chọn hình ảnh
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            // Lấy URI của hình ảnh được chọn
            Uri imageUri = data.getData();

            if(imageUri != null) {
                if(urlImage.isEmpty()) {
                    urlImage = imageUri.toString();
                }
                else {
                    urlImage += ", " + imageUri;
                }

                displayImage(imageUri);
            }

        }
        else if (requestCode == REQUEST_UPDATE_IMAGE && resultCode == RESULT_OK && data != null) {
            // Lấy URI của hình ảnh được chọn
            Uri imageUri = data.getData();

            if(imageUri != null) {
                displayImage(imageUri);
            }

        }
    }

    public void displayImage(Uri imageUri) {
        if (imageUri != null) {
            // Sử dụng Picasso để tải và hiển thị hình ảnh trong EditText (Chủ yếu để căn resize cho ảnh)
            Picasso.get().load(imageUri)
                    .resize(contentNote.getWidth(), contentNote.getHeight())
                    .centerInside()
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            SpannableStringBuilder spannableString = (SpannableStringBuilder) contentNote.getText();
                            ImageSpan imageSpan = new ImageSpan(DetailNoteActivity.this, bitmap);
                            spannableString.replace(pendingInsertStart, pendingInsertEnd, "{img}");
                            spannableString.setSpan(imageSpan,
                                    pendingInsertStart,
                                    pendingInsertStart + 5,
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        }
                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                            // Xử lý khi tải hình ảnh thất bại
                        }
                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                            // Xử lý trước khi tải hình ảnh
                        }
                    });
        }
        else {
            Toast.makeText(this, "Lỗi khi thêm ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDateTimePicker() {
        if (promptDateTimeDialog == null) {
            View view = getLayoutInflater().inflate(R.layout.time_picker, null, false);
            pkrDate = view.findViewById(R.id.pkrDate);
            pkrTime = view.findViewById(R.id.pkrTime);
            pkrTime.setIs24HourView(true);
            promptDateTimeDialog = new AlertDialog.Builder(this)
                    .setTitle("LỜI NHẮC")
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> setAlarm())
                    .setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> promptDateTimeDialog.dismiss())
                    .create();
        }

        Calendar instance = Calendar.getInstance(curLocale);

        pkrDate.updateDate(instance.get(Calendar.YEAR), instance.get(Calendar.MONTH), instance.get(Calendar.DAY_OF_MONTH));

        pkrTime.setHour(instance.get(Calendar.HOUR_OF_DAY));
        pkrTime.setMinute(instance.get(Calendar.MINUTE));

        promptDateTimeDialog.show();
    }

    private void setAlarm() {
        Calendar instance = Calendar.getInstance(curLocale);
        Calendar calendar = (Calendar) instance.clone();

        int year = pkrDate.getYear();
        int month = pkrDate.getMonth();
        int day = pkrDate.getDayOfMonth();

        int hour, minute;
        hour = pkrTime.getHour();
        minute = pkrTime.getMinute();

        calendar.set(year, month, day, hour, minute, 5);

        // Kiểm tra xem thời gian đã chọn có là thời gian trong quá khứ không
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            Toast.makeText(this, "Thời gian chọn đã qua.", Toast.LENGTH_SHORT).show();
            return;
        }

        long millis = calendar.getTimeInMillis();
        Context context = getApplicationContext();
        Intent intent = new Intent(context, AlarmReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_REMINDER, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
                : PendingIntent.FLAG_ONE_SHOT);

        ((AlarmManager) getSystemService(ALARM_SERVICE))
                .set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() - instance.getTimeInMillis() + millis,
                        pendingIntent);

        Toast.makeText(this, "Thông báo đã được đặt.", Toast.LENGTH_SHORT).show();

        // Set dữ liệu reminder trên giao diện
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        reminderView.setText(dateFormat.format(calendar.getTime()));
        reminderView.setVisibility(View.VISIBLE);
        iconClock.setVisibility(View.VISIBLE);
    }

    public void updateDateTimePicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("LỜI NHẮC GHI CHÚ");
        builder.setMessage("Bạn muốn sửa đổi hay xóa lời nhắc này?");

        builder.setPositiveButton("Sửa đổi", (dialog, which) -> showDateTimePicker());

        builder.setNegativeButton("Xóa", (dialog, which) -> {
            reminderView.setText("");
            reminderView.setVisibility(View.GONE);
            iconClock.setVisibility(View.GONE);
        });

        builder.setNeutralButton("Hủy", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
