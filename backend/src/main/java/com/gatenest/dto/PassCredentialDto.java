package com.gatenest.dto;

import java.time.Instant;

public class PassCredentialDto {
    private String visitReference;
    private String passToken;
    private String qrCodeDataUrl;
    private Instant arrivalWindowStart;
    private Instant arrivalWindowEnd;
    private String visitorName;
    private String destinationFlatNumber;
    private String destinationBlockName;
    private String hostName;

    public PassCredentialDto() {}

    public String getVisitReference() { return visitReference; }
    public void setVisitReference(String visitReference) { this.visitReference = visitReference; }

    public String getPassToken() { return passToken; }
    public void setPassToken(String passToken) { this.passToken = passToken; }

    public String getQrCodeDataUrl() { return qrCodeDataUrl; }
    public void setQrCodeDataUrl(String qrCodeDataUrl) { this.qrCodeDataUrl = qrCodeDataUrl; }

    public Instant getArrivalWindowStart() { return arrivalWindowStart; }
    public void setArrivalWindowStart(Instant arrivalWindowStart) { this.arrivalWindowStart = arrivalWindowStart; }

    public Instant getArrivalWindowEnd() { return arrivalWindowEnd; }
    public void setArrivalWindowEnd(Instant arrivalWindowEnd) { this.arrivalWindowEnd = arrivalWindowEnd; }

    public String getVisitorName() { return visitorName; }
    public void setVisitorName(String visitorName) { this.visitorName = visitorName; }

    public String getDestinationFlatNumber() { return destinationFlatNumber; }
    public void setDestinationFlatNumber(String destinationFlatNumber) { this.destinationFlatNumber = destinationFlatNumber; }

    public String getDestinationBlockName() { return destinationBlockName; }
    public void setDestinationBlockName(String destinationBlockName) { this.destinationBlockName = destinationBlockName; }

    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = hostName; }
}
