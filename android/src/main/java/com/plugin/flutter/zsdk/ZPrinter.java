package com.plugin.flutter.zsdk;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.graphics.internal.ZebraImageAndroid;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.SGD;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.graphics.ZebraImageFactory;
 
import com.zebra.sdk.printer.ZebraPrinterLinkOs;
import com.zebra.sdk.util.internal.FileUtilities;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.Result;

import android.os.Looper;

import java.io.FileOutputStream;

import io.flutter.plugin.common.MethodChannel.Result;

import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;

 

/**
 * Created by luis901101 on 2019-12-18.
 * @noinspection unused
 */
public class ZPrinter
{
    protected Context context;
    protected MethodChannel channel;
    protected Result result;
    protected final Handler handler = new Handler();
    protected PrinterConf printerConf;

    public ZPrinter(Context context, MethodChannel channel, Result result, PrinterConf printerConf)
    {
        this.context = context;
        this.channel = channel;
        this.result = result;
        this.printerConf = printerConf != null ? printerConf : new PrinterConf();
    }

    private TcpConnection newConnection(String address, int tcpPort){
        int MAX_TIME_OUT_FOR_READ = 5000;
        int TIME_TO_WAIT_FOR_MORE_DATA = 0;
        return new TcpConnection(address, tcpPort, MAX_TIME_OUT_FOR_READ, TIME_TO_WAIT_FOR_MORE_DATA);
    }

    protected void init(Connection connection){
        printerConf.init(connection);
    }

    private void onConnectionTimeOut(ConnectionException e){
        if(e != null) e.printStackTrace();
        PrinterResponse response = new PrinterResponse(ErrorCode.EXCEPTION,
                new StatusInfo(Status.UNKNOWN, Cause.NO_CONNECTION), "Connection timeout. "+e);
        handler.post(() -> result.error(response.errorCode.name(), response.message, response.toMap()));
    }

    /** @noinspection SameParameterValue*/
    private void onPrinterRebooted(String message){
        handler.post(() -> result.success(
                new PrinterResponse(
                        ErrorCode.PRINTER_REBOOTED,
                        new StatusInfo(Status.UNKNOWN, Cause.NO_CONNECTION),
                        message
                ).toMap()
        ));
    }

    private void onException(Exception e, ZebraPrinter printer){
        if(e != null) e.printStackTrace();
        PrinterResponse response = new PrinterResponse(ErrorCode.EXCEPTION,
                getStatusInfo(printer), "Unknown exception. "+e);
        handler.post(() -> result.error(response.errorCode.name(), response.message, response.toMap()));
    }

    public void doManualCalibrationOverTCPIP(final String address, final Integer port) {
        new Thread(() -> {
            Connection connection;
            ZebraPrinter printer = null;
            try
            {
                int tcpPort = port != null ? port : TcpConnection.DEFAULT_ZPL_TCP_PORT;

                connection = newConnection(address, tcpPort);
                connection.open();

                try {
                    printer = ZebraPrinterFactory.getInstance(connection);
//                    SGD.DO(SGDParams.KEY_MANUAL_CALIBRATION, null, connection);
                    printer.calibrate();
                    PrinterResponse response = new PrinterResponse(ErrorCode.SUCCESS,
                            getStatusInfo(printer), "Printer status");
                    handler.post(() -> result.success(response.toMap()));
                } finally {
                    connection.close();
                }
            }
            catch(ConnectionException e)
            {
                onConnectionTimeOut(e);
            }
            catch(Exception e)
            {
                onException(e, printer);
            }
        }).start();
    }

    public void printConfigurationLabelOverTCPIP(final String address, final Integer port) {
        new Thread(() -> {
            Connection connection;
            ZebraPrinter printer = null;
            try
            {
                int tcpPort = port != null ? port : TcpConnection.DEFAULT_ZPL_TCP_PORT;

                connection = newConnection(address, tcpPort);
                connection.open();

                try {
                    printer = ZebraPrinterFactory.getInstance(connection);
                    printer.printConfigurationLabel();
                    PrinterResponse response = new PrinterResponse(ErrorCode.SUCCESS,
                            getStatusInfo(printer), "Printer status");
                    handler.post(() -> result.success(response.toMap()));
                } finally {
                    connection.close();
                }
            }
            catch(ConnectionException e)
            {
                onConnectionTimeOut(e);
            }
            catch(Exception e)
            {
                onException(e, printer);
            }
        }).start();
    }

    public void checkPrinterStatusOverTCPIP(final String address, final Integer port) {
        new Thread(() -> {
            Connection connection;
            ZebraPrinter printer = null;
            try
            {
                int tcpPort = port != null ? port : TcpConnection.DEFAULT_ZPL_TCP_PORT;

                connection = newConnection(address, tcpPort);
                connection.open();

                try {
                    printer = ZebraPrinterFactory.getInstance(connection);

                    PrinterResponse response = new PrinterResponse(ErrorCode.SUCCESS,
                            getStatusInfo(printer), "Printer status");
                    handler.post(() -> result.success(response.toMap()));
                } finally {
                    connection.close();
                }
            }
            catch(ConnectionException e)
            {
                onConnectionTimeOut(e);
            }
            catch(Exception e)
            {
                onException(e, printer);
            }
        }).start();
    }

    public void getPrinterSettingsOverTCPIP(final String address, final Integer port) {
        new Thread(() -> {
            Connection connection;
            ZebraPrinter printer = null;
            try
            {
                int tcpPort = port != null ? port : TcpConnection.DEFAULT_ZPL_TCP_PORT;

                connection = newConnection(address, tcpPort);
                connection.open();

                try {
                    printer = ZebraPrinterFactory.getInstance(connection);
                    PrinterSettings settings = PrinterSettings.get(connection);
                    PrinterResponse response = new PrinterResponse(ErrorCode.SUCCESS,
                            getStatusInfo(printer), settings, "Printer status");
                    handler.post(() -> result.success(response.toMap()));
                } finally {
                    connection.close();
                }
            }
            catch(ConnectionException e)
            {
                onConnectionTimeOut(e);
            }
            catch(Exception e)
            {
                onException(e, printer);
            }
        }).start();
    }

    public void setPrinterSettingsOverTCPIP(final String address, final Integer port, final PrinterSettings settings) {
        new Thread(() -> {
            Connection connection;
            ZebraPrinter printer = null;
            try
            {
                if(settings == null) throw new NullPointerException("Settings can't be null");

                int tcpPort = port != null ? port : TcpConnection.DEFAULT_ZPL_TCP_PORT;

                connection = newConnection(address, tcpPort);
                connection.open();

                try {
                    printer = ZebraPrinterFactory.getInstance(connection);
                    settings.apply(connection);
                    if(!connection.isConnected()) {
                        onPrinterRebooted("New settings required Printer to reboot");
                        return;
                    }
                    PrinterSettings currentSettings = PrinterSettings.get(connection);
                    PrinterResponse response = new PrinterResponse(ErrorCode.SUCCESS,
                            getStatusInfo(printer), currentSettings, "Printer status");
                    handler.post(() -> result.success(response.toMap()));
                } finally {
                    connection.close();
                }
            }
            catch(ConnectionException e)
            {
                onConnectionTimeOut(e);
            }
            catch(Exception e)
            {
                onException(e, printer);
            }
        }).start();
    }

    /** @noinspection IOStreamConstructor*/
    public void printPdfFileOverTCPIP(final String filePath, final String address, final Integer port) {
        new Thread(() -> {
            try
            {
                if(!new File(filePath).exists()) throw new FileNotFoundException("The file: "+ filePath +"doesn't exist");

                doPrintDataStreamOverTCPIP(new FileInputStream(filePath), address, port, false);
            }
            catch(Exception e)
            {
                onException(e, null);
            }
        }).start();
    }

    /** This function needs more tests as it relies on converting the PDF to image. */
    public void printPdfAsImageOverTCPIP(final String filePath, final String address, final Integer port) {
        new Thread(() -> {
            Connection connection;
            ZebraPrinter printer = null;
            try
            {
                if(!new File(filePath).exists()) throw new FileNotFoundException("The file: "+ filePath +"doesn't exist");
                int tcpPort = port != null ? port : TcpConnection.DEFAULT_ZPL_TCP_PORT;

                connection = newConnection(address, tcpPort);
                connection.open();

                try {
                    printer = ZebraPrinterFactory.getInstance(connection);
                    if (isReadyToPrint(printer)) {
                        init(connection);

                        List<ImageData> list = PdfUtils.getImagesFromPdf(context, filePath, printerConf.getWidth(), printerConf.getHeight(), printerConf.getOrientation(), true);
                        for(int i = 0; i < list.size(); ++i) {
                            printer.printImage(new ZebraImageAndroid(list.get(i).bitmap), 0, 0, -1, -1, false);//Prints image directly from bitmap
//                            printer.printImage(list.get(i).path, 0, 0);//Prints image from file path
                        }
                        PrinterResponse response = new PrinterResponse(ErrorCode.SUCCESS,
                                getStatusInfo(printer), "Successful print");
                        handler.post(() -> result.success(response.toMap()));
                    } else {
                        PrinterResponse response = new PrinterResponse(ErrorCode.PRINTER_ERROR,
                                getStatusInfo(printer), "Printer is not ready");
                        handler.post(() -> result.error(ErrorCode.PRINTER_ERROR.name(),
                                response.message, response.toMap()));
                    }

                } finally {
                    connection.close();
                }
            }
            catch(ConnectionException e)
            {
                onConnectionTimeOut(e);
            }
            catch(Exception e)
            {
                onException(e, printer);
            }
        }).start();
    }


public void sendZplOverBluetooth(final String theBtMacAddress, final String imagePath, final PrinterSettings settings, Result result) {

    new Thread(new Runnable() {
        public void run() {
            try {
                System.out.println("******* STARTING *******");

                System.out.println("******** USING IMAGE PATH  ******** => "+  imagePath);

                // Check if the file exists
                File imageFile = new File(imagePath);
                if (!imageFile.exists()) {
                    throw new FileNotFoundException("The file: " + imagePath + " doesn't exist");
                }

                System.out.println("******** IMAGE PATH FOUND ********");

                // Instantiate connection for given Bluetooth® MAC Address.
                Connection thePrinterConn = new BluetoothConnection(theBtMacAddress);

                // Initialize
                Looper.prepare();

                // Open the connection - physical connection is established here.
                thePrinterConn.open();

                // Set the printer language to ZPL
                changePrinterLanguage(thePrinterConn, SGDParams.VALUE_ZPL_LANGUAGE);

                System.out.println("******** PRINTER LANGUAGE UPDATED TO ZPL ********"); 

                // Initialize printer configuration
                printerConf.init(thePrinterConn);
                // settings.apply(thePrinterConn);

                ZebraPrinter printer = ZebraPrinterFactory.getInstance(thePrinterConn); 
               
                // Decode the image from the file path
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(imageFile));
                if (bitmap == null) {
                    throw new Exception("Failed to decode the image from the path: " + imagePath);
                }

                 PrinterStatus printerStatus = printer.getCurrentStatus();

                ///for ZQ300 series printer only / 72mm-80mm paper roll
                int printableWidth = 576; 

                // Get image dimensions
                int imageWidth = bitmap.getWidth();
                int imageHeight = bitmap.getHeight();

                // Calculate new height to maintain aspect ratio
                int newWidth = printableWidth;
                int newHeight =   (imageHeight * newWidth) / imageWidth;

                // Resize the image based on the printer settings
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

                System.out.println("********* SENDING PRINT REQ. *********");

                // Convert the resized Bitmap to ZebraImageAndroid
                ZebraImageAndroid image = new ZebraImageAndroid(resizedBitmap);

                printer.storeImage("E:TEMP", image, -1, -1);

                // print zpl content by converting imagr into GRF format
                String zplString =
                    "^XA"                                          // Start format
                    + "^PW" + ((int) printableWidth)               // Print width
                    + "^MNN"                                       // Media tracking mode (Mark-Sense/Continuous)
                    + "^LL" + newHeight                             // Label length (height)
                    + "^PR2"                                        // Print rate (equivalent to SPEED 2)
                    + "^MD50"                                       // Darkness setting (equivalent to TONE 50)
                    + "^FO0,0^XG" + "TEMP" + ",1,1^FS"              // Draw graphic stored on the printer
                    + "^XZ";                                        // End format

                    //send the commands to the printer, the image will be printed now
                    thePrinterConn.write(zplString.getBytes());

                    
                    //delete the image at the end to prevent printer memory sutaration
                    thePrinterConn.write(("! U1 do \"file.delete\" \"E:TEMP\"\r\n").getBytes());
                    
                    //close the connection with the printer
                    thePrinterConn.close();

                    resizedBitmap.recycle();
                    imageFile.delete();


                //  printer.printImage( image, 0, 0, newWidth, newHeight, false);
                // printer.printImage(new ZebraImageAndroid(bufferedImage), 0, 0, -1, -1, false);
               
                System.out.println("********* DONE PRINT REQ. *********");
 
                Thread.sleep(500);

                Looper.myLooper().quit();

                result.success(true);
            } catch (Exception e) {
                System.out.println(e);
                // Handle communications error here.
                e.printStackTrace();
            }
        }
    }).start();
}

// Method to resize the bitmap based on the printer's settings
private Bitmap resizeBitmap(Bitmap bitmap, int width, int height) {
    return Bitmap.createScaledBitmap(bitmap, width, height, true);
}

    /** @noinspection IOStreamConstructor*/
    public void printZplFileOverTCPIP(final String filePath, final String address, final Integer port) {
        new Thread(() -> {
            try
            {
                if(!new File(filePath).exists()) throw new FileNotFoundException("The file: "+ filePath +"doesn't exist");

                doPrintDataStreamOverTCPIP(new FileInputStream(filePath), address, port, true);
            }
            catch(Exception e)
            {
                onException(e, null);
            }
        }).start();
    }

    /** @noinspection CharsetObjectCanBeUsed*/
    public void printZplDataOverTCPIP(final String data, final String address, final Integer port) {
        if(data == null || data.isEmpty()) throw new NullPointerException("ZPL data can not be empty");
        new Thread(() -> doPrintDataStreamOverTCPIP(
            new ByteArrayInputStream(data.getBytes(Charset.forName("UTF-8"))), address, port, true))
            .start();
    }

    private void doPrintDataStreamOverTCPIP(final InputStream dataStream, final String address, final Integer port, boolean isZPL) {
        Connection connection;
        ZebraPrinter printer = null;
        try
        {
            if(dataStream == null) throw new NullPointerException("Data stream can not be empty");
            int tcpPort = port != null ? port : TcpConnection.DEFAULT_ZPL_TCP_PORT;

            connection = newConnection(address, tcpPort);
            connection.open();

            try {
                printer = ZebraPrinterFactory.getInstance(connection);
                if (isReadyToPrint(printer)) {
                    init(connection);
                    if(isZPL) {
                        changePrinterLanguage(connection, SGDParams.VALUE_ZPL_LANGUAGE);
                    } else {
                        // If enablePDFDirect was required, then abort printing as the Printer will be rebooted and the connection will be closed.
                        if(enablePDFDirect(connection, true)) {
                            onPrinterRebooted("Printer was rebooted to be able to use PDF Direct feature.");
                            return;
                        }
                    }
//                    connection.write(data.getBytes()); //This would be to send zpl as a string, this fails if the string is too big
//                    printer.sendFileContents(filePath); //This would be to send zpl as a file path
                    FileUtilities.sendFileContentsInChunks(connection, dataStream); //This is the recommended way.
                    PrinterResponse response = new PrinterResponse(ErrorCode.SUCCESS,
                            getStatusInfo(printer), "Successful print");
                    handler.post(() -> result.success(response.toMap()));
                } else {
                    PrinterResponse response = new PrinterResponse(ErrorCode.PRINTER_ERROR,
                            getStatusInfo(printer), "Printer is not ready");
                    handler.post(() -> result.error(ErrorCode.PRINTER_ERROR.name(),
                            response.message, response.toMap()));
                }

            } finally {
                connection.close();
            }
        }
        catch(ConnectionException e)
        {
            onConnectionTimeOut(e);
        }
        catch(Exception e)
        {
            onException(e, printer);
        }
    }

    /** Sees if the printer is ready to print */
    public boolean isReadyToPrint(ZebraPrinter printer) {
        try {
            return printer.getCurrentStatus().isReadyToPrint;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Get printer status and cause in order to know when printer is not ready why is not ready*/
    public StatusInfo getStatusInfo(ZebraPrinter printer) {
        Status status = Status.UNKNOWN;
        Cause cause = Cause.UNKNOWN;
        if(printer != null) {
            try {
                PrinterStatus printerStatus = printer.getCurrentStatus();

                if(printerStatus.isPaused) status = Status.PAUSED;
                if(printerStatus.isReadyToPrint) status = Status.READY_TO_PRINT;

                if(printerStatus.isPartialFormatInProgress) cause = Cause.PARTIAL_FORMAT_IN_PROGRESS;
                if(printerStatus.isHeadCold) cause = Cause.HEAD_COLD;
                if(printerStatus.isHeadOpen) cause = Cause.HEAD_OPEN;
                if(printerStatus.isHeadTooHot) cause = Cause.HEAD_TOO_HOT;
                if(printerStatus.isPaperOut) cause = Cause.PAPER_OUT;
                if(printerStatus.isRibbonOut) cause = Cause.RIBBON_OUT;
                if(printerStatus.isReceiveBufferFull) cause = Cause.RECEIVE_BUFFER_FULL;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new StatusInfo(status, cause);
    }

    /**
     * This printMethod implements best practices to check the language of the printer and set the language of the printer to ZPL.
     */
    public void changePrinterLanguage(Connection connection, String language) throws ConnectionException {
        if(connection == null) return;
        if(!connection.isConnected()) connection.open();

        if(language == null) language = SGDParams.VALUE_ZPL_LANGUAGE;

        final String printerLanguage = SGD.GET(SGDParams.KEY_PRINTER_LANGUAGES, connection);
        if(!printerLanguage.equals(language))
            SGD.SET(SGDParams.KEY_PRINTER_LANGUAGES, language, connection);
    }

    /**
     * Returns true if the PDF Direct feature was not enabled and requires to be enabled which means the Printer is going to be rebooted, false otherwise.
     * */
    public boolean enablePDFDirect(Connection connection, boolean enable) throws Exception {
        if(connection == null) return false;
        if(!connection.isConnected()) connection.open();

        return VirtualDeviceUtils.changeVirtualDevice(connection, enable ? SGDParams.VALUE_PDF : SGDParams.VALUE_NONE);
    }

    public void printAllValues(Connection connection) throws Exception {
        Log.e("allSettingsValues", SGD.GET(SGDParams.VALUE_GET_ALL, connection));
    }

    public void printAllSettings(Connection connection) throws Exception {
        if(connection == null) return;
        if(!connection.isConnected()) connection.open();
        ZebraPrinterLinkOs printerLinkOs = ZebraPrinterFactory.getLinkOsPrinter(connection);
        Map<String, String> allSettings = printerLinkOs.getAllSettingValues();
        for(String key : allSettings.keySet())
            Log.e("paramSetting", key+"--->"+allSettings.get(key));
    }

    public void rebootPrinter(final String address, final Integer port) {
        new Thread(() -> {
            try {
                int tcpPort = port != null ? port : TcpConnection.DEFAULT_ZPL_TCP_PORT;
                if(PrinterUtils.reboot(newConnection(address, tcpPort))) {
                    onPrinterRebooted("Printer successfully rebooted");
                } else {
                    PrinterResponse response = new PrinterResponse(ErrorCode.EXCEPTION,
                            new StatusInfo(Status.UNKNOWN, Cause.UNKNOWN), "Printer could not be rebooted.");
                    handler.post(() -> result.error(response.errorCode.name(), response.message, response.toMap()));
                }
            }
            catch(ConnectionException e) {
                onConnectionTimeOut(e);
            }
            catch(Exception e)
            {
                onException(e, null);
            }
        }).start();
    }

    /** Takes the size of the pdf and the printer's maximum size and scales the file down */
    private String scalePrint (Connection connection, String filePath) throws Exception
    {
        int fileWidth = PdfUtils.getPageWidth(context, filePath);
        String scale = "dither scale-to-fit";

        if (fileWidth != 0) {
            String printerModel = SGD.GET("device.host_identification",connection).substring(0,5);
            double scaleFactor;

            switch(printerModel)
            {
                case "iMZ22":
                case "QLn22":
                case "ZD410":
                    scaleFactor = 2.0 / fileWidth * 100;
                    break;
                case "iMZ32":
                case "QLn32":
                case "ZQ510":
                    scaleFactor = 3.0 / fileWidth * 100;
                    break;
                case "QLn42":
                case "ZQ520":
                case "ZD420":
                case "ZD500":
                case "ZT220":
                case "ZT230":
                case "ZT410":
                    scaleFactor = 4.0 / fileWidth * 100;
                    break;
                case "ZT420":
                    scaleFactor = 6.5 / fileWidth * 100;
                    break;
                default:
                    scaleFactor = 100;
                    break;
            }

            scale = "dither scale=" + (int) scaleFactor + "x" + (int) scaleFactor;
        }

        return scale;
    }
}
