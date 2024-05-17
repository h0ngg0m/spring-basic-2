package hello.springbasic2.exception.api;

import hello.springbasic2.exhandler.ErrorResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class ApiExceptionV2Controller {

    @GetMapping("/api2/members/{id}")
    public ApiExceptionMemberDto getMemberV2(@PathVariable("id") String id) {

        if (id.equals("ex")) {
            throw new RuntimeException("잘못된 사용자");
        }

        if (id.equals("bad")) {
            throw new IllegalArgumentException("잘못된 입력 값");
        }

        return new ApiExceptionMemberDto(id, 20);
    }

    @Data
    @AllArgsConstructor
    static class ApiExceptionMemberDto {
        private String name;
        private int age;
    }
}
