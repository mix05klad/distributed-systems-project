package gr.hua.dit.petcare.holidays;

import gr.hua.dit.petcare.holidays.model.HolidayDto;

import java.time.LocalDate;
import java.util.Optional;

public interface HolidayService {
    Optional<HolidayDto> findHoliday(LocalDate date);
}
