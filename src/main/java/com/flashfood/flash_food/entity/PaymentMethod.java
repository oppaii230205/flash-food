package com.flashfood.flash_food.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.flashfood.flash_food.entity.converter.PersistableEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Payment method enumeration
 * DB: 1, 2, 3... | Backend: CASH, MOMO... | Client: "cash", "momo"...
 */
@Getter
@RequiredArgsConstructor
public enum PaymentMethod implements PersistableEnum<PaymentMethod> {
    CASH(1, "cash"),               // Tiền mặt khi nhận
    MOMO(2, "momo"),               // MoMo wallet
    ZALOPAY(3, "zalopay"),         // ZaloPay
    BANKING(4, "banking"),         // Chuyển khoản ngân hàng
    CREDIT_CARD(5, "credit_card"); // Thẻ tín dụng
    
    private final Integer value;
    private final String displayName;
    
    @JsonCreator
    public static PaymentMethod fromDisplayName(String displayName) {
        return PersistableEnum.fromDisplayName(PaymentMethod.class, displayName);
    }
    
    public static PaymentMethod fromValue(Integer value) {
        return PersistableEnum.fromValue(PaymentMethod.class, value);
    }
}
