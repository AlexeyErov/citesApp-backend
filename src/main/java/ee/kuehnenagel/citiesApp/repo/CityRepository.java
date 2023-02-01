package ee.kuehnenagel.citiesApp.repo;

import ee.kuehnenagel.citiesApp.model.City;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CityRepository extends CrudRepository<City, Long> {
    @Query("SELECT c FROM City c where " +
            "(:searchText is not null and lower(c.title) like lower(concat('%', :searchText,'%')))  " +
            "order by c.id asc")
    List<City> findByTitle(@Param("searchText") String searchText);

}
