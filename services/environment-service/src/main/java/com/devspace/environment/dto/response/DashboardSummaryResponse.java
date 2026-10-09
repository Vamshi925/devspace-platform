package com.devspace.environment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardSummaryResponse {

    private long total;
    private long ready;
    private long provisioning;
    private long failed;
    private long expired;
    private long deleted;
    private long unreadNotifications;
}