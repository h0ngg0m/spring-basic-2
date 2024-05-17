package hello.springbasic2.basic;

import hello.springbasic2.validation.item.Item;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ValidationTestController {

    @GetMapping("/validation-test")
    public String validationTest(@Valid Item item) {
        // http://localhost:8080/validation-test?id=10&itemName=hong&price=1010&quantity=99 -> 200
        // http://localhost:8080/validation-test?id=10&itemName=hong&price=1010&quantity=99999 -> 400
        System.out.println("item = " + item);
        return "ok";
    }

}
