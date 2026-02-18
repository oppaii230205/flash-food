package com.flashfood.flash_food.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Payment method enumeration
 * DB: Integer (1, 2, 3...) | Backend: Enum (CASH, MOMO...) | Client: String ("cash", "momo"...)
 */
public enum PaymentMethod {
    CASH(1, "cash"),               // Tiền mặt khi nhận
    MOMO(2, "momo"),               // MoMo wallet
    ZALOPAY(3, "zalopay"),         // ZaloPay
    BANKING(4, "banking"),         // Chuyển khoản ngân hàng
    CREDIT_CARD(5, "credit_card"); // Thẻ tín dụng
    
    private final int code;
    private final String displayName;
    
    private static final Map<Integer, PaymentMethod> CODE_LOOKUP = 
            Arrays.stream(values())
                  .collect(Collectors.toMap(PaymentMethod::getCode, e -> e));
                  
    private static final Map<String, PaymentMethod> NAME_LOOKUP = 
            Arrays.stream(values())
                  .collect(Collectors.toMap(e -> e.displayName.toLowerCase(), e -> e));
    
    PaymentMethod(int code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    public int getCode() {
        return code;
    }
    
    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
    
    public static PaymentMethod fromCode(int code) {
        PaymentMethod method = CODE_LOOKUP.get(code);
        if (method == null) {
            throw new IllegalArgumentException("Invalid PaymentMethod code: " + code);
        }
        return method;
    }
    
    @JsonCreator
    public static PaymentMethod fromDisplayName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            throw new IllegalArgumentException("PaymentMethod displayName cannot be null or empty");
        }
        PaymentMethod method = NAME_LOOKUP.get(displayName.toLowerCase());
        if (method == null) {
            throw new IllegalArgumentException("Invalid PaymentMethod name: " + displayName);
        }
        return method;
    }
}
