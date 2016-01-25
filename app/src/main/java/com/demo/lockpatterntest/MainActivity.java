package com.demo.lockpatterntest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.haibison.android.lockpattern.util.Settings;

public class MainActivity extends AppCompatActivity {

    private final String TAG = getClass().getName();
    //创建一个新的图案
    private static final int REQ_CREATE_PATTERN = 1;
    //校对已有的图案
    private static final int REQ_COMPARE_PATTERN = 2;
    //生成随机的图案（个人感觉没啥用(＃－－)/ .）
    private static final int REQ_VERIFY_PATTERN = 3;


    //密文，代表一个pattern
    private String ciphertext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initPatternConfig();


    }

    public void initPatternConfig(){

        //lockpattern会使用Sharepreference自动保存密文
        Settings.Security.setAutoSavePattern(this, true);

        //隐身模式：不显示勾画的连接线,默认关闭
        Settings.Display.setStealthMode(this, false);

        //设置验证的显示次数，默认是4次
        Settings.Display.setCaptchaWiredDots(this, 9);

        //启用自定义的解析方式 默认使用SHA1算法摘要
        //<activity
//	    android:name="com.haibison.android.lockpattern.LockPatternActivity"
//	    	    android:configChanges="keyboard|keyboardHidden|orientation|screenSize"
//	    	    android:screenOrientation="user"
//	    	    android:theme="@style/Alp.42447968.Theme.Dialog.Dark" >
//	    	    <meta-data
//	    	        android:name="encrypterClass"
//	    	        android:value="...full.qualified.name.to.your.LPEncrypter" />
//	    	</activity>

//		AlpSettings.Security.setEncrypterClass(this, LPEncrypter.class);
    }

    public void OnCreatePasswd(View view){

        Intent intent = new Intent(
                LockPatternActivity.ACTION_CREATE_PATTERN, null, this,
                LockPatternActivity.class);
        startActivityForResult(intent, REQ_CREATE_PATTERN);
    }
    public void OnCompare(View view){
        Settings.Display.setCaptchaWiredDots(this, 5);
        Intent compare = new Intent(LockPatternActivity.ACTION_COMPARE_PATTERN, null,
                this, LockPatternActivity.class);
        if(view.getId()==R.id.btn_compare&&ciphertext!=null){
            //如果没有设置setAutoSavePattern为true，则要自己保存密文，并且校对的时候传入EXTRA_PATTERN

            char[] savedPattern = ciphertext.toCharArray();
            compare.putExtra(LockPatternActivity.EXTRA_PATTERN, savedPattern);
        }
        startActivityForResult(compare, REQ_COMPARE_PATTERN);
    }


    public void OnVerificationPasswd(View view) {
        //生成随机的图案
        Intent verifyIntent = new Intent(LockPatternActivity.ACTION_VERIFY_CAPTCHA, null,
                this, LockPatternActivity.class);
        startActivityForResult(verifyIntent, REQ_VERIFY_PATTERN);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {

        switch (requestCode) {
            case REQ_CREATE_PATTERN:
                if (resultCode == RESULT_OK) {
                    char[] pattern = data.getCharArrayExtra(LockPatternActivity.EXTRA_PATTERN);
                    StringBuffer buffer = new StringBuffer();
                    for(char c:pattern){
                        buffer.append(c);
                    }
                    ciphertext = buffer.toString();
                    Log.i(TAG, "ciphertext=>" + ciphertext);
                    //bb2f0821e9d02433f1ffbaee06c6e6dc7a77daf0
                    //Toast.makeText(this, "消息摘要：" + buffer, Toast.LENGTH_SHORT).show();
                }
                break;
            case REQ_COMPARE_PATTERN:
		        /*
		         * 注意！有四种可能出现情况的返回结果
		         */
                switch (resultCode) {
                    case RESULT_OK:
                        //用户通过验证
                        Log.d(TAG, "user passed");
                        Toast.makeText(this, "用户通过验证", Toast.LENGTH_SHORT).show();
                        break;
                    case RESULT_CANCELED:
                        // 用户取消
                        Log.d(TAG, "user cancelled");
                        break;
                    case LockPatternActivity.RESULT_FAILED:
                        //用户多次失败
                        Log.d(TAG, "user failed");
                        Toast.makeText(this, "用户多次失败验证", Toast.LENGTH_SHORT).show();

                        break;
                    case LockPatternActivity.RESULT_FORGOT_PATTERN:
                        // The user forgot the pattern and invoked your recovery Activity.
                        Log.d(TAG, "user forgot");
                        break;
                }

		        /*
		         * 在任何情况下，EXTRA_RETRY_COUNT都代表着用户尝试的图案的次数
		         */
                int retryCount = data.getIntExtra(
                        LockPatternActivity.EXTRA_RETRY_COUNT, 0);
                Log.i(TAG, "用户尝试了"+retryCount+"次数");

                break;
        }
    }
}
