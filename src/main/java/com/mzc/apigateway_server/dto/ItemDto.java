package com.mzc.apigateway_server.dto;


/*
 * @Data
 * 1. @Getter
 * 2. @Setter
 * 3. @ToString
 * 4. @EqualsAndHashCode
 * 5. @RequiredArgsConstructor : final, @NonNull 필드를 대상으로 생성자 메소드를 생성한다.
 */


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {  

    private String id;
    private String name;   
    private String description;
    private long count; 
    private String regDate;       
    private String updDate;
    private String accountId;
  
}