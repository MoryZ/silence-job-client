package com.old.silence.job.client.common.log.report;

import com.old.silence.job.client.common.LogReport;

import java.util.ArrayList;
import java.util.List;


public final class LogReportFactory {

    private static final List<LogReport> REPORTS = new ArrayList<>();

    private LogReportFactory() {
    }

    static void add(LogReport logReport) {
        REPORTS.add(logReport);
    }

    public static LogReport get() {

        for (LogReport report : REPORTS) {
            if (report.supports()) {
                return report;
            }
        }

        return null;
    }

}
