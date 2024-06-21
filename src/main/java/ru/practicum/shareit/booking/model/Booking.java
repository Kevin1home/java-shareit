package ru.practicum.shareit.booking.model;

import lombok.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "bookings", schema = "public")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item_id")
    @ToString.Exclude
    @NotNull
    private Item item;

    @Column(name = "start_date")
    @NotNull
    private LocalDateTime start;

    @Column(name = "end_date")
    @NotNull
    private LocalDateTime end;

    @ManyToOne
    @JoinColumn(name = "booker_id")
    @ToString.Exclude
    @NotNull
    private User booker;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BookingStatus status;

    public Booking(Item item, LocalDateTime start, LocalDateTime end, User booker, BookingStatus status) {
        this.item = item;
        this.start = start;
        this.end = end;
        this.booker = booker;
        this.status = status;
    }
}