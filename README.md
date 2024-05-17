## 스프링 MVC 구조
<img width="820" alt="Screenshot 2024-05-16 at 3 04 13 PM" src="https://github.com/h0ngg0m/spring-basic-2/assets/125632083/005c8a82-d742-4278-bc7c-307a696c2962">
<img width="792" alt="Screenshot 2024-05-16 at 3 04 25 PM" src="https://github.com/h0ngg0m/spring-basic-2/assets/125632083/463ffa8d-2f29-409a-86e5-921d7b29397e">
<img width="801" alt="Screenshot 2024-05-16 at 3 04 35 PM" src="https://github.com/h0ngg0m/spring-basic-2/assets/125632083/984a76d2-5b2b-4cd1-bd1b-fe00107f2af4">

## Bean Validation
```java
package hello.springbasic2.validation.item;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class Item {

    private Long id;

    @NotBlank
    private String itemName;

    @NotNull
    @Range(min = 1000, max = 1000000)
    private Integer price;

    @NotNull
    @Max(9999)
    private Integer quantity;

    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}

...

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
```
`검증 어노테이션
- @NotBlank: 빈 값 또는 공백만 있는 경우를 허용하지 않는다.
- @NotNull: null을 허용하지 않는다.
- @Range: 주어진 범위 안의 값이어야 한다.
- @Max: 주어진 값보다 작거나 같아야 한다.

## 필터, 인터셉터
- 필터: 서블릿 스펙에서 제공하는 기술로, 서블릿이 동작하기 전후에 요청과 응답에 대한 처리를 할 수 있다.
- 인터셉터: 스프링에서 제공하는 기술로, 컨트롤러가 호출되기 전후에 요청과 응답에 대한 처리를 할 수 있다.

### 필터
- 흐름: HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 컨트롤러
  - 필터를 적용하면 필터가 호출 된 다음에 서블릿이 호출된다. 필터는 특정 URL 패턴에 적용할 수 있다. `'/*'`이라고 하면 모든 요청에 필터가 적용된다. 
  스프링을 사용하는 경우 여기서 말하는 서블릿은 스프링의 디스패처 서블릿이라고 생각하면 된다.
- 제한
  - HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 컨트롤러 // 로그인 사용자
  - HTTP 요청 -> WAS -> 필터(적절하지 않은 요청이라 판단, 서블릿 호출 x) // 비 로그인 사용자
- 체인: HTTP 요청 -> WAS -> 필터1 -> 필터2 -> 필터3 -> 서블릿 -> 컨트롤러
  - 필터는 체인으로 구성되어 중간에 자유롭게 필터를 추가할 수 있다.

### 필터 구현
```java
package hello.springbasic2.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.UUID;

@Slf4j
public class LogFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("LogFilter.init");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        log.info("LogFilter.doFilter");

        HttpServletRequest req = (HttpServletRequest) servletRequest;
        String requestURI = req.getRequestURI();
        String uuid = UUID.randomUUID().toString();

        try {
            log.info("REQUEST [{}][{}]", requestURI, uuid);
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Exception e) {
            throw e;
        } finally {
            log.info("RESPONSE [{}][{}]", requestURI, uuid);
        }

    }

    @Override
    public void destroy() {
        log.info("LogFilter.destroy");
    }
}


// 등록
// ...

@Configuration
public class WebConfig {

    @Bean
    public FilterRegistrationBean<Filter> logFilter() {
        FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new LogFilter());
        filterRegistrationBean.setOrder(1);
        filterRegistrationBean.addUrlPatterns("/*");

        return filterRegistrationBean;
    }
}
```
- doFilter(request, response, chain)
  - HTTP 요청이 오면 doFilter()가 호출된다.
- chain.doFilter(request, response)
  - 다음 필터가 있으면 다음 필터를 호출하고, 다음 필터가 없으면 서블릿을 호출한다.

### 인터셉터
스프링 인터셉터도 서블릿 필터와 같이 웹과 관련된 공통 관심 사항을 효과적으로 해결할 수 있는 기술이다.
서블릿 필터가 서블릿이 제공하는 기술이라면, 스프링 인터셉터는 스프링 MVC가 제공하는 기술이다. 둘다 웹과 관련된 공통 관심 사항을 처리하지만, 적용되는
순서와 범위, 그리고 사용방법이 다르다.

- 흐름: HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 스프링 인터셉터 -> 컨트롤러
  - 스프링 인터셉터는 디스패처 서블릿과 컨트롤러 사이에서 컨트롤러 호출 직전에 호출된다.
- 제한
  - HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 스프링 인터셉터 -> 컨트롤러 // 로그인 사용자
  - HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 스프링 인터셉터(적절하지 않은 요청이라 판단, 컨트롤러 호출 x) // 비 로그인 사용자
- 체인: HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 스프링 인터셉터1 -> 스프링 인터셉터2 -> 컨트롤러
  - 필터와 마찬가지로 체인으로 구성되어 중간에 자유롭게 인터셉터를 추가할 수 있다.

```java
public interface HandlerInterceptor {

    default boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {}

    default void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {}

    default void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {}
}
``` 
- preHandle: 컨트롤러 호출 전에 호출된다. (더 정확히는 핸들러 어댑터 호출 전에 호출된다.)
  - 리턴이 `true`이면 다음 인터셉터나 컨트롤러를 호출하고, `false`이면 더는 진행하지 않는다.
- postHandle: 컨트롤러 호출 후에 호출된다. (더 정확히는 핸들러 어댑터 호출 후에 호출된다.)
  - 컨트롤러에서 예외가 발생하면 호출되지 않는다. afterCompletion은 예외가 발생해도 호출된다.
- afterCompletion: 뷰가 렌더링 된 이후에 호출된다.

### 인터셉터 구현
```java
package hello.springbasic2.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Slf4j
public class LogInterceptor implements HandlerInterceptor {

    private static final String LOG_ID = "logId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String uuid = UUID.randomUUID().toString();

        request.setAttribute(LOG_ID, uuid);

        //@RequestMapping: HandlerMethod
        //정적 리소스: ResourceHttpRequestHandler
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
        }

        log.info("REQUEST [{}][{}][{}]", uuid, requestURI, handler);
        return true; //false: 더이상 진행하지 않음
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("postHandle [{}]", modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String requestURI = request.getRequestURI();
        String logId = (String)request.getAttribute(LOG_ID);
        log.info("RESPONSE [{}][{}][{}]", logId, requestURI, handler);
        if (ex != null) {
            log.error("afterCompletion error!!", ex);
        }
    }
}

// 등록
// ...

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LogInterceptor())
                .order(1)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/*.ico", "/error");
    }
   // ...
}

```

## Exception 
- 자바 직접 실행: 자바의 메인 메서드를 직접 실행하는 경우 main 이라는 이름의 쓰레드가 실행된다. 실행 도중에 예외를 잡지 못하고 처음 실행한 main 메서드를 넘어서
예외가 던져지면, 예외 정보를 남기고 해당 쓰레드는 종료된다.
- 웹 애플리케이션: 웹 애플리케이션은 사용자 요청별로 별도의 쓰레드가 할당되고, 서블릿 컨테이너 안에서 실행된다. 애플리케이션에서 예외가 발생했는데, 어디선가 try ~ catch로 예외를
잡아서 처리하면 아무런 문제가 없다. 그런데 만약에 애플리케이션에서 예외를 잡지 못하고, 서블릿 밖으로 까지 예외가 전달되면 어떻게 동작할까?
  - WAS(여기까지 전파) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러(예외발생) 

### Exception - 오류 페이지 요청 흐름
```java
package hello.springbasic2.exception;

import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class WebServerCustomizer implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {
    @Override
    public void customize(ConfigurableWebServerFactory factory) {

        ErrorPage errorPage404 = new ErrorPage(HttpStatus.NOT_FOUND, "/error-page/404");
        ErrorPage errorPage500 = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/error-page/500");
        ErrorPage errorPageEx = new ErrorPage(RuntimeException.class, "/error-page/500");

        factory.addErrorPages(errorPage404, errorPage500, errorPageEx);
    }
}

// 컨트롤러
// ...

@Slf4j
@Controller
public class ErrorPageController {

    private static final String ERROR_EXCEPTION = "jakarta.servlet.error.exception";
    private static final String ERROR_EXCEPTION_TYPE = "jakarta.servlet.error.exception_type";
    private static final String ERROR_MESSAGE = "jakarta.servlet.error.message";
    private static final String ERROR_REQUEST_URI = "jakarta.servlet.error.request_uri";
    private static final String ERROR_SERVLET_NAME = "jakarta.servlet.error.servlet_name";
    private static final String ERROR_STATUS_CODE = "jakarta.servlet.error.status_code";

    @RequestMapping("/error-page/404")
    public String error404(HttpServletRequest request) {
        printErrorInfo(request);
        return "error-page/404";
    }

    @RequestMapping("/error-page/500")
    public String error500(HttpServletRequest request) {
        printErrorInfo(request);
        return "error-page/500";
    }

    private void printErrorInfo(HttpServletRequest request) {
        log.info("ERROR_EXCEPTION: {}", request.getAttribute(ERROR_EXCEPTION));
        log.info("ERROR_EXCEPTION_TYPE: {}", request.getAttribute(ERROR_EXCEPTION_TYPE));
        log.info("ERROR_MESSAGE: {}", request.getAttribute(ERROR_MESSAGE));
        log.info("ERROR_REQUEST_URI: {}", request.getAttribute(ERROR_REQUEST_URI));
        log.info("ERROR_SERVLET_NAME: {}", request.getAttribute(ERROR_SERVLET_NAME));
        log.info("ERROR_STATUS_CODE: {}", request.getAttribute(ERROR_STATUS_CODE));
        log.info("dispatchType={}", request.getDispatcherType());
    }

}

```
- 오류 페이지 요청 흐름: WAS('/error-page/404') 다시 요청 -> 필터 -> 서블릿 -> 인터셉터 -> 컨트롤러('/error-page/404') -> View
- 예외 발생과 오류 페이지 요청 흐름
  1. WAS(여기까지 전파) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러 (예외발생)
  2. WAS('/error-page/500') 다시 요청 -> 필터 -> 서블릿 -> 인터셉터 -> 컨트롤러('/error-page/500') -> View

정리
1. 예외가 발생해서 WAS 까지 전파된다.
2. WAS는 오류 페이지 경로를 찾아서 내부에서 오류 페이지를 호출한다. 이때 오류 페이지 경로로 필터, 서블릿, 인터셉터, 컨트롤러가 `모두 다시 호출된다.`

### DispatcherType
오류가 발생하면 오류 페이지를 출력하기 위해 WAS 내부에서 다시 한번 호출이 발생한다. 이때 필터, 서블릿, 인터셉터도 모두 다시 호출된다. 
이는 비효울적이기 때문에 클라이언트로부터 발생한 정상 요청인지, 아니면 오류 페이지를 출력하기 위한 내부 요청인지 구분할 수 있게 해주는 기능이 `DispatcherType`이다.
```java
private void printErrorInfo(HttpServletRequest request) {
    // ...
    log.info("dispatchType={}", request.getDispatcherType());
}

// ...
package jakarta.servlet;

public enum DispatcherType {
    FORWARD,
    INCLUDE,
    REQUEST,
    ASYNC,
    ERROR;

    private DispatcherType() {
    }
}
```
### DispatcherType
- `REQUEST`: 클라이언트 요청
- `ERROR`: 오류 요청
- FORWARD: 포워드 요청
- INCLUDE: 인클루드 요청
- ASYNC: 비동기 요청


### DispatcherType - 필터
```java
// ...
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // ...

    @Bean
    public FilterRegistrationBean<Filter> logFilter() {
        // ...
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ERROR); // REQUEST, ERROR 두 가지 경우에 동작.

        return filterRegistrationBean;
    }
}
```
`filterRegistrationBean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ERROR);` 이렇게 두 가지를 모두 넣으면 클라이언트
요청은 물론이고, 오류 페이지 요청에서도 필터가 호출된다. 아무것도 넣지 않으면 기본 값이 `DispatcherType.REQUEST`이다. 즉 클라이언트의 요청이 있는 경우에만
필터가 적용된다. 특별히 오류 페이지 경로도 필터를 적용할 것이 아니면, 기본 값을 그대로 사용하면 된다. 

물론 오류 페이지 요청 전용 필터를 적용하고 싶으면 `DispatcherType.ERROR`만 넣어주면 된다.

### DispatcherType - 인터셉터
```java
// ...

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LogInterceptor())
                .order(1)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/*.ico", "/error", "/error-page/**"); // 적용 제외할 URL 패턴
    }
```
인터셉터는 따로 DispatcherType 타입을 지정하는 방법은 없고,`excludePathPatterns("/error", "/error-page/**")`로 오류 페이지 경로는 제외하고 적용한다.


### 스프링부트 - 오류 페이지
- 스프링부트는 기본적으로 `/error`로 오류 페이지를 처리한다.
- 게발자는 오류페이지만 등록하면 된다.
  - BasicErrorController는 기본적인 로직이 모두 개발되어 있다. 개발자는 오류 페이지 화면만 BasicErrorController가 제공하는 룰과 우선순위에
  따라 등록하면 된다. 정적 HTML 이면 정적 리소스, 뷰 템플릿을 사용해서 동적으로 오류 화면을 만들고 싶으면 뷰 템플릿 경로에 등록하면 된다.

### 오류 페이지 우선순위
1. 뷰 템플릿
   - `resources/templates/error/500.html`
   - `resources/templates/error/5xx.html`
2. 정적 리소스
   - `resources/static/error/400.html`
   - `resources/static/error/4xx.html`
   - `resources/static/error/500.html`
3. 적용 대상이 없을 때 뷰 이름
   - `resources/templates/error.html`

해당 경로에 상태 코드에 맞게 뷰 파일을 넣어두면 된다. 404, 500 같이 구체적인 것이 우선순위가 높고 4xx, 5xx 같이 범용적인 것이 우선순위가 낮다.

### API 예외 처리 - 스프링 부트 기본 오류 처리
- 스프링 부트는 기본적으로 API 예외 처리를 지원한다.

`BasicErrorController` 코드
```java
@RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {} // HTML 오류 페이지 리턴

@RequestMapping
public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {} // JSON 에러 리턴
```
- errorHtml(): `produces = MediaType.TEXT_HTML_VALUE` 클라이언트의 요청의 Accept 헤더 값이 `text/html`이면 호출돼서 view를 렌더링해서 HTML을 반환한다.
- error(): 클라이언트의 요청의 Accept 헤더 값이 `text/html`이 아니면 호출돼서 JSON을 반환한다.

### API 예외 처리 - 스프링이 제공하는 ExceptionResolver1
스프링부트가 기본으로 제공하는 ExceptionResolver는 다음과 같다.
1. ExceptionHandlerExceptionResolver: @ExceptionHandler 처리
2. ResponseStatusExceptionResolver: @ResponseStatus 처리
3. DefaultHandlerExceptionResolver: 기본 예외 처리