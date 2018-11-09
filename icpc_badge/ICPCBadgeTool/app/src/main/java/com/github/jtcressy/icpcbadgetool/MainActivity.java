package com.github.jtcressy.icpcbadgetool;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.skydoves.colorpickerpreference.ColorPickerView;


public class MainActivity extends AppCompatActivity implements BleScanDialogResult, BadgeManagerCallbacks {

    private static final int REQUEST_FINE_LOCATION = 1;
    private static final String LOG_TAG = "ICPCBadgeTool";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final byte UI_STATE_DISCONNECTED = 0x00;
    private static final byte UI_STATE_CONNECTED = 0x01;
    private static final byte UI_STATE_CONNECTING = 0x02;
    private static final byte UI_STATE_DISCONNECTING = 0x03;
    private Button connectButton;
    private Button colorApplyButton;
    private Button firstNameSendButton;
    private TextView firstNameTextView;
    private Button lastNameSendButton;
    private TextView lastNameTextView;
    private Button messageSendButton;
    private TextView messageTextView;
    private LinearLayout colorPreview;
    private LinearLayout colorActual;

    private BluetoothAdapter mBluetoothAdapter;
    private BadgeManager badgeManager;
    private BluetoothDevice mSelectedDevice;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        setContentView(R.layout.activity_main);
        connectButton = findViewById(R.id.connect_button);
        connectButton.setOnClickListener(v -> {
            if (badgeManager.isConnected()) {
                disconnectDevice();
                mSelectedDevice = null; // set to null so that the app does not re-connect after activity is resumed.
            } else {
                if (hasPermissions()) {
                    FragmentManager fm = getSupportFragmentManager();
                    BleScanDialogFragment dialog = BleScanDialogFragment.newInstance("Devices");
                    dialog.show(fm, "ble_scan_fragment_dialog");
                } else {
                    requestPermissions(new String[]{Manifest.permission.BLUETOOTH_ADMIN}, 1);
                }
            }
        });

        colorPreview = findViewById(R.id.color_preview);
        colorActual = findViewById(R.id.color_actual);
        ColorPickerView colorPickerView = findViewById(R.id.colorPickerView);
        colorPickerView.setColorListener(colorEnvelope -> colorPreview.setBackgroundColor(colorEnvelope.getColor()));
        colorApplyButton = findViewById(R.id.color_apply_button);
        colorApplyButton.setOnClickListener(v -> {
            int color = Color.TRANSPARENT;
            Drawable background = colorPreview.getBackground();
            if (background instanceof ColorDrawable) {
                color = ((ColorDrawable) background).getColor();
            }
            badgeManager.sendLedColor(color);
        });
        firstNameSendButton = findViewById(R.id.fname_send_button);
        firstNameTextView = findViewById(R.id.fname_textview);
        firstNameSendButton.setOnClickListener(v -> {
            String first_name = firstNameTextView.getText().toString();
            badgeManager.sendFirstName(first_name);
        });

        lastNameSendButton = findViewById(R.id.lname_send_button);
        lastNameTextView = findViewById(R.id.lname_textview);
        lastNameSendButton.setOnClickListener(v -> {
            String last_name = lastNameTextView.getText().toString();
            badgeManager.sendLastName(last_name);
        });

        messageSendButton = findViewById(R.id.message_send_button);
        messageTextView = findViewById(R.id.message_textview);
        messageSendButton.setOnClickListener(v -> {
            String message = messageTextView.getText().toString();
            badgeManager.sendMessage(message);
        });

        setUIState(UI_STATE_DISCONNECTED);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        badgeManager = new BadgeManager(getApplication());
        badgeManager.setGattCallbacks(this);
    }

    @Override
    public void onSelectedDevice(BluetoothDevice device) {
        mSelectedDevice = device;
        connectDevice();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            finish();
        }
        connectDevice();
    }

    @Override
    protected void onPause() {
        disconnectDevice();
        super.onPause();
    }

    private void setUIState(byte state) {
        Runnable r = () -> {
        };
        switch (state) {
            case UI_STATE_CONNECTED:
                r = () -> {
                    connectButton.setEnabled(true);
                    TextView devstatus = findViewById(R.id.device_status_text);
                    devstatus.setText(getString(R.string.device_status_text_connected));
                    connectButton.setText(R.string.device_status_connect_button_connected);
                    colorApplyButton.setEnabled(true);
                    firstNameSendButton.setEnabled(true);
                    lastNameSendButton.setEnabled(true);
                    messageSendButton.setEnabled(true);
                };
                break;
            case UI_STATE_CONNECTING:
                r = () -> {
                    connectButton.setEnabled(false);
                    TextView devstatus = findViewById(R.id.device_status_text);
                    devstatus.setText(getString(R.string.device_status_connecting));
                };
                break;
            case UI_STATE_DISCONNECTED:
                r = () -> {
                    connectButton.setEnabled(true);
                    TextView dev_status = findViewById(R.id.device_status_text);
                    dev_status.setText(getString(R.string.device_status_text_disconnected));
                    connectButton.setText(R.string.device_status_connect_button_disconnected);
                    colorApplyButton.setEnabled(false);
                    firstNameSendButton.setEnabled(false);
                    lastNameSendButton.setEnabled(false);
                    messageSendButton.setEnabled(false);
                };
                break;
            case UI_STATE_DISCONNECTING:
                r = () -> {
                    connectButton.setEnabled(false);
                    TextView dev_status = findViewById(R.id.device_status_text);
                    dev_status.setText(getString(R.string.device_status_disconnecting));
                };
                break;
        }
        mHandler.post(r);
    }

    private void disconnectDevice() {
        badgeManager.disconnect().enqueue();
    }

    private void connectDevice() {
        if (mSelectedDevice == null) return;
        badgeManager.connect(mSelectedDevice)
                .retry(3, 100)
                .useAutoConnect(false)
                .enqueue();
    }

    private boolean hasPermissions() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            requestBluetoothEnable();
            return false;
        } else if (!hasLocationPermissions()) {
            requestLocationPermission();
            return false;
        }
        return true;
    }

    private void requestBluetoothEnable() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        Log.d(LOG_TAG, "Requested user enables Bluetooth. Try starting the scan again.");
    }

    private boolean hasLocationPermissions() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
    }

    @Override
    public void onDeviceConnecting(@NonNull BluetoothDevice device) {
        setUIState(UI_STATE_CONNECTING);
    }

    @Override
    public void onDeviceConnected(@NonNull BluetoothDevice device) {
        setUIState(UI_STATE_CONNECTED);
    }

    @Override
    public void onDeviceDisconnecting(@NonNull BluetoothDevice device) {
        setUIState(UI_STATE_DISCONNECTING);
    }

    @Override
    public void onDeviceDisconnected(@NonNull BluetoothDevice device) {
        setUIState(UI_STATE_DISCONNECTED);
    }

    @Override
    public void onLinkLossOccurred(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onServicesDiscovered(@NonNull BluetoothDevice device, boolean optionalServicesFound) {

    }

    @Override
    public void onDeviceReady(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onBondingRequired(@NonNull BluetoothDevice device) {
        device.createBond();
    }

    @Override
    public void onBonded(@NonNull BluetoothDevice device) {
        Snackbar.make(connectButton, "Paired " + device.getName() + " Successfully.", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onBondingFailed(@NonNull BluetoothDevice device) {
        Snackbar.make(connectButton, "Could not pair device: " + device.getName(), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onError(@NonNull BluetoothDevice device, @NonNull String message, int errorCode) {
        Snackbar.make(connectButton, "Error " + String.valueOf(errorCode) + ": " + message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onDeviceNotSupported(@NonNull BluetoothDevice device) {
        Snackbar.make(connectButton, "Device not supported: " + device.getName(), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onFirstNameChanged(BluetoothDevice device, String value) {
        firstNameTextView.setText(value);
    }

    @Override
    public void onLastNameChanged(BluetoothDevice device, String value) {
        lastNameTextView.setText(value);
    }

    @Override
    public void onLedChanged(BluetoothDevice device, int color) {
        Snackbar.make(connectButton, "Color from device: " + Integer.toHexString(color + 0xFF000000), Snackbar.LENGTH_LONG).show();
        colorActual.setBackgroundColor(color + 0xFF000000);
    }

    @Override
    public void onMessageChanged(BluetoothDevice device, String value) {
        messageTextView.setText("");
    }
}

