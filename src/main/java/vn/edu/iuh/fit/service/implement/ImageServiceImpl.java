package vn.edu.iuh.fit.service.implement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.entity.Booking;
import vn.edu.iuh.fit.entity.Image;
import vn.edu.iuh.fit.repositories.ImageRepository;
import vn.edu.iuh.fit.service.BookingService;
import vn.edu.iuh.fit.service.ImageService;

@Service
public class ImageServiceImpl extends AbstractCrudService<Image, Long> implements ImageService {

    @Autowired
    private ImageRepository imageRepository;

    @Override
    protected JpaRepository<Image, Long> getRepository() {
        return imageRepository;
    }
}
