package com.devrenanrodrigues.travelapi.destination;

import com.devrenanrodrigues.travelapi.airport.Airport;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "destinations")
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

    @Column(length = 100, nullable = false)
    private String country;

    @Column(length = 50, nullable = false)
    private String category;

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
