package ee.kuehnenagel.citiesApp.controller;

import ee.kuehnenagel.citiesApp.model.City;
import ee.kuehnenagel.citiesApp.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping()
@CrossOrigin(origins = "http://localhost:4200")
public class CitiesController {

    @Autowired
    CityService cityService;

    @RequestMapping(value = "/init", method = RequestMethod.GET)
    public ResponseEntity<Boolean> initCitiesCsvDataToDb() {
        return new ResponseEntity<>(cityService.initCitiesCsvDataToDb(), HttpStatus.OK);
    }

    @RequestMapping(value = "/city/{cityId}", method = RequestMethod.GET)
    public ResponseEntity<City> findCityById(@PathVariable Long cityId) {
        return new ResponseEntity<>(cityService.findCityById(cityId), HttpStatus.OK);
    }

    @RequestMapping(value = "/city/{cityId}/image", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getCityImageByCityId(@PathVariable(value = "cityId") Long cityId) {
        return new ResponseEntity<>(cityService.getCityImageByCityId(cityId), HttpStatus.OK);
    }

    @RequestMapping(value = "/city/titles", method = RequestMethod.GET)
    public ResponseEntity<List<String>> getCitiesTitlesList() {
        return new ResponseEntity<>(cityService.getCitiesTitlesList(), HttpStatus.OK);
    }

    @RequestMapping(value = "/city/search", method = RequestMethod.GET)
    public ResponseEntity<City> getCityBySearchText(@RequestParam(value = "searchText") String searchText) {
        return new ResponseEntity<>(cityService.getCityBySearchText(searchText), HttpStatus.OK);
    }

    @PutMapping("/city/update")
    public ResponseEntity<City> updateCity(@RequestBody City city) {
        return new ResponseEntity<>(cityService.updateCity(city), HttpStatus.OK);
    }

    @RequestMapping(value = "/city/{cityId}/uploadFile", method = RequestMethod.POST)
    public ResponseEntity<Boolean> uploadCityImage(@PathVariable Long cityId,
                                                   @RequestParam("image") MultipartFile multipartImage) {
        return new ResponseEntity<>(cityService.uploadCityImage(multipartImage, cityId), HttpStatus.OK);
    }
}
