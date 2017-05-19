package com.example.akshit.imagesearch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button mSearchBtn;
    GridView mImagesGridView;
    TextInputEditText mQueryEditText;
    ImageAdapter mImageAdapter;
    int mScreenWidth;
    SharedPreferences mPrefs;
    public static final String NUM_COLUMNS_PREF = "num_columns";
    boolean mUserScrolled = false;
    CardView mBottomLayout;
    ProgressBar mMoreProgressBar;
    ProgressBar mSearchProgressBar;
    TextView mLoadingTextView;
    int mPageNo = 1;
    String mQueryString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSearchBtn = (Button) findViewById(R.id.search_btn);
        mImagesGridView = (GridView) findViewById(R.id.images_grid_view);
        mQueryEditText = (TextInputEditText) findViewById(R.id.query_edit_text);
        mBottomLayout = (CardView) findViewById(R.id.loading_card_view);
        mMoreProgressBar = (ProgressBar) findViewById(R.id.loading_progress_bar);
        mSearchProgressBar = (ProgressBar) findViewById(R.id.search_card_progress);
        mLoadingTextView = (TextView) findViewById(R.id.loading_text_view);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mImagesGridView.setNumColumns(mPrefs.getInt(NUM_COLUMNS_PREF, 2));

        mImageAdapter = new ImageAdapter(this);
        mImagesGridView.setAdapter(mImageAdapter);

        mImagesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent imageIntent = new Intent(getApplicationContext(), ImageActivity.class);
                imageIntent.putExtra(ImageActivity.IMAGE_INTENT_KEY, mImageAdapter.getItem(position));
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(MainActivity.this, view, ImageAdapter.TRANSITION_STRING);
                startActivity(imageIntent, options.toBundle());

            }
        });

        mImagesGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {

                    mUserScrolled = true;

                }

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (mUserScrolled
                        && firstVisibleItem + visibleItemCount == totalItemCount) {

                    mUserScrolled = false;
                    updateGridView();

                }

            }
        });

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mPageNo = 1;
                mQueryString = mQueryEditText.getText().toString().trim();
                if("".equals(mQueryString)){

                    Toast.makeText(getApplicationContext(), "Please enter a query to search!", Toast.LENGTH_SHORT).show();
                } else {
                    mSearchProgressBar.setVisibility(View.VISIBLE);
                    new SearchImagesTask(getApplicationContext(), mPageNo, mImageAdapter, mBottomLayout, mSearchProgressBar)
                            .execute(mQueryString);
                }

            }
        });

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mScreenWidth = displayMetrics.widthPixels;

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);

        switch (mPrefs.getInt(NUM_COLUMNS_PREF, 2)) {

            case 2:
                menu.findItem(R.id.action_two).setChecked(true);
                break;

            case 3:
                menu.findItem(R.id.action_three).setChecked(true);
                break;

            case 4:
                menu.findItem(R.id.action_four).setChecked(true);
                break;

        }

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {

            case R.id.action_two:
                setNumColumns(2, item);
                break;

            case R.id.action_three:
                setNumColumns(3, item);
                break;

            case R.id.action_four:
                setNumColumns(4, item);
                break;

            default:
                return true;
        }

        return super.onOptionsItemSelected(item);

    }

    private void setNumColumns(int no_of_col, MenuItem item) {

        mImagesGridView.setNumColumns(no_of_col);
        item.setChecked(true);

        SharedPreferences.Editor spEditor = mPrefs.edit();
        spEditor.putInt(NUM_COLUMNS_PREF, no_of_col);
        spEditor.apply();

    }

    private void updateGridView() {

        if(isNetworkAvailable(getApplicationContext()))
            mBottomLayout.setVisibility(View.VISIBLE);
        ++mPageNo;
        new SearchImagesTask(getApplicationContext(), mPageNo, mImageAdapter, mBottomLayout, mSearchProgressBar)
                .execute(mQueryString);

    }

    public static boolean isNetworkAvailable(Context context) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();

    }
}
