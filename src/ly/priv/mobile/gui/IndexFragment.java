package ly.priv.mobile.gui;

import ly.priv.mobile.ConstantValues;
import ly.priv.mobile.JsObject;
import ly.priv.mobile.R;
import ly.priv.mobile.Values;
import ly.priv.mobile.VerifyAuthToken;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * Displays the Index application for a user after authentication.
 * 
 * @author Gitanshu Sardana
 */

public class IndexFragment extends SherlockFragment {
	private static final String TAG = "Index";
	private WebView mWebViewIndex;

	public IndexFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		setHasOptionsMenu(true);
		View view = inflater.inflate(R.layout.new_post, container, false);
		ActionBar actionBar = getSherlockActivity().getSupportActionBar();
		actionBar.setTitle(R.string.index);
		container.removeAllViews();
		mWebViewIndex = (WebView) view.findViewById(R.id.webview_new_post);
		mWebViewIndex.getSettings().setJavaScriptEnabled(true);
		
		Boolean isRedirected = null;
		try {
			isRedirected = getArguments().getBoolean(
					ConstantValues.IS_REDIRECTED);
			loadIndex();
		} catch (NullPointerException e) {
			Log.d(TAG, "" + isRedirected);
			Values values = new Values(getActivity());
			// Checks if the User has already been verified at the Login Screen.
			// If yes, prevents re authentication. If not, creates and executes
			// a VerifyAuthToken task.
			if (!values.isUserVerifiedAtLogin()) {
				VerifyAuthToken task = new VerifyAuthToken(getActivity());
				task.execute(values.getContentServerDomain()
						+ "/token_authentications.json");
			} else
				values.setUserVerifiedAtLogin(false);
			loadIndex();
		}
		return view;
	}

	@SuppressLint("NewApi")
	void loadIndex() {
		mWebViewIndex.addJavascriptInterface(new JsObject(getActivity()),
				"androidJsBridge");

		if (Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
			mWebViewIndex.getSettings().setAllowUniversalAccessFromFileURLs(
					true);
		}
		// Logs all Js Console messages on the logcat.
		mWebViewIndex.setWebChromeClient(new WebChromeClient() {
			@Override
			public boolean onConsoleMessage(ConsoleMessage cm) {
				Log.d(TAG, cm.message() + " -- From line " + cm.lineNumber()
						+ " of " + cm.sourceId());
				return true;
			}
		});
		mWebViewIndex.setWebViewClient(new WebViewClient() {  
		    @Override  
		    public void onPageFinished(WebView view, String url)  
		    {  
		        /* This call inject JavaScript into the page which just finished loading. */  
		    	mWebViewIndex.loadUrl("javascript: var elem = window.document.getElementById('wrap').getElementsByTagName('div')[0];" +
		    			"var pTop = elem.clientHeight;" +
		    			"elem.parentNode.removeChild(elem);" +
		    			"window.document.getElementById('wrap').getElementsByClassName('container')[0].style.margin=-pTop+'px 0px 0px 0px';");  
		    }  
		});  	
		mWebViewIndex
				.loadUrl("file:///android_asset/PrivlyApplications/Index/new.html");
		
	}

	/**
	 * Inflate options menu with the layout
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.layout.menu_layout_home, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	/**
	 * Item click listener for options menu.
	 * <p>
	 * Redirect to {@link ly.priv.mobile.gui.SettingsActivity} Or
	 * {@link ly.priv.mobile.gui.LoginActivity}
	 * </p>
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.settings:
			Intent gotoSettings = new Intent(getActivity(),
					SettingsActivity.class);
			startActivity(gotoSettings);
			return true;

		case R.id.logout:
			// Logs out User from Privly Application
			Values values = new Values(getActivity());
			values.setAuthToken(null);
			values.setRememberMe(false);
			Intent gotoLogin = new Intent(getActivity(), LoginActivity.class);
			gotoLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(gotoLogin);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
