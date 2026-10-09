package com.tukokadi.app;

import android.app.Application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

public class CrashApp extends Application {

    public static final String CRASH_FILE = "last_crash.txt";

    @Override
    public void onCreate() {
        super.onCreate();

        final Thread.UncaughtExceptionHandler oldHandler =
                Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                throwable.printStackTrace(pw);
                pw.flush();

                FileOutputStream fos =
                        new FileOutputStream(new File(getFilesDir(), CRASH_FILE));
                fos.write(sw.toString().getBytes());
                fos.close();
            } catch (Exception ignored) {
                // Never let crash reporting mask the original crash.
            }

            if (oldHandler != null) {
                oldHandler.uncaughtException(thread, throwable);
            }
        });
    }
}
