package ru.berezhnoy.shop.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "product_name_snapshot", nullable = false, length = 250)
    private String productNameSnapshot;

    @Column(name = "price_at_time", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtTime;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "line_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal lineTotal;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected OrderItem() {
    }

    public Integer getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public Product getProduct() {
        return product;
    }

    public String getProductNameSnapshot() {
        return productNameSnapshot;
    }

    public BigDecimal getPriceAtTime() {
        return priceAtTime;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public static OrderItem snapshot(Product product, int quantity) {
        OrderItem item = new OrderItem();
        item.product = product;
        item.productNameSnapshot = product.getName();
        item.priceAtTime = product.getPrice();
        item.quantity = quantity;
        item.lineTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        item.createdAt = LocalDateTime.now();
        return item;
    }

    void assignTo(Order order) {
        this.order = order;
    }
}
