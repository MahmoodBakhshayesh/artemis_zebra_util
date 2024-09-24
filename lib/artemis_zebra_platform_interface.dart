import 'package:flutter/services.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'artemis_zebra_method_channel.dart';
import 'zebra_printer.dart';


abstract class ArtemisZebraUtilPlatform extends PlatformInterface {
  /// Constructs a ArtemisZebraUtilPlatform.
  ArtemisZebraUtilPlatform() : super(token: _token);

  static final Object _token = Object();

  static ArtemisZebraUtilPlatform _instance = MethodChannelArtemisZebraUtil();

  /// The default instance of [ArtemisZebraUtilPlatform] to use.
  ///
  /// Defaults to [MethodChannelArtemisZebraUtil].
  static ArtemisZebraUtilPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [ArtemisZebraUtilPlatform] when
  /// they register themselves.
  static set instance(ArtemisZebraUtilPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String?> discoverPrinters() {
    throw UnimplementedError('discoverPrinters() has not been implemented.');
  }

  Future<void> setMethodCallHandler({required Future<dynamic> Function(MethodCall call)? handler}) {
    throw UnimplementedError('setMethodCallHandler() has not been implemented.');
  }

  Future<ZebraPrinter> getZebraPrinterInstance({String? label,required void Function(ZebraPrinter) notifier,Function? statusListener}) {
    throw UnimplementedError('getZebraPrinterInstance() has not been implemented.');
  }
}
