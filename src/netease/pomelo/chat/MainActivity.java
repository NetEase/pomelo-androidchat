package netease.pomelo.chat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.netease.pomelo.DataCallBack;
import com.netease.pomelo.PomeloClient;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {

	private TextView errorTv;
	private EditText nameTxt;
	private EditText channelTxt;
	private Button loginBtn;
	public PomeloClient client;
	private String name;
	private String channel;
	private String[] users;
	private ChatApplication chatApp;
	private String reg = "^[a-zA-Z0-9_\u4e00-\u9fa5]{1,10}$";
	private String test_host = "127.0.0.1";
	private int test_port = 3014;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		nameTxt = (EditText) findViewById(R.id.name);
		channelTxt = (EditText) findViewById(R.id.channel);
		errorTv = (TextView) findViewById(R.id.error);
		loginBtn = (Button) findViewById(R.id.login);
		loginBtn.setOnClickListener(this);

		chatApp = (ChatApplication) getApplication();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		name = nameTxt.getText().toString();
		channel = channelTxt.getText().toString();
		if (!check(name) || !check(channel)) {
			errorTv.setText("Name/Channel is not legal.");
			return;
		}
		client = new PomeloClient(test_host, test_port);
		client.init();
		queryEntry();
	}

	private boolean check(String str) {
		boolean tem = false;
		Pattern pattern = Pattern.compile(reg);
		Matcher matcher = pattern.matcher(str);
		tem = matcher.matches();
		return tem;
	}

	private void queryEntry() {
		JSONObject msg = new JSONObject();
		try {
			msg.put("uid", name);
			client.request("gate.gateHandler.queryEntry", msg,
					new DataCallBack() {
						@Override
						public void responseData(JSONObject msg) {
							client.disconnect();
							try {
								String ip = msg.getString("host");
								enter(ip, msg.getInt("port"));
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					});
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void enter(String host, int port) {
		JSONObject msg = new JSONObject();
		try {
			msg.put("username", name);
			msg.put("rid", channel);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		client = new PomeloClient(host, port);
		client.init();
		client.request("connector.entryHandler.enter", msg, new DataCallBack() {
			@Override
			public void responseData(JSONObject msg) {
				if (msg.has("error")) {
					myHandler.sendMessage(myHandler.obtainMessage());
					client.disconnect();
					client = new PomeloClient(test_host, test_port);
					client.init();
					return;
				}
				try {
					JSONArray jr = msg.getJSONArray("users");
					users = new String[jr.length() + 1];
					// * represent all users
					users[0] = "*";
					for (int i = 1; i <= jr.length(); i++) {
						users[i] = jr.getString(i - 1);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				chatApp.setUsername(name);
				chatApp.setRid(channel);
				chatApp.setClient(client);
				Bundle bundle = new Bundle();
				bundle.putStringArray("users", users);
				forwardPage(bundle);
				finish();
			}
		});
	}

	private void forwardPage(Bundle bundle) {
		Intent intent = new Intent();
		intent.setClass(MainActivity.this, UsersActivity.class);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int item_id = item.getItemId();
		switch (item_id) {
		case R.id.exit: {
			super.finish();
			System.exit(0);
		}
		}
		return true;
	}

	Handler myHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			errorTv.setText("please change your name to login.");
		};
	};
}
