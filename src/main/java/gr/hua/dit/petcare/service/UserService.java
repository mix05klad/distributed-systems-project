package gr.hua.dit.petcare.service;

import gr.hua.dit.petcare.core.model.User;
import gr.hua.dit.petcare.service.model.RegisterRequest;

import java.util.List;

public interface UserService {

    User register(RegisterRequest req);

    User getById(Long id);

    User getCurrentUser(Long userId);

    List<User> getAllVets();
}
