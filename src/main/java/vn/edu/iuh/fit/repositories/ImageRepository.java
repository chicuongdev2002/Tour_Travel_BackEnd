package vn.edu.iuh.fit.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.iuh.fit.entity.Image;
import vn.edu.iuh.fit.entity.Tour;

import java.util.List;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {

    @Query("SELECT i FROM Image i WHERE i.destination.destinationId = :destinationId")
    List<Image> findByDestinationId(@Param("destinationId") Long destinationId);
    List<Image> findAllByTour(Tour tour);
}


