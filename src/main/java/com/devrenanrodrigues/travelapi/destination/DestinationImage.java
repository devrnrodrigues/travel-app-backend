package com.devrenanrodrigues.travelapi.destination;

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
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "destinations_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DestinationImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id", nullable = false)
    private Destination destination;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String url;

    @Column(name = "photographer", length = 150)
    private String photographer;

    @Column(name = "photographer_url", columnDefinition = "TEXT")
    private String photographerUrl;

    @Column(name = "is_cover", nullable = false)
    @Builder.Default
    private boolean isCover = false;

    @Column(name = "position", nullable = false)
    @Builder.Default
    private int position = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
