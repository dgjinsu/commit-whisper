package com.example.commitwhisper.dto.history;

import java.util.List;

public record GetHistoryPageRes(
    List<GetCommitSummaryHistoryRes> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean hasNext,
    boolean hasPrevious
) {

}

