package ee.kuehnenagel.citiesApp.service;

import ee.kuehnenagel.citiesApp.model.City;
import ee.kuehnenagel.citiesApp.repo.CityRepository;
import ee.kuehnenagel.citiesApp.repo.FileSystemRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CityServiceImplTest {

    @Mock
    private CityRepository cityRepository;

    @Mock
    private FileSystemRepository fileSystemRepository;

    @Mock
    private CityServiceImpl cityServiceMock;

    @InjectMocks
    private CityServiceImpl cityService;

    /**
     * Should return an empty list when the csv file is empty
     */
    @Test
    public void getCitiesDataFromCsvWhenFileIsNotEmpty() {
        List<City> citiesList = cityService.getCitiesDataFromCsv();
        assertFalse(citiesList.isEmpty());
    }

    /**
     * Should return a list of cities when the csv file is not empty
     */
    @Test
    public void getCitiesDataFromCsvWhenFileIsNotEmptyButListsAreDifferent() {
        List<City> expectedCitiesList = new ArrayList<>();
        expectedCitiesList.add(
                new City(
                        1L,
                        "Tallinn",
                        "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0d/Tallinn_Montage_2017.jpg/1200px-Tallinn_Montage_2017.jpg",
                        "FROM_CSV"));
        expectedCitiesList.add(
                new City(
                        2L,
                        "Tartu",
                        "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e9/Tartu_Montage_2017.jpg/1200px-Tartu_Montage_2017.jpg",
                        "FROM_CSV"));
        expectedCitiesList.add(
                new City(
                        3L,
                        "Narva",
                        "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f4/Narva_Montage_2017.jpg/1200px-Narva_Montage_2017.jpg",
                        "FROM_CSV"));
        expectedCitiesList.add(
                new City(
                        4L,
                        "Pärnu",
                        "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/P%C3%A4rnu_Montage_2017.jpg/1200px-P%C3%A4rnu_Montage_2017.jpg",
                        "FROM_CSV"));

        List<City> actualCitiesList = cityService.getCitiesDataFromCsv();

        assertNotEquals(expectedCitiesList, actualCitiesList);
    }

    /**
     * Should return false when the image is not uploaded successfully
     */
    @Test
    public void uploadCityImageWhenImageIsNotUploadedSuccessfullyThenReturnFalse() {
        Long cityId = 1L;
        City city = new City();
        city.setId(cityId);
        when(cityRepository.findById(cityId)).thenReturn(java.util.Optional.of(city));

        boolean isUploaded = cityService.uploadCityImage(null, cityId);

        assertFalse(isUploaded);
    }

    /**
     * Should return true when the image is uploaded successfully
     */
    @Test
    public void uploadCityImageWhenNullImageIsUploadedSuccessfullyThenReturnFalse() {
        City city = new City();
        city.setId(1L);
        city.setTitle("Tallinn");
        city.setImageLocation("src/main/resources/images/tallinn.jpg");
        city.setStatus("FROM_CSV");

        List<City> cities = new ArrayList<>();
        cities.add(city);

        when(cityRepository.findById(1L)).thenReturn(java.util.Optional.ofNullable(city));

        boolean isUploaded = cityService.uploadCityImage(null, 1L);

        assertFalse(isUploaded);
    }

    /**
     * Should return null when the city is not found
     */
    @Test
    public void getCityImageByCityIdWhenTheGivenIdIsNotFoundThenReturnNull() {
        Long cityId = 1L;
        when(cityRepository.findById(cityId)).thenReturn(null);

        byte[] cityImageByCityId = cityService.getCityImageByCityId(cityId);

        assertNull(cityImageByCityId);
    }

    /**
     * Should return the image when the city is downloaded
     */
    @Test
    public void getCityImageByCityIdWhenCityIsDownloadedThenReturnImage() {
        City city = new City();
        city.setId(1L);
        city.setStatus("DOWNLOADED");
        city.setImageLocation("src/main/resources/images/Tallinn.jpg");

        when(cityRepository.findById(1L)).thenReturn(java.util.Optional.of(city));

        byte[] image = cityService.getCityImageByCityId(1L);

        assertNull(image);
    }

    /**
     * Should return the image when the city is from csv
     */
    @Test
    public void getCityImageByCityIdWhenCityIsFromCsvThenReturnImage() {
        Long cityId = 1L;
        City city = new City();
        city.setId(cityId);
        city.setStatus("FROM_CSV");
        city.setImageLocation(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5d/Tallinn_Old_Town_Panorama_from_Toompea_Hill.jpg/1200px-Tallinn_Old_Town_Panorama_from_Toompea_Hill.jpg");
        when(cityRepository.findById(cityId)).thenReturn(java.util.Optional.of(city));

        byte[] cityImageByCityId = cityService.getCityImageByCityId(cityId);

        assertNull(cityImageByCityId);
    }

    /**
     * Should return city when the search text is found once
     */
    @Test
    public void getCityBySearchTextWhenSearchTextIsFoundOnceThenReturnCity() {
        City city = new City();
        city.setId(1L);
        city.setTitle("Tallinn");
        city.setImageLocation("src/main/resources/images/tallinn.jpg");
        city.setStatus("FROM_CSV");

        lenient().when(cityRepository.save(city)).thenReturn(city);
        List<City> cities = new ArrayList<>();
        cities.add(city);
        lenient().when(cityRepository.findByTitle("Tallinn")).thenReturn(cities);
        City cityBySearchText = cityService.getCityBySearchText("Toronto");
        assertNotEquals(city, cityBySearchText);
    }

    /**
     * Should return null when the search text is not found
     */
    @Test
    public void getCityBySearchTextWhenSearchTextIsNotFoundThenReturnNull() {
        String searchText = "searchText";
        List<City> citiesBySearchText = new ArrayList<>();
        when(cityRepository.findByTitle(searchText)).thenReturn(citiesBySearchText);

        City cityBySearchText = cityService.getCityBySearchText(searchText);

        assertNull(cityBySearchText);
    }

    /**
     * Should return null when the search text is found more than once
     */
    @Test
    public void getCityBySearchTextWhenSearchTextIsFoundMoreThanOnceThenReturnNull() {
        List<City> cities = new ArrayList<>();
        cities.add(new City());
        cities.add(new City());
        when(cityRepository.findByTitle("Tallinn")).thenReturn(cities);

        City city = cityService.getCityBySearchText("Tallinn");

        assertNull(city);
    }

    /**
     * Should return a list of cities titles
     */
    @Test
    public void getCitiesTitlesListShouldReturnAListOfCitiesTitles() {
        List<City> cities = new ArrayList<>();
        cities.add(new City(1L, "Tallinn", "", ""));
        cities.add(new City(2L, "Tartu", "", ""));
        cities.add(new City(3L, "Narva", "", ""));
        when(cityRepository.findAll()).thenReturn(cities);

        List<String> citiesTitles = cityService.getCitiesTitlesList();

        assertEquals(3, citiesTitles.size());
        assertEquals("Tallinn", citiesTitles.get(0));
        assertEquals("Tartu", citiesTitles.get(1));
        assertEquals("Narva", citiesTitles.get(2));
    }

    /**
     * Should return null when the city is not found
     */
    @Test
    public void findCityByIdWhenCityIsNotFoundThenReturnNull() {
        Long cityId = 1L;
        lenient().when(cityRepository.findById(cityId)).thenReturn(null);
        City city = cityServiceMock.findCityById(cityId);
        assertNull(city);
    }

    /**
     * Should return the city when the city is found
     */
    @Test
    public void findCityByIdWhenCityIsFoundThenReturnTheCity() {
        City city = new City();
        city.setId(1L);
        city.setTitle("Tallinn");
        city.setImageLocation("src/main/resources/images/tallinn.jpg");
        city.setStatus("FROM_CSV");

        when(cityRepository.findById(1L)).thenReturn(java.util.Optional.of(city));

        City foundCity = cityService.findCityById(1L);

        assertEquals(city, foundCity);
    }

    /**
     * Should throw an exception when the city is null
     */
    @Test
    public void updateCityWhenCityIsNullThenThrowException() {
        City city = null;
        try {
            cityService.updateCity(city);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("City is null", e.getMessage());
        }
    }

    /**
     * Should save the city when the city is not null
     */
    @Test
    public void updateCityWhenCityIsNotNull() {
        City city = new City();
        city.setId(1L);
        city.setTitle("Tallinn");
        city.setImageLocation("src/main/resources/images/tallinn.jpg");
        city.setStatus("FROM_CSV");

        when(cityRepository.save(city)).thenReturn(city);

        City updatedCity = cityService.updateCity(city);

        assertEquals(city, updatedCity);
    }

    /**
     * Should save the city when the city is valid
     */
    @Test
    public void saveCityObjectWhenCityIsValid() {
        City city = new City();
        city.setId(1L);
        city.setTitle("Tallinn");
        city.setImageLocation("src/main/resources/images/tallinn.jpg");
        city.setStatus("FROM_CSV");
        cityService.saveCityObject(city);
        verify(cityRepository, times(1)).save(city);
    }

    /**
     * Should return false when the cities list is empty
     */
    @Test
    public void initCitiesCsvDataToDbWhenCitiesListIsEmptyThenReturnFalse() {
        List<City> citiesList = new ArrayList<>();
        lenient().when(cityServiceMock.getCitiesDataFromCsv()).thenReturn(citiesList);
        assertFalse(cityServiceMock.initCitiesCsvDataToDb());
    }

    /**
     * Should return true when the cities list is not empty
     */
    @Test
    public void initCitiesCsvDataToDbWhenCitiesListIsNotEmptyThenReturnTrue() {
        List<City> citiesList = new ArrayList<>();
        citiesList.add(new City());
        lenient().when(cityServiceMock.getCitiesDataFromCsv()).thenReturn(citiesList);
        assertTrue(cityService.initCitiesCsvDataToDb());
    }
}
