package vn.edu.iuh.fit.service.implement;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.stereotype.Service;import vn.edu.iuh.fit.entity.Tour;import vn.edu.iuh.fit.repositories.TourRepository;import vn.edu.iuh.fit.service.TourService;import java.util.List;@Servicepublic class TourServiceImpl implements TourService {    @Autowired    private TourRepository tourRepository;    @Override    public Tour createTour(Tour tour) {        return tourRepository.save(tour);    }    @Override    public Tour getTourById(Long id) {        return tourRepository.findById(id).get();    }    @Override    public List<Tour> getAllTours() {        return tourRepository.findAll();    }    @Override    public void updateTour(Tour tour) {        tourRepository.save(tour);    }    @Override    public void deleteTour(Long id) {        tourRepository.delete(tourRepository.findById(id).get());    }}