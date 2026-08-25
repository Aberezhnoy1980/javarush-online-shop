package ru.berezhnoy.shop.analytics;

import java.util.List;

public record SalesSummaryResponse(
        List<CategorySalesResponse> categories,
        List<OrderStatusCountResponse> orderStatuses
) {
}
