package com.university.website.service;

import com.university.website.dto.DashboardDto;

public interface DashboardService {

    DashboardDto getDashboardData(Long userId);
}
