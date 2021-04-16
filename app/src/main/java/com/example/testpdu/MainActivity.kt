package com.example.testpdu

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: BluetoothAdapter
    private var isScanning:Boolean = false
    private lateinit var mLeDeviceListAdapter:LeDeviceListAdapter
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var arrayList: ArrayList<BluetoothDevice>
    private lateinit var rssiList:ArrayList<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val layoutManager=LinearLayoutManager(this)
        layoutManager.orientation=LinearLayoutManager.VERTICAL
        mRecyclerView=findViewById<RecyclerView>(R.id.recyclerview)
        mRecyclerView.layoutManager=layoutManager

        mRecyclerView.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))
        arrayList= ArrayList()
        rssiList= ArrayList()

        if(!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            finish()
        }

        val manager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        adapter = manager.adapter

        if (adapter==null){
            finish()
            return
        }
        Log.d("testpdu","pass manager and adapter")
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(mReceiver, IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED))


        mLeDeviceListAdapter= LeDeviceListAdapter(arrayList,rssiList)

        if (adapter.isEnabled) {
            if (getPermissionsBLE()) {
                scanLeDevice(true)
            }
        }else{
            adapter.enable()
            Log.d("testpdu","open ble")
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(mReceiver)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        if (menu==null)return true

        menuInflater.inflate(R.menu.main,menu)

        if (!isScanning){
            menu.findItem(R.id.menu_stop).isVisible=false
            menu.findItem(R.id.menu_scan).isVisible=true
            menu.findItem(R.id.menu_refresh).actionView=null

        }else{
            menu.findItem(R.id.menu_stop).isVisible=true
            menu.findItem(R.id.menu_scan).isVisible=false
            menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_indeterminate_progress)

        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.menu_scan->{
                mLeDeviceListAdapter.clear()
                scanLeDevice(true)
            }
            R.id.menu_stop->{
                scanLeDevice(false)
            }
        }
        return true

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {


                    var intent: Intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    var uri: Uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)

                } else {
                    Toast.makeText(this, getString(R.string.press_no), Toast.LENGTH_LONG).show()
                }
            } else {
                if (!adapter.isEnabled) {
                    adapter.enable()
                }
                Toast.makeText(this, getString(R.string.allow_permission), Toast.LENGTH_LONG).show()
            }
        }
    }

    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val action: String = p1?.action ?: return

            if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED == action) {
                val state: Int = p1.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)

                when (state) {
                    BluetoothAdapter.STATE_OFF -> Log.d("testpdu", "state off")
                    BluetoothAdapter.STATE_ON -> {
                        Log.d("testpdu", "state on")
                        scanLeDevice(true)
                    }
                }
            }
        }
    }

    private val mLeScanCallback:ScanCallback= object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)

            if (result==null)return

            mLeDeviceListAdapter.addDevice(result.device,result.rssi)

            mRecyclerView.adapter=mLeDeviceListAdapter
            //Log.d("testpdu"," address : ${result.device.address}, name : ${result.device.name}")
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
        }
    }

    private fun getPermissionsBLE(): Boolean {

        var isPermissionGranted: Boolean
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle(getString(R.string.need_fine_permission))
                    .setMessage(getString(R.string.need_fine_permission_to_use_ble))
                    .setPositiveButton(getString(R.string.ok_i_know),
                        DialogInterface.OnClickListener
                        { dialogInterface, i ->
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                1
                            )
                        }
                    )
                    .show()


            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            }
            isPermissionGranted = false
        } else {
            isPermissionGranted = true
        }

        return isPermissionGranted
    }


    private fun scanLeDevice(enable:Boolean) {

        val scanner= adapter.bluetoothLeScanner ?: return

        if (enable){

            Handler(Looper.getMainLooper()).postDelayed({
                isScanning=false
                scanner.stopScan(mLeScanCallback)
                invalidateOptionsMenu()
                Log.d("testpdu","........stop scan now")
            },10000)

            isScanning=true
            scanner.startScan(mLeScanCallback)

        }else{

            isScanning=false
            scanner.stopScan(mLeScanCallback)

        }
        invalidateOptionsMenu()

    }

    class LeDeviceListAdapter(private var mLeDevices:ArrayList<BluetoothDevice>, private var mRssiList:ArrayList<Int>): RecyclerView.Adapter<ViewHolder>() {



        fun getDevice(position: Int):BluetoothDevice{
            return mLeDevices[position]
        }

        fun clear(){
            mLeDevices.clear()
            mRssiList.clear()
        }

        fun addDevice(device: BluetoothDevice, rssi:Int){

            if (device.name==null)return

            if (device.name.trim() != "F100")return

            if (!mLeDevices.contains(device)){
                mLeDevices.add(device)
                mRssiList.add(rssi)
                Log.d("testpdu"," address : ${device.address}, name : ${device.name}, rssi: $rssi")
            }

        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v=LayoutInflater.from(parent.context).inflate(R.layout.listitem_device,parent,false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.txName.text= mLeDevices[position].name
            holder.txAddress.text= mLeDevices[position].address
            holder.txRssi.text=mRssiList[position].toString()

            holder.txAddress.setOnClickListener {
                Log.d("testpdu","${mLeDevices[position].name}, and ${mLeDevices[position].address}")


            }


        }

        override fun getItemCount(): Int {
            return mLeDevices.size
        }
    }

    class ViewHolder(v:View):RecyclerView.ViewHolder(v){

        val txName:TextView=v.findViewById(R.id.device_name)
        val txAddress:TextView=v.findViewById(R.id.device_address)
        val txRssi:TextView=v.findViewById(R.id.device_rssi)


    }
}