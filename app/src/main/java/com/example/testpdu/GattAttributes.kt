package com.example.testpdu

class GattAttributes {

    companion object{


        const val DEVICE_NAME="device_name"
        const val DEVICE_ADDRESS="device_address"


        const val CONFIG = "00002902-0000-1000-8000-00805f9b34fb"

        val GENERAL_ACCESS = "00001800-0000-1000-8000-00805f9b34fb"
        val GENERAL_ATTRIBUTES = "00001801-0000-1000-8000-00805f9b34fb"
        val DEVICE_INFORMATION = "0000180a-0000-1000-8000-00805f9b34fb"
        val POWER_MEASUREMENT = "0000aa10-0000-1000-8000-00805f9b34fb"
        val DEVICE_CONTROL = "0000aa20-0000-1000-8000-00805f9b34fb"
        val SYSTEM_STATUS = "0000aa30-0000-1000-8000-00805f9b34fb"


        const val device_name = "00002a00-0000-1000-8000-00805f9b34fb"
        const val appearance = "00002a01-0000-1000-8000-00805f9b34fb"
        const val peripheral_preferred_connection_parameters = "00002a04-0000-1000-8000-00805f9b34fb"

        const val device_id = "00002a23-0000-1000-8000-00805f9b34fb"
        const val model_number_string = "00002a24-0000-1000-8000-00805f9b34fb"
        const val serial_number_string = "00002a25-0000-1000-8000-00805f9b34fb"
        const val firmware_revision_string = "00002a26-0000-1000-8000-00805f9b34fb"
        const val hardware_revision_string = "00002a27-0000-1000-8000-00805f9b34fb"
        const val software_revision_string = "00002a28-0000-1000-8000-00805f9b34fb"
        const val manufacturer_name_string = "00002a29-0000-1000-8000-00805f9b34fb"
        const val ieee_regulatory = "00002a2a-0000-1000-8000-00805f9b34fb"
        const val pnp_id = "00002a50-0000-1000-8000-00805f9b34fb"

        const val current = "0000aa11-0000-1000-8000-00805f9b34fb"
        const val voltage = "0000aa12-0000-1000-8000-00805f9b34fb"
        const val watt = "0000aa13-0000-1000-8000-00805f9b34fb"
        const val power_factor = "0000aa14-0000-1000-8000-00805f9b34fb"
        const val load = "0000aa15-0000-1000-8000-00805f9b34fb"
        const val load_detected = "0000aa16-0000-1000-8000-00805f9b34fb"

        const val activate_power = "0000aa21-0000-1000-8000-00805f9b34fb"
        const val set_time = "0000aa22-0000-1000-8000-00805f9b34fb"
        const val download_request = "0000aa23-0000-1000-8000-00805f9b34fb"
        const val read_recorded_data = "0000aa24-0000-1000-8000-00805f9b34fb"
        const val nfc_tag_id = "0000aa25-0000-1000-8000-00805f9b34fb"
        const val charging_latency = "0000aa26-0000-1000-8000-00805f9b34fb"

        const val hardware_status = "0000aa31-0000-1000-8000-00805f9b34fb"
        const val software_status = "0000aa32-0000-1000-8000-00805f9b34fb"
        const val error_code = "0000aa33-0000-1000-8000-00805f9b34fb"


        const val mDeviceName="device_name"
        const val mAppearance="appearance"
        const val mPeripheralParameters="peripheral_parameters"

        const val mDeviceId="device_id"
        const val mModelNumberString="model_number_string"
        const val mSerialNumberString="serial_number_string"
        const val mFirmwareRevision="firmware_revision"
        const val mHardwareRevision="hardware_revision"
        const val mSoftwareRevision="software_revision"
        const val mManufacturerNameString="manufacture_name_string"

        const val mCurrent="current"
        const val mVoltage="voltage"
        const val mWatt="watt"
        const val mPowerFactor="power_factor"
        const val mLoad="load"
        const val mLoadDetected="load_detected"

        const val mPowerOn="power_on"
        const val mPowerOff="power_off"

        const val mSetTime="set_time"
        const val mGetTime="get_time"
        const val mDownloadOn="download_on"

        const val mReadRecordedData="read_recorded_data"
        const val mNfcTagId="nfc_tag_id"
        const val mChargingLatency="charging_latency"

        const val mHardwareStatus="hardware_status"
        const val mSoftwareStatus="software_status"
        const val mErrorCode="error_code"

    }



}