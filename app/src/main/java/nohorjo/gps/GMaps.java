package nohorjo.gps;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class GMaps {
	public static void open(String lat_long, Context c) {
		String uri = "geo:" + lat_long.replace(' ', ',');
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
		c.startActivity(intent);
	}
}
