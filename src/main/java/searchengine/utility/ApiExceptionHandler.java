package searchengine.utility;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import searchengine.dto.StatusRequest;

@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {


//    @Override
//    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
//        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
//        StatusRequest statusRequest = new StatusRequest();
//        statusRequest.setResult(false);
//        statusRequest.setError("Указанная страница не найдена");
//        modelAndView.setStatus(status);
//        modelAndView.addObject(statusRequest);
//
//        writeToLog(ex);
//        return new ResponseEntity<>(modelAndView.getModelMap(), status);
//
//    }


    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        StatusRequest statusRequest = new StatusRequest();
        statusRequest.setResult(false);
        statusRequest.setError("Указанная страница не найдена: HttpRequestMethodNotSupportedException" + status);
        modelAndView.setStatus(status);
        modelAndView.addObject(statusRequest);
        writeToLog(ex);
        return new ResponseEntity<>(modelAndView.getModelMap(), status);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        StatusRequest statusRequest = new StatusRequest();
        statusRequest.setResult(false);
        statusRequest.setError("Указанная страница не найдена: MissingServletRequestParameterException" + status);
        modelAndView.setStatus(status);
        modelAndView.addObject(statusRequest);

        return new ResponseEntity<>(modelAndView.getModelMap(), status);
    }


    @ExceptionHandler(Exception.class)
    protected ResponseEntity<StatusRequest> AnyExceptionHandler(Exception ex) {
        StatusRequest statusRequest = new StatusRequest();
        statusRequest.setResult(false);
        statusRequest.setError("Указанная страница не найдена" + ex.toString());
        writeToLog(ex);
        return new ResponseEntity<>(statusRequest, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    private void writeToLog(Exception exception) {

        if (exception instanceof MissingServletRequestParameterException
                || exception instanceof HttpRequestMethodNotSupportedException
                || exception instanceof NoHandlerFoundException) {
            log.error("Request error : " + exception.getMessage(), exception);
        } else {
            log.error("Exception : " + exception.getMessage(), exception);
        }

    }



}
