package gr.hua.dit.petcare.web.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ui")
public class HomeController {

    @GetMapping("/home")
    public String home() {
        return "home";
    }
}
