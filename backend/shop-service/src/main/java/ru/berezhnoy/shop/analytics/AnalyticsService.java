package ru.berezhnoy.shop.analytics;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.berezhnoy.shop.domain.OrderStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
public class AnalyticsService {

    static final Set<OrderStatus> REVENUE_STATUSES = EnumSet.of(
            OrderStatus.PAID,
            OrderStatus.PROCESSING,
            OrderStatus.SHIPPED,
            OrderStatus.DELIVERED
    );

    private static final LocalDate DEFAULT_FROM = LocalDate.of(2020, 1, 1);
    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 50;

    private final AnalyticsQueryRepository analyticsQueryRepository;

    public AnalyticsService(AnalyticsQueryRepository analyticsQueryRepository) {
        this.analyticsQueryRepository = analyticsQueryRepository;
    }

    @Transactional(readOnly = true)
    public List<TopProductResponse> topProducts(LocalDate from, LocalDate to, Integer limit) {
        Period period = period(from, to);
        return analyticsQueryRepository.findTopProducts(
                period.from(),
                period.toExclusive(),
                REVENUE_STATUSES,
                limit(limit)
        );
    }

    @Transactional(readOnly = true)
    public SalesSummaryResponse salesSummary(LocalDate from, LocalDate to) {
        Period period = period(from, to);
        return new SalesSummaryResponse(
                analyticsQueryRepository.findCategorySales(period.from(), period.toExclusive(), REVENUE_STATUSES),
                analyticsQueryRepository.countOrdersByStatus(period.from(), period.toExclusive())
        );
    }

    private static Period period(LocalDate from, LocalDate to) {
        LocalDate start = from == null ? DEFAULT_FROM : from;
        LocalDate end = to == null ? LocalDate.now() : to;
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("to must not be before from");
        }
        return new Period(start.atStartOfDay(), end.plusDays(1).atStartOfDay());
    }

    private static int limit(Integer limit) {
        int value = limit == null ? DEFAULT_LIMIT : limit;
        if (value < 1 || value > MAX_LIMIT) {
            throw new IllegalArgumentException("limit must be between 1 and " + MAX_LIMIT);
        }
        return value;
    }

    private record Period(LocalDateTime from, LocalDateTime toExclusive) {
    }
}
