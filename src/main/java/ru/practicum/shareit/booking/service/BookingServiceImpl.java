package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingDtoOut add(Long userId, BookingDto bookingDto) {
        User user = UserMapper.toUser(userService.getById(userId));
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена."));
        bookingValidation(bookingDto, user, item);
        Booking booking = BookingMapper.toBooking(user, item, bookingDto);
        return BookingMapper.toBookingOut(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDtoOut update(Long userId, Long bookingId, Boolean approved) {
        Booking booking = validateBookingDetailsUpdate(userId, bookingId);
        BookingStatus newStatus = approved ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        booking.setStatus(newStatus);
        return BookingMapper.toBookingOut(booking);
    }

    @Override
    public BookingDtoOut getBookingByUserId(Long userId, Long bookingId) {
        Booking booking = validateBookingByUserId(userId, bookingId);
        return BookingMapper.toBookingOut(booking);
    }

    @Override
    public List<BookingDtoOut> getAll(Long bookerId, String state, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        userService.getById(bookerId);
        switch (validState(state)) {
            case ALL:
                return bookingRepository.getAllBookingsByBookerId(bookerId, pageable).stream()
                        .map(BookingMapper::toBookingOut)
                        .collect(Collectors.toList());
            case CURRENT:
                return bookingRepository.getAllCurrentBookingsByBookerId(bookerId, LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toBookingOut)
                        .collect(Collectors.toList());

            case PAST:
                return bookingRepository.getAllPastBookingsByBookerId(bookerId, LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toBookingOut)
                        .collect(Collectors.toList());

            case FUTURE:
                return bookingRepository.getAllFutureBookingsByBookerId(bookerId, LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toBookingOut)
                        .collect(Collectors.toList());

            case WAITING:
                return bookingRepository.getAllWaitingBookingsByBookerId(bookerId, LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toBookingOut)
                        .collect(Collectors.toList());

            case REJECTED:
                return bookingRepository.getAllRejectedBookingsByBookerId(bookerId, LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toBookingOut)
                        .collect(Collectors.toList());
            default:
                throw new IllegalArgumentException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    @Override
    public List<BookingDtoOut> getAllOwner(Long ownerId, String state, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        userService.getById(ownerId);
        switch (validState(state)) {
            case ALL:
                return bookingRepository.getAllBookingsByOwnerId(ownerId, pageable).stream()
                        .map(BookingMapper::toBookingOut)
                        .collect(Collectors.toList());
            case CURRENT:
                return bookingRepository.getAllCurrentBookingsByOwnerId(ownerId, LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toBookingOut)
                        .collect(Collectors.toList());

            case PAST:
                return bookingRepository.getAllPastBookingsByOwnerId(ownerId, LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toBookingOut)
                        .collect(Collectors.toList());

            case FUTURE:
                return bookingRepository.getAllFutureBookingsByOwnerId(ownerId, LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toBookingOut)
                        .collect(Collectors.toList());

            case WAITING:
                return bookingRepository.getAllWaitingBookingsByOwnerId(ownerId, LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toBookingOut)
                        .collect(Collectors.toList());

            case REJECTED:
                return bookingRepository.getAllRejectedBookingsByOwnerId(ownerId, pageable).stream()
                        .map(BookingMapper::toBookingOut)
                        .collect(Collectors.toList());
            default:
                throw new IllegalArgumentException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    private void bookingValidation(BookingDto bookingDto, User user, Item item) {
        if (!item.getAvailable()) {
            throw new ValidationException("Вещь не доступна для бронирования.");
        }
        if (user.getId().equals(item.getOwner().getId())) {
            throw new NotFoundException("Вещь не найдена.");
        }
        if (bookingDto.getStart().isAfter(bookingDto.getEnd()) || bookingDto.getStart().isEqual(bookingDto.getEnd())) {
            throw new ValidationException("Дата окончания не может быть раньше или равна дате начала");
        }
    }

    private BookingState validState(String bookingState) {
        BookingState state = BookingState.from(bookingState);
        if (state == null) {
            throw new IllegalArgumentException("Unknown state: " + bookingState);
        }
        return state;
    }

    private Booking validateBookingDetailsUpdate(Long userId, Long bookingId) {
        Optional<Booking> bookingById = bookingRepository.findById(bookingId);
        if (bookingById.isEmpty()) {
            throw new NotFoundException("Бронь не найдена.");
        }
        Booking booking = bookingById.get();
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Пользователь не является владельцем");
        }
        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new ValidationException("Бронь не cо статусом WAITING");
        }
        return booking;
    }

    private Booking validateBookingByUserId(Long userId, Long bookingId) {
        Optional<Booking> bookingById = bookingRepository.findById(bookingId);
        if (bookingById.isEmpty()) {
            throw new NotFoundException("Бронь не найдена.");
        }
        Booking booking = bookingById.get();
        if (!booking.getBooker().getId().equals(userId)
                && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Пользователь не владелeц и не автор бронирования ");
        }
        return booking;
    }
}