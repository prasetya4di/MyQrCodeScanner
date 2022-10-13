package com.pras.myqrscanner;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;

import java.util.regex.Pattern;

public enum ResultType {
    PHONE_NUMBER("Telp", "^(tel:)\\+[1-9]{1}[0-9]{3,14}$", R.string.result_phone_number_message) {
        @Override
        public Intent intentResult(String result) {
            Uri phoneNumber = Uri.parse(result);
            return new Intent(Intent.ACTION_DIAL, phoneNumber);
        }
    },
    EMAIL("Email", "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.]+@[a-zA-Z0-9.]+$", R.string.result_email_message) {
        @Override
        public Intent intentResult(String result) {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{result});
            return emailIntent;
        }
    },
    WEB("WEB", "^(http|https):\\/\\/([a-z\\.]*)?(\\/[a-z1-9\\/]*)*\\??([\\&a-z1-9=]*)?", R.string.result_web_message) {
        @Override
        public Intent intentResult(String result) {
            Uri webpage = Uri.parse(result);
            return new Intent(Intent.ACTION_VIEW, webpage);
        }
    },
    LOCATION("Lokasi", "geo:([+-]?\\d+\\.?\\d+)\\s*,\\s*([+-]?\\d+\\.?\\d+).", R.string.result_location_message) {
        @Override
        public Intent intentResult(String result) {
            Uri location = Uri.parse(result);
            return new Intent(Intent.ACTION_VIEW, location);
        }
    },
    PRODUCT("Produk", "^[a-zA-Z0-9]{11,}$", R.string.result_product_message) {
        @Override
        public Intent intentResult(String result) {
            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, result);
            return intent;
        }
    },
    CODE("Kode", ".", R.string.result_code_message) {
        @Override
        public Intent intentResult(String result) {
            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, result);
            return intent;
        }
    };

    public final String typeName;
    public final String regex;
    public final int messageId;

    ResultType(String typeName, String regex, int messageId) {
        this.regex = regex;
        this.typeName = typeName;
        this.messageId = messageId;
    }

    public static ResultType parse(String value) {
        for (ResultType resultType : values()) {
            if (Pattern.compile(resultType.regex).matcher(value).find()) {
                return resultType;
            }
        }
        return CODE;
    }

    public abstract Intent intentResult(String result);
}
