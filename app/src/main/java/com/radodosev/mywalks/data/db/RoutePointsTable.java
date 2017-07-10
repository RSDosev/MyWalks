package com.radodosev.mywalks.data.db;

import com.radodosev.mywalks.data.model.Walk;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.rx2.structure.BaseRXModel;

import java.sql.Date;
import java.util.UUID;

/**
 * Created by Rado on 7/8/2017.
 */

@Table(database = WalksDatabase.class)
public class RoutePointsTable extends BaseRXModel {
    @Unique
    @Column
    @PrimaryKey(autoincrement = true)
    long id;

    @ForeignKey(saveForeignKeyModel = true,
            tableClass = WalksTable.class,
            references = {@ForeignKeyReference(columnName = "walk_id", foreignKeyColumnName = "id")})
    WalksTable walk;

    @Column
    double latitude;

    @Column
    double longitude;

    public RoutePointsTable(){}

    public RoutePointsTable(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
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

    public void setWalk(WalksTable walk) {
        this.walk = walk;
    }
}