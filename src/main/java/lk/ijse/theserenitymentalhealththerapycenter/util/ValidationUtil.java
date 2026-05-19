package lk.ijse.theserenitymentalhealththerapycenter.util;

import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    private static final Pattern SRI_LANKAN_PHONE_PATTERN = Pattern.compile(
            "^(?:0|\\+94|94)?[1-9][0-9]{8}$"
    );

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidSriLankanPhone(String phone) {
        if (phone == null) return false;
        return SRI_LANKAN_PHONE_PATTERN.matcher(phone.trim().replaceAll("\\s+", "")).matches();
    }

    public static boolean isRequiredFieldFilled(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
