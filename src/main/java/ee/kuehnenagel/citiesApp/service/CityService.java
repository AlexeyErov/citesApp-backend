package ee.kuehnenagel.citiesApp.service;

import ee.kuehnenagel.citiesApp.model.City;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CityService {

    Boolean initCitiesCsvDataToDb();

    City updateCity(City city);

    City findCityById(Long cityId);

    List<String> getCitiesTitlesList();

    City getCityBySearchText(String searchText);

    byte[] getCityImageByCityId(Long cityId);

    boolean uploadCityImage(MultipartFile imageFile, Long cityId);


}
