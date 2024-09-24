package com.mythic.artemis_zebra;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class MyPrinterStatus {
    @JsonProperty
     boolean isReadyToPrint;
    @JsonProperty
     boolean isHeadOpen;
    @JsonProperty
     boolean isHeadCold;
    @JsonProperty
     boolean isHeadTooHot;
    @JsonProperty
     boolean isPaperOut;
    @JsonProperty
     boolean isRibbonOut;
    @JsonProperty
     boolean isReceiveBufferFull;
    @JsonProperty
     boolean isPaused;
     int labelLengthInDots;
    @JsonProperty
     int numberOfFormatsInReceiveBuffer;
    @JsonProperty
     int labelsRemainingInBatch;
    @JsonProperty
     boolean isPartialFormatInProgress;
    @JsonProperty
     int printMode;

    // Constructor
    public MyPrinterStatus(boolean isReadyToPrint, boolean isHeadOpen, boolean isHeadCold, boolean isHeadTooHot,
                         boolean isPaperOut, boolean isRibbonOut, boolean isReceiveBufferFull, boolean isPaused,
                         int labelLengthInDots, int numberOfFormatsInReceiveBuffer, int labelsRemainingInBatch,
                         boolean isPartialFormatInProgress, int printMode) {
        this.isReadyToPrint = isReadyToPrint;
        this.isHeadOpen = isHeadOpen;
        this.isHeadCold = isHeadCold;
        this.isHeadTooHot = isHeadTooHot;
        this.isPaperOut = isPaperOut;
        this.isRibbonOut = isRibbonOut;
        this.isReceiveBufferFull = isReceiveBufferFull;
        this.isPaused = isPaused;
        this.labelLengthInDots = labelLengthInDots;
        this.numberOfFormatsInReceiveBuffer = numberOfFormatsInReceiveBuffer;
        this.labelsRemainingInBatch = labelsRemainingInBatch;
        this.isPartialFormatInProgress = isPartialFormatInProgress;
        this.printMode = printMode;
    }

    // Getters and Setters (if needed) can be added here

    // Convert the object to a JSON string using Jackson
    public String toJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // For pretty-printed JSON
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Enum for ZplPrintMode with all provided modes
    public enum ZplPrintMode {
        APPLICATOR,          // Applicator print mode
        CUTTER,              // Cutter print mode
        DELAYED_CUT,         // Delayed cut print mode
        KIOSK,               // Kiosk print mode
        LINERLESS_PEEL,      // Linerless peel print mode
        LINERLESS_REWIND,    // Linerless rewind print mode
        PARTIAL_CUTTER,      // Partial cutter print mode
        PEEL_OFF,            // Peel-off print mode
        REWIND,              // Rewind print mode
        RFID,                // RFID print mode
        TEAR_OFF,            // Tear-off print mode (this also implies Linerless Tear print mode)
        UNKNOWN              // Unknown print mode
    }

    public static void main(String[] args) {
        // Example usage
        MyPrinterStatus printerStatus = new MyPrinterStatus(true, false, false, false, true, false, false, false, 200, 5, 10, false, 0);

        String jsonOutput = printerStatus.toJson();
        if (jsonOutput != null) {
            System.out.println("JSON Output: " + jsonOutput);
        } else {
            System.out.println("Failed to convert to JSON.");
        }
    }
}
