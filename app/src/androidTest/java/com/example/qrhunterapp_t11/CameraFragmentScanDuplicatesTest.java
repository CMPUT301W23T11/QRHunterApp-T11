package com.example.qrhunterapp_t11;

//TODO: Test the following situations:
// 1. User has QR code in collection with NULL LOCATION:
//      1.1 They scan the same qrcode and refuse to update location
//      1.2 They scan the same qrcode and update location with a null location (no change)
//      1.3 They scan the same qrcode and update location with a non null location (change) ****
// 2. User has QR code in collection with NONNULL LOCATION:
//      2.1 They scan the same qrcode and refuse to update location
//      2.2 They scan the same qrcode and update location with a null location (no change)
//      2.3 They scan the same qrcode and update location with a non null location that is the same location as the original (within 30m) (no change)
//      2.3 They scan the same qrcode and update location with a non null location that is a different location than the original (further than 30m) (change)
//
public class CameraFragmentScanDuplicatesTest {
}
