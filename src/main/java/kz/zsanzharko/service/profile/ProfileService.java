package kz.zsanzharko.service.profile;

import kz.zsanzharko.exception.InvalidProfileException;
import kz.zsanzharko.model.Profile;

public interface ProfileService {
    
    Profile auth(Long chatId) throws InvalidProfileException;

    void setInitials(Long chatId, String fullName) throws InvalidProfileException;

    boolean validProfile(Long chatId) throws InvalidProfileException;

    void enableMarkChecker(Profile profile);

    void disableMarkChecker(Profile profile);
}
