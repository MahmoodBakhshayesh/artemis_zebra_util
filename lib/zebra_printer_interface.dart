import 'package:artemis_zebra/zebra_printer.dart';
import 'package:flutter/material.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

abstract class ArtemisZebraPrinterInterface {
  Future<String?> discoverPrinters() {
    throw UnimplementedError('discoverPrinters() has not been implemented.');
  }

  Future<bool> connectToPrinter(String address) {
    throw UnimplementedError('connectToPrinter() has not been implemented.');
  }

  Future<bool> printData(String data) {
    throw UnimplementedError('printData() has not been implemented.');
  }

  Future<bool> disconnectPrinter() {
    throw UnimplementedError('disconnectPrinter() has not been implemented.');
  }

  Future<bool> isPrinterConnected() {
    throw UnimplementedError('isPrinterConnected() has not been implemented.');
  }

  Future<bool> checkPermissions() {
    throw UnimplementedError('checkPermissions() has not been implemented.');
  }
}

enum PrinterType { wifi, bluetooth }
extension PrinterTypeDetails on PrinterType {
  int get id {
    switch (this) {
      case PrinterType.wifi:
        return 0;
      case PrinterType.bluetooth:
        return 1;
    }
  }

  String get label {
    switch (this) {
      case PrinterType.wifi:
        return "Wifi";
      case PrinterType.bluetooth:
        return "Bluetooth";
    }
  }
}


enum PrinterStatus { discoveringPrinter, connecting, ready, printing, disconnecting, disconnected }

extension PrinterStatusDetails on PrinterStatus {
  int get id {
    switch (this) {
      case PrinterStatus.discoveringPrinter:
        return 0;
      case PrinterStatus.connecting:
        return 1;
      case PrinterStatus.ready:
        return 2;
      case PrinterStatus.printing:
        return 3;
      case PrinterStatus.disconnecting:
        return -1;
      case PrinterStatus.disconnected:
        return -2;
    }
  }

  String get label {
    switch (this) {
      case PrinterStatus.discoveringPrinter:
        return "Discovering...";
      case PrinterStatus.connecting:
        return "Connecting...";
      case PrinterStatus.ready:
        return "Ready";
      case PrinterStatus.printing:
        return "Printing...";
      case PrinterStatus.disconnecting:
        return "Disconnecting";
      case PrinterStatus.disconnected:
        return "Disconnected";
    }
  }

  Color get color {
    switch (this) {
      case PrinterStatus.discoveringPrinter:
        return Colors.orange;
      case PrinterStatus.connecting:
        return Colors.greenAccent;
      case PrinterStatus.ready:
        return Colors.green;
      case PrinterStatus.printing:
        return Colors.yellow;
      case PrinterStatus.disconnecting:
        return Colors.redAccent;
      case PrinterStatus.disconnected:
        return Colors.red;
    }
  }
}


class FoundPrinter {
  final String name;
  final String address;
  final PrinterType type;
  final bool isConnected;

  FoundPrinter({required this.name, required this.address, required this.type,required this.isConnected});

  factory FoundPrinter.fromJson(json)=>
      FoundPrinter(name: json["name"]??"Unknown",
        address: json["address"],
        isConnected: json["isConnected"]??false,
        type: PrinterType.values.firstWhere((element) => element.id == json["type"]),);

  @override
  String toString() {
    // TODO: implement toString
    return '(${type.label})$name => $address';
  }
}

enum MediaType { label, blackMark, journal }
enum Command { calibrate, mediaType, darkness }