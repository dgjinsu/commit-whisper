package com.example.commitwhisper.repository;

import com.example.commitwhisper.dto.common.PageResponse;
import com.example.commitwhisper.dto.history.GetCommitSummaryHistoryRes;
import com.example.commitwhisper.entity.QCommitSummaryHistory;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CommitSummaryHistoryRepositoryImpl implements CommitSummaryHistoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public PageResponse<GetCommitSummaryHistoryRes> findByUserIdWithPaging(Long userId, int page, int size) {
        QCommitSummaryHistory history = QCommitSummaryHistory.commitSummaryHistory;

        // 전체 개수 조회
        Long totalCount = queryFactory
            .select(history.count())
            .from(history)
            .where(history.user.id.eq(userId))
            .fetchOne();

        long totalElements = totalCount != null ? totalCount : 0L;

        // 페이징 데이터 조회
        List<GetCommitSummaryHistoryRes> content = queryFactory
            .select(Projections.constructor(
                GetCommitSummaryHistoryRes.class,
                history.id,
                history.user.id,
                history.user.name,
                history.repoInfo.id,
                history.repoInfo.owner,
                history.repoInfo.repo,
                history.commitSha,
                history.summary,
                history.commitDate,
                history.createdAt
            ))
            .from(history)
            .leftJoin(history.user)
            .leftJoin(history.repoInfo)
            .where(history.user.id.eq(userId))
            .orderBy(history.createdAt.desc())
            .offset((long) page * size)
            .limit(size)
            .fetch();

        return PageResponse.of(content, page, size, totalElements);
    }
}

