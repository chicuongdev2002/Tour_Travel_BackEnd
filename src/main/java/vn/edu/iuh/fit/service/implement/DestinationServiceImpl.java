package vn.edu.iuh.fit.service.implement;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.data.jpa.repository.JpaRepository;import org.springframework.stereotype.Service;import vn.edu.iuh.fit.entity.Destination;import vn.edu.iuh.fit.repositories.DestinationRepository;import vn.edu.iuh.fit.service.AbstractCrudService;import vn.edu.iuh.fit.service.DestinationService;import java.util.UUID;@Servicepublic class DestinationServiceImpl extends AbstractCrudService<Destination, Long> implements DestinationService {    @Autowired    private DestinationRepository destinationRepository;    @Override    protected JpaRepository<Destination, Long> getRepository() {        return destinationRepository;    }}