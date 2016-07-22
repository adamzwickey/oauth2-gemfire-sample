package com.example;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;

import java.net.InetSocketAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by azwickey on 7/22/16.
 */
@ConfigurationProperties("gemfire.config")
public class GemfireConfigProperties {

    /**
     * Specifies one or more Gemfire locator or server addresses formatted as [host]:[port].
     */
    private InetSocketAddress[] hostAddresses = { new InetSocketAddress("localhost", 10334) };

    @NotEmpty
    public InetSocketAddress[] getHostAddresses() {
        return hostAddresses;
    }

    public void setHostAddresses(InetSocketAddress[] hostAddresses) {
        this.hostAddresses = hostAddresses;
    }
}
