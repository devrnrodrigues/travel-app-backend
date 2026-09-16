package com.devrenanrodrigues.travelapi.flight;

import com.devrenanrodrigues.travelapi.flight.client.BookingFlightClient;
import com.devrenanrodrigues.travelapi.flight.dto.FlightCardResponseDTO;
import com.devrenanrodrigues.travelapi.flight.dto.FlightLegDTO;
import com.devrenanrodrigues.travelapi.flight.dto.FlightSearchRequestDTO;
import com.devrenanrodrigues.travelapi.flight.dto.MultiStopsFlightSearchRequestDTO;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FlightService {

    private final BookingFlightClient bookingFlightClient;

    public List<FlightCardResponseDTO> searchFlights(FlightSearchRequestDTO request) {
        JsonNode rootNode = bookingFlightClient.searchFlights(request);
        return parseFlightOffers(rootNode, request.currencyCode());
    }

    public List<FlightCardResponseDTO> searchFlightsMultiStops(MultiStopsFlightSearchRequestDTO request) {
        JsonNode rootNode = bookingFlightClient.searchFlightsMultiStops(request);
        return parseFlightOffers(rootNode, request.currencyCode());
    }

    private List<FlightCardResponseDTO> parseFlightOffers(JsonNode rootNode, String fallbackCurrency) {
        if (rootNode == null || rootNode.isNull()) {
            return List.of();
        }

        if (rootNode.has("message") && !rootNode.path("status").asBoolean(false) && !rootNode.has("data")) {
            String message = rootNode.path("message").asText();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, message);
        }

        JsonNode offersNode = findFlightOffersNode(rootNode);
        if (offersNode == null || !offersNode.isArray() || offersNode.isEmpty()) {
            return List.of();
        }

        Map<String, AirlineInfo> airlineDict = extractAirlinesDictionary(rootNode);

        List<FlightCardResponseDTO> result = new ArrayList<>();
        for (JsonNode offer : offersNode) {
            FlightCardResponseDTO card = mapToFlightCard(offer, fallbackCurrency, airlineDict);
            if (card != null) {
                result.add(card);
            }
        }
        return result;
    }

    private Map<String, AirlineInfo> extractAirlinesDictionary(JsonNode rootNode) {
        Map<String, AirlineInfo> map = new HashMap<>();
        JsonNode airlinesNode = rootNode.path("data").path("aggregation").path("airlines");
        if (!airlinesNode.isArray()) {
            airlinesNode = rootNode.path("aggregation").path("airlines");
        }
        if (airlinesNode.isArray()) {
            for (JsonNode item : airlinesNode) {
                String code = item.path("iataCode").asText(item.path("code").asText(""));
                String name = item.path("name").asText("");
                String logoUrl = item.path("logoUrl").asText(item.path("logo").asText(""));
                if (!code.isBlank()) {
                    map.put(code.toUpperCase(Locale.ROOT), new AirlineInfo(name, logoUrl));
                }
            }
        }
        return map;
    }

    private JsonNode findFlightOffersNode(JsonNode root) {
        if (root.has("data")) {
            JsonNode dataNode = root.get("data");
            if (dataNode.has("flightOffers")) {
                return dataNode.get("flightOffers");
            }
            if (dataNode.has("itineraries")) {
                return dataNode.get("itineraries");
            }
            if (dataNode.isArray()) {
                return dataNode;
            }
        }
        if (root.has("flightOffers")) {
            return root.get("flightOffers");
        }
        if (root.has("itineraries")) {
            return root.get("itineraries");
        }
        return null;
    }

    private FlightCardResponseDTO mapToFlightCard(JsonNode offer, String fallbackCurrency, Map<String, AirlineInfo> airlineDict) {
        String id = offer.path("token").asText(offer.path("id").asText(UUID.randomUUID().toString()));

        JsonNode segmentsNode = offer.path("segments");
        if (!segmentsNode.isArray() || segmentsNode.isEmpty()) {
            segmentsNode = offer.path("legs");
        }

        List<FlightLegDTO> legs = new ArrayList<>();
        String mainAirlineName = "";
        String mainAirlineLogo = "";
        int maxStops = 0;

        if (segmentsNode.isArray() && !segmentsNode.isEmpty()) {
            for (int i = 0; i < segmentsNode.size(); i++) {
                JsonNode segment = segmentsNode.get(i);
                String label = (i == 0) ? "Ida" : (i == 1 ? "Volta" : "Trecho " + (i + 1));
                FlightLegDTO leg = parseSegment(segment, label);
                legs.add(leg);

                int segmentStops = countStops(segment);
                if (segmentStops > maxStops) {
                    maxStops = segmentStops;
                }

                if (i == 0) {
                    AirlineInfo airlineInfo = extractAirlineInfo(segment, airlineDict);
                    mainAirlineName = airlineInfo.name();
                    mainAirlineLogo = airlineInfo.logoUrl();
                }
            }
        }

        FlightLegDTO outbound = legs.isEmpty() ? null : legs.get(0);
        FlightLegDTO inbound = legs.size() > 1 ? legs.get(1) : null;

        String stopsLabel = (maxStops == 0) ? "Voo direto" : (maxStops == 1 ? "1 escala" : maxStops + " escalas");
        String baggageInfo = extractBaggageInfo(offer);

        PriceInfo priceInfo = extractPrice(offer, fallbackCurrency);
        String bookingUrl = extractBookingUrl(offer);

        return FlightCardResponseDTO.builder()
                .id(id)
                .airlineName(mainAirlineName)
                .airlineLogoUrl(mainAirlineLogo)
                .stopsCount(maxStops)
                .stopsLabel(stopsLabel)
                .outbound(outbound)
                .inbound(inbound)
                .allLegs(legs)
                .baggageInfo(baggageInfo)
                .currency(priceInfo.currency())
                .totalPrice(priceInfo.amount())
                .formattedPrice(priceInfo.formatted())
                .bookingUrl(bookingUrl)
                .build();
    }

    private FlightLegDTO parseSegment(JsonNode segment, String label) {
        String departureRaw = segment.path("departureTime").asText("");
        String arrivalRaw = segment.path("arrivalTime").asText("");

        String departureTime = formatTime(departureRaw);
        String arrivalTime = formatTime(arrivalRaw);
        String departureDate = formatDate(departureRaw);

        String duration = formatDuration(segment);

        JsonNode subLegs = segment.path("legs");
        String origin = "";
        String destination = "";

        if (subLegs.isArray() && !subLegs.isEmpty()) {
            JsonNode firstLeg = subLegs.get(0);
            JsonNode lastLeg = subLegs.get(subLegs.size() - 1);
            origin = extractAirport(firstLeg, true);
            destination = extractAirport(lastLeg, false);
        }

        if (origin.isBlank()) {
            origin = extractAirport(segment, true);
        }
        if (destination.isBlank()) {
            destination = extractAirport(segment, false);
        }

        return FlightLegDTO.builder()
                .label(label)
                .departureTime(departureTime)
                .arrivalTime(arrivalTime)
                .duration(duration)
                .originAirport(origin)
                .destinationAirport(destination)
                .departureDate(departureDate)
                .build();
    }

    private String extractAirport(JsonNode container, boolean isOrigin) {
        if (container == null || container.isMissingNode() || container.isNull()) {
            return "";
        }
        String[] keys = isOrigin
                ? new String[]{"departureAirport", "originAirport", "origin", "from", "fromAirport", "departure"}
                : new String[]{"arrivalAirport", "destinationAirport", "destination", "to", "toAirport", "arrival"};

        for (String key : keys) {
            if (container.has(key)) {
                String code = extractAirportCode(container.get(key));
                if (!code.isBlank()) {
                    return cleanAirportCode(code);
                }
            }
        }
        return "";
    }

    private String cleanAirportCode(String code) {
        if (code == null) {
            return "";
        }
        if (code.endsWith(".AIRPORT")) {
            return code.replace(".AIRPORT", "");
        }
        return code;
    }

    private String extractAirportCode(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return "";
        }
        if (node.has("code")) {
            return node.path("code").asText("");
        }
        if (node.has("iata")) {
            return node.path("iata").asText("");
        }
        if (node.has("iataCode")) {
            return node.path("iataCode").asText("");
        }
        if (node.has("cityName")) {
            return node.path("cityName").asText("");
        }
        if (node.has("city")) {
            return node.path("city").asText("");
        }
        if (node.has("name")) {
            return node.path("name").asText("");
        }
        if (node.isTextual()) {
            return node.asText("");
        }
        return "";
    }

    private int countStops(JsonNode segment) {
        JsonNode legs = segment.path("legs");
        if (legs.isArray() && !legs.isEmpty()) {
            return Math.max(0, legs.size() - 1);
        }
        if (segment.has("stops")) {
            return segment.path("stops").asInt(0);
        }
        return 0;
    }

    private AirlineInfo extractAirlineInfo(JsonNode segment, Map<String, AirlineInfo> airlineDict) {
        JsonNode subLegs = segment.path("legs");
        JsonNode firstLeg = (subLegs.isArray() && !subLegs.isEmpty()) ? subLegs.get(0) : segment;

        JsonNode carriersData = firstLeg.path("carriersData");
        if (carriersData.isArray() && !carriersData.isEmpty()) {
            JsonNode carrierItem = carriersData.get(0);
            String name = carrierItem.path("name").asText("");
            String code = carrierItem.path("code").asText("");
            String logo = carrierItem.path("logo").asText("");

            if (name.isBlank() && !code.isBlank() && airlineDict != null && airlineDict.containsKey(code.toUpperCase(Locale.ROOT))) {
                name = airlineDict.get(code.toUpperCase(Locale.ROOT)).name();
            }
            if (logo.isBlank() && !code.isBlank() && airlineDict != null && airlineDict.containsKey(code.toUpperCase(Locale.ROOT))) {
                logo = airlineDict.get(code.toUpperCase(Locale.ROOT)).logoUrl();
            }
            if (name.isBlank()) {
                name = code;
            }
            return new AirlineInfo(name, logo);
        }

        String carrierCode = firstLeg.path("carrierCode").asText(segment.path("carrierCode").asText(""));
        if (carrierCode.isBlank()) {
            JsonNode carrierInfo = firstLeg.path("flightInfo").path("carrierInfo");
            carrierCode = carrierInfo.path("operatingCarrier").asText(
                    carrierInfo.path("marketingCarrier").asText(
                            carrierInfo.path("carrier").asText("")
                    )
            );
        }

        if (!carrierCode.isBlank()) {
            String upperCode = carrierCode.toUpperCase(Locale.ROOT);
            if (airlineDict != null && airlineDict.containsKey(upperCode)) {
                AirlineInfo dictInfo = airlineDict.get(upperCode);
                String name = dictInfo.name().isBlank() ? upperCode : dictInfo.name();
                return new AirlineInfo(name, dictInfo.logoUrl());
            }
            return new AirlineInfo(upperCode, "");
        }

        return new AirlineInfo("", "");
    }

    private String extractBaggageInfo(JsonNode offer) {
        int handPieces = 0;
        int handWeight = 0;
        int checkedPieces = 0;
        int checkedWeight = 0;
        boolean hasPersonalItem = false;

        JsonNode includedSegments = offer.path("includedProducts").path("segments");
        if (includedSegments.isArray() && !includedSegments.isEmpty()) {
            JsonNode firstSeg = includedSegments.get(0);
            if (firstSeg.isArray()) {
                for (JsonNode item : firstSeg) {
                    String type = item.path("luggageType").asText("");
                    int maxPiece = item.path("maxPiece").asInt(1);
                    double weight = item.path("maxWeightPerPiece").asDouble(0.0);
                    String unit = item.path("massUnit").asText("");

                    if ("CHECKED_IN".equalsIgnoreCase(type)) {
                        checkedPieces = Math.max(checkedPieces, maxPiece);
                        int kg = convertToKg(weight, unit);
                        if (kg > 0) {
                            checkedWeight = kg;
                        }
                    } else if ("HAND".equalsIgnoreCase(type)) {
                        handPieces = Math.max(handPieces, maxPiece);
                        int kg = convertToKg(weight, unit);
                        if (kg > 0) {
                            handWeight = kg;
                        }
                    } else if ("PERSONAL_ITEM".equalsIgnoreCase(type)) {
                        hasPersonalItem = true;
                    }
                }
            }
        }

        if (checkedPieces == 0 && handPieces == 0) {
            JsonNode segments = offer.path("segments");
            if (segments.isArray()) {
                for (JsonNode seg : segments) {
                    JsonNode checkedLuggage = seg.path("travellerCheckedLuggage");
                    if (checkedLuggage.isArray()) {
                        for (JsonNode c : checkedLuggage) {
                            JsonNode allowance = c.path("luggageAllowance");
                            int pieces = allowance.path("maxPiece").asInt(1);
                            double weight = allowance.path("maxWeightPerPiece").asDouble(0.0);
                            String unit = allowance.path("massUnit").asText("");
                            int kg = convertToKg(weight, unit);
                            checkedPieces = Math.max(checkedPieces, pieces);
                            if (kg > 0) {
                                checkedWeight = kg;
                            }
                        }
                    }

                    JsonNode cabinLuggage = seg.path("travellerCabinLuggage");
                    if (cabinLuggage.isArray()) {
                        for (JsonNode c : cabinLuggage) {
                            JsonNode allowance = c.path("luggageAllowance");
                            int pieces = allowance.path("maxPiece").asInt(1);
                            double weight = allowance.path("maxWeightPerPiece").asDouble(0.0);
                            String unit = allowance.path("massUnit").asText("");
                            int kg = convertToKg(weight, unit);
                            handPieces = Math.max(handPieces, pieces);
                            if (kg > 0) {
                                handWeight = kg;
                            }
                            if (c.path("personalItem").asBoolean(false)) {
                                hasPersonalItem = true;
                            }
                        }
                    }
                }
            }
        }

        List<String> parts = new ArrayList<>();
        if (checkedPieces > 0) {
            if (checkedWeight > 0) {
                parts.add("Despachada: " + checkedPieces + "x " + checkedWeight + "kg");
            } else {
                parts.add("Despachada: " + checkedPieces + " volume(s)");
            }
        }

        if (handPieces > 0) {
            if (handWeight > 0) {
                parts.add("Mão: " + handPieces + "x " + handWeight + "kg");
            } else {
                parts.add("Mão: " + handPieces + " volume(s)");
            }
        } else if (hasPersonalItem && checkedPieces == 0) {
            parts.add("Item pessoal");
        }

        return String.join(" + ", parts);
    }

    private int convertToKg(double weight, String unit) {
        if (weight <= 0) {
            return 0;
        }
        if ("LB".equalsIgnoreCase(unit)) {
            return (int) Math.round(weight * 0.45359237);
        }
        return (int) Math.round(weight);
    }

    private PriceInfo extractPrice(JsonNode offer, String fallbackCurrency) {
        JsonNode priceBreakdown = offer.path("priceBreakdown");
        JsonNode totalNode = priceBreakdown.path("total");

        String currency = fallbackCurrency;
        BigDecimal amount = BigDecimal.ZERO;

        if (!totalNode.isMissingNode() && !totalNode.isNull()) {
            currency = totalNode.path("currencyCode").asText(fallbackCurrency);
            if (totalNode.has("units")) {
                long units = totalNode.path("units").asLong(0);
                long nanos = totalNode.path("nanos").asLong(0);
                double total = units + (nanos / 1_000_000_000.0);
                amount = BigDecimal.valueOf(total).setScale(2, RoundingMode.HALF_UP);
            } else if (totalNode.has("value")) {
                amount = BigDecimal.valueOf(totalNode.path("value").asDouble(0)).setScale(2, RoundingMode.HALF_UP);
            }
        } else if (offer.has("price")) {
            JsonNode priceNode = offer.get("price");
            currency = priceNode.path("currency").asText(fallbackCurrency);
            amount = BigDecimal.valueOf(priceNode.path("total").asDouble(0)).setScale(2, RoundingMode.HALF_UP);
        }

        String formatted = currency + " " + amount;
        return new PriceInfo(amount, currency, formatted);
    }

    private String extractBookingUrl(JsonNode offer) {
        if (offer.has("bookingUrl")) {
            return offer.path("bookingUrl").asText("");
        }
        if (offer.has("deepLink")) {
            return offer.path("deepLink").asText("");
        }
        if (offer.has("url")) {
            return offer.path("url").asText("");
        }
        String token = offer.path("token").asText("");
        if (!token.isBlank()) {
            return "https://www.booking.com/flights/index.html?token=" + token;
        }
        return "";
    }

    private String formatTime(String rawDateTime) {
        if (rawDateTime == null || rawDateTime.isBlank()) {
            return "";
        }
        if (rawDateTime.contains("T")) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(rawDateTime, DateTimeFormatter.ISO_DATE_TIME);
                return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
            } catch (Exception ignored) {
                String[] parts = rawDateTime.split("T");
                if (parts.length > 1 && parts[1].length() >= 5) {
                    return parts[1].substring(0, 5);
                }
            }
        }
        return rawDateTime;
    }

    private String formatDate(String rawDateTime) {
        if (rawDateTime == null || rawDateTime.isBlank()) {
            return "";
        }
        if (rawDateTime.contains("T")) {
            return rawDateTime.split("T")[0];
        }
        return rawDateTime;
    }

    private String formatDuration(JsonNode segment) {
        if (segment.has("totalTime")) {
            long totalSeconds = segment.path("totalTime").asLong(0);
            if (totalSeconds > 0) {
                long hours = totalSeconds / 3600;
                long minutes = (totalSeconds % 3600) / 60;
                return String.format(Locale.ROOT, "%02dh %02dm", hours, minutes);
            }
        }
        if (segment.has("duration")) {
            String raw = segment.path("duration").asText("");
            if (raw.startsWith("PT")) {
                try {
                    Duration duration = Duration.parse(raw);
                    long hours = duration.toHours();
                    long minutes = duration.toMinutesPart();
                    return String.format(Locale.ROOT, "%02dh %02dm", hours, minutes);
                } catch (Exception ignored) {
                }
            }
            if (!raw.isBlank()) {
                return raw;
            }
        }
        return "";
    }

    private record AirlineInfo(String name, String logoUrl) {}
    private record PriceInfo(BigDecimal amount, String currency, String formatted) {}
}
