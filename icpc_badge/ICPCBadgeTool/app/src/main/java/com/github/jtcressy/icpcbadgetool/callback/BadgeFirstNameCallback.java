package com.github.jtcressy.icpcbadgetool.callback;

import android.bluetooth.BluetoothDevice;

public interface BadgeFirstNameCallback {
    void onFirstNameChanged(final BluetoothDevice device, final String value);
}
