package gr.hua.dit.petcare.web.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VetHomeController {

    @GetMapping("/ui/vet/home")
    public String vetHome() {
        return "vet/home"; // vet/home.html
    }
}
