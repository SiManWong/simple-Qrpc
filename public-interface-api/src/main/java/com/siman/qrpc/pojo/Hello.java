package com.siman.qrpc.pojo;

import lombok.*;

import java.io.Serializable;

/**
 * @author SiMan
 * @date 2021/1/17 2:05
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Hello implements Serializable {
    private String message;
    private String description;
}
