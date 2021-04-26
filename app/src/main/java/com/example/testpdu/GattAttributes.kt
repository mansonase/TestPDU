package com.example.testpdu

class GattAttributes {

    companion object{


        const val DEVICE_NAME="device_name"
        const val DEVICE_ADDRESS="device_address"


        const val CONFIG = "00002902-0000-1000-8000-00805f9b34fb"

        val GENERAL_ACCESS = "00001800-0000-1000-8000-00805f9b34fb"
        val GENERAL_ATTRIBUTES = "00001801-0000-1000-8000-00805f9b34fb"
        val DEVICE_INFORMATION = "0000180A-0000-1000-8000-00805f9b34fb"
        val POWER_MEASUREMENT = "0000AA10-0000-1000-8000-00805f9b34fb"
        val DEVICE_CONTROL = "0000AA20-0000-1000-8000-00805f9b34fb"
        val SYSTEM_STATUS = "0000AA30-0000-1000-8000-00805f9b34fb"


        const val device_name = "00002A00-0000-1000-8000-00805f9b34fb"
        const val appearance = "00002A01-0000-1000-8000-00805f9b34fb"
        const val peripheral_preferred_connection_parameters = "00002A04-0000-1000-8000-00805f9b34fb"

        const val device_id = "00002A23-0000-1000-8000-00805f9b34fb"
        const val model_number_string = "00002A24-0000-1000-8000-00805f9b34fb"
        const val serial_number_string = "00002A25-0000-1000-8000-00805f9b34fb"
        const val firmware_revision_string = "00002A26-0000-1000-8000-00805f9b34fb"
        const val hardware_revision_string = "00002A27-0000-1000-8000-00805f9b34fb"
        const val software_revision_string = "00002A28-0000-1000-8000-00805f9b34fb"

        const val current = "0000AA11-0000-1000-8000-00805f9b34fb"
        const val voltage = "0000AA12-0000-1000-8000-00805f9b34fb"
        const val watt = "0000AA13-0000-1000-8000-00805f9b34fb"
        const val power_factor = "0000AA14-0000-1000-8000-00805f9b34fb"
        const val load = "0000AA15-0000-1000-8000-00805f9b34fb"
        const val load_detected = "0000AA16-0000-1000-8000-00805f9b34fb"

        const val activate_power = "0000AA21-0000-1000-8000-00805f9b34fb"
        const val set_time = "0000AA22-0000-1000-8000-00805f9b34fb"
        const val download_request = "0000AA23-0000-1000-8000-00805f9b34fb"
        const val read_recorded_data = "0000AA24-0000-1000-8000-00805f9b34fb"
        const val nfc_tag_id = "0000AA25-0000-1000-8000-00805f9b34fb"
        const val charging_latency = "0000AA26-0000-1000-8000-00805f9b34fb"

        const val hardware_status = "0000AA31-0000-1000-8000-00805f9b34fb"
        const val software_status = "0000AA32-0000-1000-8000-00805f9b34fb"
        const val error_code = "0000AA33-0000-1000-8000-00805f9b34fb"


        const val mDeviceName="device_name"
        const val mAppearance="appearance"
        const val mPeripheralParameters="peripheral_parameters"

        const val mDeviceId="device_id"
        const val mModelNumberString="model_number_string"
        const val mSerialNumberString="serial_number_string"
        const val mFirmwareRevision="firmware_revision"
        const val mHardwareRevision="hardware_revision"
        const val mSoftwareRevision="software_revision"

        const val mCurrent="current"
        const val mVoltage="voltage"
        const val mWatt="watt"
        const val mPowerFactor="power_factor"
        const val mLoad="load"
        const val mLoadDetected="load_detected"

        const val mActivatePower="activate_power"
        const val mSetTime="set_time"
        const val mDownloadRequest= "download_request"
        const val mReadRecordedData="read_recorded_data"
        const val mNfcTagId="nfc_tag_id"
        const val mChargingLatency="charging_latency"

        const val mHardwareStatus="hardware_status"
        const val mSoftwareStatus="software_status"
        const val mErrorCode="error_code"

    }



}