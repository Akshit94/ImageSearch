package com.example.akshit.imagesearch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.example.akshit.imagesearch.data.PhotosContract;
import com.example.akshit.imagesearch.data.PhotosDbHelper;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

class SearchImagesTask extends AsyncTask<String, Integer, String[]> {

    private static final String LOG_TAG = SearchImagesTask.class.getSimpleName();
    private Context mContext;
    private int mPageNo;
    private ImageAdapter mImageAdapter;
    private CardView mBottomLayout;
    private ProgressBar mSearchProgressBar;
    private String AND_QUERY = "=? AND ";

    SearchImagesTask(Context context, int pageNo, ImageAdapter imageAdapter, CardView bottomLayout, ProgressBar searchBar) {
        mContext = context;
        mPageNo = pageNo;
        mImageAdapter = imageAdapter;
        mBottomLayout = bottomLayout;
        mSearchProgressBar = searchBar;
    }

    private void clearOldCache(SQLiteDatabase sqLiteDatabase, String param){

        Cursor retCursor = sqLiteDatabase.query(PhotosContract.PhotoEntry.TABLE_NAME,
                null,
                PhotosContract.PhotoEntry.QUERY_STRING + AND_QUERY + PhotosContract.PhotoEntry.PAGE_NUMBER + "=?",
                new String[]{param.toLowerCase(), Integer.toString(mPageNo)},
                null, null, null);

        while (retCursor.moveToNext()) {

            Picasso.with(mContext)
                    .invalidate(retCursor.getString(retCursor.getColumnIndex(PhotosContract.PhotoEntry.PHOTO_URL)));
        }
        sqLiteDatabase.delete(PhotosContract.PhotoEntry.TABLE_NAME,
                PhotosContract.PhotoEntry.QUERY_STRING + AND_QUERY + PhotosContract.PhotoEntry.PAGE_NUMBER + "=?",
                new String[]{param.toLowerCase(), Integer.toString(mPageNo)});

        retCursor.close();
    }

    private String[] getImagesUrlFromJson(String flickrJsonStr, String param) throws JSONException {

        final String FLICKR_PHOTOS = "photos";
        final String FLICKR_PHOTO_ITEM = "photo";
        final String FLICKR_PHOTO_ID = "id";
        final String FLICKR_PHOTO_SECRET = "secret";
        final String FLICKR_PHOTO_FARM = "farm";
        final String FLICKR_PHOTO_SERVER = "server";
        final String PHOTO_BASE_URL_1 = "https://farm";
        final String PHOTO_BASE_URL_2 = ".staticflickr.com/";
        final String PHOTO_URL_END = "_m.jpg";

        JSONObject jsonObject = new JSONObject(flickrJsonStr);
        JSONObject jsonPhotos = jsonObject.getJSONObject(FLICKR_PHOTOS);
        JSONArray jsonPhotoItemArray = jsonPhotos.getJSONArray(FLICKR_PHOTO_ITEM);

        String[] imagesUrl = new String[jsonPhotoItemArray.length()];

        PhotosDbHelper mOpenHelper = new PhotosDbHelper(mContext);
        SQLiteDatabase sqLiteDatabase = mOpenHelper.getWritableDatabase();

        clearOldCache(sqLiteDatabase, param);

        for (int i = 0; i < jsonPhotoItemArray.length(); ++i) {

            JSONObject jsonPhotoItem = jsonPhotoItemArray.getJSONObject(i);

            String photoId = jsonPhotoItem.getString(FLICKR_PHOTO_ID);
            String photoSecret = jsonPhotoItem.getString(FLICKR_PHOTO_SECRET);
            String photoFarm = jsonPhotoItem.getString(FLICKR_PHOTO_FARM);
            String photoServer = jsonPhotoItem.getString(FLICKR_PHOTO_SERVER);

            String url = PHOTO_BASE_URL_1 + photoFarm +
                    PHOTO_BASE_URL_2 + photoServer +
                    "/" + photoId + "_" + photoSecret + PHOTO_URL_END;

            imagesUrl[i] = url;

            ContentValues photoValues = new ContentValues();

            photoValues.put(PhotosContract.PhotoEntry.QUERY_STRING, param.toLowerCase());
            photoValues.put(PhotosContract.PhotoEntry.PAGE_NUMBER, mPageNo);
            photoValues.put(PhotosContract.PhotoEntry.PHOTO_URL, url);

            sqLiteDatabase.insert(PhotosContract.PhotoEntry.TABLE_NAME, null, photoValues);

        }

        return imagesUrl;

    }

    private String[] callFlickrApi(String param) {

        HttpsURLConnection urlConnection = null;
        BufferedReader reader = null;

        final String FLICKR_BASE_URL = "https://api.flickr.com/services/rest/?";
        final String METHOD_PARAM = "method";
        final String PER_PAGE_PARAM = "per_page";
        final String PAGE_PARAM = "page";
        final String JSON_CALLBACK_PARAM = "nojsoncallback";
        final String FORMAT_PARAM = "format";
        final String SORT_PARAM = "sort";
        final String TEXT_PARAM = "text";
        final String API_KEY_PARAM = "api_key";
        final String method = "flickr.photos.search";
        final String per_page = "20";
        final String sort = "relevance";
        final String format = "json";

        String flickrJsonString;
        String query = param.replace(" ", "+");

        try {

            Uri.Builder uriBuilder = Uri.parse(FLICKR_BASE_URL).buildUpon();
            Uri builtUri = uriBuilder.appendQueryParameter(METHOD_PARAM, method)
                    .appendQueryParameter(PER_PAGE_PARAM, per_page)
                    .appendQueryParameter(PAGE_PARAM, Integer.toString(mPageNo))
                    .appendQueryParameter(JSON_CALLBACK_PARAM, Integer.toString(1))
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(SORT_PARAM, sort)
                    .appendQueryParameter(TEXT_PARAM, query)
                    .appendQueryParameter(API_KEY_PARAM, BuildConfig.FLICKR_API_KEY)
                    .build();
            URL url = new URL(builtUri.toString());

            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();

            if (inputStream == null) {

                return new String[0];

            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {

                buffer.append(line).append("\n");

            }

            if (buffer.length() == 0) {

                return new String[0];

            }

            flickrJsonString = buffer.toString();
            return getImagesUrlFromJson(flickrJsonString, param);

        } catch (IOException e) {

            Log.e(LOG_TAG, "Error ", e);
            return new String[0];

        } catch (JSONException e) {

            Log.e(LOG_TAG, "Error ", e);

        } finally {

            if (urlConnection != null) {

                urlConnection.disconnect();

            }
            if (reader != null) {

                try {

                    reader.close();

                } catch (final IOException e) {

                    Log.e(LOG_TAG, "Error closing stream", e);

                }

            }

        }

        return new String[0];

    }

    @Override
    protected String[] doInBackground(String... params) {

        if (MainActivity.isNetworkAvailable(mContext)) {

            return callFlickrApi(params[0]);

        } else {

            PhotosDbHelper mOpenHelper = new PhotosDbHelper(mContext);
            SQLiteDatabase sqLiteDatabase = mOpenHelper.getReadableDatabase();
            Cursor retCursor = sqLiteDatabase.query(PhotosContract.PhotoEntry.TABLE_NAME,
                    null,
                    PhotosContract.PhotoEntry.QUERY_STRING + AND_QUERY + PhotosContract.PhotoEntry.PAGE_NUMBER + "=?",
                    new String[]{params[0].toLowerCase(), Integer.toString(mPageNo)},
                    null, null, null);
            int cursorLen = retCursor.getCount();

            String[] imagesUrl = new String[cursorLen];
            int i = 0;

            while (retCursor.moveToNext()) {
                imagesUrl[i] = retCursor.getString(retCursor.getColumnIndex(PhotosContract.PhotoEntry.PHOTO_URL));
                ++i;
            }

            retCursor.close();
            return imagesUrl;
        }

    }

    @Override
    protected void onPostExecute(String[] strings) {

        super.onPostExecute(strings);

        if (strings.length != 0) {
            if (mPageNo == 1)
                mImageAdapter.clear();
            for (String s : strings) {
                mImageAdapter.add(s);
            }

            mImageAdapter.notifyDataSetChanged();
        }

        mBottomLayout.setVisibility(View.GONE);
        mSearchProgressBar.setVisibility(View.GONE);

    }
}