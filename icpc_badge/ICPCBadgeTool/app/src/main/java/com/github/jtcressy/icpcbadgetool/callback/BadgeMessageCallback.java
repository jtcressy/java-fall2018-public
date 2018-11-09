package com.github.jtcressy.icpcbadgetool.callback;

import android.bluetooth.BluetoothDevice;

public interface BadgeMessageCallback {
    void onMessageChanged(final BluetoothDevice device, final String value);
}
