package com.example.akshit.imagesearch.data;

import android.provider.BaseColumns;

public class PhotosContract {

    PhotosContract(){}

    public static final class PhotoEntry implements BaseColumns{

        PhotoEntry(){}

        public static final String TABLE_NAME = "photos";
        public static final String QUERY_STRING = "query";
        public static final String PAGE_NUMBER = "page_no";
        public static final String PHOTO_URL = "url";

    }
}
