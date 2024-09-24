
import 'dart:developer';

import 'package:flutter/services.dart';

import 'artemis_zebra_platform_interface.dart';
import 'zebra_printer.dart';

class ArtemisZebraUtil {

  ArtemisZebraUtil(){
    ArtemisZebraUtilPlatform.instance.setMethodCallHandler(handler: _methodCallHandler);
  }
  Future<String?> getPlatformVersion() {
    return ArtemisZebraUtilPlatform.instance.getPlatformVersion();
  }

  Future<String?> discoverPrinters() {
    return ArtemisZebraUtilPlatform.instance.discoverPrinters();
  }

  Future<String?> setMethodCallHandler() {
    return ArtemisZebraUtilPlatform.instance.discoverPrinters();
  }

  static Future<ZebraPrinter> getPrinterInstance({String? label,required void Function(ZebraPrinter) notifier,Function? statusListener}) async {
    return ArtemisZebraUtilPlatform.instance.getZebraPrinterInstance(label:label,notifier:notifier,statusListener: statusListener);
  }

  Future<dynamic> _methodCallHandler(MethodCall methodCall) async {


    if (methodCall.method == "printerFound") {
      log("printerFound");
      // String barcode = methodCall.arguments.toString();

    }else if(methodCall.method == "discoveryDone"){
      log("discoveryDone");
      String? ocrJson = await methodCall.arguments;
      if (ocrJson == null) return null;
      try {
        // OcrData ocrData = OcrData.fromJson(jsonDecode(ocrJson));
        // if (widget.onOcrRead == null) {
        //   log("Text ${ocrData.text} Detected but no onOcrRead is not  Implemented");
        // } else {
        //   widget.onOcrRead!(ocrData);
        // }
      } catch (e) {
        return null;
      }
    }

  }


}


class ZebraPrinterStatus {
  final bool isPaused;
  final int numberOfFormatsInReceiveBuffer;
  final bool isReadyToPrint;
  final bool isPaperOut;
  final bool isPartialFormatInProgress;
  final bool isReceiveBufferFull;
  final int labelLengthInDots;
  final bool isRibbonOut;
  final bool isHeadTooHot;
  final int labelsRemainingInBatch;
  final bool isHeadOpen;
  final bool isHeadCold;
  final int printMode;

  ZebraPrinterStatus({
    required this.isPaused,
    required this.numberOfFormatsInReceiveBuffer,
    required this.isReadyToPrint,
    required this.isPaperOut,
    required this.isPartialFormatInProgress,
    required this.isReceiveBufferFull,
    required this.labelLengthInDots,
    required this.isRibbonOut,
    required this.isHeadTooHot,
    required this.labelsRemainingInBatch,
    required this.isHeadOpen,
    required this.isHeadCold,
    required this.printMode,
  });

  ZebraPrinterStatus copyWith({
    bool? isPaused,
    int? numberOfFormatsInReceiveBuffer,
    bool? isReadyToPrint,
    bool? isPaperOut,
    bool? isPartialFormatInProgress,
    bool? isReceiveBufferFull,
    int? labelLengthInDots,
    bool? isRibbonOut,
    bool? isHeadTooHot,
    int? labelsRemainingInBatch,
    bool? isHeadOpen,
    bool? isHeadCold,
    int? printMode,
  }) =>
      ZebraPrinterStatus(
        isPaused: isPaused ?? this.isPaused,
        numberOfFormatsInReceiveBuffer: numberOfFormatsInReceiveBuffer ?? this.numberOfFormatsInReceiveBuffer,
        isReadyToPrint: isReadyToPrint ?? this.isReadyToPrint,
        isPaperOut: isPaperOut ?? this.isPaperOut,
        isPartialFormatInProgress: isPartialFormatInProgress ?? this.isPartialFormatInProgress,
        isReceiveBufferFull: isReceiveBufferFull ?? this.isReceiveBufferFull,
        labelLengthInDots: labelLengthInDots ?? this.labelLengthInDots,
        isRibbonOut: isRibbonOut ?? this.isRibbonOut,
        isHeadTooHot: isHeadTooHot ?? this.isHeadTooHot,
        labelsRemainingInBatch: labelsRemainingInBatch ?? this.labelsRemainingInBatch,
        isHeadOpen: isHeadOpen ?? this.isHeadOpen,
        isHeadCold: isHeadCold ?? this.isHeadCold,
        printMode: printMode ?? this.printMode,
      );

  factory ZebraPrinterStatus.fromJson(Map<String, dynamic> json) => ZebraPrinterStatus(
    isPaused: json["isPaused"],
    numberOfFormatsInReceiveBuffer: json["numberOfFormatsInReceiveBuffer"]??0,
    isReadyToPrint: json["isReadyToPrint"],
    isPaperOut: json["isPaperOut"],
    isPartialFormatInProgress: json["isPartialFormatInProgress"],
    isReceiveBufferFull: json["isReceiveBufferFull"],
    labelLengthInDots: json["labelLengthInDots"]??0,
    isRibbonOut: json["isRibbonOut"],
    isHeadTooHot: json["isHeadTooHot"],
    labelsRemainingInBatch: json["labelsRemainingInBatch"],
    isHeadOpen: json["isHeadOpen"],
    isHeadCold: json["isHeadCold"],
    printMode: json["printMode"],
  );

  Map<String, dynamic> toJson() => {
    "isPaused": isPaused,
    "numberOfFormatsInReceiveBuffer": numberOfFormatsInReceiveBuffer,
    "isReadyToPrint": isReadyToPrint,
    "isPaperOut": isPaperOut,
    "isPartialFormatInProgress": isPartialFormatInProgress,
    "isReceiveBufferFull": isReceiveBufferFull,
    "labelLengthInDots": labelLengthInDots,
    "isRibbonOut": isRibbonOut,
    "isHeadTooHot": isHeadTooHot,
    "labelsRemainingInBatch": labelsRemainingInBatch,
    "isHeadOpen": isHeadOpen,
    "isHeadCold": isHeadCold,
    "printMode": printMode,
  };


  @override
  String toString() {
    if(isReadyToPrint) return "Ready";
    if(isPaperOut) return "PaperOut";
    if(isReceiveBufferFull) return "Full";
    if(isRibbonOut) return "RibbonOut";
    if(isHeadTooHot) return "Hot";
    if(isHeadCold) return "Cold";
    if(isHeadOpen) return "Open";
    if(isReadyToPrint) return "Ready";

    return "Unknown";
  }
}