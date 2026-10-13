package com.gatenest.dto;

import java.util.List;
import java.util.Map;

public class DashboardStatsDto {
    // Admin metrics
    private int activeResidents;
    private int pendingRegistrations;
    private int visitorsInside;
    private int entriesToday;
    private int exitsToday;
    private int pendingVisitorApprovals;

    // Security metrics
    private int waitingApprovals;
    private int expectedVisitorsToday;
    private int currentVisitors;
    private int overdueVisits;

    // Resident metrics
    private int upcomingInvitations;
    private int requestsAwaitingApproval;
    private int ownVisitorsInside;
    private int ownCompletedVisits;

    // Daily entries chart
    private List<Map<String, Object>> dailyEntries;

    public DashboardStatsDto() {}

    public int getActiveResidents() { return activeResidents; }
    public void setActiveResidents(int activeResidents) { this.activeResidents = activeResidents; }

    public int getPendingRegistrations() { return pendingRegistrations; }
    public void setPendingRegistrations(int pendingRegistrations) { this.pendingRegistrations = pendingRegistrations; }

    public int getVisitorsInside() { return visitorsInside; }
    public void setVisitorsInside(int visitorsInside) { this.visitorsInside = visitorsInside; }

    public int getEntriesToday() { return entriesToday; }
    public void setEntriesToday(int entriesToday) { this.entriesToday = entriesToday; }

    public int getExitsToday() { return exitsToday; }
    public void setExitsToday(int exitsToday) { this.exitsToday = exitsToday; }

    public int getPendingVisitorApprovals() { return pendingVisitorApprovals; }
    public void setPendingVisitorApprovals(int pendingVisitorApprovals) { this.pendingVisitorApprovals = pendingVisitorApprovals; }

    public int getWaitingApprovals() { return waitingApprovals; }
    public void setWaitingApprovals(int waitingApprovals) { this.waitingApprovals = waitingApprovals; }

    public int getExpectedVisitorsToday() { return expectedVisitorsToday; }
    public void setExpectedVisitorsToday(int expectedVisitorsToday) { this.expectedVisitorsToday = expectedVisitorsToday; }

    public int getCurrentVisitors() { return currentVisitors; }
    public void setCurrentVisitors(int currentVisitors) { this.currentVisitors = currentVisitors; }

    public int getOverdueVisits() { return overdueVisits; }
    public void setOverdueVisits(int overdueVisits) { this.overdueVisits = overdueVisits; }

    public int getUpcomingInvitations() { return upcomingInvitations; }
    public void setUpcomingInvitations(int upcomingInvitations) { this.upcomingInvitations = upcomingInvitations; }

    public int getRequestsAwaitingApproval() { return requestsAwaitingApproval; }
    public void setRequestsAwaitingApproval(int requestsAwaitingApproval) { this.requestsAwaitingApproval = requestsAwaitingApproval; }

    public int getOwnVisitorsInside() { return ownVisitorsInside; }
    public void setOwnVisitorsInside(int ownVisitorsInside) { this.ownVisitorsInside = ownVisitorsInside; }

    public int getOwnCompletedVisits() { return ownCompletedVisits; }
    public void setOwnCompletedVisits(int ownCompletedVisits) { this.ownCompletedVisits = ownCompletedVisits; }

    public List<Map<String, Object>> getDailyEntries() { return dailyEntries; }
    public void setDailyEntries(List<Map<String, Object>> dailyEntries) { this.dailyEntries = dailyEntries; }
}
