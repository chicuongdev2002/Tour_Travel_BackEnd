package vn.edu.iuh.fit.service.implement;import org.springframework.data.domain.Page;import org.springframework.data.domain.PageRequest;import org.springframework.data.domain.Pageable;import org.springframework.data.domain.Sort;import org.springframework.data.jpa.repository.JpaRepository;import vn.edu.iuh.fit.service.CrudService;import java.util.List;public abstract class AbstractCrudService<T, ID> implements CrudService<T, ID> {    protected abstract JpaRepository<T, ID> getRepository();    @Override    public T create(T entity) {        return getRepository().save(entity);    }    @Override    public T getById(ID id) {        return getRepository().findById(id).orElse(null);    }    @Override    public List<T> getAll() {        return getRepository().findAll();    }    @Override    public void update(T entity) {        getRepository().save(entity);    }    @Override    public void delete(ID id) {        getRepository().deleteById(id);    }    public Page<T> getPageList(int page, int size, String sortBy, String sortDirection){        Pageable pageable;        if(sortBy != null) {            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);            pageable = PageRequest.of(page, size, sort);            return getRepository().findAll(pageable);        }        pageable = PageRequest.of(page, size);        return getRepository().findAll(pageable);    }}