package com.andreykaraman.simplefirechat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.andreykaraman.simplefirechat.model.ChatMessage;
import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseListAdapter;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private FirebaseListAdapter mListAdapter;
    private ListView listView;
    private EditText inputField;
    private ImageView addPhoto;
    private ImageView sendMessage;
    private Firebase myFirebaseRef;
    private Cloudinary cloudinary;
    private String innerUrl;
    private ProgressDialog progress;

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


        inputField = (EditText) findViewById(R.id.input_field);

        addPhoto = (ImageView) findViewById(R.id.add_picture);
        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPictureFromGallery();
            }
        });

        sendMessage = (ImageView) findViewById(R.id.send_message);
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryUploadImageAndSendMessage();
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
                    image.setVisibility(View.VISIBLE);
                    Picasso.with(getApplicationContext()).load(model.getImageUrl()).into(image);
                } else {
                    image.setVisibility(View.GONE);
                }
            }
        };
        listView.setAdapter(mListAdapter);

        progress = new ProgressDialog(this);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.setMessage("Image loading");
    }

    private void tryUploadImageAndSendMessage() {
        if (!TextUtils.isEmpty(innerUrl)) {
            Upload upload = new Upload();
            upload.execute(innerUrl);
        } else
            sendMessage();
    }

    private void sendMessage() {

        String text = inputField.getText().toString();
        Map<String, Object> values = new HashMap<>();
        values.put("name", "User name");
        values.put("text", text);
        values.put("imageUrl", innerUrl);

        myFirebaseRef.push().setValue(values);
        inputField.setText("");
        innerUrl = "";

        Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
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
                innerUrl = Utils.getPath(this, selectedImageUri);
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
        protected void onPreExecute() {
            super.onPreExecute();
            progress.show();
        }

        @Override
        protected String doInBackground(String... urls) {
            Map response;
            String imageUrl = "";
            File file = new File(urls[0]);

            try {
                response = cloudinary.uploader().upload(file, ObjectUtils.asMap(
                        "transformation", new Transformation().crop("limit").width(800)));
                if (response!=null)
                    imageUrl = response.get("url").toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return imageUrl;
        }

        @Override
        protected void onPostExecute(String result) {
            innerUrl = result;
            progress.dismiss();
            sendMessage();
        }
    }
}
