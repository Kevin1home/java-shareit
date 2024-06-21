package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

import static ru.practicum.shareit.item.ItemController.X_SHARER_USER_ID;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDtoOut add(@RequestHeader(X_SHARER_USER_ID) Long userId,
                             @Valid @RequestBody BookingDto bookingDto) {
        log.info("Запрос на создание нового бронирования вещи от пользователя c id: {} ", userId);
        return bookingService.add(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDtoOut update(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                @PathVariable("bookingId") Long bookingId,
                                @RequestParam(name = "approved") Boolean approved) {
        log.info("Запрос на обновление статуса бронирования вещи от владельца с id: {}", userId);
        return bookingService.update(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDtoOut getBookingByUserId(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                            @PathVariable("bookingId") Long bookingId) {
        log.info("GET запрос на получение данных о  бронировании от пользователя с id: {}", userId);
        return bookingService.getBookingByUserId(userId, bookingId);
    }

    @GetMapping
    public List<BookingDtoOut> getAll(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                      @RequestParam(value = "state", defaultValue = "ALL") String bookingState,
                                      @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
                                      @RequestParam(value = "size", defaultValue = "10") @Min(1) Integer size) {
        log.info("GET запрос на получение списка всех бронирований текущего пользователя с id: {} и статусом {}", userId, bookingState);
        return bookingService.getAll(userId, bookingState, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDtoOut> getAllOwner(@RequestHeader(X_SHARER_USER_ID) Long ownerId,
                                           @RequestParam(value = "state", defaultValue = "ALL") String bookingState,
                                           @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
                                           @RequestParam(value = "size", defaultValue = "10") @Min(1) Integer size) {
        log.info("GET запрос на получение списка всех бронирований текущего владельца с id: {} и статусом {}", ownerId, bookingState);
        return bookingService.getAllOwner(ownerId, bookingState, from, size);
    }
}