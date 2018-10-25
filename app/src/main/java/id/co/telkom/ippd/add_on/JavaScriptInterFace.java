package id.co.telkom.ippd.add_on;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class JavaScriptInterFace {
    Context mContext;
    JavaScriptInterFace(Context c) {
        mContext = c;
    }
    @JavascriptInterface
    public int changeImage(){
        Log.e("Got", "it"+2);
        return 2;
    }

    @JavascriptInterface
    public void showToast(){
        Toast.makeText(mContext, "hi", Toast.LENGTH_SHORT).show();
    }

}