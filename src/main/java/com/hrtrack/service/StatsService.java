package com.hrtrack.service;

import com.hrtrack.domain.dtos.StatsUserDto;
import com.hrtrack.domain.dtos.StatsRhDto;
import lombok.Data;

public interface StatsService {
    StatsUserDto getUserStats(String email);
    StatsRhDto getRhStats();
}