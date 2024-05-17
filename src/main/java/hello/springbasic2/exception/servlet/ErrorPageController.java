package hello.springbasic2.exception.servlet;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

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
