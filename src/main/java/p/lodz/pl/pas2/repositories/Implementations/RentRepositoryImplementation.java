package p.lodz.pl.pas2.repositories.Implementations;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import p.lodz.pl.pas2.model.Rent;
import p.lodz.pl.pas2.repositories.RentRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
@Scope("singleton")
public class RentRepositoryImplementation implements RentRepository {
    private final List<Rent> rents;


    public RentRepositoryImplementation() {
        this.rents = new CopyOnWriteArrayList<>();
    }

    @Override
    public Rent findRent(UUID id) {
        for (Rent rent : rents) {
            if (rent.getId().equals(id)) {
                return rent;
            }
        }
        return null;
    }

    @Override
    public Rent saveRent(Rent rent) {
        rent.setId(UUID.randomUUID());
        rents.add(rent);
        return rent;
    }

    @Override
    public boolean deleteRent(UUID id) {
        for (Rent rent : rents) {
            if (rent.getId().equals(id)) {
                rents.remove(rent);
                return true;
            }
        }
        return false;
    }


    @Override
    public List<Rent> findCurrentRents() {
        LocalDate currentDate = LocalDate.now();
        List<Rent> currentRents = new ArrayList<>();

        for (Rent rent : rents) {
            boolean isStartDateBeforeOrEqual = rent.getStartDate().isBefore(currentDate) || rent.getStartDate().isEqual(currentDate);
            boolean isEndDateNullOrAfter = rent.getEndDate() == null || rent.getEndDate().isAfter(currentDate);
            boolean isEndDateNullAndStartDateAfter = rent.getEndDate() == null && rent.getStartDate().isAfter(currentDate);

            if ((isStartDateBeforeOrEqual && isEndDateNullOrAfter) || isEndDateNullAndStartDateAfter) {
                currentRents.add(rent);
            }
        }

        return currentRents;
    }

    @Override
    public List<Rent> findPastRents() {
        LocalDate currentDate = LocalDate.now();
        List<Rent> pastRents = new ArrayList<>();

        for (Rent rent : rents) {
            if (rent.getEndDate() != null && rent.getEndDate().isBefore(currentDate)) {
                pastRents.add(rent);
            }
        }

        return pastRents;
    }

    @Override
    public Rent updateEndTime(UUID id, LocalDate endTime) {
        for(Rent rent : rents) {
            if(rent.getId().equals(id)) {
                rent.setEndDate(endTime);
                return rent;
            }
        }
        return null;
    }

    @Override
    public List<Rent> findCurrentRentsById(UUID movieId) {
        return null;
    }
}
