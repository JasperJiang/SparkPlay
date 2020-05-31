/*
 * Licensed Materials - Property of PwC
 * (c) Copyright Pwc Corp.  2020. All Rights Reserved.
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with PwC Corp.
 */

package com.example.sparkonk8s.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
@ToString
@Setter
@Getter
@Slf4j
public class AppProps {

    String sparkDefaultWorkLoadNamespace;
}
