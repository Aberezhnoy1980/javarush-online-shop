package ru.berezhnoy.shop.analytics;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import ru.berezhnoy.shop.domain.OrderStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public class AnalyticsQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<TopProductResponse> findTopProducts(
            LocalDateTime from,
            LocalDateTime toExclusive,
            Collection<OrderStatus> revenueStatuses,
            int limit
    ) {
        return entityManager.createQuery(
                        """
                                select new ru.berezhnoy.shop.analytics.TopProductResponse(
                                    p.id,
                                    p.name,
                                    sum(oi.quantity),
                                    sum(oi.lineTotal),
                                    (select avg(r.rating) from Review r where r.product = p)
                                )
                                from OrderItem oi
                                join oi.order o
                                join oi.product p
                                where o.createdAt >= :from
                                  and o.createdAt < :to
                                  and o.status in :revenueStatuses
                                group by p.id, p.name
                                order by sum(oi.lineTotal) desc
                                """,
                        TopProductResponse.class
                )
                .setParameter("from", from)
                .setParameter("to", toExclusive)
                .setParameter("revenueStatuses", revenueStatuses)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<CategorySalesResponse> findCategorySales(
            LocalDateTime from,
            LocalDateTime toExclusive,
            Collection<OrderStatus> revenueStatuses
    ) {
        return entityManager.createQuery(
                        """
                                select new ru.berezhnoy.shop.analytics.CategorySalesResponse(
                                    c.id,
                                    c.name,
                                    count(distinct o.id),
                                    sum(oi.quantity),
                                    sum(oi.lineTotal)
                                )
                                from OrderItem oi
                                join oi.order o
                                join oi.product p
                                join p.category c
                                where o.createdAt >= :from
                                  and o.createdAt < :to
                                  and o.status in :revenueStatuses
                                group by c.id, c.name
                                order by sum(oi.lineTotal) desc
                                """,
                        CategorySalesResponse.class
                )
                .setParameter("from", from)
                .setParameter("to", toExclusive)
                .setParameter("revenueStatuses", revenueStatuses)
                .getResultList();
    }

    public List<OrderStatusCountResponse> countOrdersByStatus(LocalDateTime from, LocalDateTime toExclusive) {
        return entityManager.createQuery(
                        """
                                select new ru.berezhnoy.shop.analytics.OrderStatusCountResponse(
                                    o.status,
                                    count(o)
                                )
                                from Order o
                                where o.createdAt >= :from
                                  and o.createdAt < :to
                                group by o.status
                                order by o.status
                                """,
                        OrderStatusCountResponse.class
                )
                .setParameter("from", from)
                .setParameter("to", toExclusive)
                .getResultList();
    }
}
