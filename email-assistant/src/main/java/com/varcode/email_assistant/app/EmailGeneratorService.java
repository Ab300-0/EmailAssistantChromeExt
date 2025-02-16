package com.varcode.email_assistant.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class EmailGeneratorService {

    private final WebClient webClient;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public EmailGeneratorService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String generateEmailReply(@RequestBody EmailRequest emailRequest){
        //Build the prompt
        String prompt=buildPrompt(emailRequest);
        //Craft the request
        Map<String,Object> requestBody=Map.of(
             "contents",new Object[]{
                        Map.of("parts",new Object[]{
                                Map.of("text",prompt)
                                })
                }
        );
        //Do the request and get response
            String response=webClient.post()
                    .uri(geminiApiUrl+geminiApiKey)
                    .header("Content-Type","application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

        //Return ResponseEntity and response

        return extractResponseContent(response);
    }

    private String extractResponseContent(String response) {
        try {
            ObjectMapper mapper=new ObjectMapper();
            JsonNode rootNode=mapper.readTree(response);
            return rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        }catch (Exception e){
            return "Error processing request"+e.getMessage();
        }
    }

    private String buildPrompt(EmailRequest emailRequest) {
//        StringBuilder prompt = new StringBuilder();
//        prompt.append("Generate a professional email reply for hte following email content. Please don't generate a subject line ");
//        if (emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()) {
//            prompt.append("Use a ").append(emailRequest.getTone()).append(" tone.");
//        }
//        prompt.append("\nOriginal email: \n").append(emailRequest.getEmailContent());
//        System.out.println(emailRequest.getEmailContent());
//        return prompt.toString();

        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a thoughtful and professional email reply for the following email content. Respond appropriately, even if the input is brief or incomplete, such as a one-liner. Please ensure the response is meaningful and aligns with the given tone, if specified. Do not generate a subject line.");
        if (emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()) {
            prompt.append(" Use a ").append(emailRequest.getTone()).append(" tone.");
        }
        prompt.append(" You can use the following personal and professional details to tailor the response if needed: ");
        prompt.append("\n- Name: Abhishek Rajaram Jagtap");
        prompt.append("\n- Contact Number: 8459342801");
        prompt.append("\n- Email: officialabhishek3000@gmail.com");
        prompt.append("\n- Education: Computer Science, Waghire College, Saswad");
        prompt.append("\n- Current Role: Technical Trainer at ByteXL");
        prompt.append("\n- Company: Founder of Varcode, a coding courses institute focusing on hands-on projects and industry readiness");
        prompt.append("\n- Other Projects: Blogify (MERN stack project documentation) and C Shield (focused on detecting violence against women)");
        prompt.append("\n- GitHub Presence: GitHub email - officialabhishek3000@gmail.com");
        prompt.append("\nOriginal email:\n").append(emailRequest.getEmailContent());
        return prompt.toString();


    }
}
