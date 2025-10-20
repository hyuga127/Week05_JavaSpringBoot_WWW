package vn.edu.iuh.fit.week05.backend.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI jobRecruitmentOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8080");
        devServer.setDescription("Development Server");
        
        Server prodServer = new Server();
        prodServer.setUrl("https://api.jobplatform.com");
        prodServer.setDescription("Production Server");
        
        Contact contact = new Contact();
        contact.setEmail("21121781@gm.uit.edu.vn");
        contact.setName("Tran Gia Huy");
        contact.setUrl("https://github.com/your-username");
        
        License mitLicense = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");
        
        Info info = new Info()
                .title("Job Recruitment Platform API")
                .version("1.0.0")
                .contact(contact)
                .description("RESTful API for Job Recruitment Platform - Allows companies to post jobs and candidates to search for matching positions based on their skills.")
                .termsOfService("https://www.example.com/terms")
                .license(mitLicense);
        
        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer));
    }
}
