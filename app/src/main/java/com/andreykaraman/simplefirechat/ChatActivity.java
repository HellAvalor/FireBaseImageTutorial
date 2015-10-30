package com.andreykaraman.simplefirechat;

import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.andreykaraman.simplefirechat.model.ChatMessage;
import com.cloudinary.Cloudinary;
import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseListAdapter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private FirebaseListAdapter mListAdapter;
    private ListView listView;
    private Firebase myFirebaseRef;
    private Cloudinary cloudinary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        myFirebaseRef = new Firebase(getString(R.string.firebase_url));

        Map config = new HashMap();
        config.put("cloud_name", getString(R.string.cloudinary_cloud_name));
        config.put("api_key", getString(R.string.cloudinary_api_key));
        config.put("api_secret", getString(R.string.cloudinary_api_secret));
        cloudinary = new Cloudinary(config);

        setContentView(R.layout.activity_chat);

        listView = (ListView) findViewById(R.id.list_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPictureFromGallery();
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });

        mListAdapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class,
                R.layout.list_item_image, myFirebaseRef) {
            @Override
            protected void populateView(View v, ChatMessage model) {
                ((TextView) v.findViewById(R.id.nameText)).setText(model.getName());
                ((TextView) v.findViewById(R.id.text)).setText(model.getText());
                ImageView image = ((ImageView) v.findViewById(R.id.image));
                if (!TextUtils.isEmpty(model.getImageUrl())) {
                    byte[] imageAsBytes = Base64.decode(model.getImageUrl(), Base64.DEFAULT);

                    Bitmap bmp = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
                    image.setVisibility(View.VISIBLE);
                    image.setImageBitmap(bmp);
//                    Picasso.with(getApplicationContext()).load(bmp).into(image);
                } else {
                    image.setVisibility(View.GONE);
                }
            }
        };

        listView.setAdapter(mListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getPictureFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), 1);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                Uri selectedImageUri = data.getData();

                Upload upload = new Upload();
                upload.execute(getPath(selectedImageUri));
//                Bitmap bm = null;
//                String encodedImage = "";
//                try {
//                    bm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
//                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                    bm.compress(Bitmap.CompressFormat.JPEG, 80, baos); //bm is the bitmap object
//                    byte[] b = baos.toByteArray();
//
//                    encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
////                String text = textEdit.getText().toString();
//                Map<String,Object> values = new HashMap<>();
//                values.put("name", "Android User");
//                values.put("text", "Test text");
//                values.put("imageUrl", encodedImage);
//
//                myFirebaseRef.push().setValue(values);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mListAdapter.cleanup();
    }

    private class Upload extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            Map response;

            File file = new File(urls[0]);

            try {
                response = cloudinary.uploader().upload(file, null);

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }

    public String getPath(Uri uri) {
        String data = MediaStore.MediaColumns.DATA;
        String[] proj = {data};
        Cursor cursor = new CursorLoader(this, uri, proj, null, null,
                null).loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(data);
        cursor.moveToFirst();
        if (cursor.getCount() > 0)
            return cursor.getString(column_index);
        else
            return "";
    }
}
