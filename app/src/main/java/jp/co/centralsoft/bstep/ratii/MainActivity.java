package jp.co.centralsoft.bstep.ratii;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.altbeacon.beacon.service.BeaconService;

import java.util.List;

import jp.co.centralsoft.bstep.ratii.jp.co.centralsoft.bstep.ratii.service.BeaconReceiveService;

public class MainActivity extends AppCompatActivity {

    public final static String TAG = "Ratti";

    /**
     * 要素
     */
    protected TextView distanceVal;
    protected TextView messageVal;
    protected Button startServiceBtn;
    protected Button stopServiceBtn;
    /**
     * アプリ設定
     */
    SharedPreferences sharedPreferences;
    /**
     * ビーコン受信サービス
     */
    Intent intentService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "アプリ起動");

        // アプリの設定ファイルのロード
        this.sharedPreferences = this.getSharedPreferences("bstep_data", Context.MODE_PRIVATE);

        // 設定を元にサービスを作る
        this.intentService = new Intent(getBaseContext(), BeaconReceiveService.class);
        this.intentService.putExtra("uuid", this.sharedPreferences.getString("uuid", "04137D10-42C4-41D1-8E23-FCEB7FDE5856"));
        this.intentService.putExtra("major", this.sharedPreferences.getString("major", "1"));
        this.intentService.putExtra("minor", this.sharedPreferences.getString("minor", "1"));
        this.intentService.putExtra("regionName", this.sharedPreferences.getString("regionName", "BSTEP"));

        // 要素をひも付け
        distanceVal = (TextView) findViewById(R.id.distanceVal);
        messageVal = (TextView) findViewById(R.id.messageVal);
        startServiceBtn = (Button) findViewById(R.id.startServiceBtn);
        stopServiceBtn = (Button) findViewById(R.id.stopServiceBtn);

        // ボタンイベント(サービス開始ボタン)
        startServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(intentService);
                messageVal.setText(getServiceSatus() ? "サービス起動" : "サービス停止？");
            }
        });

        // ボタンイベント(サービス終了ボタン)
        stopServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageVal.setText("サービス停止？");
                stopService(intentService);
                messageVal.setText(getServiceSatus() ? "サービス起動" : "サービス停止？");
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        messageVal.setText(getServiceSatus() ? "サービス起動" : "サービス停止？");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    boolean getServiceSatus() {
        // サービスが実行中かチェック
        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> listServiceInfo = am.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo curr : listServiceInfo) {
            // クラス名を比較
            if (curr.service.getClassName().equals(BeaconService.class.getName())) {
                // 実行中のサービスと一致
//                Toast.makeText(this, "サービス実行中", Toast.LENGTH_LONG).show();
                return true;
            }
        }
        return false;
    }
}
