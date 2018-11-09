package com.github.jtcressy.icpcbadgetool;

import com.github.jtcressy.icpcbadgetool.callback.BadgeFirstNameCallback;
import com.github.jtcressy.icpcbadgetool.callback.BadgeLastNameCallback;
import com.github.jtcressy.icpcbadgetool.callback.BadgeLedCallback;
import com.github.jtcressy.icpcbadgetool.callback.BadgeMessageCallback;

import no.nordicsemi.android.ble.BleManagerCallbacks;

public interface BadgeManagerCallbacks extends BleManagerCallbacks, BadgeFirstNameCallback,
        BadgeLastNameCallback, BadgeMessageCallback, BadgeLedCallback {
}
