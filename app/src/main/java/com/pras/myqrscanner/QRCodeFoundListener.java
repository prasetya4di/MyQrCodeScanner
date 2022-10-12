package com.pras.myqrscanner;

public interface QRCodeFoundListener {
    void onQRCodeFound(String qrCode);

    void qrCodeNotFound();
}
