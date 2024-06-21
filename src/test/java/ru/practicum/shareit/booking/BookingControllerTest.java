package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.item.ItemController.X_SHARER_USER_ID;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    private User user;
    private Item item;
    private BookingDto bookingDto;
    private BookingDtoOut bookingDtoOut;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("username")
                .email("email@email.com")
                .build();

        item = Item.builder()
                .id(1L)
                .name("item name")
                .description("description")
                .owner(user)
                .build();

        bookingDto = BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1L))
                .end(LocalDateTime.now().plusDays(2L))
                .build();

        bookingDtoOut = BookingDtoOut.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1L))
                .end(LocalDateTime.now().plusDays(2L))
                .status(BookingStatus.WAITING)
                .booker(UserMapper.toUserDto(user))
                .item(ItemMapper.toItemDtoOut(item))
                .build();
    }

    @Test
    @SneakyThrows
    void createBookingWhenBookingIsValid() {
        when(bookingService.add(user.getId(), bookingDto)).thenReturn(bookingDtoOut);

        String result = mockMvc.perform(post("/bookings")
                        .contentType("application/json")
                        .header(X_SHARER_USER_ID, user.getId())
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(bookingDtoOut), result);
    }

    @Test
    @SneakyThrows
    void createBookingWhenBookingIsNotValid() {
        bookingDto.setItemId(null);
        bookingDto.setStart(null);
        bookingDto.setEnd(null);

        when(bookingService.add(user.getId(), bookingDto)).thenReturn(bookingDtoOut);

        mockMvc.perform(post("/bookings")
                        .contentType("application/json")
                        .header(X_SHARER_USER_ID, user.getId())
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).add(user.getId(), bookingDto);
    }

    @Test
    @SneakyThrows
    void findAllBookingShouldReturnBadRequest() {
        Integer from = -1;
        Integer size = 10;

        mockMvc.perform(get("/bookings")
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType("application/json")
                        .header(X_SHARER_USER_ID, user.getId()))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).getAll(user.getId(), "ALL", from, size);
    }

    @Test
    @SneakyThrows
    void getAllOwnerShouldReturnBadRequest() {
        Integer from = -1;
        Integer size = 10;

        mockMvc.perform(get("/bookings/owner")
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType("application/json")
                        .header(X_SHARER_USER_ID, user.getId()))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).getAllOwner(user.getId(), "ALL", from, size);
    }

    @Test
    @SneakyThrows
    void updateWhenBookingIsValid() {
        Boolean approved = true;
        Long bookingId = 1L;

        when(bookingService.update(user.getId(), bookingId, approved)).thenReturn(bookingDtoOut);

        String result = mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .contentType("application/json")
                        .header(X_SHARER_USER_ID, user.getId())
                        .param("approved", String.valueOf(approved)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(bookingDtoOut), result);
    }

    @Test
    @SneakyThrows
    void getByIdWhenBookingIsValid() {
        Long bookingId = 1L;

        when(bookingService.getBookingByUserId(user.getId(), bookingId)).thenReturn(bookingDtoOut);

        String result = mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(X_SHARER_USER_ID, user.getId())).andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(bookingDtoOut), result);
    }

    @Test
    @SneakyThrows
    void getAllShouldReturnStatusIsOk() {
        Integer from = 0;
        Integer size = 10;
        String state = "ALL";

        when(bookingService.getAll(user.getId(), BookingState.ALL.toString(), 0, 10))
                .thenReturn(List.of(bookingDtoOut));

        String result = mockMvc.perform(get("/bookings")
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .header(X_SHARER_USER_ID, user.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(List.of(bookingDtoOut)), result);
    }


    @Test
    @SneakyThrows
    void getAllByOwner() {
        Integer from = 0;
        Integer size = 10;
        String state = "ALL";

        when(bookingService.getAllOwner(user.getId(), BookingState.ALL.toString(), 0, 10))
                .thenReturn(List.of(bookingDtoOut));

        String result = mockMvc.perform(get("/bookings/owner")
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .header(X_SHARER_USER_ID, user.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(List.of(bookingDtoOut)), result);
    }

}