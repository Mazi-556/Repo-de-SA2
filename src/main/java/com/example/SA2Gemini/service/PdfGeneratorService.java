package com.example.SA2Gemini.service;

import com.lowagie.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.context.WebContext;
import java.util.Map;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;
import org.thymeleaf.web.IWebExchange;

@Service
public class PdfGeneratorService {

    @Autowired
    private TemplateEngine templateEngine; // Autowire Thymeleaf's TemplateEngine

    public byte[] generatePdfFromHtml(String htmlContent) throws DocumentException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(htmlContent);
        renderer.layout();
        renderer.createPDF(outputStream);
        return outputStream.toByteArray();
    }





    public String generateHtml(String templateName, Map<String, Object> variables, HttpServletRequest request, HttpServletResponse response) {
        // Build IWebExchange from HttpServletRequest and HttpServletResponse
        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(request.getServletContext());
        IWebExchange webExchange = application.buildExchange(request, response);

        // Use the correct WebContext constructor
        WebContext webContext = new WebContext(webExchange, request.getLocale(), variables);
        return templateEngine.process(templateName, webContext);
    }
}
