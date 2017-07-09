package com.radodosev.mywalks.domain;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;

import com.radodosev.mywalks.R;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

import static com.radodosev.mywalks.domain.WalksTracker.WalkTrackState.Status.ERROR;

/**
 * Created by blue on 9.7.2017 г..
 */

public class WalkTrackerService extends Service {
    private static int SERVICE_NOTIFICATION_ID = 213;

    private static Subject<WalksTracker.WalkTrackState> trackEmitter = BehaviorSubject.create();
    private WalksTracker walksTracker;
    private Disposable trackingDisposable;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setTheWalksTracker();
        walksTracker.startTracking(this);

        startForeground(SERVICE_NOTIFICATION_ID, new Notification.Builder(this)
                .setContentText("Walk tracking in progress...")
                .setSmallIcon(R.drawable.ic_directions_walk)
                .build());
        return START_STICKY;
    }

    private void setTheWalksTracker() {
        walksTracker = DI.provideWalksTracker();

        trackingDisposable = walksTracker.subscribeForWalkState()
                .subscribe(walkTrackState -> {
                    if (walkTrackState.getStatus() == ERROR) {
                        stopForeground(true);
                        stopSelf();
                    }

                    trackEmitter.onNext(walkTrackState);
                }, trackEmitter::onError);
    }

    public static Observable<WalksTracker.WalkTrackState> subscribe() {
        if (trackEmitter.hasThrowable())
            trackEmitter = BehaviorSubject.create();
        return trackEmitter;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        walksTracker.stopTracking();
        trackingDisposable.dispose();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void start(Context context) {
        context.startService(new Intent(context, WalkTrackerService.class));
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, WalkTrackerService.class));
    }

    public static boolean isRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (WalkTrackerService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
