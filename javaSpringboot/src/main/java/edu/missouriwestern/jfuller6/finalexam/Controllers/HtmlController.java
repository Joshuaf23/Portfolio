package edu.missouriwestern.jfuller6.finalexam.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import edu.missouriwestern.jfuller6.finalexam.Models.StockRepository;
import edu.missouriwestern.jfuller6.finalexam.Models.StocksModel;

@Controller
public class HtmlController {
    private final StockRepository repo;

    @Autowired
    public HtmlController(StockRepository repo){
        this.repo = repo;
    }

    @GetMapping(path="/")
    public String root(Model model){
        model.addAttribute("stocksCount", repo.count());
        return "index";
    }

    @GetMapping(path="/allStocks")
    public String allStocks(Model model){
        model.addAttribute("stocks", repo.findAll());
        return "allStocks";
    }
}
