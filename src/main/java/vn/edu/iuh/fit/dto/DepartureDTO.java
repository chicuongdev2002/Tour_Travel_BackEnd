package vn.edu.iuh.fit.dto;import java.util.UUID;import java.time.LocalDate;import java.math.BigDecimal;import lombok.Data;@Datapublic class DepartureDTO {    private long departureId;    private LocalDate startDate;    private LocalDate endDate;    private int availableSeats;    private int maxParticipants;    private BigDecimal price;}