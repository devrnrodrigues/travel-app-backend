package com.devrenanrodrigues.travelapi.destination;

import com.devrenanrodrigues.travelapi.airport.Airport;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.devrenanrodrigues.travelapi.category.Category;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "destinations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_destination_name_country",
                columnNames = {"name", "country"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Destination {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nearest_airport_id")
    private Airport nearestAirport;

    @Column(length = 150, nullable = false)
    private String name;

    @Column(length = 100, nullable = false)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 100, nullable = false)
    private String country;

    @BatchSize(size = 50)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "destination_categories",
            joinColumns = @JoinColumn(name = "destination_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    private Set<Category> categories = new HashSet<>();

    public String getCategory() {
        return (categories != null && !categories.isEmpty()) ? categories.iterator().next().getName() : null;
    }

    @Column(nullable = false, columnDefinition = "double precision default 0.0")
    @Builder.Default
    private Double rating = 0.0;

    @Column(name = "review_count", nullable = false, columnDefinition = "integer default 0")
    @Builder.Default
    private Integer reviewCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "ai_status", length = 30, nullable = false, columnDefinition = "varchar(30) default 'PENDING'")
    @Builder.Default
    private AiStatus aiStatus = AiStatus.PENDING;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "photo_query", length = 150, nullable = false)
    private String photoQuery;

    @Column(name = "cover_image_url", columnDefinition = "text")
    private String coverImageUrl;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "gallery_urls", columnDefinition = "text[]")
    private List<String> galleryUrls;

    @Column(name = "ai_summary", columnDefinition = "text")
    private String aiSummary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_cost_estimates", columnDefinition = "jsonb")
    private String aiCostEstimates;

    @Column(name = "ai_cached_at")
    private Instant aiCachedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
