package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.UserRepository;
import at.technikum.apps.mtcg.repository.UserRepository_db;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class SessionService {
    private final UserRepository userRepository;

    public SessionService() {
        this.userRepository = new UserRepository_db();
    }

}
