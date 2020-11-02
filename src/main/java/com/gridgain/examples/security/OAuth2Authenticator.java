package com.gridgain.examples.security;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.plugin.security.AuthenticationContext;
import org.apache.ignite.plugin.security.SecurityPermissionSet;
import org.apache.ignite.plugin.security.SecuritySubject;
import org.apache.ignite.plugin.security.SecuritySubjectType;
import org.gridgain.grid.internal.processors.security.AllowAllPermissionSet;
import org.gridgain.grid.security.Authenticator;
import org.gridgain.grid.security.SecuritySubjectAdapter;
import org.gridgain.grid.security.passcode.AuthenticationAclProvider;

/**
 * OAuth2 Authenticator
 */
public class OAuth2Authenticator implements Authenticator {
    /** User info url. */
    private String userInfoUrl;

    /** Authenticator acl provider. */
    private AuthenticationAclProvider aclProvider;

    /**
     * @param userInfoUrl User info url.
     */
    public void setUserInfoUrl(String userInfoUrl) {
        this.userInfoUrl = userInfoUrl;
    }

    /**
     * @param aclProvider Authenticator acl provider.
     */
    public void setAclProvider(AuthenticationAclProvider aclProvider) {
        this.aclProvider = aclProvider;
    }

    /** {@inheritDoc} */
    @Override public boolean supported(SecuritySubjectType type) {
        return true;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override public SecuritySubject authenticate(AuthenticationContext authCtx) throws IgniteCheckedException {
        try {
            if (authCtx.credentials() == null)
                return null;

            String login = (String)authCtx.credentials().getLogin();
            String pwd = (String)authCtx.credentials().getPassword();

            if (login != null || pwd != null) {
                SecurityPermissionSet perms = aclProvider.acl().get(authCtx.credentials());

                if (perms == null)
                    return null;

                return new SecuritySubjectAdapter(
                    authCtx.subjectId(),
                    authCtx.subjectType(),
                    login,
                    authCtx.address(),
                    perms);
            }

            if (!(authCtx.credentials().getUserObject() instanceof Map))
                return null;

            Map<String, String> userObj = (Map<String, String>)authCtx.credentials().getUserObject();

            String accessTok = userObj.get("accessToken");

            URL url = new URL(userInfoUrl + "?access_token=" + accessTok);

            HttpURLConnection conn = null;

            try {
                conn = (HttpURLConnection)url.openConnection();

                conn.setDoOutput(true);
                conn.setRequestMethod("GET");

                OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());

                out.flush();
                out.close();

                if (conn.getResponseCode() == 200) {
                    return new SecuritySubjectAdapter(
                        authCtx.subjectId(),
                        authCtx.subjectType(),
                        login,
                        authCtx.address(),
                        new AllowAllPermissionSet());
                }

                return null;
            }
            finally {
                if (conn != null)
                    conn.disconnect();
            }
        }
        catch (Exception e) {
            throw new IgniteCheckedException(e);
        }
    }

    /** {@inheritDoc} */
    @Override public boolean isGlobalNodeAuthentication() {
        return false;
    }
}
