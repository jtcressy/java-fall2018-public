package com.github.jtcressy.icpcbadgetool.callback;

import android.bluetooth.BluetoothDevice;

public interface BadgeLastNameCallback {
    void onLastNameChanged(final BluetoothDevice device, final String value);
}
