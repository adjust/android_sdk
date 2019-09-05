package com.uodis.opendevice.aidl;
/** Important: Please do not revise the order of the method in this AIDL file */

interface OpenDeviceIdentifierService {
     /** Obtain OAID */
     String getOaid();
     /** Obtain limit ad tracking parameter, true: limit tracking; false: do not limit tracking*/
     boolean isOaidTrackLimited();

}
