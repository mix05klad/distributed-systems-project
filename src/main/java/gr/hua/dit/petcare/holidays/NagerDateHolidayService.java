package gr.hua.dit.petcare.holidays;

import gr.hua.dit.petcare.holidays.model.HolidayDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NagerDateHolidayService implements HolidayService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NagerDateHolidayService.class);

    private final RestTemplate restTemplate;

    @Value("${holidays.base-url:https://date.nager.at}")
    private String baseUrl;

    @Value("${holidays.country-code:GR}")
    private String countryCode;

    // Cache by year για να μην χτυπάμε συνέχεια το API
    private final Map<Integer, List<HolidayDto>> yearCache = new ConcurrentHashMap<>();

    public NagerDateHolidayService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Optional<HolidayDto> findHoliday(LocalDate date) {
        if (date == null) return Optional.empty();

        int year = date.getYear();
        List<HolidayDto> holidays = yearCache.computeIfAbsent(year, this::fetchYearHolidays);

        if (holidays == null || holidays.isEmpty()) return Optional.empty();

        return holidays.stream()
                .filter(h -> date.equals(h.getDate()))
                .findFirst();
    }

    private List<HolidayDto> fetchYearHolidays(int year) {
        try {
            String url = baseUrl + "/api/v3/PublicHolidays/{year}/{countryCode}";
            HolidayDto[] arr = restTemplate.getForObject(url, HolidayDto[].class, year, countryCode);

            if (arr == null) return List.of();

            return Arrays.asList(arr);
        } catch (Exception ex) {
            LOGGER.warn("Failed to fetch holidays for year {} country {}: {}", year, countryCode, ex.getMessage());
            return List.of();
        }
    }
}
