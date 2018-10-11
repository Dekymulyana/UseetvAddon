// TelkomSTB.aidl
package id.co.telkom.ippd.stbinterface;

// Declare any non-default types here with import statements

interface TelkomSTB {
    String getIndihomeId();
    String getMACAddressWireline();
    String getMACAddressWireless();
    String getSerialNumber();
    String getDeviceManufacturer();
    String getCurrentChannel();
    String getCurrentVOD();
}
