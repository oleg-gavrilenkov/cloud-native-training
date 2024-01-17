package com.task10.handlers;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;


public class SecParams {

	private AWSCognitoIdentityProvider cognitoIdentityProvider;
	private String userPoolId;
	private String clientId;

	public AWSCognitoIdentityProvider getCognitoIdentityProvider() {
		return cognitoIdentityProvider;
	}

	public void setCognitoIdentityProvider(AWSCognitoIdentityProvider cognitoIdentityProvider) {
		this.cognitoIdentityProvider = cognitoIdentityProvider;
	}

	public String getUserPoolId() {
		return userPoolId;
	}

	public void setUserPoolId(String userPoolId) {
		this.userPoolId = userPoolId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
}
