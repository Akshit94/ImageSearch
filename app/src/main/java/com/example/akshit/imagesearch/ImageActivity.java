package com.example.akshit.imagesearch;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class ImageActivity extends AppCompatActivity {
    public static final String IMAGE_INTENT_KEY = "image_intent";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        postponeEnterTransition();

        final ImageView imageView = (ImageView) findViewById(R.id.image_view_detail);

        Intent imageIntent = getIntent();
        if(imageIntent != null && imageIntent.hasExtra(IMAGE_INTENT_KEY)){
            String url = imageIntent.getStringExtra(IMAGE_INTENT_KEY);
            Picasso.with(getApplicationContext())
                    .load(url)
                    .placeholder(R.drawable.ic_placeholder)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .error(R.drawable.ic_placeholder)
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            startTransition(imageView);
                        }

                        @Override
                        public void onError() {
                            startTransition(imageView);
                        }
                    });
        }
    }

    private void startTransition(final ImageView imageView){

        imageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                startPostponedEnterTransition();
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            supportFinishAfterTransition();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        supportFinishAfterTransition();
    }
}
