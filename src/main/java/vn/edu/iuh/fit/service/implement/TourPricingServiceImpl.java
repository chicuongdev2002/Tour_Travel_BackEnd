package vn.edu.iuh.fit.service.implement;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.data.jpa.repository.JpaRepository;import org.springframework.stereotype.Service;import vn.edu.iuh.fit.entity.TourPricing;import vn.edu.iuh.fit.repositories.TourPricingRepository;import vn.edu.iuh.fit.service.AbstractCrudService;import vn.edu.iuh.fit.service.TourPricingService;import java.util.UUID;@Servicepublic class TourPricingServiceImpl extends AbstractCrudService<TourPricing, Long> implements TourPricingService {    @Autowired    private TourPricingRepository tourPricingRepository;    @Override    protected JpaRepository<TourPricing, Long> getRepository() {        return tourPricingRepository;    }}