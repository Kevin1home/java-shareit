package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;

import java.util.List;

public interface BookingService {
    BookingDtoOut add(Long userId, BookingDto bookingDto);

    BookingDtoOut update(Long userId, Long bookingId, Boolean approved);

    BookingDtoOut getBookingByUserId(Long userId, Long bookingId);

    List<BookingDtoOut> getAll(Long userId, String state, Integer from, Integer size);

    List<BookingDtoOut> getAllOwner(Long userId, String state, Integer from, Integer size);
}