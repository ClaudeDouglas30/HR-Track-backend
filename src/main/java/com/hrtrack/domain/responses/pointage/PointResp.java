package com.hrtrack.domain.responses.pointage;

public record PointResp(
         Boolean success,
         String message,
         PointageResponse point
){
    
}