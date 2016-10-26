package com.ywangwang.yww;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class DeviceFragment extends Fragment {
	private static final String TAG = "ControlFragment";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, TAG + "-->>onCreate");
	}

	private ListView lvDevice;
	private List<Map<String, Object>> listDevice;
	private MySimpleAdapter saDevice;
	SwipeRefreshLayout swipeRefreshLayout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.i(TAG, TAG + "-->>onCreateView");
		View view = inflater.inflate(R.layout.fragment_device, container, false);
		swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
		swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
		swipeRefreshLayout.setOnRefreshListener(refreshListener);

		lvDevice = (ListView) view.findViewById(R.id.lvDevice);
		listDevice = new ArrayList<Map<String, Object>>();
		saDevice = new MySimpleAdapter(getActivity(), listDevice, R.layout.device_list_item, new String[] { "tvDeviceInfo", "tvDeviceStatus" }, new int[] { R.id.tvDeviceInfo, R.id.tvDeviceStatus });
		lvDevice.setAdapter(saDevice);
		lvDevice.setOnItemClickListener(itemClickListener);

		getActivity().registerReceiver(broadcastReceiver, new IntentFilter(GlobalInfo.BROADCAST_ACTION));
		if (null == savedInstanceState && GlobalInfo.client == null) {
			swipeRefreshLayout.post(new Runnable() {
				@Override
				public void run() {
					swipeRefreshLayout.setRefreshing(true);
					getDeviceList();
				}
			});
		}
		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.i(TAG, "onSaveInstanceState");
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.i(TAG, TAG + "-->>onStart");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i(TAG, TAG + "-->>onResume");
		addWaterCodes(GlobalInfo.client);
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i(TAG, TAG + "-->>onPause");
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.i(TAG, TAG + "-->>onStop");
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.i(TAG, TAG + "-->>onDestroyView");
	}

	@Override
	public void onDestroy() {
		getActivity().unregisterReceiver(broadcastReceiver);
		super.onDestroy();
		Log.i(TAG, TAG + "-->>onDestroy");
	}

	@Override
	public void onDetach() {
		super.onDetach();
		Log.i(TAG, TAG + "-->>onDetach");
	}

	OnRefreshListener refreshListener = new OnRefreshListener() {
		@Override
		public void onRefresh() {
			getDeviceList();
		}
	};

	private void getDeviceList() {
		addWaterCodes(null);
		getActivity().sendBroadcast(new Intent(GlobalInfo.BROADCAST_SERVICE_ACTION).putExtra(GlobalInfo.BROADCAST_GET_DEVICE_LIST, true));
	}

	OnItemClickListener itemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
			if (GlobalInfo.client != null) {
				if (GlobalInfo.client.gxjOnline[position] == Client.OFFLINE) {
					String msg = "设备离线，无法远程控制，\n设备编号：" + String.format("%012X", GlobalInfo.client.gxjIds[position]);
					AlertDialog.Builder builer = new Builder(getActivity());
					builer.setTitle("设备信息");
					builer.setMessage(msg);
					builer.setPositiveButton("确定", null);
					builer.show();
				} else {
					startActivity(new Intent(getActivity(), DeviceControl.class).putExtra("deviceId", GlobalInfo.client.gxjIds[position]));
				}
			}
		}
	};

	void addWaterCodes(Client client) {
		listDevice.clear();
		if (client != null && client.gxjIds.length > 0) {
			for (int i = 0; i < client.gxjIds.length; i++) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("tvDeviceInfo", "管线机(" + String.format("%012X", client.gxjIds[i]) + ")");
				if (client.gxjOnline[i] == Client.ONLINE) {
					map.put("tvDeviceStatus", "在线");
				} else {
					map.put("tvDeviceStatus", "离线");
				}
				listDevice.add(map);
			}
		}
		saDevice.notifyDataSetChanged();
	}

	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getExtras().getBoolean(GlobalInfo.BROADCAST_GET_DEVICE_LIST_SUCCESS, false) == true) {
				addWaterCodes(GlobalInfo.client);
				swipeRefreshLayout.setRefreshing(false);
			} else if (intent.getExtras().getBoolean(GlobalInfo.BROADCAST_GET_DEVICE_LIST_FAIL, false) == true) {
				swipeRefreshLayout.setRefreshing(false);
			}
		}
	};

	class MySimpleAdapter extends SimpleAdapter {
		public MySimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
			super(context, data, resource, from, to);
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.device_list_item, null);
			}
			if (GlobalInfo.client != null && GlobalInfo.client.gxjIds.length > position) {
				TextView tv = (TextView) convertView.findViewById(R.id.tvDeviceStatus);
				if (GlobalInfo.client.gxjOnline[position] == true) {
					tv.setTextColor(GlobalInfo.COLOR_GREEN);
				} else {
					tv.setTextColor(GlobalInfo.COLOR_RED);
				}
			}
			return super.getView(position, convertView, parent);
		}
	}
}
