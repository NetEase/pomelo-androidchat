package netease.pomelo.chat;


import com.netease.pomelo.PomeloClient;
import android.app.Application;

public class ChatApplication extends Application {

	private PomeloClient client;
	private String user;
	private String username;
	private String rid;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	public PomeloClient getClient() {
		return client;
	}

	public void setClient(PomeloClient client) {
		this.client = client;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getRid() {
		return rid;
	}

	public void setRid(String rid) {
		this.rid = rid;
	}

}
