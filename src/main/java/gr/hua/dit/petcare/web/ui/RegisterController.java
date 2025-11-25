package gr.hua.dit.petcare.web.ui;

import gr.hua.dit.petcare.service.UserService;
import gr.hua.dit.petcare.service.model.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class RegisterController {

    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("register", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("register") @Valid RegisterRequest req) {
        userService.register(req);
        return "redirect:/login?registered";
    }
}
