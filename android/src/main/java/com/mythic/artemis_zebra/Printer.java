package com.mythic.artemis_zebra;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.discovery.BluetoothDiscoverer;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveryHandler;
import com.zebra.sdk.printer.discovery.NetworkDiscoverer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;
import pub.devrel.easypermissions.EasyPermissions;

public class Printer extends Service implements MethodChannel.MethodCallHandler {
    private static final int ACCESS_COARSE_LOCATION_REQUEST_CODE = 100021;
    private static final int ON_DISCOVERY_ERROR_GENERAL = -1;
    private static final int ON_DISCOVERY_ERROR_BLUETOOTH = -2;
    private static final int ON_DISCOVERY_ERROR_LOCATION = -3;
    private Connection printerConnection;
    private ZebraPrinter printer;
    private Context context;
    private ActivityPluginBinding binding;
    private MethodChannel methodChannel;
    private String selectedAddress = null;
    private String macAddress = null;
    private boolean tempIsPrinterConnect;
    private static ArrayList<DiscoveredPrinter> discoveredPrinters = new ArrayList<>();
    private static ArrayList<DiscoveredPrinter> sendedDiscoveredPrinters = new ArrayList<>();
    private static int countDiscovery = 0;
    private static int countEndScan = 0;
    private boolean isZebraPrinter = true;
    private Socketmanager socketmanager;

    public Printer() {
        // Default constructor is required for the Android service to work correctly
    }

    public Printer(ActivityPluginBinding binding, BinaryMessenger binaryMessenger) {
        this.context = binding.getActivity();
        this.binding = binding;
        this.methodChannel = new MethodChannel(binaryMessenger, "ZebraPrinterInstance" + this.toString());
        methodChannel.setMethodCallHandler(this);
    }

    @Override
    public void onMethodCall(@NonNull final MethodCall call, @NonNull final MethodChannel.Result result) {

        switch (call.method) {
            case "checkPermissions":
                checkPermissions(result);
                break;

            case "discoverPrinters":
                discoverPrinters(context, result);
                break;

            case "connectToPrinter":
                String address = Objects.requireNonNull(call.argument("address")).toString();
                connectToPrinter(address, result);
                break;

            case "printData":
                String data = Objects.requireNonNull(call.argument("data")).toString();
                printData(data, result);
                break;

            case "disconnectPrinter":
                disconnectPrinter(result);
                break;

            case "isPrinterConnected":
                isPrinterConnect(result);
                break;


            case "checkPrinterStatus":
                checkPrinterStatus(result);
                break;

            default:
                result.notImplemented();
        }

//        if (call.method.equals("print")) {
//            print(call.argument("Data").toString());
//        } else if (call.method.equals("checkPermission")) {
//            checkPermission(context, result);
//        } else if (call.method.equals("convertBase64ImageToZPLString")) {
//            convertBase64ImageToZPLString(call.argument("Data").toString())
//                    , Integer.valueOf(call.argument("rotation").toString()), result);
//        } else if (call.method.equals("disconnect")) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    disconnect();
//
//                }
//            }).start();
//        } else if (call.method.equals("isPrinterConnected")) {
//            isPrinterConnect();
//        } else if (call.method.equals("discoverPrinters")) {
//            if (checkIsLocationNetworkProviderIsOn()) {
//                discoveryPrinters(context, methodChannel);
//            } else {
//                onDiscoveryError(context, methodChannel, ON_DISCOVERY_ERROR_LOCATION, "Your location service is off.");
//            }
//
//        } else if (call.method.equals("setMediaType")) {
//            String mediaType = call.argument("MediaType");
//            setMediaType(mediaType);
//        } else if (call.method.equals("setSettings")) {
//            String settingCommand = call.argument("SettingCommand");
//            setSettings(settingCommand);
//        } else if (call.method.equals("setDarkness")) {
//            int darkness = call.argument("Darkness");
//            setDarkness(darkness);
//        } else if (call.method.equals("connectToPrinter")) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    final boolean r = false;
//                    connectToSelectPrinter(call.argument("Address").toString());
//                    ((Activity) context).runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (r) result.success(true);
//                            else result.success(false);
//                        }
//                    });
//                }
//            }).start();
//
//        } else if (call.method.equals(("connectToGenericPrinter"))) {
//            connectToGenericPrinter(call.argument("Address").toString());
//        } else {
//            result.notImplemented();
//        }
    }

    public void discoverPrinters(final Context context, final MethodChannel.Result result) {
        try {
//            BluetoothDiscoverer.findPrinters(context,
//                @Override
//                public void foundPrinter(final DiscoveredPrinter discoveredPrinter) {
//                    Log.println(Log.ASSERT,"Printer Found!!!","Printer Found");
////                    discoveredPrinters.add(discoveredPrinter);
//                    ((Activity) context).runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//
//                            Log.println(Log.ASSERT,"Printer Found!!!","Printer Found");
//
//                        }
//                    });
//                }
//                @Override
//
//
//            );

            BluetoothDiscoverer.findPrinters(this.context, new DiscoveryHandler() {
                @Override
                public void foundPrinter(final DiscoveredPrinter discoveredPrinter) {
                    Log.println(Log.ASSERT, "Printer Found!!!", "Printer Found  &&&&");
                    ((Activity) context).runOnUiThread(() -> {

                        Log.println(Log.ASSERT, "Printer Found!!!", "Printer Found ****");
                        String address = discoveredPrinter.address;
                        String name = discoveredPrinter.getDiscoveryDataMap().get("FRIENDLY_NAME");
                        HashMap<String, Object> arguments = new HashMap<>();
                        arguments.put("address", address);
                        arguments.put("name", name);
                        arguments.put("type", 1);
                        arguments.put("isConnected", discoveredPrinter.getConnection().isConnected());
                        methodChannel.invokeMethod("printerFound", new Gson().toJson(arguments));
                    });
                }

                @Override
                public void discoveryFinished() {
                    countEndScan++;
                    Log.println(Log.ASSERT, "Bluetooth Discovery", "Discovery Finish");
                    if (countEndScan == 2) {
                        countEndScan = 0;
                        result.success("DiscoveryDone");
                    }
                }

                @Override
                public void discoveryError(String s) {
                    HashMap<String, Object> arguments = new HashMap<>();
                    arguments.put("error", s);
                    methodChannel.invokeMethod("discoveryError", arguments);
                }
            });


//            BluetoothDiscoverer.findPrinters(context, new DiscoveryHandler() {
//                @Override
//                public void foundPrinter(final DiscoveredPrinter discoveredPrinter) {
////                    discoveredPrinters.add(discoveredPrinter);
//                    ((Activity) context).runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Log.println(Log.ASSERT,"Printer Found!!!","Printer Found");
//                            HashMap<String, Object> arguments = new HashMap<>();
//                            arguments.put("address", discoveredPrinter.address);
//                            arguments.put("name", discoveredPrinter.getDiscoveryDataMap().get("FRIENDLY_NAME"));
//                            arguments.put("type", 1);
//                            methodChannel.invokeMethod("printerFound", new Gson().toJson(arguments));
//                        }
//                    });
//                }
//
//                @Override
//                public void discoveryFinished() {
//                    result.success("DiscoveryDone");
//                }
//
//                @Override
//                public void discoveryError(String s) {
//                    Log.println(Log.ASSERT,"P","FindPrintersError!!!!!"+s);
//
//                    HashMap<String, Object> arguments = new HashMap<>();
//                    arguments.put("error", s);
//                    methodChannel.invokeMethod("discoveryError", arguments);
//                }
//            });

//            BluetoothDiscoverer.findPrinters();
            NetworkDiscoverer.findPrinters(new DiscoveryHandler() {
                @Override
                public void foundPrinter(DiscoveredPrinter discoveredPrinter) {
                    ((Activity) context).runOnUiThread(() -> {
                        HashMap<String, Object> arguments = new HashMap<>();
                        arguments.put("address", discoveredPrinter.address);
                        arguments.put("name", discoveredPrinter.getDiscoveryDataMap().get("SYSTEM_NAME"));
                        arguments.put("type", 0);
                        arguments.put("isConnected", discoveredPrinter.getConnection().isConnected());
//                        arguments.put("isConnected", discoveredPrinter);
                        methodChannel.invokeMethod("printerFound", new Gson().toJson(arguments));
                    });
                }

                @Override
                public void discoveryFinished() {
                    countEndScan++;
                    Log.println(Log.ASSERT, "Network Discovery", "Discovery Finish");
                    if (countEndScan == 2) {
                        result.success("DiscoveryDone");
                    }
//
                }

                @Override
                public void discoveryError(String s) {
                    HashMap<String, Object> arguments = new HashMap<>();
                    arguments.put("error", s);
                    methodChannel.invokeMethod("discoveryError", arguments);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.println(Log.ASSERT, "P", e.toString());

        }
    }

    public void connectToPrinter(final String address, final MethodChannel.Result result) {
        if (address.contains(":")) {
            printerConnection = new BluetoothConnection(address);
        } else {
            try {
                printerConnection = new TcpConnection(address, getTcpPortNumber());
            } catch (NumberFormatException e) {
                result.error("1", e.toString(), e);
            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    printerConnection.open();
                } catch (ConnectionException e) {
                    DemoSleeper.sleep(1000);
                    disconnectPrinter(null);
                    result.success(false);
                }
                if (printerConnection.isConnected()) {
                    try {
                        printer = ZebraPrinterFactory.getInstance(printerConnection);
                        startService(address);
                        result.success(true);
                    } catch (ConnectionException e) {
                        printer = null;
                        DemoSleeper.sleep(1000);
                        disconnectPrinter(null);
                        stopService();
                        result.success(false);
                    } catch (ZebraPrinterLanguageUnknownException e) {
                        printer = null;
                        DemoSleeper.sleep(1000);
                        disconnectPrinter(null);
                        stopService();
                        result.success(false);

                    }
                }
            }
        }).start();

    }

    private void startService(final String address) {
        Intent serviceIntent = new Intent(context, Printer.class);
        serviceIntent.putExtra("printer_address", address);
        context.startService(serviceIntent);
    }

    private void stopService() {
        Intent serviceIntent = new Intent(context, Printer.class);
        context.stopService(serviceIntent);
    }

    private void printData(String data, final MethodChannel.Result result) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] bytes = convertDataToByte(data);
                    printerConnection.write(bytes);
                    DemoSleeper.sleep(1500);

                    if (printerConnection instanceof BluetoothConnection) {
                        DemoSleeper.sleep(500);
                    }
                    result.success(true);
                } catch (ConnectionException e) {
                    result.error("1", e.toString(), e);
                    disconnectPrinter(null);
                }
            }
        }).start();


    }

    public void disconnectPrinter(final MethodChannel.Result result) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isZebraPrinter) {
                        try {
                            if (printerConnection != null) {
                                printerConnection.close();
                            }

                        } catch (ConnectionException e) {
                            e.printStackTrace();
                        } finally {
                            if (result != null) {
                                stopService();
                                result.success(true);
                            }
                        }
                    } else {
                        socketmanager.close();
                        if (result != null) {
                            stopService();
                            result.success(true);
                        }
                    }
                } catch (Exception e) {
                    DemoSleeper.sleep(1000);
                    result.success(true);
                }

            }
        }).start();


    }

    public void isPrinterConnect(final MethodChannel.Result result) {
        if (isZebraPrinter) {
            tempIsPrinterConnect = true;
            if (printerConnection != null && printerConnection.isConnected()) {
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            printerConnection.write("Test".getBytes());
                        } catch (ConnectionException e) {
                            e.printStackTrace();
                            disconnectPrinter(null);
                            tempIsPrinterConnect = false;
                        }
                    }
                }).start();
                if (tempIsPrinterConnect) {
                    result.success(true);
                } else {
                    result.success(false);
                }
            } else {
                result.success(false);
            }
        } else {
            if (socketmanager != null) {
                if (socketmanager.getIstate()) {
                    result.success(true);
                } else {
                    result.success(false);
                }
            } else {
                result.success(false);
            }
        }
    }

    public void checkPrinterStatus(final MethodChannel.Result result) {
            tempIsPrinterConnect = true;
            if (printerConnection != null && printerConnection.isConnected()) {
                new Thread(new Runnable() {
                    public void run() {

                        try {
                            printerConnection.open();
                            ZebraPrinter printer = ZebraPrinterFactory.getInstance(printerConnection);

                            PrinterStatus printerStatus = printer.getCurrentStatus();

                            MyPrinterStatus myPrinterStatus = new MyPrinterStatus(printerStatus.isReadyToPrint, printerStatus.isHeadOpen, printerStatus.isHeadCold, printerStatus.isHeadTooHot,printerStatus.isPaperOut,printerStatus.isRibbonOut,printerStatus.isReceiveBufferFull,printerStatus.isPaused,printerStatus.labelLengthInDots,printerStatus.numberOfFormatsInReceiveBuffer,printerStatus.labelsRemainingInBatch,printerStatus.isPartialFormatInProgress,printerStatus.printMode.ordinal());

                            String jsonOutput = myPrinterStatus.toJson();
//                            if (jsonOutput != null) {
//                                System.out.println("JSON Output: " + jsonOutput);
//                            } else {
//                                System.out.println("Failed to convert to JSON.");
//                            }


                            result.success(jsonOutput);
                            if (printerStatus.isReadyToPrint) {
                                System.out.println("Ready To Print");
                            } else if (printerStatus.isPaused) {
                                System.out.println("Cannot Print because the printer is paused.");
                            } else if (printerStatus.isHeadOpen) {
                                System.out.println("Cannot Print because the printer head is open.");
                            } else if (printerStatus.isPaperOut) {
                                System.out.println("Cannot Print because the paper is out.");
                            } else {
                                System.out.println("Cannot Print.");
                            }
                        } catch (ConnectionException e) {
                            result.success("Not Connected");
//                            e.printStackTrace();
//                            result.error(e.toString(),e.toString(),e);
                        } catch (ZebraPrinterLanguageUnknownException e) {
                            result.success("Not Connected");
//                            e.printStackTrace();
//                            result.error(e.toString(),e.toString(),e);
                        } finally {
                        }
                    }
                }).start();
            } else {
                result.success("Not Connected");
//                result.error("Not Connected","not connected",null);
            }
    }


    private void checkPermissions(final MethodChannel.Result result) {
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
            if (context.checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.BLUETOOTH_ADVERTISE);
            }
            if (context.checkSelfPermission(Manifest.permission.BLUETOOTH_PRIVILEGED) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.BLUETOOTH_PRIVILEGED);
            }
            if (context.checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.FOREGROUND_SERVICE);
            }
            if (context.checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.BLUETOOTH);
            }
            if (context.checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.BLUETOOTH_ADMIN);
            }
            if (!listPermissionsNeeded.isEmpty()) {
                binding.addRequestPermissionsResultListener(new PluginRegistry.RequestPermissionsResultListener() {
                    @Override
                    public boolean onRequestPermissionsResult(int requestCode, String[] listPermissionsNeeded, int[] grantResults) {
//                        if (requestCode == ACCESS_COARSE_LOCATION_REQUEST_CODE) {
                        if (grantResults.length > 0)
                            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                                try {
                                    result.success(true);
                                    return true;
                                } catch (Exception e) {
                                    result.success(false);
                                    return false;
                                }
                            }
//                        }
                        try {
                            result.success(false);
                            return false;
                        } catch (Exception e) {
                            result.success(false);
                            return false;
                        }
                    }
                });
                ((Activity) context).requestPermissions(listPermissionsNeeded.toArray(new String[0]), ACCESS_COARSE_LOCATION_REQUEST_CODE);
            } else {
                result.success(true);
            }

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            if (context.checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.BLUETOOTH);
            }
//            if (context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
//                listPermissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN);
//            }
            if (context.checkSelfPermission(Manifest.permission.BLUETOOTH_PRIVILEGED) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.BLUETOOTH_PRIVILEGED);
            }
            if (context.checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.FOREGROUND_SERVICE);
            }
//            if (context.checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
//                listPermissionsNeeded.add(Manifest.permission.BLUETOOTH_ADVERTISE);
//            }

            if (!listPermissionsNeeded.isEmpty()) {
                binding.addRequestPermissionsResultListener(new PluginRegistry.RequestPermissionsResultListener() {
                    @Override
                    public boolean onRequestPermissionsResult(int requestCode, String[] listPermissionsNeeded, int[] grantResults) {
//                        if (requestCode == ACCESS_COARSE_LOCATION_REQUEST_CODE) {
                        if (grantResults.length > 0)
                            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                                try {
                                    result.success(true);
                                    return true;
                                } catch (Exception e) {
                                    result.success(false);
                                    return false;
                                }
                            }
//                        }
                        try {
                            result.success(false);
                            return false;
                        } catch (Exception e) {
                            result.success(false);
                            return false;
                        }
                    }
                });
                ((Activity) context).requestPermissions(listPermissionsNeeded.toArray(new String[0]), ACCESS_COARSE_LOCATION_REQUEST_CODE);
            } else {
                result.success(true);
            }
        }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//
//        }


    }


//    public ZebraPrinter connect(boolean isBluetoothPrinter) {
//
//
//        printerConnection = null;
//        if (isBluetoothPrinter) {
//
//        } else {
//            try {
//
//
//            }
//        }
//        try {
//            printerConnection.open();
//
//        } catch (ConnectionException e) {
//            DemoSleeper.sleep(1000);
//            disconnect();
//            return null;
//        }
//
//        ZebraPrinter printer = null;
//
//        if (printerConnection.isConnected()) {
//            try {
//                printer = ZebraPrinterFactory.getInstance(printerConnection);
//            } catch (ConnectionException e) {
//                printer = null;
//                DemoSleeper.sleep(1000);
//                disconnect();
//            } catch (ZebraPrinterLanguageUnknownException e) {
//                printer = null;
//                DemoSleeper.sleep(1000);
//                disconnect();
//            }
//        }
//        setStatus(context.getString(R.string.connected), context.getString(R.string.connectedColor));
//        return printer;
//    }


//    public static void discoveryPrinters(final Context context, final MethodChannel methodChannel) {
//
//        try {
//            sendedDiscoveredPrinters.clear();
//            for (DiscoveredPrinter dp :
//                    discoveredPrinters) {
//                addNewDiscoverPrinter(dp, context, methodChannel);
//            }
//            countEndScan = 0;
//            BluetoothDiscoverer.findPrinters(context, new DiscoveryHandler() {
//                @Override
//                public void foundPrinter(final DiscoveredPrinter discoveredPrinter) {
//                    discoveredPrinters.add(discoveredPrinter);
//                    ((Activity) context).runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            addNewDiscoverPrinter(discoveredPrinter, context, methodChannel);
//                        }
//                    });
//                }
//
//                @Override
//                public void discoveryFinished() {
//                    countEndScan++;
//                    finishScanPrinter(context, methodChannel);
//                }
//
//                @Override
//                public void discoveryError(String s) {
//                    if(s.contains("Bluetooth radio is currently disabled"))
//                        onDiscoveryError(context, methodChannel, ON_DISCOVERY_ERROR_BLUETOOTH, s);
//                    else
//                        onDiscoveryError(context, methodChannel, ON_DISCOVERY_ERROR_GENERAL, s);
//                    countEndScan++;
//                    finishScanPrinter(context, methodChannel);
//                }
//            });
//
//
//            NetworkDiscoverer.findPrinters(new DiscoveryHandler() {
//                @Override
//                public void foundPrinter(DiscoveredPrinter discoveredPrinter) {
//                    addNewDiscoverPrinter(discoveredPrinter, context, methodChannel);
//
//                }
//
//                @Override
//                public void discoveryFinished() {
//                    countEndScan++;
//                    finishScanPrinter(context, methodChannel);
//                }
//
//                @Override
//                public void discoveryError(String s) {
//                    onDiscoveryError(context, methodChannel, ON_DISCOVERY_ERROR_GENERAL, s);
//                    countEndScan++;
//                    finishScanPrinter(context, methodChannel);
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    private static void onDiscoveryError(Context context, final MethodChannel methodChannel, final int errorCode, final String errorText) {
//        ((Activity) context).runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                HashMap<String, Object> arguments = new HashMap<>();
//                arguments.put("ErrorCode", errorCode);
//                arguments.put("ErrorText", errorText);
//                methodChannel.invokeMethod("onDiscoveryError", arguments);
//            }
//        });
//
//    }


//    private static void addPrinterToDiscoveryPrinterList(DiscoveredPrinter discoveredPrinter) {
//        for (DiscoveredPrinter dp :
//                discoveredPrinters) {
//            if (dp.address.equals(discoveredPrinter.address))
//                return;
//        }
//
//        discoveredPrinters.add(discoveredPrinter);
//    }


//    private static void addNewDiscoverPrinter(final DiscoveredPrinter discoveredPrinter, Context context, final MethodChannel methodChannel) {
//
//        addPrinterToDiscoveryPrinterList(discoveredPrinter);
//        ((Activity) context).runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                for (DiscoveredPrinter dp :
//                        sendedDiscoveredPrinters) {
//                    if (dp.address.equals(discoveredPrinter.address))
//                        return;
//                }
//                sendedDiscoveredPrinters.add(discoveredPrinter);
//                HashMap<String, Object> arguments = new HashMap<>();
//
//                arguments.put("Address", discoveredPrinter.address);
//                if (discoveredPrinter.getDiscoveryDataMap().get("SYSTEM_NAME") != null) {
//                    arguments.put("Name", discoveredPrinter.getDiscoveryDataMap().get("SYSTEM_NAME"));
//                    arguments.put("IsWifi", true);
//                    methodChannel.invokeMethod("printerFound"
//                            , arguments);
//                } else {
//                    arguments.put("Name", discoveredPrinter.getDiscoveryDataMap().get("FRIENDLY_NAME"));
//                    arguments.put("IsWifi", false);
//                    methodChannel.invokeMethod("printerFound"
//                            , arguments);
//                }
//            }
//        });
//    }


//    private static void finishScanPrinter(final Context context, final MethodChannel methodChannel) {
//        if (countEndScan == 2) {
//            if (discoveredPrinters.size() == 0) {
//                if (discoveryPrintersAgain(context, methodChannel))
//                    return;
//            }
//            ((Activity) context).runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    methodChannel.invokeMethod("onPrinterDiscoveryDone",
//                            context.getResources().getString(R.string.done));
//                }
//            });
//        }
//    }

//    private static boolean discoveryPrintersAgain(Context context, MethodChannel methodChannel) {
//        System.out.print("Discovery printers again");
//        countDiscovery++;
//        if (countDiscovery < 2) {
//            discoveryPrinters(context, methodChannel);
//            return true;
//        }
//        return false;
//    }


//    public void print(final String data) {
//        new Thread(new Runnable() {
//            public void run() {
////                enableTestButton(false);
//                Looper.prepare();
//                doConnectionTest(data);
//                Looper.loop();
//                Looper.myLooper().quit();
//            }
//        }).start();
//    }


//    private void doConnectionTest(String data) {
//
//        if (isZebraPrinter) {
//            if (printer != null) {
//                printData(data);
//            } else {
//                disconnect();
//            }
//        } else {
//            printDataGenericPrinter(data);
//        }
//    }

//    private void printDataGenericPrinter(String data) {
//        setStatus(context.getString(R.string.sending_data), context.getString(R.string.connectingColor));
//        socketmanager.threadconnectwrite(convertDataToByte(data));
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        if (socketmanager.getIstate()) {
//            setStatus(context.getResources().getString(R.string.done), context.getString(R.string.connectedColor));
//        } else {
//            setStatus(context.getResources().getString(R.string.disconnect), context.getString(R.string.disconnectColor));
//        }
//
//        byte sendCut[] = {0x0a, 0x0a, 0x1d, 0x56, 0x01};
//        socketmanager.threadconnectwrite(sendCut);
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        if (!socketmanager.getIstate()) {
//            setStatus(context.getResources().getString(R.string.disconnect)
//                    , context.getString(R.string.disconnectColor));
//        }
//    }


//    public boolean connectToSelectPrinter(String address) {
//        isZebraPrinter = true;
//        setStatus(context.getString(R.string.connecting), context.getString(R.string.connectingColor));
//        selectedAddress = null;
//        macAddress = null;
//        boolean isBluetoothPrinter;
//        if (address.contains(":")) {
//            macAddress = address;
//            isBluetoothPrinter = true;
//        } else {
//            this.selectedAddress = address;
//            isBluetoothPrinter = false;
//        }
//        printer = connect(isBluetoothPrinter);
//        if (printer != null) return true;
//        return false;
//    }


//    public void connectToGenericPrinter(String ipAddress) {
//        this.isZebraPrinter = false;
//        if (isPrinterConnect().equals(context.getString(R.string.connected))) {
//            disconnect();
//            setStatus(context.getString(R.string.connecting), context.getString(R.string.connectingColor));
//        }
//        if (socketmanager == null)
//            socketmanager = new Socketmanager(context);
//        socketmanager.mPort = getGenericPortNumber();
//        socketmanager.mstrIp = ipAddress;
//        socketmanager.threadconnect();
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        if (socketmanager.getIstate()) {
//            setStatus(context.getString(R.string.connected), context.getString(R.string.connectedColor));
//        } else {
//            setStatus(context.getString(R.string.disconnect), context.getString(R.string.disconnectColor));
//        }
//    }


//    private String getMacAddress() {
//        return macAddress;
//    }

//    private static String convertMacAddressToMacAddressApp(String macAddress) {
//        return macAddress;
//    }
//
//    private String getTcpAddress() {
//        return selectedAddress;
//    }


    private void setStatus(final String message, final String color) {
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Printer set status: " + message);
                HashMap<String, Object> arguments = new HashMap<>();
                arguments.put("Status", message);
                arguments.put("Color", color + "");
                methodChannel.invokeMethod("changePrinterStatus", arguments);
            }
        });

    }

    private int getTcpPortNumber() {
        return 6101;
    }

    private int getGenericPortNumber() {
        return 9100;
    }

    private byte[] convertDataToByte(String data) {
        return data.getBytes();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Create a notification for the foreground service
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(
//                    "PrinterChannelId",
//                    "Printer Service",
//                    NotificationManager.IMPORTANCE_DEFAULT
//            );
//            NotificationManager manager = context.getApplicationContext().getSystemService(NotificationManager.class);
//            if (manager != null) {
//                manager.createNotificationChannel(channel);
//            }
//        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }


//        Intent notificationIntent = new Intent(this, Printer.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//
//        Notification notification = new NotificationCompat.Builder(this, "PrinterChannelId")
//                .setContentTitle("Zebra Printer Service")
//                .setContentText("The printer connection is active")
////                .setSmallIcon(R.drawable.ic_printer) // your icon
//                .setContentIntent(pendingIntent)
//                .build();
//
//        // Start the service in the foreground
//
//        startForeground(1, notification);
    }

    private void createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(
//                    "PrinterChannelId",
//                    "Printer Service Channel",
//                    NotificationManager.IMPORTANCE_DEFAULT
//            );
//            NotificationManager manager = getSystemService(NotificationManager.class);
//            if (manager != null) {
//                manager.createNotificationChannel(channel);
//            }
//        }
    }

    @Override
    public void onDestroy() {
//        if (printerConnection != null && printerConnection.isConnected()) {
//            try {
//                printerConnection.close();
//            } catch (ConnectionException e) {
//                e.printStackTrace();
//            }
//        }
        super.onDestroy();
    }

    //    public static String getZplCode(Bitmap bitmap, Boolean addHeaderFooter, int rotation) {
//        ZPLConverter zp = new ZPLConverter();
//        zp.setCompressHex(true);
//        zp.setBlacknessLimitPercentage(50);
//        Bitmap grayBitmap = toGrayScale(bitmap, rotation);
//        return zp.convertFromImage(grayBitmap, addHeaderFooter);
//    }

//    public static Bitmap toGrayScale(Bitmap bmpOriginal, int rotation) {
//        int width, height;
//        bmpOriginal = rotateBitmap(bmpOriginal, rotation);
//        height = bmpOriginal.getHeight();
//        width = bmpOriginal.getWidth();
//        Bitmap grayScale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        grayScale.eraseColor(Color.WHITE);
//        Canvas c = new Canvas(grayScale);
//        Paint paint = new Paint();
//        ColorMatrix cm = new ColorMatrix();
//        cm.setSaturation(0);
//        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
//        paint.setColorFilter(f);
//        c.drawBitmap(bmpOriginal, 0, 0, paint);
//        return grayScale;
//    }


//    public static Bitmap rotateBitmap(Bitmap source, float angle) {
//        Matrix matrix = new Matrix();
//        matrix.postRotate(angle);
//        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
//    }

//    public void setSettings(String settings) {
//        print(settings);
//    }

//    public void setDarkness(int darkness) {
//        String setting = "             ! U1 setvar \"print.tone\" \"" + darkness + "\"\n";
//        setSettings(setting);
//    }
//
//    public void setMediaType(String mediaType) {
//        String settings;
//        if (mediaType.equals("Label")) {
//            settings = "! U1 setvar \"media.type\" \"label\"\n" +
//                    "             ! U1 setvar \"media.sense_mode\" \"gap\"\n" +
//                    //    "             ! U1 setvar \"print.tone\" \"0\"\n" +
//                    "              ~jc^xa^jus^xz";
//        } else if (mediaType.equals("BlackMark")) {
//            settings = "! U1 setvar \"media.type\" \"label\"\n" +
//                    "             ! U1 setvar \"media.sense_mode\" \"bar\"\n" +
//                    //    "             ! U1 setvar \"print.tone\" \"0\"\n" +
//                    "              ~jc^xa^jus^xz";
//        } else {
//            settings = //"! U1 SPEED 4\n" +
//                    "      ! U1 setvar \"print.tone\" \"0\"\n" +
//                            "      ! U1 setvar \"media.type\" \"journal\"\n";
//            //   "      ! U1 setvar \"media.sense_mode\" \"bar\"" +
//            // " ~jc^xa^jus^xz";
//        }
//        setSettings(settings);
//    }
//
//    private void convertBase64ImageToZPLString(String data, int rotation, MethodChannel.Result result) {
//        try {
//            byte[] decodedString = Base64.decode(data, Base64.DEFAULT);
//            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//            result.success(Printer.getZplCode(decodedByte, false, rotation));
//        } catch (Exception e) {
//            result.error("-1", "Error", null);
//        }
//    }
//
//
//
//    private boolean checkIsLocationNetworkProviderIsOn() {
//        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//        try {
//            return lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//        } catch (Exception ex) {
//            return false;
//        }
//    }
}