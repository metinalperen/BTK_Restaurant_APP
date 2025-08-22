package com.example.demo.dto.request;





import jakarta.validation.constraints.Email;


import jakarta.validation.constraints.NotBlank;


import jakarta.validation.constraints.Size;


import lombok.Data;





@Data


public class BootstrapAdminRequestDTO {


    @NotBlank


    @Size(min = 2, max = 50)


    private String name;





    @NotBlank


    @Email


    @Size(max = 100)


    private String email;


}