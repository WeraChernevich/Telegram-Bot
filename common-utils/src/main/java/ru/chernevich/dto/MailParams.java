package ru.chernevich.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MailParams {
    private String id;
    private String mailTo;

}
