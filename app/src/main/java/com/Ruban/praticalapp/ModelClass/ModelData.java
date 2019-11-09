package com.Ruban.praticalapp.ModelClass;

import android.provider.BaseColumns;

public class ModelData {
    ModelData()
    {}

    public static class DatabaseModelView implements BaseColumns
    {
        public static final String DATABASE_NAME = "UserData.db";
        public static final String TABLE_NAME = "User_table";
        public static final String COLUMN_ID = "User_id";
        public static final String COLUMN_IMAGE = "User_image";
        public static final String COLUMN_USER_NAME = "User_name";
        public static final String COLUMN_USER_ADDRESS = "User_address";
        public static final String COLUMN_EMAIL = "User_email";


    }
}
