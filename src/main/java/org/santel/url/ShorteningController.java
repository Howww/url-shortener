package org.santel.url;

import org.santel.url.model.*;
import org.springframework.web.bind.annotation.*;

import javax.inject.*;
import javax.servlet.http.*;
import java.io.*;
import java.net.*;

@RestController
public class ShorteningController {

    @Inject
    private MappingModel mappingModel;

    //TODO security: XSS vulnerability
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
