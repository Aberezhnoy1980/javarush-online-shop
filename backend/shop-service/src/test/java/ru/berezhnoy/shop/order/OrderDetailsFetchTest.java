package ru.berezhnoy.shop.order;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import ru.berezhnoy.shop.PostgresTestConfiguration;
import ru.berezhnoy.shop.domain.Order;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
@Transactional
class OrderDetailsFetchTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void orderDetailsGraphDoesNotScaleWithItemCount() {
        Statistics statistics = entityManager.getEntityManagerFactory()
                .unwrap(SessionFactory.class)
                .getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();

        Order order = orderRepository.findByIdAndUser_Id(1, 1).orElseThrow();
        OrderDetailsResponse details = OrderMapper.toDetails(order);

        assertThat(details.items()).hasSize(2);
        assertThat(details.payment()).isNotNull();
        assertThat(statistics.getPrepareStatementCount())
                .as("EntityGraph should load order, items, products and payment in one SELECT")
                .isLessThanOrEqualTo(1);
    }
}
