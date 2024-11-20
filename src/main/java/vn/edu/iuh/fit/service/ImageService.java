package vn.edu.iuh.fit.service;

import vn.edu.iuh.fit.entity.Image;
import vn.edu.iuh.fit.entity.Tour;

import java.util.List;

public interface ImageService extends CrudService<Image, Long>{
    List<Image> findAllByTouur(Tour tour);
}
