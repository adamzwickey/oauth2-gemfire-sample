package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.session.data.gemfire.config.annotation.web.http.EnableGemFireHttpSession;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * Created by azwickey on 7/22/16.
 */
@RestController("/")
//@EnableGemFireHttpSession
public class ExampleSessionController {

    Logger LOG = LoggerFactory.getLogger(ExampleSessionController.class);

    @RequestMapping("session")
    @ResponseBody
    public Map<String, String> session(HttpSession session, @RequestParam(required = false) String name, @RequestParam(required = false) String value){
        if(!StringUtils.isEmpty(name) && !StringUtils.isEmpty(value)) {
            LOG.info("adding " + name + ":" + value + " to http session");
            session.setAttribute(name, value);
        }

        Map<String, String> sessionAttributes = new HashMap<>();

        for (String attributeName : toIterable(session.getAttributeNames())) {
            sessionAttributes.put(attributeName, String.valueOf(session.getAttribute(attributeName)));
        }

        return sessionAttributes;
    }

    <T> Iterable<T> toIterable(final Enumeration<T> enumeration) {
        return new Iterable<T>() {
            public Iterator<T> iterator() {
                return (enumeration == null ? Collections.<T>emptyIterator()
                        : new Iterator<T>() {
                    public boolean hasNext() {
                        return enumeration.hasMoreElements();
                    }

                    public T next() {
                        return enumeration.nextElement();
                    }

                    public void remove() {
                        throw new UnsupportedOperationException("Auto-generated method stub");
                    }
                });
            }
        };
    }
}
