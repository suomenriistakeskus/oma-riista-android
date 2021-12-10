package fi.riista.mobile.utils;

import android.text.TextUtils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

public class PhoneNumberValidator {

    private static final String DEFAULT_REGION = "FI";

    public static boolean isValid(String inputValue) {
        if (TextUtils.isEmpty(inputValue)) {
            return true;
        }

        try {
            validateAndFormat(inputValue, DEFAULT_REGION);

            return true;

        } catch (NumberParseException e) {
            return false;
        }
    }

    public static String validateAndFormat(String number) throws NumberParseException {
        return validateAndFormat(number, DEFAULT_REGION);
    }

    public static String validateAndFormat(String number, String region) throws NumberParseException {
        Phonenumber.PhoneNumber phoneNumber = validate(number, region);
        return format(phoneNumber);
    }

    private static Phonenumber.PhoneNumber validate(String number, String region) throws NumberParseException {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(number, region);
        if (!phoneUtil.isValidNumber(phoneNumber)) {
            throw new NumberParseException(NumberParseException.ErrorType.NOT_A_NUMBER, "Not a valid phone number:" + number);
        }
        return phoneNumber;
    }

    private static String format(Phonenumber.PhoneNumber phoneNumber) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        return phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
    }
}
