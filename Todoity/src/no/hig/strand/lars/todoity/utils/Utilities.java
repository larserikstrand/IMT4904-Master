package no.hig.strand.lars.todoity.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;


public class Utilities {
	
	private static final String LOG_TAG = "Todoity.NewTaskActivity";
	private static final String PLACES_API_BASE =
			"https://maps.googleapis.com/maps/api/place";
	private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
	private static final String OUT_JSON = "/json";
	
	private static final String API_KEY = 
			"AIzaSyCWS6Qt_QtgfD-aOst-TYV__hUi-5hVxVY";
	
	
	public Utilities() {}
	
	
	public static ArrayList<String> autocomplete(String input) {
	    ArrayList<String> resultList = null;
	    HttpURLConnection conn = null;
	    StringBuilder jsonResults = new StringBuilder();
	    try {
	        StringBuilder sb = new StringBuilder(
	        		PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
	        sb.append("?sensor=false&key=" + API_KEY);
	        sb.append("&input=" + URLEncoder.encode(input, "utf8"));

	        URL url = new URL(sb.toString());
	        conn = (HttpURLConnection) url.openConnection();
	        InputStreamReader in = new InputStreamReader(conn.getInputStream());

	        // Load the results into a StringBuilder
	        int read;
	        char[] buff = new char[1024];
	        while ((read = in.read(buff)) != -1) {
	            jsonResults.append(buff, 0, read);
	        }
	    } catch (MalformedURLException e) {
	        Log.e(LOG_TAG, "Error processing Places API URL", e);
	        return resultList;
	    } catch (IOException e) {
	        Log.e(LOG_TAG, "Error connecting to Places API", e);
	        return resultList;
	    } finally {
	        if (conn != null) {
	            conn.disconnect();
	        }
	    }

	    try {
	        // Create a JSON object hierarchy from the results
	        JSONObject jsonObj = new JSONObject(jsonResults.toString());
	        JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

	        // Extract the Place descriptions from the results
	        resultList = new ArrayList<String>(predsJsonArray.length());
	        for (int i = 0; i < predsJsonArray.length(); i++) {
	            resultList.add(predsJsonArray
	            		.getJSONObject(i).getString("description"));
	        }
	    } catch (JSONException e) {
	        Log.e(LOG_TAG, "Cannot process JSON results", e);
	    }

	    return resultList;
	}
	
	
	
	@SuppressLint("SimpleDateFormat")
	public static String getTodayDate() {
		SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMM dd, yyyy");
		Calendar c = new GregorianCalendar();
		
		return formatter.format(c.getTime());
	}
	
	
	
	@SuppressLint("SimpleDateFormat")
	public static String timeToString(long millis) {
		Date d = new Date(millis);
		SimpleDateFormat formatter = new SimpleDateFormat(
				"EEEE, MMM dd, yyyy, HH:mm");
		
		return formatter.format(d.getTime());
	}
	
	
	
	@SuppressLint("SimpleDateFormat")
	public static long getTimeInMillis(String date) {
		long dateInMillis = 0;
		SimpleDateFormat formatter = 
				new SimpleDateFormat("EEEE, MMM dd, yyyy");
		formatter.setLenient(false);
		try {
			Date d = formatter.parse(date);
			dateInMillis = d.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		return dateInMillis;
	}
	
	
	
	@SuppressLint("SimpleDateFormat")
	public static long getTimeOfDay(long time) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return time - c.getTimeInMillis();
	}
	
	
	
	/**
	 * Converts time of day from String format (HH:mm) to millisecond long.
	 * @param time - Time string to be converted.
	 * @return time in milliseconds as a long value.
	 */
	@SuppressLint("SimpleDateFormat")
	public static long getTimeOfDay(String time) {
		Calendar c = Calendar.getInstance();
		String times[] = time.split(":");
		c.set(0, 0, 0, Integer.valueOf(times[0]), Integer.valueOf(times[0]), 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTimeInMillis();
	}
	
	
	
	public static int getDayOfWeek(long time) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		return c.get(Calendar.DAY_OF_WEEK);
	}
	
	
	
	@SuppressLint("SimpleDateFormat")
	public static class DateComparator implements Comparator<String> {
		@Override
		public int compare(String lhs, String rhs) {
			SimpleDateFormat formatter = 
					new SimpleDateFormat("EEEE, MMM dd, yyyy");
			formatter.setLenient(false);
			Date date1 = null, date2 = null;
			try {
				date1 = formatter.parse(lhs);
				date2 = formatter.parse(rhs);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return date1.compareTo(date2);
		}
	}
	
	
	
	public interface ConfirmDialogListener {
		public abstract void PositiveClick(DialogInterface dialog, int id);
	}
	
	
	
	public static void showConfirmDialog(Context context, String title,
			String msg, final ConfirmDialogListener target) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title).setMessage(msg);
		builder.setPositiveButton(android.R.string.yes, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				target.PositiveClick(dialog, which);
			}
		});
		builder.setNegativeButton(android.R.string.no, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();	
			}
		});
		
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	
	
	public static class ErrorDialogFragment extends DialogFragment {
		private Dialog mDialog;
		
		
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}
		
		
		
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}



		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
		
	}
	
	
	public static class Installation {
	    private static String sID = null;
	    private static final String INSTALLATION = "INSTALLATION";

	    public synchronized static String id(Context context) {
	        if (sID == null) {  
	            File installation = new File(context.getFilesDir(), INSTALLATION);
	            try {
	                if (!installation.exists())
	                    writeInstallationFile(installation);
	                sID = readInstallationFile(installation);
	            } catch (Exception e) {
	                throw new RuntimeException(e);
	            }
	        }
	        return sID;
	    }

	    private static String readInstallationFile(File installation) throws IOException {
	        RandomAccessFile f = new RandomAccessFile(installation, "r");
	        byte[] bytes = new byte[(int) f.length()];
	        f.readFully(bytes);
	        f.close();
	        return new String(bytes);
	    }

	    private static void writeInstallationFile(File installation) throws IOException {
	        FileOutputStream out = new FileOutputStream(installation);
	        String id = UUID.randomUUID().toString();
	        out.write(id.getBytes());
	        out.close();
	    }
	}
	
}
