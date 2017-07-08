package com.radodosev.mywalks.data.db;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.rx2.structure.BaseRXModel;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by Rado on 7/8/2017.
 */

@Table(database = WalksDatabase.class)
public class WalksTable extends BaseRXModel {

    @Unique
    @Column
    @PrimaryKey(autoincrement=true)
    long id;

    @Column
    Date startTime;

    @Column
    Date endTime;

    @ForeignKey(saveForeignKeyModel = true)
    RoutePointsTable routePointsTable;

    List<RoutePointsTable> routePoints;

    public WalksTable(Date startTime, Date endTime, List<RoutePointsTable> routePoints){
        this.startTime = startTime;
        this.endTime = endTime;
        this.routePoints = routePoints;
    }


    @OneToMany(methods = OneToMany.Method.ALL, variableName = "routePoints")
    public List<RoutePointsTable> getRoutePoints() {
        if (routePoints == null) {
            routePoints = SQLite.select()
                    .from(RoutePointsTable.class)
                    .where(RoutePointsTable_Table.walkId.eq(id))
                    .queryList();
        }
        return routePoints;
    }

    public Date getEndTime() {
        return endTime;
    }

    public Date getStartTime() {
        return startTime;
    }
}
