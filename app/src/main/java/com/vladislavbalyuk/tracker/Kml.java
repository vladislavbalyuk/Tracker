package com.vladislavbalyuk.tracker;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Kml {

    public static void sendKml(Context ctx, Track track) {
        new SendKmlTask(ctx, track).execute();
    }

    static class  SendKmlTask extends AsyncTask<Void, Void, Void> {

    Context ctx;
    Track track;
    String pass;

    public SendKmlTask(Context ctx, Track track) {
        this.ctx = ctx;
        this.track = track;
    }

    @Override
    protected Void doInBackground(Void... params) {
        saveKml();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        Intent intentShareFile = new Intent(Intent.ACTION_SEND);
        File fileKml = new File(pass);

        if (fileKml.exists()) {
            intentShareFile.setType("text/plain");
            intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileKml));

            ctx.startActivity(Intent.createChooser(intentShareFile, "File kml"));
        }

    }

    public void saveKml() {

        TimeZone tz = TimeZone.getDefault();

        String passDir = Environment.getExternalStorageDirectory().getPath() + "/kml/";
        pass = passDir + track.getName() + ".kml";
        File fileDir = new File(passDir);
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }

        if (track.getPoints().size() > 0) {
            String startAdress = track.getPoints().get(0).getAdress();
            String endAdress = track.getPoints().get(track.getPoints().size() - 1).getAdress();
            Date startDate = track.getPoints().get(0).getDate();
            Date endDate = track.getPoints().get(0).getDate();

            SimpleDateFormat localSdf = new SimpleDateFormat("yyyyMMddhhmmss'000'", Locale.getDefault());
            SimpleDateFormat gmtSdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'.000Z'", Locale.getDefault());
            gmtSdf.setTimeZone(TimeZone.getTimeZone("GMT"));

            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(pass, false));
                writer.write("<?xml version='1.0' encoding='UTF-8' ?>" + "\r\n");
                writer.write("<kml xmlns=\"http://earth.google.com/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\">" + "\r\n");
                writer.write("  <Document>" + "\r\n");
                writer.write("    <name>" + track.getName() + "</name>" + "\r\n");
                writer.write("    <ExtendedData>" + "\r\n");
                writer.write("      <tracksStartAddress>" + startAdress + " </tracksStartAddress>" + "\r\n");
                writer.write("      <tracksEndAddress>" + endAdress + " </tracksEndAddress>" + "\r\n");
                writer.write("    </ExtendedData>" + "\r\n");
                writer.write("    <TimeStamp>" + "\r\n");
                writer.write("      <when>" + gmtSdf.format(startDate) + "</when>" + "\r\n");
                writer.write("    </TimeStamp>" + "\r\n");
                writer.write("    <TimeStampLocal>" + "\r\n");
                writer.write("      <when>" + localSdf.format(startDate) + "</when>" + "\r\n");
                writer.write("    </TimeStampLocal>" + "\r\n");
                writer.write("    <Style id=\"redLine1\">" + "\r\n");
                writer.write("      <LineStyle>" + "\r\n");
                writer.write("        <color>FFAE0000</color>" + "\r\n");
                writer.write("        <width>4</width>" + "\r\n");
                writer.write("      </LineStyle>" + "\r\n");
                writer.write("    </Style>" + "\r\n");
                writer.write("    <Placemark>" + "\r\n");
                writer.write("      <name>#1." + track.getName() + "</name>" + "\r\n");
                writer.write("      <TimeZone>" + "\r\n");
                writer.write("        <displayName>" + tz.getDisplayName() + "</displayName>" + "\r\n");
                writer.write("        <offset>" + tz.getRawOffset() + "</offset>" + "\r\n");
                writer.write("      </TimeZone>" + "\r\n");
                writer.write("      <TimeSpan>" + "\r\n");
                writer.write("        <begin>" + gmtSdf.format(startDate) + "</begin>" + "\r\n");
                writer.write("        <end>" + gmtSdf.format(endDate) + "</end>" + "\r\n");
                writer.write("      </TimeSpan>" + "\r\n");
                writer.write("      <TimeSpanLocal>" + "\r\n");
                writer.write("        <begin>" + localSdf.format(startDate) + "</begin>" + "\r\n");
                writer.write("        <end>" + localSdf.format(endDate) + "</end>" + "\r\n");
                writer.write("      </TimeSpanLocal>" + "\r\n");
                writer.write("      <styleUrl>#redLine1</styleUrl>" + "\r\n");
                writer.write("      <gx:MultiTrack>" + "\r\n");
                writer.write("        <altitudeMode>absolute</altitudeMode>" + "\r\n");
                writer.write("        <gx:interpolate>0</gx:interpolate>" + "\r\n");
                writer.write("        <gx:Track>" + "\r\n");
                for (Point point : track.getPoints()) {
                    writer.write("          <gx:coord>" + String.valueOf(point.getLongitude()) + " " + String.valueOf(point.getLatitude()) + " " + String.valueOf(point.getAltitude()) + "</gx:coord>" + "\r\n");
                    writer.write("          <when>" + gmtSdf.format(point.getDate()) + "</when>" + "\r\n");
                }
                writer.write("        </gx:Track>" + "\r\n");
                writer.write("      </gx:MultiTrack>" + "\r\n");
                writer.write("    </Placemark>" + "\r\n");
                writer.write("  </Document>" + "\r\n");
                writer.write("</kml>");
                writer.close();
            } catch (IOException e) {

                e.printStackTrace();
            }

        }
    }

}

}
