package kz.zsanzharko.model;

import kz.zsanzharko.enums.Lang;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import kz.zsanzharko.enums.ProfileRole;
import kz.zsanzharko.enums.ProfileState;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Profile {
    private ProfileState state;
    private Long chatId;
    private String fullName;
    private ProfileRole role;
    private Lang lang;

    public boolean validate() {
        return fullName != null && !fullName.isBlank() &&
                state != ProfileState.REGISTER;
    }
}
