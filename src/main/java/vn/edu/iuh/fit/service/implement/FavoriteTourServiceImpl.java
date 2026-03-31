package vn.edu.iuh.fit.service.implement;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.dto.FavoriteTourDTO;
import vn.edu.iuh.fit.dto.TourListDTO;
import vn.edu.iuh.fit.entity.Departure;
import vn.edu.iuh.fit.entity.Discount;
import vn.edu.iuh.fit.entity.FavoriteTour;
import vn.edu.iuh.fit.entity.Review;
import vn.edu.iuh.fit.entity.Tour;
import vn.edu.iuh.fit.entity.TourPricing;
import vn.edu.iuh.fit.entity.User;
import vn.edu.iuh.fit.enums.ParticipantType;
import vn.edu.iuh.fit.enums.TourType;
import vn.edu.iuh.fit.repositories.FavoriteTourRepository;
import vn.edu.iuh.fit.repositories.ReviewRepository;
import vn.edu.iuh.fit.repositories.TourPricingRepository;
import vn.edu.iuh.fit.repositories.TourRepository;
import vn.edu.iuh.fit.repositories.UserRepository;
import vn.edu.iuh.fit.service.FavoriteTourService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FavoriteTourServiceImpl implements FavoriteTourService {

    @Autowired
    private FavoriteTourRepository favoriteTourRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private TourPricingRepository tourPricingRepository;

    @Override
    public FavoriteTourDTO addFavoriteTour(Long userId, Long tourId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new IllegalArgumentException("Tour not found with ID: " + tourId));

        FavoriteTour favoriteTour = new FavoriteTour();
        favoriteTour.setUser(user);
        favoriteTour.setTour(tour);
        favoriteTour.setAddedDate(LocalDateTime.now(ZoneId.of("Asia/Bangkok")));

        FavoriteTour savedFavoriteTour = favoriteTourRepository.save(favoriteTour);
        FavoriteTourDTO favoriteTourDTO = new FavoriteTourDTO();
        favoriteTourDTO.setId(savedFavoriteTour.getId());
        favoriteTourDTO.setUserId(user.getUserId());
        favoriteTourDTO.setTourId(tour.getTourId());
        favoriteTourDTO.setAddedDate(savedFavoriteTour.getAddedDate());
        return favoriteTourDTO;
    }

    @Override
    public void removeFavoriteTour(Long userId, Long tourId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found for id: " + userId));
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new EntityNotFoundException("Tour not found for id: " + tourId));

        FavoriteTour favoriteTour = favoriteTourRepository.findByUserAndTour(user, tour);
        if (favoriteTour == null) {
            throw new EntityNotFoundException("Favorite tour not found for userId: " + userId + " and tourId: " + tourId);
        }
        favoriteTourRepository.delete(favoriteTour);
    }

    @Override
    public Set<Tour> getFavoriteToursByUser(User user) {
        return null;
    }

    @Override
    public List<TourListDTO> getFavoriteToursByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        List<FavoriteTour> favoriteTours = favoriteTourRepository.findByUserOrderByAddedDateDesc(user);
        if (favoriteTours.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> orderedTourIds = favoriteTours.stream()
                .map(FavoriteTour::getTour)
                .filter(Objects::nonNull)
                .map(Tour::getTourId)
                .distinct()
                .toList();
        Map<Long, Tour> hydratedToursById = loadToursById(orderedTourIds);
        Map<Long, List<TourPricing>> pricingByDepartureId = loadLatestPricingMap(hydratedToursById.values());

        LocalDateTime threeDaysFromNow = LocalDateTime.now(ZoneId.of("Asia/Bangkok")).plusDays(3);
        Set<Long> seenTourIds = new HashSet<>();
        List<TourListDTO> result = new ArrayList<>();

        for (FavoriteTour favoriteTour : favoriteTours) {
            Tour tour = favoriteTour.getTour() == null ? null : hydratedToursById.get(favoriteTour.getTour().getTourId());
            if (tour == null || !tour.isActive() || tour.getTourType() == TourType.DELETE || !seenTourIds.add(tour.getTourId())) {
                continue;
            }

            boolean hasAvailableDeparture = tour.getDepartures().stream()
                    .anyMatch(departure -> departure.isActive()
                            && departure.getAvailableSeats() != null
                            && departure.getAvailableSeats() > 0
                            && departure.getStartDate() != null
                            && departure.getStartDate().isAfter(threeDaysFromNow));
            if (!hasAvailableDeparture) {
                continue;
            }

            result.add(buildTourListDTO(tour, pricingByDepartureId, null, null, null, true));
        }

        return result;
    }

    @Override
    public List<TourListDTO> getAllFavoriteTours() {
        List<FavoriteTour> favoriteTours = favoriteTourRepository.findAll();
        if (favoriteTours.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Long> favoriteCountMap = favoriteTours.stream()
                .map(FavoriteTour::getTour)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Tour::getTourId, Collectors.counting()));

        List<Long> tourIds = favoriteCountMap.keySet().stream().toList();
        Map<Long, Tour> hydratedToursById = loadToursById(tourIds);
        Map<Long, List<TourPricing>> pricingByDepartureId = loadLatestPricingMap(hydratedToursById.values());
        List<Review> reviews = reviewRepository.findByTour_TourIdIn(tourIds);

        Map<Long, Long> reviewCountMap = reviews.stream()
                .filter(review -> review.getTour() != null)
                .collect(Collectors.groupingBy(review -> review.getTour().getTourId(), Collectors.counting()));

        Map<Long, Double> averageRatingMap = reviews.stream()
                .filter(review -> review.getTour() != null)
                .collect(Collectors.groupingBy(
                        review -> review.getTour().getTourId(),
                        Collectors.averagingInt(Review::getRating)
                ));

        return favoriteCountMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue(Comparator.reverseOrder()))
                .map(entry -> {
                    Tour tour = hydratedToursById.get(entry.getKey());
                    if (tour == null || !tour.isActive() || tour.getTourType() == TourType.DELETE) {
                        return null;
                    }
                    return buildTourListDTO(
                            tour,
                            pricingByDepartureId,
                            entry.getValue(),
                            reviewCountMap.getOrDefault(tour.getTourId(), 0L),
                            roundAverageRating(averageRatingMap.getOrDefault(tour.getTourId(), 0.0)),
                            false
                    );
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private Map<Long, Tour> loadToursById(List<Long> tourIds) {
        if (tourIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return tourRepository.findAllWithListingRelationsByTourIdIn(tourIds).stream()
                .collect(Collectors.toMap(Tour::getTourId, tour -> tour, (left, right) -> left));
    }

    private Map<Long, List<TourPricing>> loadLatestPricingMap(Iterable<Tour> tours) {
        List<Departure> departures = new ArrayList<>();
        for (Tour tour : tours) {
            departures.addAll(tour.getDepartures());
        }
        if (departures.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, LocalDateTime> departureStartDates = departures.stream()
                .collect(Collectors.toMap(Departure::getDepartureId, Departure::getStartDate, (left, right) -> left));

        List<Long> departureIds = departures.stream()
                .map(Departure::getDepartureId)
                .toList();

        List<TourPricing> sortedPricings = tourPricingRepository.findTourPricingByDepartureIds(departureIds).stream()
                .sorted(Comparator.comparing(TourPricing::getModifiedDate, Comparator.nullsLast(LocalDateTime::compareTo)).reversed())
                .toList();

        Map<Long, EnumMap<ParticipantType, TourPricing>> latestByDeparture = new HashMap<>();
        for (TourPricing pricing : sortedPricings) {
            Long departureId = pricing.getDeparture().getDepartureId();
            LocalDateTime startDate = departureStartDates.get(departureId);
            if (startDate != null && pricing.getModifiedDate() != null && pricing.getModifiedDate().isAfter(startDate)) {
                continue;
            }
            latestByDeparture
                    .computeIfAbsent(departureId, ignored -> new EnumMap<>(ParticipantType.class))
                    .putIfAbsent(pricing.getParticipantType(), pricing);
        }

        Map<Long, List<TourPricing>> pricingByDepartureId = new HashMap<>();
        for (Departure departure : departures) {
            EnumMap<ParticipantType, TourPricing> departurePricings = latestByDeparture.getOrDefault(
                    departure.getDepartureId(),
                    new EnumMap<>(ParticipantType.class)
            );
            List<TourPricing> latestPricings = Arrays.stream(ParticipantType.values())
                    .map(participantType -> departurePricings.getOrDefault(
                            participantType,
                            TourPricing.builder()
                                    .departure(departure)
                                    .participantType(participantType)
                                    .price(BigDecimal.ZERO)
                                    .modifiedDate(departure.getStartDate() != null ? departure.getStartDate().minusSeconds(1) : null)
                                    .build()
                    ))
                    .toList();
            pricingByDepartureId.put(departure.getDepartureId(), latestPricings);
        }

        return pricingByDepartureId;
    }

    private TourListDTO buildTourListDTO(
            Tour tour,
            Map<Long, List<TourPricing>> pricingByDepartureId,
            Long likeCount,
            Long reviewCount,
            Double averageRating,
            boolean includeDiscount
    ) {
        BigDecimal originalPrice = getLowestPrice(tour, pricingByDepartureId);
        BigDecimal displayPrice = originalPrice;

        if (includeDiscount) {
            Discount activeDiscount = findActiveDiscount(tour);
            if (activeDiscount != null) {
                BigDecimal discountPercentage = BigDecimal.valueOf(activeDiscount.getDiscountAmount());
                BigDecimal discountAmount = originalPrice.multiply(
                        discountPercentage.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                );
                displayPrice = originalPrice.subtract(discountAmount);
                if (displayPrice.compareTo(BigDecimal.ZERO) < 0) {
                    displayPrice = BigDecimal.ZERO;
                }
            }
        }

        return TourListDTO.builder()
                .tourId(tour.getTourId())
                .tourName(tour.getTourName())
                .tourType(tour.getTourType())
                .tourDescription(tour.getTourDescription())
                .price(displayPrice)
                .originalPrice(includeDiscount ? originalPrice : null)
                .duration(tour.getDuration())
                .maxParticipants(tour.getDepartures().stream()
                        .findFirst()
                        .map(Departure::getMaxParticipants)
                        .orElse(null))
                .startLocation(tour.getStartLocation())
                .startDate(tour.getDepartures().stream()
                        .map(Departure::getStartDate)
                        .filter(Objects::nonNull)
                        .min(LocalDateTime::compareTo)
                        .orElse(null))
                .availableSeats(tour.getDepartures().stream()
                        .map(Departure::getAvailableSeats)
                        .filter(Objects::nonNull)
                        .reduce(0, Integer::sum))
                .imageUrl(tour.getImages().isEmpty() ? null : tour.getImages().iterator().next().getImageUrl())
                .likeCount(likeCount)
                .reviewCount(reviewCount)
                .averageRating(averageRating)
                .build();
    }

    private BigDecimal getLowestPrice(Tour tour, Map<Long, List<TourPricing>> pricingByDepartureId) {
        return tour.getDepartures().stream()
                .flatMap(departure -> pricingByDepartureId.getOrDefault(departure.getDepartureId(), List.of()).stream())
                .map(TourPricing::getPrice)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    private Discount findActiveDiscount(Tour tour) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Bangkok"));
        return tour.getDiscounts().stream()
                .filter(discount -> discount.getStartDate() != null && discount.getEndDate() != null)
                .filter(discount -> discount.getStartDate().isBefore(now)
                        && discount.getEndDate().isAfter(now)
                        && (discount.getCountUse() == null || discount.getCountUse() > 0))
                .findFirst()
                .orElse(null);
    }

    private double roundAverageRating(Double averageRating) {
        return BigDecimal.valueOf(averageRating)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
