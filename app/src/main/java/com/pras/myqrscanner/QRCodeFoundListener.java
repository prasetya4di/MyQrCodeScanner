package com.pras.myqrscanner;

public interface QRCodeFoundListener {
    void onQRCoeFound(String qrCode);

    void qrCodeNotFound();
}
