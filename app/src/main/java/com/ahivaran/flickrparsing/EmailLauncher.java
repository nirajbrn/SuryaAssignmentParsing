package com.ahivaran.flickrparsing;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Loader;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.ahivaran.flickrparsing.adapter.UserRecyclerAdapter;
import com.ahivaran.flickrparsing.data.UserData;
import com.ahivaran.flickrparsing.util.Constant;
import com.ahivaran.flickrparsing.util.InternetConnectionDetector;
import com.ahivaran.flickrparsing.util.SimpleDividerItemDecoration;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class EmailLauncher extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<UserData>>, Constant{
    private static final String TAG = EmailLauncher.class.getSimpleName();

    private UserRecyclerAdapter mUserRecyclerAdapter;
    private EditText mEmailInputEt;
    private static String mEmailId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_launcher_layout);

        mUserRecyclerAdapter = new UserRecyclerAdapter(this, null);
        RecyclerView userRecyclerView = (RecyclerView)findViewById(R.id.flickr_recycler_view);
        userRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        userRecyclerView.setLayoutManager(layoutManager);
        userRecyclerView.setItemAnimator(new DefaultItemAnimator());
        userRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
        userRecyclerView.setAdapter(mUserRecyclerAdapter);
        InternetConnectionDetector connectionDetector = new InternetConnectionDetector(this);
        boolean isConnected = connectionDetector.isConnectingToInternet();
        if(isConnected){
            showEmailDialog();

        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.internet_title);
            builder.setMessage(R.string.internet_message);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });
            builder.show();

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_image_launcher, menu);
        return true;
    }

    public static class UserDetailsLoader extends AsyncTaskLoader<ArrayList<UserData>> {

        public static ArrayList<UserData> mImageDataList;
        ProgressDialog mProgressDialog;

        public UserDetailsLoader(Context context) {
            super(context);
            mProgressDialog = new ProgressDialog(context);
        }

        @Override
        public ArrayList<UserData> loadInBackground() {
            Log.d(TAG, "loadInBackground():");
            JSONObject jsonObjectData = getJsonObject();
            if(jsonObjectData != null){
                try {
                    mImageDataList = new ArrayList<>();
                    JSONArray itemsArray = jsonObjectData.getJSONArray(ITEM_TAG);

                    for (int i = 0; i < itemsArray.length(); i++) {
                        UserData userData = new UserData();
                        JSONObject itemObj = itemsArray.getJSONObject(i);
                        userData.fName = itemObj.getString(FNAME_TAG);
                        userData.lName = itemObj.getString(LNAME_TAG);
                        userData.emailId = itemObj.getString(EMAIL_TAG);
                        userData.imageUrl = itemObj.getString(IMAGE_URL_TAG);
                        mImageDataList.add(userData);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //Log.i(TAG, "loadInBackground(): jsonData: ");
            return mImageDataList;
        }

        @Override
        public void deliverResult(ArrayList<UserData> data) {
            if(mProgressDialog.isShowing()){
                mProgressDialog.dismiss();
            }
            if (isStarted()) {
                super.deliverResult(data);
            }
        }

        @Override
        protected void onStartLoading() {
            Log.d(TAG, "onStartLoading():");
            mProgressDialog.setMessage("Please wait..");
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();

            if (mImageDataList != null) {
                deliverResult(mImageDataList);
            }

            if (takeContentChanged() || mImageDataList == null) {
                forceLoad();
            }
        }

        @Override
        protected void onStopLoading() {
            mImageDataList = null;
            cancelLoad();
        }

        @Override
        protected void onReset() {
            onStopLoading();
            mImageDataList = null;
        }

        @Override
        public void onCanceled(ArrayList<UserData> data) {
            super.onCanceled(data);
        }

        private JSONObject getJsonObject() {
            Log.d(TAG, "getJsonObject(): ");
            InputStream is = null;
            JSONObject jObj = null;
            String json = "";
            // Making HTTP request
            try {
                // defaultHttpClient
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(jsonUrl);
                JSONObject jsonEmailObj = new JSONObject();
                jsonEmailObj.put("emailId", mEmailId);
                httpPost.setEntity(new StringEntity(jsonEmailObj.toString()));
                httpPost.setHeader("Accept", "application/json");
                //httpPost.setHeader("Content-type", "application/json");
                httpPost.setHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");

                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }catch (JSONException e){

            }

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        is, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                json = sb.toString();
            } catch (Exception e) {
                Log.e("Buffer Error", "Error converting result " + e.toString());
            }

            // try parse the string to a JSON object
            try {
                Log.i(TAG, "getJsonObject(): json: "+json);
                jObj = new JSONObject(json);
            } catch (JSONException e) {
                Log.e("JSON Parser", "Error parsing data " + e.toString());
            }

            // return JSON String
            return jObj;

        }

    }

    @Override
    public Loader<ArrayList<UserData>> onCreateLoader(int id, Bundle args) {
        return new UserDetailsLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<UserData>> loader, ArrayList<UserData> data) {
        mUserRecyclerAdapter.setBillDataList(data);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<UserData>> loader) {
        mUserRecyclerAdapter.setBillDataList(null);
    }

    private void showEmailDialog(){
        AlertDialog.Builder emailAlertBuilder = new AlertDialog.Builder(this);
        emailAlertBuilder.setTitle(R.string.email_id);
        mEmailInputEt = new EditText(this);
        emailAlertBuilder.setView(mEmailInputEt);
        emailAlertBuilder.setPositiveButton(R.string.member_done, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        emailAlertBuilder.setCancelable(false);
        AlertDialog alertDialog = emailAlertBuilder.create();
        alertDialog.show();
        Button dialogBtn = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        dialogBtn.setOnClickListener(new CustomAlertListener(alertDialog));
    }

    class CustomAlertListener implements View.OnClickListener{
        private final AlertDialog mDialog;

        public CustomAlertListener(AlertDialog dialog){
            this.mDialog = dialog;
        }

        @Override
        public void onClick(View v) {
            String emailId = mEmailInputEt.getText().toString();
            if(isValidEmail(emailId)) {
                mEmailId = emailId;
                getLoaderManager().restartLoader(0, null, EmailLauncher.this);
                mDialog.dismiss();
            } else {
                RelativeLayout parentView = (RelativeLayout)findViewById(R.id.email_parser_layout);
                showSnacksToast(parentView, R.string.enter_email);
            }

        }

        private boolean isValidEmail(String emailId){
            return android.util.Patterns.EMAIL_ADDRESS.matcher(emailId).matches();
        }
    }

    private void showSnacksToast(View view, int resourseId){
        Snackbar.make(view, resourseId, Snackbar.LENGTH_LONG)
                .show();
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
}
