package org.santel.url;

import org.santel.url.model.*;
import org.springframework.web.bind.annotation.*;

import javax.inject.*;
import javax.servlet.http.*;
import java.io.*;
import java.net.*;

//TODO security: an XSS vulnerability would arise if the long url is ever returned to a browser or agent
//TODO           ... for which ESAPI should be used (https://owasp-esapi-java.googlecode.com/svn/trunk_doc/latest/org/owasp/esapi/Encoder.html)
@RestController
public class ShorteningController {

    @Inject
    private MappingModel mappingModel;

    @RequestMapping(value = "/shorten") //TODO constrain to method, e.g. method = RequestMethod.POST
    public String shortenUrl(@RequestParam(value="url") String longUrlAsString) throws MalformedURLException {
        URL longUrl = new URL(longUrlAsString);
        URL shortUrl = mappingModel.shortenUrl(longUrl);
        return shortUrl.toString();
    }

    @RequestMapping(value = "/{code:[a-zA-Z0-9]+}")
    public void expandAndRedirect(@PathVariable String code, HttpServletResponse httpServletResponse) throws IOException {
        URL longUrl = mappingModel.expandUrl(code);
        httpServletResponse.sendRedirect(longUrl.toString());
    }
}
