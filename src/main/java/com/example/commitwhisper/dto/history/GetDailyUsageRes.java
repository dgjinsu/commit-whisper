package com.example.commitwhisper.dto.history;

import java.time.LocalDate;

public record GetDailyUsageRes(
    LocalDate date,
    long count
) {

}

