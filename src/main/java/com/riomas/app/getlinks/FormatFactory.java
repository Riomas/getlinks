package com.riomas.app.getlinks;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class FormatFactory {

    public static DateFormat getSecondsFormat() {
        return new SimpleDateFormat(PatternConstants.SS_SSS);
    }
    public static DateFormat getMinutesFormat() {
        return new SimpleDateFormat(PatternConstants.MM_SS);
    }
    public static DateFormat getHoursMinutesFormat() {
        return new SimpleDateFormat(PatternConstants.H_MM_SS);
    }
    public static DateFormat getLiteralMinutesFormat() {
        return new SimpleDateFormat(PatternConstants.M_M_S_S);
    }
    public static DateFormat getLiteralHoursMinutesFormat() {
        return new SimpleDateFormat(PatternConstants.H_H_M_M_S_S);
    }

}