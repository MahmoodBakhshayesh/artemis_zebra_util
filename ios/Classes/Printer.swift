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
                
            case "checkPrinterStatus":
                self.checkPrinterStatus(result: result)
                break
                
            case "sendZplOverTcp":
                let data = (myArgs?["data"] as! NSString)
                self.sendZplOverTcp(data: data ,result: result)
                break
                
            case "sendCpclOverTcp":
                let data = (myArgs?["data"] as! NSString)
                self.sendCpclOverTcp(data: data ,result: result)
                break
                
            case "sampleWithGCD":
                let data = (myArgs?["data"] as! NSString)
                self.sampleWithGCD(data: data ,result: result)
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
    
//    func discoverNetworkPrinters(completion: @escaping ([DiscoveredPrinterNetwork]?) -> Void) {
//        DispatchQueue.global(qos: .background).async {
//            // Perform network discovery
//            let printers = NetworkDiscoverer.discoverPrinters() as? [DiscoveredPrinterNetwork]
//            
//            DispatchQueue.main.async {
//                completion(printers)
//            }
//        }
//    }
    
    

    
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
        print("connecting instance  \(self.toString()) to " + address)
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
                let r = self.connection as! TcpPrinterConnection
                
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
        print("sending from  \(self.toString()) to " + (self.connection?.toString() ?? "unknown"))
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
    
    func checkPrinterStatus(result: @escaping FlutterResult) {
        // Instantiate connection for TCP port at the given address.
       
        if(self.connection==nil){
            result("Not Connected")
            return
        }
        if let zebraPrinterConnection = self.connection as? TcpPrinterConnection {
            // obj is a string array. Do something with stringArray

        

        // Open the connection - physical connection is established here.
//        let success = zebraPrinterConnection.open()
        
        do {
            // Get printer instance from connection.
            let printer = try ZebraPrinterFactory.getInstance(zebraPrinterConnection)
            
            // Get the current status of the printer.
            let printerStatus = try printer.getCurrentStatus()
            let status = MyPrinterStatus(isReadyToPrint: printerStatus.isReadyToPrint, isHeadOpen: printerStatus.isHeadOpen, isHeadCold: printerStatus.isHeadCold, isHeadTooHot: printerStatus.isHeadTooHot, isPaperOut: printerStatus.isPaperOut, isRibbonOut: printerStatus.isRibbonOut, isReceiveBufferFull: printerStatus.isReceiveBufferFull, isPaused: printerStatus.isPaused, labelLengthInDots: printerStatus.labelLengthInDots, numberOfFormatsInReceiveBuffer: printerStatus.numberOfFormatsInReceiveBuffer, labelsRemainingInBatch: printerStatus.labelsRemainingInBatch, isPartialFormatInProgress: printerStatus.isPartialFormatInProgress, printMode: printerStatus.printMode.rawValue)
            
            let jsonEncoder = JSONEncoder()
            let jsonData = try! jsonEncoder.encode(status)
            let json = String(data: jsonData, encoding: String.Encoding.utf8)
            
            result(json)
        } catch {
            // Handle any errors thrown by the printer operations.
            result(error.localizedDescription)
            showAlert(title: "Error", message: error.localizedDescription)
        }
        
        }
        else {
            result("Not TCP")
            return
        }
    }
    
    func sendZplOverTcp(data: NSString,result: @escaping FlutterResult) {
        // Instantiate connection for ZPL TCP port at given address.
        
        let thePrinterConn = self.connection!
        
       
        // Open the connection - physical connection is established here.
        var success = self.connection!.open()
       
        // This example prints "This is a ZPL test." near the top of the label.
        let zplData = "\(data)"
        var error: NSError?
        
        // Send the data to printer as a byte array.
        if let data = zplData.data(using: .utf8) {
            success = success && (thePrinterConn.write(data, error: &error) != 0)
            
        }else{
            
        }
        
        if !success || error != nil {
            
            result(error?.localizedDescription)
            showAlert(title: "Error", message: error?.localizedDescription ?? "Unknown error")
        }else{
            result("Done")
        }
        
        // Close the connection to release resources.
//      thePrinterConn.close()
    }
    
    func sendCpclOverTcp(data: NSString,result: @escaping FlutterResult) {
        // Instantiate connection for CPCL TCP port at given address.
        let thePrinterConn = self.connection!
        
        // Open the connection - physical connection is established here.
        var success = thePrinterConn.open()
        
        // This example prints "This is a CPCL test." near the top of the label.
        let cpclData = "\(data)"
        var error: NSError?
        
        // Send the data to printer as a byte array.
        if let data = cpclData.data(using: .utf8) {
            success = success && (thePrinterConn.write(data, error: &error) != 0)
        }
        
        if !success || error != nil {
           
            showAlert(title: "Error", message: error?.localizedDescription ?? "Unknown error")
            result(error?.localizedDescription)
        }else{
            result("Done")
        }
        
        // Close the connection to release resources.
//        thePrinterConn.close()
    }
    
    func sampleWithGCD(data: NSString,result: @escaping FlutterResult) {
        // Dispatch this task to the default queue
        DispatchQueue.global(qos: .default).async {
            // Instantiate connection to TCP port at a given address
            let thePrinterConn = self.connection!
            
            // Open the connection - physical connection is established here.
            var success = thePrinterConn.open()
            
            // This example prints "This is a ZPL test." near the top of the label.
            let zplData = "\(data)"
            var error: NSError?
            
            // Send the data to printer as a byte array.
            if let data = zplData.data(using: .utf8) {
                success = success && (thePrinterConn.write(data, error: &error) != 0)
            }
            
            // Dispatch GUI work back onto the main queue
            DispatchQueue.main.async {
                if !success || error != nil {
                    self.showAlert(title: "Error", message: error?.localizedDescription ?? "Unknown error")
                    result(error?.localizedDescription)
                }else{
                    result("Done")
                }
    
            }
            
            // Close the connection to release resources.
//            thePrinterConn.close()
        }}
    
    private func showAlert(title: String, message: String) {
        print("sending from  \(title) to " + (message))
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

class MyResponseValidator: NSObject,ResponseValidator {
    func isResponseComplete(_ data: Data!) -> Bool {
        print(data)
        return true
    }
    
    // Implement the necessary methods for the ResponseValidator protocol
}


enum ZplPrintMode: String, Codable {
    case tearOff = "TearOff"
    case peelOff = "PeelOff"
    case cutter = "Cutter"
    // Add more modes as per your requirements
}

class MyPrinterStatus: Codable {
    var isReadyToPrint: Bool
    var isHeadOpen: Bool
    var isHeadCold: Bool
    var isHeadTooHot: Bool
    var isPaperOut: Bool
    var isRibbonOut: Bool
    var isReceiveBufferFull: Bool
    var isPaused: Bool
    var labelLengthInDots: Int
    var numberOfFormatsInReceiveBuffer: Int
    var labelsRemainingInBatch: Int
    var isPartialFormatInProgress: Bool
    var printMode: UInt32
    
    // Initializer
    init(isReadyToPrint: Bool, isHeadOpen: Bool, isHeadCold: Bool, isHeadTooHot: Bool, isPaperOut: Bool, isRibbonOut: Bool, isReceiveBufferFull: Bool, isPaused: Bool, labelLengthInDots: Int, numberOfFormatsInReceiveBuffer: Int, labelsRemainingInBatch: Int, isPartialFormatInProgress: Bool, printMode: UInt32) {
        self.isReadyToPrint = isReadyToPrint
        self.isHeadOpen = isHeadOpen
        self.isHeadCold = isHeadCold
        self.isHeadTooHot = isHeadTooHot
        self.isPaperOut = isPaperOut
        self.isRibbonOut = isRibbonOut
        self.isReceiveBufferFull = isReceiveBufferFull
        self.isPaused = isPaused
        self.labelLengthInDots = labelLengthInDots
        self.numberOfFormatsInReceiveBuffer = numberOfFormatsInReceiveBuffer
        self.labelsRemainingInBatch = labelsRemainingInBatch
        self.isPartialFormatInProgress = isPartialFormatInProgress
        self.printMode = printMode
    }
    
    // Convert the object to JSON
    func toJson() -> String? {
        let encoder = JSONEncoder()
        encoder.outputFormatting = .prettyPrinted // For readable JSON output
        do {
            let jsonData = try encoder.encode(self)
            return String(data: jsonData, encoding: .utf8)
        } catch {
            print("Error encoding object to JSON: \(error)")
            return nil
        }
    }
}
