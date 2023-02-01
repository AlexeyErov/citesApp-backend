package ee.kuehnenagel.citiesApp.repo;

import ee.kuehnenagel.citiesApp.CitiesAppApplication;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

@Repository
public class FileSystemRepository {

    private static final String separator = FileSystems.getDefault().getSeparator();
    private static final URL RESOURCES_DIR = CitiesAppApplication.class.getResource(separator);
    public Path saveCityImage(byte[] content, String cityName, String imageFormat) throws IOException, URISyntaxException {
        if (RESOURCES_DIR != null) {
            URI uri = RESOURCES_DIR.toURI();
            String mainPath = Paths.get(uri) + separator + "downloadedImages" + separator;
            Path newFile = Paths.get(mainPath + new Date().getTime() + "-" + cityName + imageFormat);
            Files.createDirectories(newFile.getParent());

            Files.write(newFile, content);

            return newFile;
        }
        return null;
    }

    public FileSystemResource findInFileSystem(String location) {
        try {
            return new FileSystemResource(Paths.get(location));
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public boolean isValidURL(String url) throws MalformedURLException {
        UrlValidator validator = new UrlValidator();
        return validator.isValid(url);
    }
}
