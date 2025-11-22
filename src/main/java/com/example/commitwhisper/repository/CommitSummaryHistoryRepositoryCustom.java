package com.example.commitwhisper.repository;

import com.example.commitwhisper.dto.common.PageResponse;
import com.example.commitwhisper.dto.history.GetCommitSummaryHistoryRes;

public interface CommitSummaryHistoryRepositoryCustom {
    PageResponse<GetCommitSummaryHistoryRes> findByUserIdWithPaging(Long userId, int page, int size);
}

