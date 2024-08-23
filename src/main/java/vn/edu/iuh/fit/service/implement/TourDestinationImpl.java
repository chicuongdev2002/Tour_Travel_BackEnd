package vn.edu.iuh.fit.service.implement;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.stereotype.Service;import vn.edu.iuh.fit.entity.TourDestination;import vn.edu.iuh.fit.repositories.TourDestinationRepository;import vn.edu.iuh.fit.service.TourDestinationService;import java.util.List;@Servicepublic class TourDestinationImpl implements TourDestinationService {    @Autowired    private TourDestinationRepository tourDestinationRepository;    @Override    public TourDestination createTourDestination(TourDestination tourDestination) {        return tourDestinationRepository.save(tourDestination);    }    @Override    public TourDestination getTourDestinationById(Long id) {        return tourDestinationRepository.findById(id).get();    }    @Override    public List<TourDestination> getAllTourDestinations() {        return tourDestinationRepository.findAll();    }    @Override    public void updateTourDestination(TourDestination tourDestination) {        tourDestinationRepository.save(tourDestination);    }    @Override    public void deleteTourDestination(Long id) {        tourDestinationRepository.delete(tourDestinationRepository.findById(id).get());    }}