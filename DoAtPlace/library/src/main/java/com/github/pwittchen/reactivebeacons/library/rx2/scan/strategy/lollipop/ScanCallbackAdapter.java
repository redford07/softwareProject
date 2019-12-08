/*
 * Copyright (C) 2016 Piotr Wittchen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pwittchen.reactivebeacons.library.rx2.scan.strategy.lollipop;

import android.annotation.TargetApi;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Build;

import com.github.pwittchen.reactivebeacons.library.rx2.Beacon;
import com.github.pwittchen.reactivebeacons.library.rx2.FutureAdapter;

import io.reactivex.Observable;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ScanCallbackAdapter extends ScanCallback {
    private final FutureAdapter futureAdapter = new FutureAdapter();

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        futureAdapter.setBeacon(Beacon.create(result));
    }

    public Observable<Beacon> toObservable() {
        return Observable.fromFuture(futureAdapter);
    }
}
