package ru.berezhnoy.shop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
class SchemaBaselineTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void allBaselineTablesExist() {
        List<String> tables = jdbcTemplate.queryForList(
                """
                        SELECT table_name
                        FROM information_schema.tables
                        WHERE table_schema = 'public'
                          AND table_name IN (
                            'users', 'categories', 'brands', 'products',
                            'carts', 'cart_items', 'orders', 'order_items',
                            'payments', 'reviews'
                          )
                        ORDER BY table_name
                        """,
                String.class
        );

        assertThat(tables).containsExactly(
                "brands",
                "cart_items",
                "carts",
                "categories",
                "order_items",
                "orders",
                "payments",
                "products",
                "reviews",
                "users"
        );
    }

    @Test
    void demoUserHasCart() {
        Integer cartCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM carts WHERE user_id = 1",
                Integer.class
        );
        String email = jdbcTemplate.queryForObject(
                "SELECT email FROM users WHERE id = 1",
                String.class
        );

        assertThat(email).isEqualTo("ivan.petrov@example.com");
        assertThat(cartCount).isEqualTo(1);
    }

    @Test
    void newOrderHasPositiveTotalAndPendingPaymentWithoutTransaction() {
        BigDecimal total = jdbcTemplate.queryForObject(
                "SELECT total_amount FROM orders WHERE id = 1",
                BigDecimal.class
        );
        String transactionId = jdbcTemplate.queryForObject(
                "SELECT transaction_id FROM payments WHERE order_id = 1",
                String.class
        );

        assertThat(total).isEqualByComparingTo("249970.00");
        assertThat(transactionId).isNull();
    }
}
