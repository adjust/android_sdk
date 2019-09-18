/** IMPORTANT:
        1. Below interface on uncommenting generates OpenDeviceIdentifierService.java
        2. This is exactaly the same class which exist in the src within sdk package namespace
        3. Reason for not using the generated class directly, is to avoid the situation of
           duplicate class compilation error, as there might be a possiblity of having same aidl
           getting used by the client directly or indirectly (by some other integrated library)
        4. Also, please do not revise the order of the method in this AIDL file */

/*package com.uodis.opendevice.aidl;

interface OpenDeviceIdentifierService {
     // Obtain OAID
     String getOaid();
     // Obtain limit ad tracking parameter, true: limit tracking; false: do not limit tracking
     boolean isOaidTrackLimited();

}*/
