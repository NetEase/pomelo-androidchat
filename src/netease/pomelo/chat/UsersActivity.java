package netease.pomelo.chat;

import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.netease.pomelo.DataCallBack;
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
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class UsersActivity extends Activity implements OnItemClickListener,
		OnClickListener {

	private PomeloClient client;
	private String[] users;
	private String rid;
	private Button refresh;
	private ChatApplication chatApp;
	private ListView list;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.users_list);

		list = (ListView) findViewById(R.id.userList);
		refresh = (Button) findViewById(R.id.refresh);
		refresh.setOnClickListener(this);

		// get parameters
		Bundle bundle = this.getIntent().getExtras();
		users = bundle.getStringArray("users");

		chatApp = (ChatApplication) getApplication();
		client = chatApp.getClient();
		rid = chatApp.getRid();

		updateUsers(users);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int index, long arg3) {
		chatApp.setUser(users[index]);
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

	@Override
	public void onClick(View v) {
		JSONObject msg = new JSONObject();
		try {
			msg.put("rid", rid);
		} catch (JSONException ex) {
			ex.printStackTrace();
		}
		client.request("chat.chatHandler.getUsers", msg, new DataCallBack() {
			@Override
			public void responseData(JSONObject msg) {
				JSONArray jr;
				try {
					jr = msg.getJSONArray("users");
					users = new String[jr.length() + 1];
					users[0] = "*";
					for (int i = 1; i <= jr.length(); i++) {
						users[i] = jr.getString(i - 1);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				myHandler.sendMessage(myHandler.obtainMessage());
			}
		});
	}

	private void updateUsers(String[] users) {
		ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> map;
		for (String user : users) {
			map = new HashMap<String, String>();
			if (user.equals("*"))
				map.put("ItemTitle", "all");
			else
				map.put("ItemTitle", user);
			map.put("ItemText", "online user");
			mylist.add(map);
		}
		SimpleAdapter mSchedule = new SimpleAdapter(this, mylist,
				R.layout.list_item, new String[] { "ItemTitle", "ItemText" },
				new int[] { R.id.ItemTitle, R.id.ItemText });
		list.setAdapter(mSchedule);
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
			updateUsers(users);
		};
	};
}
