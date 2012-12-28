package netease.pomelo.chat;

import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;
import com.netease.pomelo.DataEvent;
import com.netease.pomelo.DataListener;
import com.netease.pomelo.PomeloClient;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class UsersActivity extends Activity implements OnItemClickListener {

	private PomeloClient client;
	private SimpleAdapter adapter;
	private String[] users;
	private ChatApplication chatApp;
	private ListView list;
	private ArrayList<HashMap<String, String>> userlist;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.users_list);

		list = (ListView) findViewById(R.id.userList);
		chatApp = (ChatApplication) getApplication();
		client = chatApp.getClient();

		// get parameters
		Bundle bundle = this.getIntent().getExtras();
		users = bundle.getStringArray("users");

		initUserList();
		onUpdateUserList();
	}

	private void onUpdateUserList() {
		client.on("onAdd", new DataListener() {
			@Override
			public void receiveData(DataEvent event) {
				updateItem(event, 0);
			}
		});
		client.on("onLeave", new DataListener() {
			@Override
			public void receiveData(DataEvent event) {
				updateItem(event, -1);
			}
		});
	}

	private void updateItem(DataEvent event, int flag) {
		JSONObject msg = event.getMessage();
		try {
			String user = msg.getString("user");
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("ItemTitle", user);
			map.put("ItemText", "online user");
			if (flag == 0)
				userlist.add(map);
			else
				userlist.remove(map);
			myHandler.sendMessage(myHandler.obtainMessage());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> view, View v, int index, long lg) {
		TextView item = (TextView) v.findViewById(R.id.ItemTitle);
		String user = item.getText().toString();
		chatApp.setUser(user.equals("all") ? "*" : user);
		Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
		startActivity(intent);
	}

	protected void dialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage("Are you sure to quit?");
		builder.setTitle("Tip");
		builder.setPositiveButton("ok",
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						finish();
					}
				});
		builder.setNegativeButton("cancel",
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			dialog();
			return false;
		}
		return false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		System.exit(0);
	}

	private void initUserList() {
		userlist = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> map;
		for (String user : users) {
			map = new HashMap<String, String>();
			if (user.equals("*"))
				map.put("ItemTitle", "all");
			else
				map.put("ItemTitle", user);
			map.put("ItemText", "online user");
			userlist.add(map);
		}
		adapter = new SimpleAdapter(this, userlist, R.layout.list_item,
				new String[] { "ItemTitle", "ItemText" }, new int[] {
						R.id.ItemTitle, R.id.ItemText });
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int item_id = item.getItemId();
		switch (item_id) {
		case R.id.exit: {
			this.finish();
			System.exit(0);
		}
		}
		return true;
	}

	Handler myHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			adapter.notifyDataSetChanged();
		};
	};
}
