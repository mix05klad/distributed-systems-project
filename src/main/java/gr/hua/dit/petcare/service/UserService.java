package gr.hua.dit.petcare.service;

import gr.hua.dit.petcare.core.model.User;
import gr.hua.dit.petcare.service.model.RegisterRequest;

public interface UserService {
    User register(RegisterRequest req);
}
