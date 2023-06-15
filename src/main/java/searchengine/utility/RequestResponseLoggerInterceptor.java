package searchengine.utility;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Map;

@Slf4j
public class RequestResponseLoggerInterceptor implements HandlerInterceptor {

   private static long start = System.currentTimeMillis();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        Map<String, String[]> stringMap = request.getParameterMap();
        StringBuilder parameterMap = new StringBuilder();
        for (Map.Entry<String, String[]> s : stringMap.entrySet()) {
            parameterMap.append(s.getKey() + ": " + Arrays.toString(s.getValue())).append("\n");
        }
        log.info("Logging request:\n" + request + "\n" +
                "Method: " + request.getMethod() + "\n" +
                "RequestURL: " + request.getRequestURL() + "\n" +
                "ParameterMap: " + parameterMap.toString().trim() + "\n" +
                "CharacterEncoding: " + request.getCharacterEncoding());

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        long timeProcessing = System.currentTimeMillis() - start;
        log.info("Time processing "  + request + " - " + timeProcessing + " ms" + "\n");
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
       log.info("Logging response to "+ request +":\n" + response + "\n" +
               "Status: " + response.getStatus() + "\n" +
               "ContentType: " + response.getContentType() + "\n" +
              "CharacterEncoding: " + response.getCharacterEncoding() + "\n");
    }


}
