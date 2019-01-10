package id.co.telkom.ippd.add_on;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.net.Uri;

import java.net.MalformedURLException;
import java.net.URL;
import net.sunniwell.app.ott.huawei.service.IPTV;

import com.huawei.iptv.stb.fordataaccess.IForDataAccess;

import java.util.Map;

import id.co.telkom.ippd.stbinterface.TelkomSTB;
import ztestb.iptv.aidl.ServiceIPTVAidl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Deky's on 8/10/2018.
 */

public class wifiID extends AppCompatActivity {

    //var indihome ID
    private ServiceIPTVAidl zteIptvService = null;
    private IPTV huaweiIptvService = null;
    private IForDataAccess huaweiIptvService2 = null;
    private TelkomSTB telkomSTB = null;
    Map<String, String> iptvIdentity = new ArrayMap<>();
    private boolean useZTE = false;
    private boolean useHuawei = false;
    private boolean useHuawei2 = false;
    private boolean useSTBTelkom = false;
    private boolean useFiberhome = false;
    public String id_ih;
    public String vendor;

    //var url parameter
    //public String LOAD_BASE_URL = "http://10.0.8.56/addon/tvStorage";
    public String LOAD_BASE_URL = "http://10.0.8.58/addon/tvStorage";

    final String ID_IH = "indihome_id";
    final String Source = "source";
    Uri builtUri;

    //var version application
    String linkAptoide = "http://ws75.aptoide.com/api/7/getApp/store_name=useeapps/package_name=";
    String currentVersion = BuildConfig.VERSION_NAME;
    String updateVersion;

    //var activity main xml
    private WebView mWebView;
    private TextView urlText;
    private ProgressBar progressBar;
    private TextView versionID;

    private WebView mywebview;
    private Button btnsend;
    private EditText editText;

    public wifiID() throws MalformedURLException {
    }
    private JavaScriptInterFace javaScriptInterFace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebView = (WebView) findViewById(R.id.webview);
        progressBar  = (ProgressBar) findViewById(R.id.progressBar);
        versionID = (TextView) findViewById(R.id.versionID);
        versionID.setText("Versi Aplikasi : " + currentVersion);
        new ProgressTask().execute(0);
    }

    private class ProgressTask extends AsyncTask<Integer,Integer,Void>{

        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setMax(100);
            new VersionCheck().execute();
        }

        protected Void doInBackground(Integer... params) {
            int start=params[0];
            for(int i=start;i<=100;i+=25){
                progressBar.setProgress(i);
                SystemClock.sleep(500);
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            loadAIDL();
        }
    }

    private class VersionCheck extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            String final_url = linkAptoide+BuildConfig.APPLICATION_ID;
            String jsonStr = sh.makeServiceCall(final_url);

            if (jsonStr == null){
                Log.d("NotifService", "Couldn't get json from server. Check LogCat for possible errors!");
                updateVersion=currentVersion;
            }

            else if (jsonStr.equals("error")){
                Log.d("NotifService", "URL Not Valid");
                updateVersion=currentVersion;
            }
            else{
                try {
                    JSONObject parentObject = new JSONObject(jsonStr);
                    JSONObject parent2object = parentObject.getJSONObject("nodes");
                    JSONObject parent3object = parent2object.getJSONObject("versions");
                    JSONArray parent4Array = parent3object.getJSONArray("list");
                    JSONObject array5Object = parent4Array.getJSONObject(0);
                    JSONObject parent6object = array5Object.getJSONObject("file");
                    updateVersion = parent6object.getString("vername");
                }
                catch (final JSONException e) {
                    // Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Toast.makeText(getApplicationContext(),"Json parsing error: " + e.getMessage(),Toast.LENGTH_LONG).show();
                            Log.d("NotifService", "JSON Parsing Error");
                        }
                    });
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (Float.parseFloat(currentVersion) == Float.parseFloat(updateVersion)){
            }
            else if (Float.parseFloat(currentVersion) < Float.parseFloat(updateVersion))
            {
                final Dialog dialogUpdate = new Dialog(wifiID.this);
                dialogUpdate.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogUpdate.setContentView(R.layout.dialogupdate);
                dialogUpdate.setCancelable(false);
                TextView dialogExitText = (TextView) dialogUpdate.findViewById(R.id.textdialog3);
                dialogExitText.setText("Silahkan perbarui aplikasi anda");
                Button dialogButtonUpdate = (Button) dialogUpdate.findViewById(R.id.buttonUpdate);
                dialogButtonUpdate.setFocusable(true);
                dialogButtonUpdate.requestFocus();
                dialogButtonUpdate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("aptoidetv://cm.aptoidetv.pt.useeapps/appview?package=" + BuildConfig.APPLICATION_ID));
                        startActivity(intent); }
                });
                Button dialogButtonExit = (Button) dialogUpdate.findViewById(R.id.buttonClose);
                dialogButtonExit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogUpdate.dismiss();
                    }
                });
                dialogUpdate.show();
            }
        }
    }


    private void loadAIDL () {  //FUNGSI Choose AIDL
        Log.d("NotifService", "loading AIDL");
        if (getFiberhomeApiIndihomeId() != null && !getFiberhomeApiIndihomeId().isEmpty()){
            useFiberhome = true;
        } else if(getIndihomeIDZTE()){
            Log.d("NotifService", "Your Using ZTE");
            useZTE = true;
        } else if(getIndihomeIDHuawei()){
            useHuawei = true;
        } else if(getIndihomeIDHuawei2()){
            useHuawei2 = true;
        }
        else if(loadSTBIndihomeAIDL()){
            useSTBTelkom = true;
        }
        else {
            Log.d("NotifService", "IndihomeApi not exists");
            Log.d("NotifService", "connect on loadAIDL");
        }
    }

    private boolean getIndihomeIDZTE(){
        if (zteIptvService != null){
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            try{
                Intent intent = new Intent(ServiceIPTVAidl.class.getName());
                intent.setPackage("com.itv.android.iptv");

                boolean retval = bindService(intent, iptvServiceConnection, BIND_AUTO_CREATE);

                if (!retval){
                    Intent intent2 = new Intent("ztestb.iptv.aidl.ServiceIPTVAidl");
                    intent2.setPackage("ztestb.iptv.aidl");
                    retval = bindService(intent2, iptvServiceConnection, BIND_AUTO_CREATE);
                }
                return retval;
            }catch (IllegalArgumentException ex){
                ex.printStackTrace();
            }
        }else{
            try{
                return bindService(new Intent(ztestb.iptv.aidl.ServiceIPTVAidl.class.getName()), iptvServiceConnection, BIND_AUTO_CREATE);
            }catch (IllegalArgumentException ex){
                ex.printStackTrace();
            }
        }
        return false;
    }

    private boolean getIndihomeIDHuawei(){
        if (huaweiIptvService != null){
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            try{
                Intent intent = new Intent("net.sunniwell.app.ott.huawei.service.IPTV");
                intent.setPackage("net.sunniwell.app.ott.huawei.service");
                boolean retval = bindService(intent, iptvServiceConnection, BIND_AUTO_CREATE);
                return retval;
            }catch (IllegalArgumentException ex){
                ex.printStackTrace();
            }
        }else{
            try{
                return bindService(new Intent("net.sunniwell.app.ott.huawei.service.remote"), iptvServiceConnection, BIND_AUTO_CREATE);
            }catch (IllegalArgumentException ex){
                ex.printStackTrace();
            }
        }
        return false;
    }

    private boolean getIndihomeIDHuawei2(){
        if (huaweiIptvService != null){
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            try{
                Intent intent = new Intent();
                intent.setClassName("com.huawei.iptv.stb","com.huawei.iptv.stb.fordataaccess.ForDataAccess");
                boolean retval = bindService(intent, iptvServiceConnection, BIND_AUTO_CREATE);
                return retval;
            }catch (IllegalArgumentException ex){
                ex.printStackTrace();
            }
        }else{
            try{
                Intent intent = new Intent();
                intent.setClassName("com.huawei.iptv.stb","com.huawei.iptv.stb.fordataaccess.ForDataAccess");
                return bindService(intent, iptvServiceConnection, BIND_AUTO_CREATE);
            }catch (IllegalArgumentException ex){
                ex.printStackTrace();
            }
        }
        return false;
    }

    private boolean loadSTBIndihomeAIDL(){
        if (telkomSTB != null){
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            try{
                Log.d("NotifService", "loading STB Indihome AIDL");
                Intent intent = new Intent(id.co.telkom.ippd.stbinterface.TelkomSTB.class.getName());
                intent.setPackage("id.co.inovasiriset.tvms.stbinterface.implementation");
                boolean retval = bindService(intent, iptvServiceConnection, BIND_AUTO_CREATE);
                return retval;
            }catch (IllegalArgumentException ex){
                ex.printStackTrace();
            }
        }else{
            try{
                bindService(new Intent(id.co.telkom.ippd.stbinterface.TelkomSTB.class.getName()), iptvServiceConnection, BIND_AUTO_CREATE);
            }catch (IllegalArgumentException ex){
                ex.printStackTrace();
            }
        }
        return false;
    }

    private String getFiberhomeApiIndihomeId(){
        try{
            String fiberhomeIndihomeId = SystemPropertiesProxy.get(getApplicationContext(),"sys.Indihome.username");

            if (fiberhomeIndihomeId != null) {
                id_ih = SystemPropertiesProxy.get(getApplicationContext(), "sys.Indihome.username");
                vendor = "fiberhome";
                builtUri = Uri.parse(LOAD_BASE_URL).buildUpon()
                        .appendQueryParameter(ID_IH, id_ih)
                        .appendQueryParameter(Source, vendor)
                        .build();
                callUrl(builtUri);

            }
            else{
                return null;
            }
            return fiberhomeIndihomeId;
        }catch (Exception e){
            return null;
        }
    }

    private ServiceConnection iptvServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d("NotifService", "component name: " + componentName.flattenToString());
            if (iBinder == null){
                Log.d("NotifService", "STB IBinder == null");
            }

            if (useZTE){
                zteIptvService = ServiceIPTVAidl.Stub.asInterface(iBinder);
                try {
                    id_ih = zteIptvService.getIPTVPlatFormUser();
                    vendor = "zte";
                    builtUri = Uri.parse(LOAD_BASE_URL).buildUpon()
                            .appendQueryParameter(ID_IH, id_ih)
                            .appendQueryParameter(Source, vendor)
                            .build();
                    callUrl(builtUri);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }else if (useHuawei){
                huaweiIptvService = IPTV.Stub.asInterface(iBinder);
                try {
                    id_ih = huaweiIptvService.getParam("ntvuseraccount", 0);
                    vendor = "huawei";
                    builtUri = Uri.parse(LOAD_BASE_URL).buildUpon()
                            .appendQueryParameter(ID_IH, id_ih)
                            .appendQueryParameter(Source, vendor)
                            .build();
                    callUrl(builtUri);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }else if (useHuawei2){
                huaweiIptvService2 = IForDataAccess.Stub.asInterface(iBinder);
                try {
                    id_ih = huaweiIptvService2.getValue("Iptv.AccountID");
                    vendor = "huawei2";
                    builtUri = Uri.parse(LOAD_BASE_URL).buildUpon()
                            .appendQueryParameter(ID_IH, id_ih)
                            .appendQueryParameter(Source, vendor)
                            .build();
                    callUrl(builtUri);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }else if (useSTBTelkom){
                telkomSTB = TelkomSTB.Stub.asInterface(iBinder);
                try {
                    id_ih = telkomSTB.getIndihomeId();
                    vendor = "STBTelkom";
                    builtUri = Uri.parse(LOAD_BASE_URL).buildUpon()
                            .appendQueryParameter(ID_IH, id_ih)
                            .appendQueryParameter(Source, vendor)
                            .build();
                    callUrl(builtUri);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            zteIptvService = null;
            huaweiIptvService = null;
            huaweiIptvService2 = null;
            telkomSTB = null;
        }
    };

    @Override
    public void onBackPressed () {

        String title= mWebView.getTitle();

        int pemisah = title.indexOf("-");
        String back = title.substring(pemisah+1, title.length());
        //Toast.makeText(getApplicationContext(),title, Toast.LENGTH_LONG).show();
//==================================================================
        //Start on Pressed Code
        //==================================================================
        //Home
        //==================================================================
        if(title.equals("home1")||title.equals("home2")){
            final Dialog dialogExit = new Dialog(wifiID.this);
            dialogExit.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogExit.setContentView(R.layout.dialogexit);
            dialogExit.setCancelable(true);
            TextView dialogExitText = (TextView) dialogExit.findViewById(R.id.textdialog);
            dialogExitText.setText("Apakah anda yakin ingin keluar dari aplikasi ?");
            Button dialogButtonOk = (Button) dialogExit.findViewById(R.id.buttonOK);
            dialogButtonOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    wifiID.this.finish();
                    System.exit(0);
                }
            });

            Button dialogButtonCancel = (Button) dialogExit.findViewById(R.id.buttonCancel);
            dialogButtonCancel.setFocusable(true);
            dialogButtonCancel.requestFocus();
            dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogExit.dismiss();
                }
            });
            dialogExit.show();
        }

        //==================================================================
        //First Page
        //==================================================================

        else if(back.equals("home1")){
            builtUri = Uri.parse("http://10.0.8.56/addon/").buildUpon()
                    .appendQueryParameter(ID_IH, id_ih)
                    .appendQueryParameter(Source, vendor)
                    .build();
            mWebView.loadUrl(builtUri.toString());
        }

        else if(back.equals("home2")){
            builtUri = Uri.parse("http://10.0.8.57/addon/").buildUpon()
                    .appendQueryParameter(ID_IH, id_ih)
                    .appendQueryParameter(Source, vendor)
                    .build();
            mWebView.loadUrl(builtUri.toString());
        }
        //==================================================================
        //Detail Flow
        //==================================================================

        else if(title.equals("detilPembelian-"+back)){
            builtUri = Uri.parse(LOAD_BASE_URL+back).buildUpon()
                    .appendQueryParameter(ID_IH, id_ih)
                    .appendQueryParameter(Source, vendor)
                    .build();
            mWebView.loadUrl(builtUri.toString());
        }
        else if(title.equals("verifikasiOtp-"+back)){
            builtUri = Uri.parse(LOAD_BASE_URL+back).buildUpon()
                    .appendQueryParameter(ID_IH, id_ih)
                    .appendQueryParameter(Source, vendor)
                    .build();
            mWebView.loadUrl(builtUri.toString());
        }
        else if(title.equals("completePembelian-"+back)){
            builtUri = Uri.parse(LOAD_BASE_URL+back).buildUpon()
                    .appendQueryParameter(ID_IH, id_ih)
                    .appendQueryParameter(Source, vendor)
                    .build();
            mWebView.loadUrl(builtUri.toString());
        }
        else if(title.equals("gantiNomor-"+back)){
            builtUri = Uri.parse(LOAD_BASE_URL+back).buildUpon()
                    .appendQueryParameter(ID_IH, id_ih)
                    .appendQueryParameter(Source, vendor)
                    .build();
            mWebView.loadUrl(builtUri.toString());
        }
        else if(title.equals("error-"+back)){
            builtUri = Uri.parse(LOAD_BASE_URL+back).buildUpon()
                    .appendQueryParameter(ID_IH, id_ih)
                    .appendQueryParameter(Source, vendor)
                    .build();
            mWebView.loadUrl(builtUri.toString());
        }
        else if(title.equals("sukses-"+back)){
            builtUri = Uri.parse(LOAD_BASE_URL+back).buildUpon()
                    .appendQueryParameter(ID_IH, id_ih)
                    .appendQueryParameter(Source, vendor)
                    .build();
            mWebView.loadUrl(builtUri.toString());
        }
        //==================================================================
        //Back Modal
        //==================================================================
        else if (title.equals("wifiid-home-modal")){
            mWebView.loadUrl("javascript:backcloseModal();");
        }
        //==================================================================
        //Back Else Condition To Home Page
        //==================================================================
        else{
            builtUri = Uri.parse(LOAD_BASE_URL).buildUpon()
                    .appendQueryParameter(ID_IH, id_ih)
                    .appendQueryParameter(Source, vendor)
                    .build();
            mWebView.loadUrl(builtUri.toString());

        }

        //==================================================================
        //Back Usually
        //==================================================================
        /*
        else if (mWebView.isFocused() && mWebView.canGoBack()) {
            mWebView.goBack();
        }
        */
        //==================================================================
        //End on Pressed Code
        //==================================================================

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method firs
        System.exit(0);
    }

    private void callUrl (Uri uri) { //call URL
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true);
            }

            URL url = new URL(uri.toString());
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.getSettings().setAppCacheEnabled(true);
            mWebView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onReceivedTitle(WebView view, String title) {
                    super.onReceivedTitle(view, title);
                    CharSequence pnotfound = "Object not found!";
                    if (title.contains(pnotfound)) {
                        setContentView(R.layout.layout_cant_access);
                        Button dialogButtonRefresh2 = (Button) findViewById(R.id.buttonRefresh2);
                        dialogButtonRefresh2.setFocusable(true);
                        dialogButtonRefresh2.requestFocus();
                        dialogButtonRefresh2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (android.os.Build.VERSION.SDK_INT >= 11){
                                    recreate();
                                }else{
                                    Intent intent = getIntent();
                                    finish();
                                    startActivity(intent);
                                }
                            }
                        });
                    }
                }
            });

            mWebView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView webView, String url_new) {
                    webView.loadUrl(url_new);
                    return true;
                }

                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    setContentView(R.layout.layout_no_iptv);
                    Button dialogButtonRefresh = (Button) findViewById(R.id.buttonRefresh);
                    dialogButtonRefresh.setFocusable(true);
                    dialogButtonRefresh.requestFocus();
                    dialogButtonRefresh.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (android.os.Build.VERSION.SDK_INT >= 11){
                                recreate();
                            }else{
                                Intent intent = getIntent();
                                finish();
                                startActivity(intent);
                            }
                        }
                    });
                }

                public void onPageFinished(WebView view, String url) {
                    mWebView.setVisibility(View.VISIBLE);
                }

            });
            mWebView.loadUrl(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

}