package jp.co.centralsoft.bstep.ratii;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    private final static String IBEACON_FORMAT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
    TextView destanceVal;
    TextView messageVal;
    Button startServiceBtn;
    Button stopServiceBtn;
    BeaconManager beaconManager;
    Region region;
    Handler mHandler = new Handler();

    boolean scanFlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        destanceVal = (TextView) findViewById(R.id.distanceVal);
        messageVal = (TextView) findViewById(R.id.messageVal);
        startServiceBtn = (Button) findViewById(R.id.startServiceBtn);
        stopServiceBtn = (Button) findViewById(R.id.stopServiceBtn);

        initializeTextContent();

        // ボタンイベント(サービス開始ボタン)
        startServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!scanFlg) {
                    startScan();
                }
            }
        });

        // ボタンイベント(サービス終了ボタン)
        stopServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopScan();
                scanFlg = false;
            }
        });

    }

    protected void startScan() {

        // インスタンス化
        beaconManager = BeaconManager.getInstanceForApplication(this);
        // パーサ設定(iBeaconのフォーマットを設定する)
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(IBEACON_FORMAT));

        // uuidが"04137D10-42C4-41D1-8E23-FCEB7FDE5856"、
        // major番号が"1"、minor番号が"1"のBeaconを監視する場合
        Identifier uuid = Identifier.parse("04137D10-42C4-41D1-8E23-FCEB7FDE5856");
        Identifier major = Identifier.parse("1");
        Identifier minor = Identifier.parse("1");
        region = new Region("BSTEP_TARGET", uuid, major, minor);
        beaconManager.bind(this);
    }

    protected void stopScan() {
        if(beaconManager!=null){
            beaconManager.unbind(this); // サービスの停止
            beaconManager = null;
        }
        initializeTextContent();
    }

    protected void initializeTextContent() {
        // 初期化

        destanceVal.setText("none");
        messageVal.setText("サービス停止中");
//        mHandler.post(new Runnable() {
//            public void run() {
//            }
//        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScan();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopScan();
    }

    @Override
    public void onBeaconServiceConnect() {
        try {
            // ビーコン情報の監視を開始
            beaconManager.startMonitoringBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                // 領域への入場を検知

                // レンジングの開始
                try {
                    beaconManager.startRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didExitRegion(Region region) {
                // 領域からの退場を検知
                // レンジングの停止
                try {
                    beaconManager.stopRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {
                // 領域への入退場のステータス変化を検知
            }
        });

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {

                // 検出したビーコンの情報を全部Logに書き出す
                for (final Beacon beacon : beacons) {
                    // ログの出力
                    Log.d("Beacon", "UUID:" + beacon.getId1() + ", major:" + beacon.getId2()
                            + ", minor:" + beacon.getId3() + ", Distance:" + beacon.getDistance()
                            + ",RSSI" + beacon.getRssi());

                    final double distance = beacon.getDistance();

                    mHandler.post(new Runnable() {
                        public void run() {
                            TextView distanceVal = (TextView) findViewById(R.id.distanceVal);
                            distanceVal.setText(String.valueOf(distance));
                            TextView messageVal = (TextView) findViewById(R.id.messageVal);
                            if (distance < 2.0) {
//                                messageVal.setText("ちかいよおおおお");
                                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                                long[] pattern = {0, 200, 200, 200};
                                vibrator.vibrate(pattern, -1);
                                Uri uri = Uri.parse("http://www.central-soft.co.jp/");
                                Intent i = new Intent(Intent.ACTION_VIEW,uri);
                                startActivity(i);
                            } else {
                                messageVal.setText("まだまだー");
                            }
                        }
                    });
                }
            }
        });
    }


}
