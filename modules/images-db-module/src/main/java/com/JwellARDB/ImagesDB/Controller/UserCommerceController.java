package com.JwellARDB.ImagesDB.Controller;

import com.JwellARDB.ImagesDB.Repository.*;
import com.JwellARDB.ImagesDB.entity.*;
import com.JwellARDB.ImagesDB.services.FirebaseService;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserCommerceController {

    private final FirebaseService firebaseService;
    private final JewelryRepository jewelryRepository;
    private final WatchlistRepository watchlistRepository;
    private final CartItemRepository cartItemRepository;
    private final SavedAddressRepository savedAddressRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    private String uidFromToken(String token) throws Exception {
        return firebaseService.verifyToken(token);
    }

    @GetMapping("/watchlist")
    public ResponseEntity<?> getWatchlist(
            @RequestHeader("Authorization") String token
    ) throws Exception {

        String uid = uidFromToken(token);

        List<WatchlistItem> entries = watchlistRepository.findByUserIdOrderByAddedAtDesc(uid);
        List<Long> ids = entries.stream()
                .map(WatchlistItem::getJewelleryId)
                .distinct()
                .toList();

        Map<Long, JwelryItem> itemMap = jewelryRepository.findAllById(ids)
                .stream()
                .collect(Collectors.toMap(JwelryItem::getId, item -> item));

        List<JewelryItemDTO> result = entries.stream()
                .map(e -> itemMap.get(e.getJewelleryId()))
                .filter(Objects::nonNull)
                .map(this::toJewelryDto)
                .toList();

        return ResponseEntity.ok(result);
    }

    @PostMapping("/watchlist/{jewelleryId}")
    public ResponseEntity<?> addToWatchlist(
            @RequestHeader("Authorization") String token,
            @PathVariable Long jewelleryId
    ) throws Exception {

        String uid = uidFromToken(token);

        if (!jewelryRepository.existsById(jewelleryId)) {
            return ResponseEntity.notFound().build();
        }

        Optional<WatchlistItem> existing = watchlistRepository.findByUserIdAndJewelleryId(uid, jewelleryId);
        if (existing.isPresent()) {
            return ResponseEntity.ok("Already in watchlist");
        }

        watchlistRepository.save(new WatchlistItem(
                null,
                uid,
                jewelleryId,
                LocalDateTime.now()
        ));

        return ResponseEntity.ok("Added to watchlist");
    }

    @DeleteMapping("/watchlist/{jewelleryId}")
    public ResponseEntity<?> removeFromWatchlist(
            @RequestHeader("Authorization") String token,
            @PathVariable Long jewelleryId
    ) throws Exception {

        String uid = uidFromToken(token);
        watchlistRepository.deleteByUserIdAndJewelleryId(uid, jewelleryId);

        return ResponseEntity.ok("Removed from watchlist");
    }

    @GetMapping("/cart")
    public ResponseEntity<?> getCart(
            @RequestHeader("Authorization") String token
    ) throws Exception {

        String uid = uidFromToken(token);

        List<CartItem> entries = cartItemRepository.findByUserIdOrderByAddedAtDesc(uid);
        List<Long> ids = entries.stream()
                .map(CartItem::getJewelleryId)
                .distinct()
                .toList();

        Map<Long, JwelryItem> itemMap = jewelryRepository.findAllById(ids)
                .stream()
                .collect(Collectors.toMap(JwelryItem::getId, item -> item));

        List<CartItemDTO> result = entries.stream()
                .map(e -> {
                    JwelryItem item = itemMap.get(e.getJewelleryId());
                    if (item == null) return null;
                    return new CartItemDTO(
                            item.getId(),
                            item.getName(),
                            item.getDescription(),
                            item.getImageUrl(),
                            item.getPrice(),
                            e.getQuantity() != null ? e.getQuantity() : 1
                    );
                })
                .filter(Objects::nonNull)
                .toList();

        return ResponseEntity.ok(result);
    }

    @PostMapping("/cart/{jewelleryId}")
    public ResponseEntity<?> addToCart(
            @RequestHeader("Authorization") String token,
            @PathVariable Long jewelleryId
    ) throws Exception {

        String uid = uidFromToken(token);

        if (!jewelryRepository.existsById(jewelleryId)) {
            return ResponseEntity.notFound().build();
        }

        Optional<CartItem> existing = cartItemRepository.findByUserIdAndJewelleryId(uid, jewelleryId);
        if (existing.isPresent()) {
            return ResponseEntity.ok("Already in cart");
        }

        LocalDateTime now = LocalDateTime.now();
        cartItemRepository.save(new CartItem(
                null,
                uid,
                jewelleryId,
                1,
                now,
                now
        ));

        return ResponseEntity.ok("Added to cart");
    }

    @DeleteMapping("/cart/{jewelleryId}")
    public ResponseEntity<?> removeFromCart(
            @RequestHeader("Authorization") String token,
            @PathVariable Long jewelleryId
    ) throws Exception {

        String uid = uidFromToken(token);
        cartItemRepository.deleteByUserIdAndJewelleryId(uid, jewelleryId);

        return ResponseEntity.ok("Removed from cart");
    }

    @GetMapping("/addresses")
    public ResponseEntity<?> getAddresses(
            @RequestHeader("Authorization") String token
    ) throws Exception {

        String uid = uidFromToken(token);

        List<AddressDTO> dto = savedAddressRepository.findByUserIdOrderByUpdatedAtDesc(uid)
                .stream()
                .map(this::toAddressDto)
                .toList();

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/addresses")
    @Transactional
    public ResponseEntity<?> addAddress(
            @RequestHeader("Authorization") String token,
            @RequestBody AddressUpsertRequest request
    ) throws Exception {

        String uid = uidFromToken(token);
        String validationError = validateAddress(request);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }

        boolean hasAny = savedAddressRepository.countByUserId(uid) > 0;
        boolean makeDefault = Boolean.TRUE.equals(request.getIsDefault()) || !hasAny;

        if (makeDefault) {
            clearDefaultAddress(uid);
        }

        LocalDateTime now = LocalDateTime.now();

        SavedAddress address = new SavedAddress(
                null,
                uid,
                request.getFullName().trim(),
                request.getPhone().trim(),
                request.getLine1().trim(),
                emptyToNull(request.getLine2()),
                request.getCity().trim(),
                request.getState().trim(),
                request.getPincode().trim(),
                makeDefault,
                now,
                now
        );

        SavedAddress saved = savedAddressRepository.save(address);
        return ResponseEntity.ok(toAddressDto(saved));
    }

    @PutMapping("/addresses/{id}")
    @Transactional
    public ResponseEntity<?> updateAddress(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody AddressUpsertRequest request
    ) throws Exception {

        String uid = uidFromToken(token);
        String validationError = validateAddress(request);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }

        SavedAddress address = savedAddressRepository.findByIdAndUserId(id, uid)
                .orElse(null);

        if (address == null) {
            return ResponseEntity.notFound().build();
        }

        boolean makeDefault = Boolean.TRUE.equals(request.getIsDefault()) || Boolean.TRUE.equals(address.getIsDefault());
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultAddress(uid);
            makeDefault = true;
        }

        address.setFullName(request.getFullName().trim());
        address.setPhone(request.getPhone().trim());
        address.setLine1(request.getLine1().trim());
        address.setLine2(emptyToNull(request.getLine2()));
        address.setCity(request.getCity().trim());
        address.setState(request.getState().trim());
        address.setPincode(request.getPincode().trim());
        address.setIsDefault(makeDefault);
        address.setUpdatedAt(LocalDateTime.now());

        SavedAddress updated = savedAddressRepository.save(address);
        return ResponseEntity.ok(toAddressDto(updated));
    }

    @DeleteMapping("/addresses/{id}")
    @Transactional
    public ResponseEntity<?> deleteAddress(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id
    ) throws Exception {

        String uid = uidFromToken(token);

        SavedAddress address = savedAddressRepository.findByIdAndUserId(id, uid)
                .orElse(null);

        if (address == null) {
            return ResponseEntity.notFound().build();
        }

        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());
        savedAddressRepository.delete(address);

        if (wasDefault) {
            List<SavedAddress> remaining = savedAddressRepository.findByUserIdOrderByUpdatedAtDesc(uid);
            if (!remaining.isEmpty()) {
                SavedAddress first = remaining.get(0);
                first.setIsDefault(true);
                first.setUpdatedAt(LocalDateTime.now());
                savedAddressRepository.save(first);
            }
        }

        return ResponseEntity.ok("Address deleted");
    }

    @PostMapping("/orders")
    @Transactional
    public ResponseEntity<?> placeOrder(
            @RequestHeader("Authorization") String token,
            @RequestBody CreateOrderRequest request
    ) throws Exception {

        String uid = uidFromToken(token);

        if (request.getAddressId() == null || request.getItemIds() == null || request.getItemIds().isEmpty()) {
            return ResponseEntity.badRequest().body("addressId and itemIds are required");
        }

        SavedAddress address = savedAddressRepository.findByIdAndUserId(request.getAddressId(), uid)
                .orElse(null);

        if (address == null) {
            return ResponseEntity.badRequest().body("Invalid address");
        }

        List<Long> itemIds = request.getItemIds()
                .stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (itemIds.isEmpty()) {
            return ResponseEntity.badRequest().body("No valid item ids");
        }

        List<JwelryItem> jewelleryItems = jewelryRepository.findAllById(itemIds);
        if (jewelleryItems.size() != itemIds.size()) {
            return ResponseEntity.badRequest().body("Some jewelry items were not found");
        }

        Map<Long, JwelryItem> jewelryMap = jewelleryItems.stream()
                .collect(Collectors.toMap(JwelryItem::getId, item -> item));

        double subtotal = 0.0;
        for (Long id : itemIds) {
            JwelryItem item = jewelryMap.get(id);
            subtotal += item.getPrice() != null ? item.getPrice() : 0.0;
        }

        double deliveryFee = itemIds.isEmpty() ? 0.0 : 49.0;
        double totalAmount = subtotal + deliveryFee;

        OrderEntity order = new OrderEntity(
                null,
                uid,
                address.getId(),
                address.getFullName(),
                address.getPhone(),
                address.getLine1(),
                address.getLine2(),
                address.getCity(),
                address.getState(),
                address.getPincode(),
                subtotal,
                deliveryFee,
                totalAmount,
                "PLACED",
                LocalDateTime.now()
        );

        OrderEntity savedOrder = orderRepository.save(order);

        for (Long id : itemIds) {
            JwelryItem item = jewelryMap.get(id);
            double unitPrice = item.getPrice() != null ? item.getPrice() : 0.0;

            orderItemRepository.save(new OrderItemEntity(
                    null,
                    savedOrder.getId(),
                    id,
                    1,
                    unitPrice,
                    unitPrice
            ));
        }

        cartItemRepository.deleteByUserIdAndJewelleryIdIn(uid, itemIds);

        return ResponseEntity.ok(new PlaceOrderResponse(
                savedOrder.getId(),
                savedOrder.getStatus(),
                savedOrder.getTotalAmount(),
                savedOrder.getPlacedAt()
        ));
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getOrders(
            @RequestHeader("Authorization") String token
    ) throws Exception {

        String uid = uidFromToken(token);

        List<OrderEntity> orders = orderRepository.findByUserIdOrderByPlacedAtDesc(uid);

        List<OrderHistoryDTO> result = new ArrayList<>();

        for (OrderEntity order : orders) {
            List<OrderItemEntity> orderItems = orderItemRepository.findByOrderIdOrderByIdAsc(order.getId());
            List<Long> jewelryIds = orderItems.stream().map(OrderItemEntity::getJewelleryId).toList();

            Map<Long, JwelryItem> itemMap = jewelryRepository.findAllById(jewelryIds)
                    .stream()
                    .collect(Collectors.toMap(JwelryItem::getId, item -> item));

            List<OrderHistoryItemDTO> itemDTOs = orderItems.stream().map(oi -> {
                JwelryItem item = itemMap.get(oi.getJewelleryId());
                return new OrderHistoryItemDTO(
                        oi.getJewelleryId(),
                        item != null ? item.getName() : "Unknown Item",
                        item != null ? item.getDescription() : "",
                        item != null ? item.getImageUrl() : "",
                        oi.getQuantity(),
                        oi.getUnitPrice(),
                        oi.getLineTotal()
                );
            }).toList();

            result.add(new OrderHistoryDTO(
                    order.getId(),
                    order.getSubtotal(),
                    order.getDeliveryFee(),
                    order.getTotalAmount(),
                    order.getStatus(),
                    order.getPlacedAt(),
                    order.getFullName(),
                    order.getPhone(),
                    order.getLine1(),
                    order.getLine2(),
                    order.getCity(),
                    order.getState(),
                    order.getPincode(),
                    itemDTOs
            ));
        }

        return ResponseEntity.ok(result);
    }

    private void clearDefaultAddress(String uid) {
        List<SavedAddress> all = savedAddressRepository.findByUserIdOrderByUpdatedAtDesc(uid);
        for (SavedAddress item : all) {
            if (Boolean.TRUE.equals(item.getIsDefault())) {
                item.setIsDefault(false);
                item.setUpdatedAt(LocalDateTime.now());
                savedAddressRepository.save(item);
            }
        }
    }

    private String validateAddress(AddressUpsertRequest request) {
        if (request.getFullName() == null || request.getFullName().isBlank()) return "fullName is required";
        if (request.getPhone() == null || request.getPhone().isBlank()) return "phone is required";
        if (request.getLine1() == null || request.getLine1().isBlank()) return "line1 is required";
        if (request.getCity() == null || request.getCity().isBlank()) return "city is required";
        if (request.getState() == null || request.getState().isBlank()) return "state is required";
        if (request.getPincode() == null || request.getPincode().isBlank()) return "pincode is required";
        return null;
    }

    private String emptyToNull(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    private JewelryItemDTO toJewelryDto(JwelryItem item) {
        return new JewelryItemDTO(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getImageUrl(),
                item.getPrice()
        );
    }

    @Getter
    @AllArgsConstructor
    static class JewelryItemDTO {
        private Long id;
        private String name;
        private String description;
        private String imageUrl;
        private Double price;
    }

    @Getter
    @AllArgsConstructor
    static class CartItemDTO {
        private Long id;
        private String name;
        private String description;
        private String imageUrl;
        private Double price;
        private Integer quantity;
    }

    @Getter
    @AllArgsConstructor
    static class AddressDTO {
        private Long id;
        private String fullName;
        private String phone;
        private String line1;
        private String line2;
        private String city;
        private String state;
        private String pincode;
        private Boolean isDefault;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    static class AddressUpsertRequest {
        private String fullName;
        private String phone;
        private String line1;
        private String line2;
        private String city;
        private String state;
        private String pincode;
        private Boolean isDefault;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    static class CreateOrderRequest {
        private Long addressId;
        private List<Long> itemIds;
    }

    @Getter
    @AllArgsConstructor
    static class PlaceOrderResponse {
        private Long orderId;
        private String status;
        private Double totalAmount;
        private LocalDateTime placedAt;
    }

    @Getter
    @AllArgsConstructor
    static class OrderHistoryItemDTO {
        private Long jewelleryId;
        private String name;
        private String description;
        private String imageUrl;
        private Integer quantity;
        private Double unitPrice;
        private Double lineTotal;
    }

    @Getter
    @AllArgsConstructor
    static class OrderHistoryDTO {
        private Long id;
        private Double subtotal;
        private Double deliveryFee;
        private Double totalAmount;
        private String status;
        private LocalDateTime placedAt;
        private String fullName;
        private String phone;
        private String line1;
        private String line2;
        private String city;
        private String state;
        private String pincode;
        private List<OrderHistoryItemDTO> items;
    }

    private AddressDTO toAddressDto(SavedAddress address) {
        return new AddressDTO(
                address.getId(),
                address.getFullName(),
                address.getPhone(),
                address.getLine1(),
                address.getLine2(),
                address.getCity(),
                address.getState(),
                address.getPincode(),
                address.getIsDefault()
        );
    }
}
