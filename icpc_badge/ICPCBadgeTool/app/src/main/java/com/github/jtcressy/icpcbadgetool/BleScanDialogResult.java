package com.github.jtcressy.icpcbadgetool;

import android.bluetooth.BluetoothDevice;

public interface BleScanDialogResult {
    void onSelectedDevice(BluetoothDevice device);
}
