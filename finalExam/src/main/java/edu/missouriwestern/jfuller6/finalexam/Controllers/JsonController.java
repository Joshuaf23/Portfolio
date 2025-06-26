package edu.missouriwestern.jfuller6.finalexam.Controllers;

import edu.missouriwestern.jfuller6.finalexam.Models.StockRepository;
import edu.missouriwestern.jfuller6.finalexam.Models.StocksModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
public class JsonController {
    private final StockRepository repo;

    @Autowired
    public JsonController(StockRepository repo){
        this.repo = repo;
    }

    @RequestMapping(path="/today")
    public String today(){
        return LocalDate.now().toString();
    }

    @RequestMapping(path = "/stock/{number}")
    public String number(@PathVariable String number){
        String message = String.format("<h1>The number is %s.</h1>", number);
        return message;
    }

    @RequestMapping(path="/allStockJson")
    public List<StocksModel> getAllStocks(){
        return repo.findAll();
    }

    @RequestMapping(path="/oneJson/{number}")
    public StocksModel getOneStock(@PathVariable String number){
        List<StocksModel> list = repo.findAll();
        return list.get(Integer.parseInt(number));
    }
}
