package com.github.jtcressy.icpcbadgetool;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

import static android.content.Context.BLUETOOTH_SERVICE;

public class BleScanDialogFragment extends DialogFragment {
    private BleScanDialogResult mCallback;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mScanner;
    private Handler mHandler;
    // Device scan callback.
    private ScanCallback mLeScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    BluetoothDevice device = result.getDevice();
                    mHandler.post(() -> {
                        final String devname = device.getName();
                        if (devname != null) {
                            Log.d("DEVNAME", devname);
                            if (devname.toLowerCase().contains(getString(R.string.device_name_filter))) {
                                mLeDeviceListAdapter.addDevice(device);
                                mLeDeviceListAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            };

    public static BleScanDialogFragment newInstance(String title) {
        BleScanDialogFragment frag = new BleScanDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onDestroyView() {
        scanLeDevice(false);
        super.onDestroyView();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (BleScanDialogResult) getActivity();
        } catch (ClassCastException e) {
            Log.d("BleScanDialog", "Activity doesn't implement the BleScanDialogResult interface");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ble_scan_fragment_dialog, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BluetoothManager bluetoothManager = (BluetoothManager) Objects.requireNonNull(getActivity()).getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mHandler = new Handler();
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        ListView mListView = view.findViewById(R.id.ble_device_listview);
        mListView.setAdapter(mLeDeviceListAdapter);
        mListView.setOnItemClickListener((parent, view1, position, id) -> {
            BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
            mCallback.onSelectedDevice(device);
            dismiss();
        });
        // Get field from view
        // Fetch arguments from bundle and set title
        String title = Objects.requireNonNull(getArguments()).getString("title", "Enter Name");
        getDialog().setTitle(title);
        Objects.requireNonNull(getDialog().getWindow()).setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        scanLeDevice(true);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        scanLeDevice(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        scanLeDevice(false);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mScanner = mBluetoothAdapter.getBluetoothLeScanner();

            // Filter scans manually in the OnScanResult callback,
            // by checking if device name matches a pattern.
            mScanner.startScan(mLeScanCallback);
            Log.d("BleScanFragment", "Started Scan");
        } else {
            mScanner.stopScan(mLeScanCallback);
            Log.d("BleScanFragment", "Stopped Scan");
        }
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<>();
            mInflator = LayoutInflater.from(getContext());
        }

        void addDevice(BluetoothDevice device) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.ble_device_listitem, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = view.findViewById(R.id.device_address);
                viewHolder.deviceName = view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(getString(R.string.device_default_name));
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }
}
