package p.lodz.pl.pas2.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import p.lodz.pl.pas2.controllers.RentController;
import p.lodz.pl.pas2.exceptions.movieExceptions.MovieInUseException;
import p.lodz.pl.pas2.exceptions.rentExceptions.*;
import p.lodz.pl.pas2.exceptions.userExceptions.UserNotActiveException;
import p.lodz.pl.pas2.model.*;
import p.lodz.pl.pas2.request.RentRequest;
import p.lodz.pl.pas2.msg.MovieMsg;
import p.lodz.pl.pas2.msg.RentMsg;
import p.lodz.pl.pas2.msg.UserMsg;
import p.lodz.pl.pas2.security.UserAuthProvider;
import p.lodz.pl.pas2.services.MovieService;
import p.lodz.pl.pas2.services.RentService;
import p.lodz.pl.pas2.services.UserService;

import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class RentControllerTest {
    @MockBean
    private MovieService movieService;
    @MockBean
    private UserService userService;
    @MockBean
    private RentService rentService;
    @MockBean
    private UserAuthProvider userAuthProvider;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void addRent() throws Exception {
        UUID clientId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();

        Client activeUser = new Client("MaciekM", true, "Maciek", "Maciek", "1234");
        Movie availableMovie = new Movie("AvailableMovie", 20);

        RentRequest rentRequest = new RentRequest(clientId, movieId, LocalDate.now());
        Rent rent = new Rent(activeUser, availableMovie,rentRequest.getStartDate());

        Mockito.when(userService.getUser(clientId)).thenReturn(activeUser);
        Mockito.when(movieService.getMovie(movieId)).thenReturn(availableMovie);
        Mockito.when(rentService.addRent(Mockito.any(Rent.class))).thenReturn(rent);

        mockMvc.perform(post("/api/v1/rents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(rentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.client.username").value(activeUser.getUsername()))
                .andExpect(jsonPath("$.client.active").value(activeUser.isActive()))
                .andExpect(jsonPath("$.movie.title").value(availableMovie.getTitle()))
                .andExpect(jsonPath("$.movie.cost").value(availableMovie.getCost()))
                .andExpect(jsonPath("$.startDate").value(rentRequest.getStartDate().toString()));

        Mockito.when(rentService.addRent(Mockito.any(Rent.class)))
                .thenThrow( new UserNotActiveException(UserMsg.USER_NOT_ACTIVE))
                .thenThrow( new MovieInUseException(MovieMsg.MOVIE_IS_RENTED))
                .thenThrow( new StartDateException(RentMsg.WRONG_START_DATE))
                .thenThrow( new EndDateException(RentMsg.WRONG_END_DATE));

        mockMvc.perform(post("/api/v1/rents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(rentRequest)))
                .andExpect(status().isLocked())
                .andExpect(content().string(UserMsg.USER_NOT_ACTIVE));

        mockMvc.perform(post("/api/v1/rents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(rentRequest)))
                .andExpect(status().isLocked())
                        .andExpect(content().string(MovieMsg.MOVIE_IS_RENTED));

        mockMvc.perform(post("/api/v1/rents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(rentRequest)))
                .andExpect(status().isBadRequest())
                        .andExpect(content().string(RentMsg.WRONG_START_DATE));

        mockMvc.perform(post("/api/v1/rents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(rentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(RentMsg.WRONG_END_DATE));

    }
    @Test
    public void getCurrentRents() throws Exception {
        Rent rent1 = new Rent(UUID.randomUUID(), new Client("MaciekM", true, "Maciek", "Maciek", "1234"), new Movie("movie1", 10), LocalDate.now(), null);
        Rent rent2 = new Rent(UUID.randomUUID(), new Client("MaciekM", true, "Maciek", "Maciek", "1234"), new Movie("movie2", 15), LocalDate.now(), null);
        List<Rent> currentRents = Arrays.asList(rent1, rent2);

        Mockito.when(rentService.getCurrentRents()).thenReturn(currentRents)
                .thenThrow(RentsNotFoundException.class);

        mockMvc.perform(get("/api/v1/rents/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(rent1.getId().toString()))
                .andExpect(jsonPath("$[0].client.username").value(rent1.getUser().getUsername()))
                .andExpect(jsonPath("$[0].client.active").value(rent1.getUser().isActive()))
                .andExpect(jsonPath("$[0].movie.title").value(rent1.getMovie().getTitle()))
                .andExpect(jsonPath("$[0].movie.cost").value(rent1.getMovie().getCost()))
                .andExpect(jsonPath("$[0].startDate").value(rent1.getStartDate().toString()))
                .andExpect(jsonPath("$[1].id").value(rent2.getId().toString()))
                .andExpect(jsonPath("$[1].client.username").value(rent2.getUser().getUsername()))
                .andExpect(jsonPath("$[1].client.active").value(rent2.getUser().isActive()))
                .andExpect(jsonPath("$[1].movie.title").value(rent2.getMovie().getTitle()))
                .andExpect(jsonPath("$[1].movie.cost").value(rent2.getMovie().getCost()))
                .andExpect(jsonPath("$[1].startDate").value(rent2.getStartDate().toString()));

        mockMvc.perform(get("/api/v1/rents/current"))
                .andExpect(status().isNoContent());
    }
    @Test
    public void getPastRents() throws Exception {
        Rent rent1 = new Rent(UUID.randomUUID(), new Client("MaciekM", true, "Maciek", "Maciek", "1234"), new Movie("movie1", 10), LocalDate.now().minusDays(10), LocalDate.now().minusDays(5));
        Rent rent2 = new Rent(UUID.randomUUID(), new Client("MaciekM", true, "Maciek", "Maciek", "1234"), new Movie("movie2", 15), LocalDate.now().minusDays(8), LocalDate.now().minusDays(2));
        List<Rent> pastRents = Arrays.asList(rent1, rent2);

        Mockito.when(rentService.getPastRents()).thenReturn(pastRents)
                .thenThrow(RentsNotFoundException.class);

        mockMvc.perform(get("/api/v1/rents/past"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(rent1.getId().toString()))
                .andExpect(jsonPath("$[0].client.username").value(rent1.getUser().getUsername()))
                .andExpect(jsonPath("$[0].client.active").value(rent1.getUser().isActive()))
                .andExpect(jsonPath("$[0].movie.title").value(rent1.getMovie().getTitle()))
                .andExpect(jsonPath("$[0].movie.cost").value(rent1.getMovie().getCost()))
                .andExpect(jsonPath("$[0].startDate").value(rent1.getStartDate().toString()))
                .andExpect(jsonPath("$[0].endDate").value(rent1.getEndDate().toString()))
                .andExpect(jsonPath("$[1].id").value(rent2.getId().toString()))
                .andExpect(jsonPath("$[1].client.username").value(rent2.getUser().getUsername()))
                .andExpect(jsonPath("$[1].client.active").value(rent2.getUser().isActive()))
                .andExpect(jsonPath("$[1].movie.title").value(rent2.getMovie().getTitle()))
                .andExpect(jsonPath("$[1].movie.cost").value(rent2.getMovie().getCost()))
                .andExpect(jsonPath("$[1].startDate").value(rent2.getStartDate().toString()))
                .andExpect(jsonPath("$[1].endDate").value(rent2.getEndDate().toString()));

        mockMvc.perform(get("/api/v1/rents/past"))
                .andExpect(status().isNoContent());
    }
    @Test
    public void deleteRent() throws Exception {
        UUID rentId = UUID.randomUUID();

        Mockito.when(rentService.deleteRent(rentId)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/rents/{id}", rentId))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        Mockito.when(rentService.deleteRent(rentId))
                .thenThrow(new RentalStillOngoingException(RentMsg.RENT_NOT_ENDED))
                .thenThrow(new ThereIsNoSuchRentToDelete(RentMsg.RENT_NOT_FOUND));
        mockMvc.perform(delete("/api/v1/rents/{id}", rentId))
                .andExpect(status().isLocked());

        mockMvc.perform(delete("/api/v1/rents/{id}", rentId))
                .andExpect(status().isNotFound());

    }

    @Test
    public void endRent_ValidEndDate() throws Exception {
        UUID rentId = UUID.randomUUID();
        LocalDate endDate = LocalDate.now().plusDays(5);
        Map<String, String> endDateMap = Collections.singletonMap("endDate", endDate.toString());

        Rent existingRent = new Rent(rentId, new Client("MaciekM", true, "Maciek", "Maciek", "1234"), new Movie("movie", 10), LocalDate.now(), null);
        Rent updatedRent = new Rent(rentId, existingRent.getUser(), existingRent.getMovie(), existingRent.getStartDate(), endDate);

        Mockito.when(rentService.setEndTime(rentId, endDate)).thenReturn(updatedRent)
                .thenThrow(ThereIsNoSuchRentToUpdateException.class);

        mockMvc.perform(patch("/api/v1/rents/{id}", rentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(endDateMap)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updatedRent.getId().toString()))
                .andExpect(jsonPath("$.client.username").value(updatedRent.getUser().getUsername()))
                .andExpect(jsonPath("$.client.active").value(updatedRent.getUser().isActive()))
                .andExpect(jsonPath("$.movie.title").value(updatedRent.getMovie().getTitle()))
                .andExpect(jsonPath("$.movie.cost").value(updatedRent.getMovie().getCost()))
                .andExpect(jsonPath("$.startDate").value(updatedRent.getStartDate().toString()))
                .andExpect(jsonPath("$.endDate").value(updatedRent.getEndDate().toString()));

        mockMvc.perform(patch("/api/v1/rents/{id}", rentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(endDateMap)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void invalidDate() throws Exception {
        UUID clientId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();

        RentRequest invalidRentRequest = new RentRequest(clientId, movieId, LocalDate.now().minusDays(1));

        mockMvc.perform(post("/api/v1/rents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(invalidRentRequest)))
                .andExpect(status().isBadRequest());
    }
    @Test
    public void badDateFormat() throws Exception {
        UUID rentId = UUID.randomUUID();
        Map<String, String> invalidEndDateMap = Collections.singletonMap("endDate", "invalid-date");

        mockMvc.perform(patch("/api/v1/rents/{id}",rentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(invalidEndDateMap)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Incorrect date format"));
    }

    @Test
    public void getRentById() throws Exception {
        UUID clientId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        UUID rentId = UUID.randomUUID();
        LocalDate date = LocalDate.now();

        Client activeUser = new Client("MaciekM", true, "Maciek", "Maciek", "1234");
        Movie availableMovie = new Movie("AvailableMovie", 20);

        RentRequest rentRequest = new RentRequest(clientId, movieId, date);
        Rent rent = new Rent(activeUser,availableMovie,rentRequest.getStartDate());
        Mockito.when(rentService.getRent(rentId)).thenReturn(rent)
                .thenThrow(RentNotFoundException.class);

        mockMvc.perform(get("/api/v1/rents/{id}", rentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.client.id").value(activeUser.getId()))
                .andExpect(jsonPath("$.movie.id").value(availableMovie.getId()))
                .andExpect(jsonPath("$.startDate").value(date.toString()));

        mockMvc.perform(get("/api/v1/rents/{id}", rentId))
                .andExpect(status().isNoContent());
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
