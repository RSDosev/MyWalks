package com.radodosev.mywalks.data.db;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Created by Rado on 7/8/2017.
 */

@Database(name = WalksDatabase.NAME, version = WalksDatabase.VERSION)
public class WalksDatabase {
    public static final String NAME = "WalksDatabase";

    public static final int VERSION = 1;
}
