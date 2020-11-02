# GridGain OAuth2.0 Authenticator Example


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
