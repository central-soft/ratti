package jp.co.centralsoft.bstep.ratii.jp.co.centralsoft.bstep.ratii.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

import jp.co.centralsoft.bstep.ratii.MainActivity;

/**
 * ビーコン受信クラス
 * Created by Daiki Nakamura on 2016/01/11.
 */
public class BeaconReceiveService extends Service implements BeaconConsumer {

    /**
     * iBeaconフォーマット
     */
    private final static String IBEACON_FORMAT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";

    /**
     * Beaconマネージャー
     */
    protected BeaconManager beaconManager;

    /**
     * 通知マネージャー
     */
    protected NotificationManager notificationManager;

    /**
     * Beacon情報
     */
    protected Identifier uuid;
    protected Identifier major;
    protected Identifier minor;
    protected String regionName;

    @Override
    public void onCreate() {
        super.onCreate();

        // BeaconManagerのインスタンス化・IBEACONフォーマットの設定
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(IBEACON_FORMAT));

        // 通知マネージャーのインスタンス化
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Beaconの設定情報を受け取る
        this.uuid = Identifier.parse(intent.getStringExtra("uuid"));
        this.major = Identifier.parse(intent.getStringExtra("major"));
        this.minor = Identifier.parse(intent.getStringExtra("minor"));
        this.regionName = intent.getStringExtra("regionName");

        // BeaconManagerをバインド(受信開始)
        beaconManager.bind(this);

        // 通知エリア
        notificationManager.notify(MainActivity.TAG, 1, this.getNotification(this));

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // BeaconManagerをアンバインド(受信終了)
        if (beaconManager != null) {
            beaconManager.unbind(this);
            beaconManager = null;
        }

        // 通知エリアをアンバインド
        notificationManager.cancel(MainActivity.TAG, 1);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onBeaconServiceConnect() {

        // 監視対象
        Region region = new Region("BSTEP_TARGET", this.uuid, this.major, this.minor);

        try {
            // ビーコン情報の監視を開始
            beaconManager.startMonitoringBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        // 領域監視
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

                    String message = "さーびすからのメッセージ";
                    sendBroadCast(message);

                    // サービスの停止
                    onDestroy();
                }
            }
        });
    }

    protected void sendBroadCast(String message) {

        Intent broadcastIntent = new Intent();
        broadcastIntent.putExtra("message", message);
        broadcastIntent.setAction("UPDATE_ACTION");
        getBaseContext().sendBroadcast(broadcastIntent);

    }

    /**
     * 通知領域の作成
     * @param context
     * @return
     */
    private Notification getNotification(Context context) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(context, 0, notificationIntent, 0);

        // ステータスバー・通知エリアに表示する内容をセット
        Notification.Builder builder = new Notification.Builder(context);
        builder.setTicker("iBeacon受信サービス");
        builder.setContentTitle("BSTEP");
        builder.setContentText("iBeacon受信サービス起動中");
        builder.setContentInfo("info");
        builder.setWhen(System.currentTimeMillis());
        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(android.R.drawable.ic_menu_info_details);
        Notification notification = builder.build();

        // 実行中フラグ
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        return notification;
    }
}
