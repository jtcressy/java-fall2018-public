package com.github.jtcressy.icpcbadgetool.callback;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

import no.nordicsemi.android.ble.callback.DataSentCallback;
import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.data.Data;

public abstract class BadgeLedDataCallback implements ProfileDataCallback, DataSentCallback, BadgeLedCallback {
    @Override
    public void onDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
        parse(device, data);
    }

    @Override
    public void onDataSent(@NonNull BluetoothDevice device, @NonNull Data data) {
        parse(device, data);
    }

    private void parse(@NonNull BluetoothDevice device, @NonNull Data data) {
        if (data.size() != Integer.SIZE / 8) {
            onInvalidDataReceived(device, data);
            return;
        }

        final Integer value = data.getIntValue(Data.FORMAT_UINT32, 0);
        if (value < 0xFFFFFF && value > 0x0) {
            onLedChanged(device, value);
        } else {
            onInvalidDataReceived(device, data);
        }
    }
}
