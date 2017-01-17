package fi.riista.mobile.utils;

import java.util.regex.Pattern;

/**
 * Finnish hunting permit number is for example 2013-3-450-00260-2, where
 * <ul>
 * <li>2013: year when permit is given, 4 digits</li>
 * <li>3: how many years permit is valid, 1,2,3,4 or 5</li>
 * <li>450: RKA code, zero padded to 3 digits</li>
 * <li>00260: running permit number counter, zero padded to 5 digits</li>
 * <li>2: checksum, calculated just as finnish creditor reference (suomalainen viitenumero)</li>
 * </ul>
 */
public class FinnishHuntingPermitNumberValidator {

    private static final Pattern REGEX_PATTERN = Pattern.compile("[1-9][0-9]{3}-[1-5]-[0-9]{3}-[0-9]{5}-[0-9]");
    private static final int[] WEIGHTS = new int[]{7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7};
    private static final int VALID_LENGTH = 18;

    public static boolean validate(final String value, final boolean verifyChecksum) {
        if (value == null || value.isEmpty()) {
            return true;
        }

        if (value.length() != VALID_LENGTH) {
            return false;
        }

        if (!REGEX_PATTERN.matcher(value).matches()) {
            return false;
        }

        if (!verifyChecksum) {
            return true;
        }

        final char checksum = value.charAt(value.length() - 1);
        final char calculatedChecksum = calculateChecksum(value);

        return checksum == calculatedChecksum;
    }


    private static char calculateChecksum(final String s) {
        return calculateChecksumOnlyDigits(onlyDigits(s));
    }

    private static String onlyDigits(String value) {
        return value.replaceAll("-", "");
    }

    private static char calculateChecksumOnlyDigits(final String s) {
        int sum = 0;

        for (int i = 0; i < WEIGHTS.length; i++) {
            sum += Character.getNumericValue(s.charAt(i)) * WEIGHTS[i];
        }

        final int remainder = sum % 10;

        if (remainder == 0) {
            return '0';
        }
        return Character.forDigit(10 - remainder, 10);
    }
}
