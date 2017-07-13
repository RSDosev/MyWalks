package com.radodosev.mywalks.data.db;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.rx2.structure.BaseRXModel;

/**
 * Created by Rado on 7/8/2017.
 */

@Table(database = WalksDatabase.class)
public class RoutePointsTable extends BaseRXModel {
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_WALK_ID = "walk_id";

    @Unique
    @Column(name = COLUMN_ID)
    @PrimaryKey(autoincrement = true)
    long id;

    @ForeignKey(tableClass = WalksTable.class,
            stubbedRelationship = true,
            references = {@ForeignKeyReference(columnName = COLUMN_WALK_ID, foreignKeyColumnName = WalksTable.COLUMN_ID)})
    WalksTable walk;

    @Column
    double latitude;

    @Column
    double longitude;

    @Column
    float speed;

    public RoutePointsTable() {
    }

    public RoutePointsTable(double latitude, double longitude, float speed) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
    }

    public WalksTable getWalk() {
        return walk;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public float getSpeed() {
        return speed;
    }

    public void setWalk(WalksTable walk) {
        this.walk = walk;
    }
}