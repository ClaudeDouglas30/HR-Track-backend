package com.hrtrack.domain.responses.conge;

import com.hrtrack.domain.dtos.LeaveRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Réponse paginée pour les demandes de congés.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaginatedLeaveRequestResponse {
    private boolean success;
    private String message;
    private List<LeaveRequestDto> leaveRequests;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    private long pendingCount;
    private long approvedCount;
    private long rejectedCount;
}

