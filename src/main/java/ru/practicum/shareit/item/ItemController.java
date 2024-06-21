package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoOut;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoOut;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@Validated
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    public static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ItemDtoOut add(@RequestHeader(X_SHARER_USER_ID) Long userId,
                          @Valid @RequestBody ItemDto itemDto) {
        log.info("POST Запрос на добавление пользователем с id = {} предмета {}", userId, itemDto.toString());
        return itemService.add(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDtoOut update(@RequestHeader(X_SHARER_USER_ID) Long userId,
                             @RequestBody ItemDto itemDto,
                             @PathVariable Long itemId) {
        log.info("PATCH Запрос на обновление предмета с id = {} пользователем с id = {} ", itemId, userId);
        return itemService.update(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDtoOut getItemById(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                  @PathVariable("itemId") Long itemId) {
        log.info("GET Запрос на получение предмета с id = {} пользователем с id = {} ", itemId, userId);
        return itemService.getItemById(userId, itemId);
    }

    @GetMapping
    public List<ItemDtoOut> getAll(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                   @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
                                   @RequestParam(value = "size", defaultValue = "10") @Min(1) Integer size
    ) {
        log.info("GET Запрос на получение предметов пользователя с id = {}", userId);
        return itemService.getAll(userId, from, size);
    }

    @GetMapping("/search")
    public List<ItemDtoOut> search(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                   @RequestParam(name = "text") String text,
                                   @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
                                   @RequestParam(value = "size", defaultValue = "10") @Min(1) Integer size) {
        log.info("GET Запрос на поиск предметов c текстом = {}", text);
        return itemService.search(userId, text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDtoOut createComment(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                       @Validated @RequestBody CommentDto commentDto,
                                       @PathVariable Long itemId) {
        log.info("POST Запрос на создание комментария id = {}", itemId);
        return itemService.createComment(userId, commentDto, itemId);
    }
}