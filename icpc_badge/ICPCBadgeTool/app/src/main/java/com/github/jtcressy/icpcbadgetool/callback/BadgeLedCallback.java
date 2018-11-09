package com.github.jtcressy.icpcbadgetool.callback;

import android.bluetooth.BluetoothDevice;

public interface BadgeLedCallback {
    void onLedChanged(final BluetoothDevice device, final int color);
}
