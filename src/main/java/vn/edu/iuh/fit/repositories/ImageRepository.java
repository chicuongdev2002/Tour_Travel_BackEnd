package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entity.Image;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
}