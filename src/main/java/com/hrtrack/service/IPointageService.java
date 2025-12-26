package com.hrtrack.service;

import com.hrtrack.domain.entities.User;
import com.hrtrack.domain.requests.pointage.PointageRequest;
import com.hrtrack.domain.responses.pointage.PointageResponse;


import java.util.List;

public interface IPointageService {

    PointageResponse pointer(PointageRequest request, Long userId) throws Exception;

    List<PointageResponse> getPointages(User user);
}