import Cocoa
import FlutterMacOS

public class ArtemisZebraPlugin: NSObject, FlutterPlugin {
    
    var printers = [Printer]()
    var binaryMessenger: FlutterBinaryMessenger?
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "artemis_zebra", binaryMessenger: registrar.messenger)
        let instance = ArtemisZebraPlugin()
        instance.binaryMessenger = registrar.messenger
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        switch (call.method){
        case "getInstance":
            let printer = Printer.getInstance(binaryMessenger: self.binaryMessenger!)
            printers.append(printer)
            result(printer.toString())
            
            
        default:
            result("macOS " + ProcessInfo.processInfo.operatingSystemVersionString)
        }
    }
}
