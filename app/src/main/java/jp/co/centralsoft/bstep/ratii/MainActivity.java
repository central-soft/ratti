package jp.co.centralsoft.bstep.ratii;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.altbeacon.beacon.service.BeaconService;

import java.util.List;

import jp.co.centralsoft.bstep.ratii.jp.co.centralsoft.bstep.ratii.service.BeaconPopupService;
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
    // サービスから値を受け取ったら動かしたい内容を書く
    protected Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            String message = bundle.getString("message");
            moveWebPage();
        }
    };
    /**
     * アプリ設定
     */
    SharedPreferences sharedPreferences;
    /**
     * ビーコン受信サービス
     */
    Intent intentService;
    BeaconPopupService beaconPopupService;
    private IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "アプリ起動");

        // サービス作成
        createBeaconReceiveService();

        // 要素をひも付け
        distanceVal = (TextView) findViewById(R.id.distanceVal);
        messageVal = (TextView) findViewById(R.id.messageVal);
        startServiceBtn = (Button) findViewById(R.id.startServiceBtn);
        stopServiceBtn = (Button) findViewById(R.id.stopServiceBtn);

        // ボタンイベント(サービス開始ボタン)
        startServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!getServiceSates()) {
                    startService(intentService);
                    beaconPopupService = new BeaconPopupService();
                    intentFilter = new IntentFilter();
                    intentFilter.addAction("UPDATE_ACTION");
                    registerReceiver(beaconPopupService, intentFilter);
                    beaconPopupService.registerHandler(updateHandler);
                }
                finish();
            }
        });

        // ボタンイベント(サービス終了ボタン)
        stopServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(intentService);
                updateStates();
            }
        });

        updateStates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStates();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    protected void createBeaconReceiveService(){

        // アプリの設定ファイルのロード
        this.sharedPreferences = this.getSharedPreferences("bstep_data", Context.MODE_PRIVATE);

        // 設定を元にサービスを作る
        this.intentService = new Intent(getBaseContext(), BeaconReceiveService.class);
        this.intentService.putExtra("uuid", this.sharedPreferences.getString("uuid", "04137D10-42C4-41D1-8E23-FCEB7FDE5856"));
        this.intentService.putExtra("major", this.sharedPreferences.getString("major", "1"));
        this.intentService.putExtra("minor", this.sharedPreferences.getString("minor", "1"));
        this.intentService.putExtra("regionName", this.sharedPreferences.getString("regionName", "BSTEP"));
    }

    /**
     * ビーコン受信サービスの状態を確認して、
     * ステータスを更新する
     */
    protected void updateStates(){

        messageVal.setText(getServiceSates() ? "サービス起動" : "サービス停止");
    }

    /**
     * ビーコン受信サービスの起動状態を確認する
     * @return true:起動中 false:未起動
     */
    protected boolean getServiceSates() {
        // サービスが実行中かチェック
        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> listServiceInfo = am.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo curr : listServiceInfo) {
            // クラス名を比較
            if (curr.service.getClassName().equals(BeaconService.class.getName())) {
                // 実行中のサービスと一致
                return true;
            }
        }
        return false;
    }

    /**
     * セントラルソフトのホームページを起動する。
     */
    protected void moveWebPage() {

        Uri uri = Uri.parse("http://52.27.243.20/index.php/beacon");
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(i);
    }
}
