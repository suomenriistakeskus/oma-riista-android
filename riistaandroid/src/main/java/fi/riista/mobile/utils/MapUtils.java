package fi.riista.mobile.utils;

import android.util.Pair;

/**
 * Helper class that provides coordinate conversion between WGS84 and ETRS-TM35FIN
 * The formulas can be found from http://docs.jhs-suositukset.fi/jhs-suositukset/JHS154_liite1/JHS154_liite1.html
 */
public class MapUtils {

    static double Ca = 6378137.0;
    static double Cb = 6356752.314245;
    static double Cf = 1.0 / 298.257223563;
    static double Cn = Cf / (2.0 - Cf);
    static double CA1 = Ca / (1.0 + Cn) * (1.0 + Math.pow(Cn, 2.0) / 4.0 + Math.pow(Cn, 4) / 64.0);
    static double Ch1 = 1.0 / 2.0 * Cn - 2.0 / 3.0 * Math.pow(Cn, 2.0) + 37.0 / 96.0 * Math.pow(Cn, 3.0) - 1.0 / 360.0 * Math.pow(Cn, 4.0);
    static double Ch2 = 1.0 / 48.0 * Math.pow(Cn, 2) + 1.0 / 15.0 * Math.pow(Cn, 3) - 437.0 / 1440.0 * Math.pow(Cn, 4);
    static double Ch3 = 17.0 / 480.0 * Math.pow(Cn, 3) - 37.0 / 840.0 * Math.pow(Cn, 4);
    static double Ch4 = 4397.0 / 161280.0 * Math.pow(Cn, 4);
    static double Ch1p = 1.0 / 2.0 * Cn - 2.0 / 3.0 * Math.pow(Cn, 2) + 5.0 / 16.0 * Math.pow(Cn, 3) + 41.0 / 180.0 * Math.pow(Cn, 4);
    static double Ch2p = 13.0 / 48.0 * Math.pow(Cn, 2) - 3.0 / 5.0 * Math.pow(Cn, 3) + 557.0 / 1440.0 * Math.pow(Cn, 4);
    static double Ch3p = 61.0 / 240.0 * Math.pow(Cn, 3) - 103.0 / 140.0 * Math.pow(Cn, 4);
    static double Ch4p = 49561.0 / 161280.0 * Math.pow(Cn, 4);
    static double Ce = Math.sqrt(2.0 * Cf - Math.pow(Cf, 2));
    static double Ck0 = 0.9996;
    static double Clo0 = deg2rad(27.0);
    static double CE0 = 500000.0;

    public static Pair<Long, Long> WGS84toETRSTM35FIN(Double latitude, double longitude) {
        double la = deg2rad(latitude);
        double lo = deg2rad(longitude);
        double Q = asinh(Math.tan(la)) - Ce * atanh(Ce * Math.sin(la));
        double be = Math.atan(Math.sinh(Q));
        double nnp = atanh(Math.cos(be) * Math.sin(lo - Clo0));
        double Ep = Math.asin(Math.sin(be) * Math.cosh(nnp));
        double E1 = Ch1p * Math.sin(2.0 * Ep) * Math.cosh(2.0 * nnp);
        double E2 = Ch2p * Math.sin(4.0 * Ep) * Math.cosh(4.0 * nnp);
        double E3 = Ch3p * Math.sin(6.0 * Ep) * Math.cosh(6.0 * nnp);
        double E4 = Ch4p * Math.sin(8.0 * Ep) * Math.cosh(8.0 * nnp);
        double nn1 = Ch1p * Math.cos(2.0 * Ep) * Math.sinh(2.0 * nnp);
        double nn2 = Ch2p * Math.cos(4.0 * Ep) * Math.sinh(4.0 * nnp);
        double nn3 = Ch3p * Math.cos(6.0 * Ep) * Math.sinh(6.0 * nnp);
        double nn4 = Ch4p * Math.cos(8.0 * Ep) * Math.sinh(8.0 * nnp);
        double E = Ep + E1 + E2 + E3 + E4;
        double nn = nnp + nn1 + nn2 + nn3 + nn4;
        long etrs_x = (long) (CA1 * E * Ck0);
        long etrs_y = (long) (CA1 * nn * Ck0 + CE0);
        return new Pair<Long, Long>(etrs_x, etrs_y);
    }

    public static Pair<Double, Double> ETRMStoWGS84(long etrs_x, long etrs_y) {
        double E = etrs_x / (CA1 * Ck0);
        double nn = (etrs_y - CE0) / (CA1 * Ck0);
        double E1p = Ch1 * Math.sin(2.0 * E) * Math.cosh(2.0 * nn);
        double E2p = Ch2 * Math.sin(4.0 * E) * Math.cosh(4.0 * nn);
        double E3p = Ch3 * Math.sin(6.0 * E) * Math.cosh(6.0 * nn);
        double E4p = Ch4 * Math.sin(8.0 * E) * Math.cosh(8.0 * nn);
        double nn1p = Ch1 * Math.cos(2.0 * E) * Math.sinh(2.0 * nn);
        double nn2p = Ch2 * Math.cos(4.0 * E) * Math.sinh(4.0 * nn);
        double nn3p = Ch3 * Math.cos(6.0 * E) * Math.sinh(6.0 * nn);
        double nn4p = Ch4 * Math.cos(8.0 * E) * Math.sinh(8.0 * nn);
        double Ep = E - E1p - E2p - E3p - E4p;
        double nnp = nn - nn1p - nn2p - nn3p - nn4p;
        double be = Math.asin(Math.sin(Ep) / Math.cosh(nnp));
        double Q = asinh(Math.tan(be));
        double Qp = Q + Ce * atanh(Ce * Math.tanh(Q));
        Qp = Q + Ce * atanh(Ce * Math.tanh(Qp));
        Qp = Q + Ce * atanh(Ce * Math.tanh(Qp));
        Qp = Q + Ce * atanh(Ce * Math.tanh(Qp));
        double latitude = rad2deg(Math.atan(Math.sinh(Qp)));
        double longitude = rad2deg(Clo0 + Math.asin(Math.tanh(nnp) / Math.cos(be)));
        return new Pair<Double, Double>(latitude, longitude);
    }

    static double asinh(double x) {
        return Math.log(x + Math.sqrt(x * x + 1.0));
    }

    static double acosh(double x) {
        return Math.log(x + Math.sqrt(x * x - 1.0));
    }

    static double atanh(double x) {
        return 0.5 * Math.log((x + 1.0) / (1.0 - x));
    }

    static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    static double rad2deg(double rad) {
        return (rad * 180) / Math.PI;
    }
}
