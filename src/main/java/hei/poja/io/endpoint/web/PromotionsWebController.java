package hei.poja.io.endpoint.web;

import org.springframework.ui.Model;
import hei.poja.io.service.GraduateExportService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@AllArgsConstructor
public class PromotionsWebController {
    private final GraduateExportService graduateExportService;

    @GetMapping("/promotions")
    public String promotions(Model model) {
        model.addAttribute("promotions", graduateExportService.promotions());
        return "promotions";
    }
}
