package kz.zsanzharko.service.profile;

import kz.zsanzharko.enums.ProfileState;
import kz.zsanzharko.model.Profile;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class BasicProfileServiceImpl implements ProfileService {
    private final List<Profile> authProfileList;

    public BasicProfileServiceImpl() {
        this.authProfileList = new LinkedList<>();
    }

    @Override
    public Profile auth(Long chatId) {
        for (Profile profile : authProfileList) {
            if (profile.getChatId().equals(chatId)) {
                return profile;
            }
        }
        var profile = new Profile();
        profile.setChatId(chatId);
        profile.setState(ProfileState.REGISTER);
        authProfileList.add(profile);
        return profile;
    }

    @Override
    public void setInitials(Long chatId, String fullName) {
        for (Profile profile : authProfileList) {
            if (profile.getChatId().equals(chatId)) {
                profile.setFullName(fullName);
                profile.setState(ProfileState.NONE);
                return;
            }
        }
        throw new RuntimeException(chatId + " can't find in memory");
    }

    @Override
    public boolean validProfile(Long chatId) {
        for (Profile profile : authProfileList) {
            if (Objects.equals(profile.getChatId(), chatId)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void enableMarkChecker(Profile profile) {

    }

    @Override
    public void disableMarkChecker(Profile profile) {

    }
}
