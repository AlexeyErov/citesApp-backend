package ee.kuehnenagel.citiesApp.service;

import ee.kuehnenagel.citiesApp.model.City;
import ee.kuehnenagel.citiesApp.repo.CityRepository;
import ee.kuehnenagel.citiesApp.repo.FileSystemRepository;
import ee.kuehnenagel.citiesApp.utils.StatusEnum;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CityServiceImpl implements CityService {

    private static final String COMMA_DELIMITER = ",";

    private static final String CSV_DATA_DIR = "src/main/resources/cities.csv";

    private final CityRepository cityRepository;

    private final FileSystemRepository fileSystemRepository;

    public CityServiceImpl(CityRepository cityRepository, FileSystemRepository fileSystemRepository) {
        this.cityRepository = cityRepository;
        this.fileSystemRepository = fileSystemRepository;
    }

    public Boolean initCitiesCsvDataToDb() {
        List<City> citiesList = getCitiesDataFromCsv();
        for (City city : citiesList) {
            saveCityObject(city);
        }
        return !citiesList.isEmpty();
    }

    public void saveCityObject(City city) {
        if (city == null) {
            throw new IllegalArgumentException("City is null");
        } else if (city.getTitle().isBlank() || city.getStatus().isBlank() || city.getImageLocation().isBlank()) {
            throw new IllegalArgumentException("City data is empty");
        }
        cityRepository.save(city);
    }

    public City updateCity(City city) {
        if (city == null) {
            throw new IllegalArgumentException("City is null");
        } else if (city.getTitle().isBlank() || city.getStatus().isBlank() || city.getImageLocation().isBlank()) {
            throw new IllegalArgumentException("City data is empty");
        }
        return cityRepository.save(city);
    }

    public City findCityById(Long cityId) {
        Optional<City> cityById = cityRepository.findById(cityId);
        if (cityById != null && cityById.isPresent()) {
            return cityById.orElse(null);
        } else {
            return null;
        }
    }

    public List<String> getCitiesTitlesList() {
        List<String> citiesTitles = new ArrayList<>();
        Iterable<City> all = cityRepository.findAll();
        all.forEach(city -> citiesTitles.add(city.getTitle()));
        return citiesTitles;
    }

    public City getCityBySearchText(String searchText) {
        List<City> citiesBySearchText = cityRepository.findByTitle(searchText);
        if (!citiesBySearchText.isEmpty()) {
            if (citiesBySearchText.size() == 1) {
                return findCityById(citiesBySearchText.get(0).getId());
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public byte[] getCityImageByCityId(Long cityId) {
        if (cityId != null) {
            City cityById = findCityById(cityId);
            if (cityById != null && cityById.getStatus().equals(StatusEnum.FROM_CSV.name())) {
                String updatedImageLocation = saveImageToFileStorage(cityById);
                if (!cityById.getImageLocation().equals(updatedImageLocation)) {
                    updateCityLocation(cityById, updatedImageLocation);
                } else {
                    return null;
                }
            }

            if (cityById != null) {
                return getFileSystemResource(cityById);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public boolean uploadCityImage(MultipartFile imageFile, Long cityId) {
        City cityById = findCityById(cityId);
        try {
            if (imageFile != null) {
                byte[] imageInBytes = imageFile.getBytes();
                String cityTitle = cityById.getTitle();
                String cityImageFormat = getCityImageFormat(cityById.getImageLocation());
                Path pathToCityImageInFileStorage = fileSystemRepository.saveCityImage(imageInBytes, cityTitle, cityImageFormat);
                String updatedImageLocation = pathToCityImageInFileStorage.toString();
                updateCityLocation(cityById, updatedImageLocation);
                return true;
            } else {
                return false;
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }

        return false;
    }

    List<City> getCitiesDataFromCsv() {
        List<City> citiesList = new ArrayList<>();
        Path pathToFile = Paths.get(CityServiceImpl.CSV_DATA_DIR);
        try (BufferedReader br = Files.newBufferedReader(pathToFile)) {
            br.readLine();
            String row;
            while ((row = br.readLine()) != null) {
                String[] rowAttributes = row.split(COMMA_DELIMITER);
                City city = getCityFromCsvRow(rowAttributes);
                citiesList.add(city);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return citiesList;
    }

    private City getCityFromCsvRow(String[] rowAttributes) {
        Long cityId = Long.parseLong(rowAttributes[0]);
        String cityName = rowAttributes[1];
        String cityImageUrl = rowAttributes[2];
        return new City(cityId, cityName, cityImageUrl, StatusEnum.FROM_CSV.name());
    }

    private String saveImageToFileStorage(City city) {
        try {
            byte[] cityImageContent = getCityImageContent(city);
            if (cityImageContent.length == 0) {
                return city.getImageLocation();
            }
            String cityTitle = city.getTitle();
            String cityImageFormat = getCityImageFormat(city.getImageLocation());
            Path pathToCityImageInFileStorage = fileSystemRepository.saveCityImage(cityImageContent, cityTitle, cityImageFormat);
            return pathToCityImageInFileStorage.toAbsolutePath().toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return city.getImageLocation();
    }

    private byte[] getCityImageContent(City city) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = null;
        URL url = null;
        try {
            if (fileSystemRepository.isValidURL(city.getImageLocation())) {
                url = new URL(city.getImageLocation());
                is = url.openStream();

                byte[] byteChunk = new byte[4096];
                int n;

                while ((n = is.read(byteChunk)) > 0) {
                    baos.write(byteChunk, 0, n);
                }
            }
        } catch (IOException e) {
            System.err.printf("Failed while reading bytes from %s: %s", url != null ? url.toExternalForm() : null, e.getMessage());
            e.printStackTrace();
        } finally {
            if (is != null) {
                is.close();
            }
        }

        return baos.toByteArray();
    }

    private void updateCityLocation(City city, String updatedImageLocation) {
        city.setImageLocation(updatedImageLocation);
        city.setStatus(StatusEnum.DOWNLOADED.name());
        updateCity(city);
    }


    private String getCityImageFormat(String locationString) {
        return locationString.substring(locationString.lastIndexOf("."));
    }

    private byte[] getFileSystemResource(City city) {
        FileSystemResource inFileSystem = fileSystemRepository.findInFileSystem(city.getImageLocation());
        try {
            if (inFileSystem != null) {
                return Files.readAllBytes(inFileSystem.getFile().toPath());
            }
        } catch (IOException e) {
            System.err.printf("Failed while reading bytes from %s: %s", inFileSystem, e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
