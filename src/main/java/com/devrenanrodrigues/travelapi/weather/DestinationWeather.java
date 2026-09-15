package com.devrenanrodrigues.travelapi.weather;

import com.devrenanrodrigues.travelapi.destination.Destination;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "destination_weather")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DestinationWeather {

    @Id
    private UUID destinationId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "destination_id")
    private Destination destination;

    @Column(nullable = false)
    private Double temperature;

    @Column(name = "wind_speed", nullable = false)
    private Double windSpeed;

    @Column(name = "rain_probability", nullable = false)
    private Integer rainProbability;

    @Column(name = "condition_text", length = 50, nullable = false)
    private String conditionText;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
