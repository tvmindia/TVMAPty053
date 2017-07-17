package com.tech.thrithvam.partyec;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;


@ReportsCrashes(
        formUri =  "http://192.168.1.109:456/api/common/InsertErrorLog",
        reportType = HttpSender.Type.JSON,
        httpMethod = HttpSender.Method.POST,
        formUriBasicAuthLogin = "partyec@tvm-2017",
        formUriBasicAuthPassword = "",
        customReportContent = { ReportField.ANDROID_VERSION,
                ReportField.APP_VERSION_CODE,
                ReportField.AVAILABLE_MEM_SIZE,
                ReportField.BUILD,
                ReportField.CRASH_CONFIGURATION,
                ReportField.LOGCAT,
                ReportField.PACKAGE_NAME,
                ReportField.REPORT_ID},
        mode = ReportingInteractionMode.SILENT
)
public class AppApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
//        ACRA.init(this);
    }
}
