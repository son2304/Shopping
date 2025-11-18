package com.kt.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

public interface CurrentUser {
	Long getId();

	String getLoginId();
}
