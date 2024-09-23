//
//  Printer.swift
//  Runner
//
//  Created by faranegar on 6/21/20.
//
import AVFoundation
import Foundation


class Printer{
    
    var connection : ZebraPrinterConnection?
    var channel : FlutterMethodChannel?
    var selectedIPAddress: String? = nil
    var selectedMacAddress: String? = nil
    var isZebraPrinter :Bool = true
    var wifiManager: POSWIFIManager?
    var isConnecting :Bool = false
    var backgroundTask: UIBackgroundTaskIdentifier = .invalid

    
    static func getInstance(binaryMessenger : FlutterBinaryMessenger) -> Printer {
        let printer = Printer()
        printer.setMethodChannel(binaryMessenger: binaryMessenger)
        return printer
    }
    
    func setMethodChannel(binaryMessenger : FlutterBinaryMessenger) {
        self.channel = FlutterMethodChannel(name: "ZebraPrinterInstance" + toString(), binaryMessenger: binaryMessenger)
        self.channel?.setMethodCallHandler({(call,  result) in
            let args = call.arguments
            let myArgs = args as? [String: Any]
            switch call.method {
            case "discoverPrinters":
                self.discoverPrinters(result: result);
                break
                
            case "connectToPrinter":
                let address = (myArgs?["address"] as! String)
                self.connectToPrinter(address: address,result: result);
                break
                
            case "printData":
                let data = (myArgs?["data"] as! NSString)
                self.printData(data: data ,result: result);
                break
                
            case "disconnectPrinter":
                self.disconnect(result: result)
                break
                
            case "isPrinterConnected":
                self.isPrinterConnect(result: result)
                break
            default:
                result("Unimplemented Method")
            }
        })
    }
    
    //Send dummy to get user permission for local network
    func dummyConnect(){
        let connection = TcpPrinterConnection(address: "0.0.0.0", andWithPort: 9100)
        connection?.open()
        connection?.close()
    }
    
    func discoverPrinters(result: @escaping FlutterResult){
        dummyConnect()
        let manager = EAAccessoryManager.shared()
        let devices = manager.connectedAccessories
        for d in devices {
            print("Message from ios: orinter found")
//            let data: [String: Any] = [
//                "name": d.name,
//                "address": d.serialNumber,
//                "type": 1
//            ]

            let data = DeviceData(name: d.name, address: d.serialNumber, type: 1,isConnected: d.isConnected)

            let jsonEncoder = JSONEncoder()
            let jsonData = try! jsonEncoder.encode(data)
            let json = String(data: jsonData, encoding: String.Encoding.utf8)
            self.channel?.invokeMethod("printerFound", arguments: json)
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
            result("Discovery Done Devices Found: "+String(devices.count))
        }
    }
    
    

    
    func connectToGenericPrinter(address: String,result: @escaping FlutterResult) {
        self.isZebraPrinter = false
        if self.wifiManager != nil{
            self.wifiManager?.posDisConnect()
        }
        self.wifiManager = POSWIFIManager()
        self.wifiManager?.posConnect(withHost: address, port: 9100, completion: { (r) in
            if r == true {
                result(true)
            } else {
                result(false)
            }
        })
    }
    
    func connectToPrinter(address: String,result: @escaping FlutterResult){
        if self.isConnecting == false {
            self.isConnecting = true
            self.isZebraPrinter = true
            selectedIPAddress = nil

            // Close any existing connection before starting a new one
            if self.connection != nil {
                self.connection?.close()
            }
            
            backgroundTask = UIApplication.shared.beginBackgroundTask(withName: "PrinterConnection") {
                            // End the task if time expires
                            UIApplication.shared.endBackgroundTask(self.backgroundTask)
                            self.backgroundTask = .invalid
                        }

            // Perform the connection process on a background thread
            DispatchQueue.global(qos: .userInitiated).async {
                // Determine the type of connection based on the address format
                if !address.contains(".") {
                    self.connection = MfiBtPrinterConnection(serialNumber: address)
                } else {
                    self.connection = TcpPrinterConnection(address: address, andWithPort: 9100)
                }

                // Introduce a small delay before attempting to open the connection
                Thread.sleep(forTimeInterval: 1)

                let isOpen = self.connection?.open()

                DispatchQueue.main.async {
                    self.isConnecting = false

                    if isOpen == true {
                        Thread.sleep(forTimeInterval: 1)
                        self.selectedIPAddress = address
                        result(true)
                    } else {
                        result(false)
                    }
                    
                    if self.backgroundTask != .invalid {
                        UIApplication.shared.endBackgroundTask(self.backgroundTask)
                        self.backgroundTask = .invalid
                    }
                }
            }
        }
    }
    
    func isPrinterConnect(result: @escaping FlutterResult){
        if self.isZebraPrinter == true {
            if self.connection?.isConnected() == true {
                result(true)
            }
            else {
                result(false)
            }
        } else {
            if(self.wifiManager?.connectOK == true){
                result(true)
            } else {
                result(false)
            }
        }
    }
    
    
    func disconnect(result: FlutterResult?) {
        DispatchQueue.global(qos: .userInitiated).async { [weak self] in
            guard let self = self else { return }

            if self.isZebraPrinter {
                if let connection = self.connection {
                    connection.close()  // Close the connection in the background thread
                }
                DispatchQueue.main.async {
                    // Notify the Flutter side that the connection is lost
                    self.channel?.invokeMethod("connectionLost", arguments: nil)
                    result?(true)  // Call the result callback on the main thread
                }
            } else {
                if let wifiManager = self.wifiManager {
                    wifiManager.posDisConnect()  // Disconnect Wi-Fi connection in the background thread
                }
                DispatchQueue.main.async {
                    // Notify the Flutter side that the connection is lost
                    self.channel?.invokeMethod("connectionLost", arguments: nil)
                    result?(true)  // Call the result callback on the main thread
                }
            }

            // End the background task in the background thread
            if self.backgroundTask != .invalid {
                UIApplication.shared.endBackgroundTask(self.backgroundTask)
                self.backgroundTask = .invalid
            }
        }
    }
    
    func printData(data: NSString, result: @escaping FlutterResult) {
        DispatchQueue.global(qos: .utility).async {
            let dataBytes = Data(bytes: data.utf8String!, count: data.length)
            if self.isZebraPrinter == true {
                var error: NSError?
                let r = self.connection?.write(dataBytes, error: &error)
                if r == -1, let error = error {
                    print(error)
                    result(false)
                    self.disconnect(result: nil)
                    
                    return
                }
            } else {
                self.wifiManager?.posWriteCommand(with: dataBytes, withResponse: { (result) in
                    
                })
            }
            sleep(1)
            DispatchQueue.main.async {
                result(true)
            }
        }
    }
    
    
    func toString() -> String{
        return String(UInt(bitPattern: ObjectIdentifier(self)))
    }
}

struct DeviceData: Codable {
    var name: String?
    var address: String?
    var type: Int?
    var isConnected: Bool
}
