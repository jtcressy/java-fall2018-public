package com.github.jtcressy.icpcbadgetool;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.support.annotation.NonNull;

import com.github.jtcressy.icpcbadgetool.callback.BadgeFirstNameDataCallback;
import com.github.jtcressy.icpcbadgetool.callback.BadgeLastNameDataCallback;
import com.github.jtcressy.icpcbadgetool.callback.BadgeLedDataCallback;
import com.github.jtcressy.icpcbadgetool.callback.BadgeMessageDataCallback;

import java.nio.ByteBuffer;

import no.nordicsemi.android.ble.BleManager;

public class BadgeManager extends BleManager<BadgeManagerCallbacks> {


    private final BadgeFirstNameDataCallback mFnameCallback = new BadgeFirstNameDataCallback() {
        @Override
        public void onFirstNameChanged(BluetoothDevice device, String value) {
            mCallbacks.onFirstNameChanged(device, value);
        }
    };
    private final BadgeLastNameDataCallback mLnameCallback = new BadgeLastNameDataCallback() {
        @Override
        public void onLastNameChanged(BluetoothDevice device, String value) {
            mCallbacks.onLastNameChanged(device, value);
        }
    };
    private final BadgeMessageDataCallback mMessageCallback = new BadgeMessageDataCallback() {
        @Override
        public void onMessageChanged(BluetoothDevice device, String value) {
            mCallbacks.onMessageChanged(device, value);
        }
    };
    private final BadgeLedDataCallback mLedCallback = new BadgeLedDataCallback() {
        @Override
        public void onLedChanged(BluetoothDevice device, int color) {
            mCallbacks.onLedChanged(device, color);
        }

    };
    private BluetoothGattCharacteristic mFirstNameChar, mLastNameChar, mMessageChar, mLedChar;
    private BleManagerGattCallback mGattCallback = new BleManagerGattCallback() {
        @Override
        protected void initialize() {
            readCharacteristic(mFirstNameChar).with(mFnameCallback).enqueue();
            readCharacteristic(mLastNameChar).with(mLnameCallback).enqueue();
            readCharacteristic(mLedChar).with(mLedCallback).enqueue();
        }

        @Override
        protected boolean isRequiredServiceSupported(@NonNull BluetoothGatt gatt) {
            final BluetoothGattService user_data_service = gatt.getService(BadgeGattAttributes.SERVICE_USER_DATA);
            final BluetoothGattService rgb_led_service = gatt.getService(BadgeGattAttributes.SERVICE_RGB_LED);
            final BluetoothGattService message_service = gatt.getService(BadgeGattAttributes.SERVICE_BADGE_MESSAGE);
            if (user_data_service != null) {
                mFirstNameChar = user_data_service.getCharacteristic(BadgeGattAttributes.CHARACTERISTIC_USER_DATA_FNAME);
                mLastNameChar = user_data_service.getCharacteristic(BadgeGattAttributes.CHARACTERISTIC_USER_DATA_LNAME);
            }
            if (rgb_led_service != null) {
                mLedChar = rgb_led_service.getCharacteristic(BadgeGattAttributes.CHARACTERISTIC_RGB_LED_COLOR);
            }
            if (message_service != null) {
                mMessageChar = message_service.getCharacteristic(BadgeGattAttributes.CHARACTERISTIC_BADGE_MESSAGE);
            }

            boolean ledWritable = false;
            if (mLedChar != null) {
                final int rxProperties = mLedChar.getProperties();
                ledWritable = (rxProperties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0;
            }
            boolean fnameWritable = false;
            if (mFirstNameChar != null) {
                final int rxProperties = mFirstNameChar.getProperties();
                fnameWritable = (rxProperties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0;
            }
            boolean lnameWritable = false;
            if (mLastNameChar != null) {
                final int rxProperties = mLastNameChar.getProperties();
                lnameWritable = (rxProperties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0;
            }
            boolean messageWritiable = false;
            if (mMessageChar != null) {
                final int rxProperties = mMessageChar.getProperties();
                messageWritiable = (rxProperties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0;
            }

            return mFirstNameChar != null &&
                    fnameWritable &&
                    mLastNameChar != null &&
                    lnameWritable &&
                    mMessageChar != null &&
                    messageWritiable &&
                    mLedChar != null &&
                    ledWritable;
        }

        @Override
        protected void onDeviceDisconnected() {
            mFirstNameChar = null;
            mLastNameChar = null;
            mMessageChar = null;
            mLedChar = null;
        }
    };

    BadgeManager(final Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return mGattCallback;
    }

    void sendLedColor(final int hexcolor) {
        if (mLedChar == null) return;
        byte[] bytes = ByteBuffer.allocate(4).putInt(hexcolor).array();
        writeCharacteristic(mLedChar, bytes).with(mLedCallback).enqueue();
        readCharacteristic(mLedChar).with(mLedCallback).enqueue();
    }

    void sendFirstName(final String value) {
        if (mFirstNameChar == null) return;
        byte[] bytes = value.getBytes();
        writeCharacteristic(mFirstNameChar, bytes).with(mFnameCallback).enqueue();
    }

    void sendLastName(final String value) {
        if (mLastNameChar == null) return;
        byte[] bytes = value.getBytes();
        writeCharacteristic(mLastNameChar, bytes).with(mLnameCallback).enqueue();
    }

    void sendMessage(final String value) {
        if (mMessageChar == null) return;
        byte[] bytes = value.getBytes();
        writeCharacteristic(mMessageChar, bytes).with(mMessageCallback).enqueue();
    }
}
