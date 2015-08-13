package jp.co.centralsoft.bstep.ratii;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private boolean bltbln;
    private static final long SCAN_PERIOD = 10000;
    private BluetoothAdapter mBluetoothAdapter;
    //private Handler mHandler;
    private Button push_btnst;
    private Button push_btned;

    private TextView txt_minor;
    private TextView txt_major;
    private TextView txt_uuid;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        //mBluetoothAdapter.startLeScan(mLeScanCallback);

        push_btnst = (Button)findViewById(R.id.btn_st);
        push_btned = (Button)findViewById(R.id.btn_ed);

        txt_minor = (TextView)findViewById(R.id.minor);
        txt_major = (TextView)findViewById(R.id.major);
        txt_uuid = (TextView)findViewById(R.id.uuid);

        push_btnst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Uri uri = Uri.parse("http://www.central-soft.co.jp");
                //Intent i = new Intent(Intent.ACTION_VIEW,uri);
                //startActivity(i);
                bltbln = false;
                txt_major.setText("");
                txt_minor.setText("");
                txt_uuid.setText("");

                mBluetoothAdapter.startLeScan(mLeScanCallback);
            }
        });

        push_btned.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);

                txt_major.setText("");
                txt_minor.setText("");
                txt_uuid.setText("");


            }
        });


    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {



            if (scanRecord.length > 30 && !bltbln) {
                //iBeacon の場合 6 byte 目から、 9 byte 目はこの値に固定されている。
                if ((scanRecord[5] == (byte) 0x4c) && (scanRecord[6] == (byte) 0x00) &&
                        (scanRecord[7] == (byte) 0x02) && (scanRecord[8] == (byte) 0x15)) {

                    String uuid2 = getScanData(9, 24, scanRecord);
                    String major2 = getScanData(25, 26, scanRecord);
                    String minor2 = getScanData(27, 28, scanRecord);
                    String strength2 = String.valueOf(scanRecord[29]);

                    String uuid = IntToHex2(scanRecord[9] & 0xff)
                            + IntToHex2(scanRecord[10] & 0xff)
                            + IntToHex2(scanRecord[11] & 0xff)
                            + IntToHex2(scanRecord[12] & 0xff)
                            + "-"
                            + IntToHex2(scanRecord[13] & 0xff)
                            + IntToHex2(scanRecord[14] & 0xff)
                            + "-"
                            + IntToHex2(scanRecord[15] & 0xff)
                            + IntToHex2(scanRecord[16] & 0xff)
                            + "-"
                            + IntToHex2(scanRecord[17] & 0xff)
                            + IntToHex2(scanRecord[18] & 0xff)
                            + "-"
                            + IntToHex2(scanRecord[19] & 0xff)
                            + IntToHex2(scanRecord[20] & 0xff)
                            + IntToHex2(scanRecord[21] & 0xff)
                            + IntToHex2(scanRecord[22] & 0xff)
                            + IntToHex2(scanRecord[23] & 0xff)
                            + IntToHex2(scanRecord[24] & 0xff);

                    String major = IntToHex2(scanRecord[25] & 0xff) + IntToHex2(scanRecord[26] & 0xff);
                    String minor = IntToHex2(scanRecord[27] & 0xff) + IntToHex2(scanRecord[28] & 0xff);

                    txt_major.setText(major);
                    txt_minor.setText(minor);
                    txt_uuid.setText(uuid);

                    //検知
                    bltbln = true;

                    Uri uri = Uri.parse("http://www.central-soft.co.jp");
                    Intent i = new Intent(Intent.ACTION_VIEW,uri);
                    startActivity(i);



                }
            }
        }
        private String IntToHex2(int i) {
            char hex_2[] = {Character.forDigit((i >> 4) & 0x0f, 16), Character.forDigit(i & 0x0f, 16)};
            String hex_2_str = new String(hex_2);
            return hex_2_str.toUpperCase();
        }

        public String getScanData(int start, int end, byte[] scanRecord) {
            StringBuilder result = new StringBuilder(end - start);
            for (int i = start; i <= end; i++) {
                result.append(convertHex(scanRecord[i] & 0xff));
            }
            return result.toString();
        }

        public String convertHex(int i) {
            char hexArray[] = {
                    Character.forDigit((i >> 4) & 0x0f, 16), Character.forDigit(i & 0x0f, 16)
            };
            return new String(hexArray).toUpperCase();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
