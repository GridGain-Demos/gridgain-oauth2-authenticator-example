# GridGain OAuth2.0 Authenticator Example

If you want to use GridGain Control Center and GridGain security cluster in 
Single Sign-On manner with Google, you have to do few steps:

1. Register OpenID Connect application in Google development console.
2. Setup Control Center config.
3. Put `jar` of this project to GridGain libs.
4. Setup GridGain cluster config.

After that you will be able to login in Control Center with Google and use 
Google's access token to interact with a secured cluster. 

Authenticator in this project doesn't support roles restriction. 
It only checks if token is valid. 

Example of Control Center's `application.yml` config:

```yaml
spring.security.oauth2.client:
  registration:
    google:
      client-id: <client id from Google development console>
      client-secret: <client secret
```

Example of GridGain's config:

```xml
<?xml version="1.0" encoding="UTF-8"?>

<!--
    Copyright (C) GridGain Systems. All Rights Reserved.
    _________        _____ __________________        _____
    __  ____/___________(_)______  /__  ____/______ ____(_)_______
    _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
    / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
    \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
-->

<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean class="org.apache.ignite.plugin.security.SecurityCredentials" id="server.cred">
        <constructor-arg value="server"/>
        <constructor-arg value="password"/>
    </bean>

    <bean id="ignite.cfg" class="org.apache.ignite.configuration.IgniteConfiguration">
        <property name="pluginConfigurations">
            <list>
                <bean class="org.gridgain.grid.configuration.GridGainConfiguration">
                    <property name="authenticator">
                        <bean class="com.gridgain.examples.security.OAuth2Authenticator">
                            <property name="userInfoUrl" value="https://openidconnect.googleapis.com/v1/userinfo"/>

                            <property name="aclProvider">
                                <bean class="org.gridgain.grid.security.passcode.AuthenticationAclBasicProvider">
                                    <constructor-arg>
                                        <map>
                                            <!-- server.cred credentials and associated permissions (everything is allowed) -->
                                            <entry key-ref="server.cred" value="{defaultAllow:true}"/>
                                        </map>
                                    </constructor-arg>
                                </bean>
                            </property>
                        </bean>
                    </property>

                    <!-- Credentials for the current node. -->
                    <property name="securityCredentialsProvider">
                        <bean class="org.apache.ignite.plugin.security.SecurityCredentialsBasicProvider">
                            <constructor-arg ref="server.cred"/>
                        </bean>
                    </property>
                </bean>
            </list>
        </property>
    </bean>
</beans>
```
