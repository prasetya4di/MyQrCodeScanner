package com.pras.myqrscanner;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;

import java.util.regex.Pattern;

public enum ResultType {
    PHONE_NUMBER("Telp", "^\\+[1-9]{1}[0-9]{3,14}$") {
        @Override
        public Intent intentResult(String result) {
            Uri phoneNumber = Uri.parse("tel:" + result);
            return new Intent(Intent.ACTION_DIAL, phoneNumber);
        }
    },
    EMAIL("Email", "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.]+@[a-zA-Z0-9.]+$") {
        @Override
        public Intent intentResult(String result) {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{result});
            return emailIntent;
        }
    },
    WEB("WEB", "^(http|https):\\/\\/(www).([a-z\\.]*)?(\\/[a-z1-9\\/]*)*\\??([\\&a-z1-9=]*)?") {
        @Override
        public Intent intentResult(String result) {
            Uri webpage = Uri.parse(result);
            return new Intent(Intent.ACTION_VIEW, webpage);
        }
    },
    LOCATION("Lokasi", "([+-]?\\d+\\.?\\d+)\\s*,\\s*([+-]?\\d+\\.?\\d+)") {
        @Override
        public Intent intentResult(String result) {
            Uri location = Uri.parse("geo:" + result);
            return new Intent(Intent.ACTION_VIEW, location);
        }
    },
    PRODUCT("Produk", "^[a-zA-Z0-9]{11,}$") {
        @Override
        public Intent intentResult(String result) {
            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, result);
            return intent;
        }
    },
    CODE("Kode", ".") {
        @Override
        public Intent intentResult(String result) {
            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, result);
            return intent;
        }
    };

    public final String typeName;
    public final String regex;

    ResultType(String typeName, String regex) {
        this.regex = regex;
        this.typeName = typeName;
    }

    public static ResultType parse(String value) {
        for (ResultType resultType : values()) {
            if (Pattern.matches(resultType.regex, value)) {
                return resultType;
            }
        }
        return CODE;
    }

    public abstract Intent intentResult(String result);
}
