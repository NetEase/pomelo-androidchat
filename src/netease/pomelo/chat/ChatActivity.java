package netease.pomelo.chat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.netease.pomelo.DataCallBack;
import com.netease.pomelo.DataEvent;
import com.netease.pomelo.DataListener;
import com.netease.pomelo.PomeloClient;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class ChatActivity extends Activity implements OnClickListener {

	private ChatApplication chatApp;
	private ListView historyLv;
	private EditText entryTxt;
	private TextView barTv;
	private Button sendBtn;
	private int flag;
	private String from;
	private String target;
	private String aim;
	private String content;
	private PomeloClient client;
	private String rid;
	private String username;
	private ArrayList<Map<String, String>> list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_main);

		historyLv = (ListView) findViewById(R.id.history);
		entryTxt = (EditText) findViewById(R.id.entry);
		barTv = (TextView) findViewById(R.id.bar);
		sendBtn = (Button) findViewById(R.id.send);
		sendBtn.setOnClickListener(this);

		chatApp = (ChatApplication) getApplication();
		client = chatApp.getClient();
		target = chatApp.getUser();
		rid = chatApp.getRid();
		username = chatApp.getUsername();

		barTv.setText("Name: " + username + "  Room: " + rid);

		list = new ArrayList<Map<String, String>>();

		// wait from broadcast message
		client.on("onChat", new DataListener() {
			@Override
			public void receiveData(DataEvent event) {
				JSONObject msg = event.getMessage();
				try {
					from = msg.getString("from");
					aim = msg.getString("target");
					content = msg.getString("msg");
					flag = 1;
				} catch (JSONException e) {
					e.printStackTrace();
				}
				myHandler.sendMessage(myHandler.obtainMessage());
			}

		});

		client.on("onLeave", new DataListener() {
			@Override
			public void receiveData(DataEvent event) {
				JSONObject msg = event.getMessage();
				try {
					String user = msg.getString("user");
					if (target.equals(user))
						leaveHandler.sendMessage(leaveHandler.obtainMessage());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

		});

		client.on("onAdd", new DataListener() {
			@Override
			public void receiveData(DataEvent event) {
				JSONObject msg = event.getMessage();
				try {
					String user = msg.getString("user");
					if (target.equals(user))
						addHandler.sendMessage(addHandler.obtainMessage());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

		});
	}

	protected void dialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage("The target user is not online now.");
		builder.setTitle("Tip");
		builder.setPositiveButton("ok",
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	@Override
	public void onClick(View v) {
		content = entryTxt.getText().toString();
		if (content.equals(""))
			return;
		entryTxt.setText("");
		JSONObject msg = new JSONObject();
		try {
			msg.put("content", content);
			msg.put("target", target);
			msg.put("rid", rid);
			msg.put("from", username);
			client.request("chat.chatHandler.send", msg, new DataCallBack() {
				@Override
				public void responseData(JSONObject msg) {
					flag = 0;
					if (!target.equals("*") && !target.equals(username))
						myHandler.sendMessage(myHandler.obtainMessage());
				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void addMessage(String from, String target, String content,
			String aim, String username, int flag) {
		if (flag == 1) {
			target = aim;
			username = from;
		}
		target = target.equals("*") ? "all" : target;
		String result = username + " says to " + target + ": " + content;
		Map<String, String> map = new HashMap<String, String>();
		map.put("ChatContent", result);
		map.put("DateContent", getDate());
		list.add(map);
		SimpleAdapter mSchedule = new SimpleAdapter(this, list,
				R.layout.chat_item,
				new String[] { "ChatContent", "DateContent" }, new int[] {
						R.id.ChatContent, R.id.DateContent });
		historyLv.setAdapter(mSchedule);
	}

	private String getDate() {
		Date now = new Date(System.currentTimeMillis());
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		String nowStr = format.format(now);
		return nowStr;
	}

	Handler myHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			addMessage(from, target, content, aim, username, flag);
		};
	};

	Handler addHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			sendBtn.setEnabled(true);
		};
	};

	Handler leaveHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			dialog();
			sendBtn.setEnabled(false);
		};
	};
}
