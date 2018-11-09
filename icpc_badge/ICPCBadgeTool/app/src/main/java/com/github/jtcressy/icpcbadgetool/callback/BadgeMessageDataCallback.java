package com.github.jtcressy.icpcbadgetool.callback;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

import no.nordicsemi.android.ble.callback.DataSentCallback;
import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.data.Data;

public abstract class BadgeMessageDataCallback implements ProfileDataCallback, DataSentCallback, BadgeMessageCallback {
    @Override
    public void onDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
        parse(device, data);
    }

    @Override
    public void onDataSent(@NonNull BluetoothDevice device, @NonNull Data data) {
        parse(device, data);
    }

    private void parse(@NonNull BluetoothDevice device, @NonNull Data data) {
        if (data.size() < 1) {
            onInvalidDataReceived(device, data);
            return;
        }

        final String value = data.getStringValue(0);
        onMessageChanged(device, value);
    }
}
