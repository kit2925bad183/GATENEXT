package com.gatenest.dto;

import com.gatenest.model.Visit;
import com.gatenest.model.VisitStatus;

import java.time.Instant;

public class PassVerificationResponse {
    private boolean valid;
    private String reason;
    private Visit visit;
    private boolean canCheckIn;
    private boolean canCheckOut;
    private boolean isWithinArrivalWindow;
    private Instant arrivalWindowStart;
    private Instant arrivalWindowEnd;

    public PassVerificationResponse() {}

    public static PassVerificationResponse valid(Visit visit, boolean canCheckIn, boolean canCheckOut, boolean isWithinArrivalWindow) {
        PassVerificationResponse res = new PassVerificationResponse();
        res.setValid(true);
        res.setVisit(visit);
        res.setCanCheckIn(canCheckIn);
        res.setCanCheckOut(canCheckOut);
        res.setWithinArrivalWindow(isWithinArrivalWindow);
        res.setArrivalWindowStart(visit.getArrivalWindowStart());
        res.setArrivalWindowEnd(visit.getArrivalWindowEnd());
        res.setReason("Pass is valid and active.");
        return res;
    }

    public static PassVerificationResponse invalid(String reason, Visit visit) {
        PassVerificationResponse res = new PassVerificationResponse();
        res.setValid(false);
        res.setVisit(visit);
        res.setReason(reason);
        res.setCanCheckIn(false);
        res.setCanCheckOut(false);
        if (visit != null) {
            res.setArrivalWindowStart(visit.getArrivalWindowStart());
            res.setArrivalWindowEnd(visit.getArrivalWindowEnd());
        }
        return res;
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Visit getVisit() { return visit; }
    public void setVisit(Visit visit) { this.visit = visit; }

    public boolean isCanCheckIn() { return canCheckIn; }
    public void setCanCheckIn(boolean canCheckIn) { this.canCheckIn = canCheckIn; }

    public boolean isCanCheckOut() { return canCheckOut; }
    public void setCanCheckOut(boolean canCheckOut) { this.canCheckOut = canCheckOut; }

    public boolean isWithinArrivalWindow() { return isWithinArrivalWindow; }
    public void setWithinArrivalWindow(boolean withinArrivalWindow) { isWithinArrivalWindow = withinArrivalWindow; }

    public Instant getArrivalWindowStart() { return arrivalWindowStart; }
    public void setArrivalWindowStart(Instant arrivalWindowStart) { this.arrivalWindowStart = arrivalWindowStart; }

    public Instant getArrivalWindowEnd() { return arrivalWindowEnd; }
    public void setArrivalWindowEnd(Instant arrivalWindowEnd) { this.arrivalWindowEnd = arrivalWindowEnd; }
}
